package org.jembi.jempi.em;

import org.apache.commons.codec.language.DoubleMetaphone;
import org.apache.commons.text.similarity.JaroWinklerSimilarity;
import org.apache.commons.text.similarity.SimilarityScore;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jembi.jempi.AppConfig;
import org.jembi.jempi.shared.kafka.MyKafkaConsumerByPartition;
import org.jembi.jempi.shared.kafka.MyKafkaProducer;
import org.jembi.jempi.shared.models.BatchEntity;
import org.jembi.jempi.shared.models.CustomMU;
import org.jembi.jempi.shared.models.GlobalConstants;
import org.jembi.jempi.shared.serdes.JsonPojoDeserializer;
import org.jembi.jempi.shared.serdes.JsonPojoSerializer;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;

import static java.lang.Math.abs;
import static java.lang.Math.log;

/*
 *
 * Implements the EM algorithm as specified in section 3 from
 * https://www.ons.gov.uk/methodology/methodologicalpublications/generalmethodology/onsworkingpaperseries
 * /developingstandardtoolsfordatalinkagefebruary2021
 *
 * https://www.ons.gov.uk/methodology/methodologicalpublications/generalmethodology/onsworkingpaperseries
 * /developingstandardtoolsfordatalinkagefebruary2021
 * */

class CustomEMTask {

    private static final Logger LOGGER = LogManager.getLogger(CustomEMTask.class);
    private static final double LOG2 = log(2.0);

    private static final int IDX_GIVEN_NAME = 0;
    private static final int IDX_FAMILY_NAME = 1;
    private static final int IDX_GENDER = 2;
    private static final int IDX_DOB = 3;
    private static final int IDX_CITY = 4;
    private static final int IDX_PHONE_NUMBER = 5;
    private static final int N_LINK_FIELDS = 6;
    private static final int MISSING_ELEMENT_INT = Integer.MIN_VALUE;
    private static final int LEVELS = 3;
    private static final DoubleMetaphone DOUBLE_METAPHONE = new DoubleMetaphone();

    CustomEMTask() {
        LOGGER.info("RUN EMTask");
    }

    private static Deserializer<String> stringDeserializer() {
        return new StringDeserializer();
    }

    private static Deserializer<BatchEntity> entityJsonValueDeserializer() {
        return new JsonPojoDeserializer<>(BatchEntity.class);
    }

    static String getPhonetic(String s) {
        return s == null ? null : DOUBLE_METAPHONE.doubleMetaphone(s);
    }

    private void sendToKafka(final double[] mHat, final double[] uHat) throws InterruptedException {
        final CustomMU rec = new CustomMU(mHat, uHat);
        LOGGER.info("{}", rec);
        try {
            final var myProducer = new MyKafkaProducer<String, CustomMU>(GlobalConstants.TOPIC_MU_LINKER,
                                                                         new StringSerializer(),
                                                                         new JsonPojoSerializer<>(),
                                                                         AppConfig.KAFKA_CLIENT_ID);
            myProducer.produceSync("MU", rec);
            myProducer.close();
        } catch (ExecutionException e) {
            LOGGER.error("{}", e.getMessage());
        }
    }

    private int[] setRowLevels(final int rowNumber, final SimilarityScore<Double> similarityScore,
                               final String[] left, final String[] right) {
        final int[] row = new int[N_LINK_FIELDS + 1];
        for (int i = 0; i < N_LINK_FIELDS; i++) {
            final String l = left[i];
            final String r = right[i];
            if (l == null || r == null) {
                row[i] = MISSING_ELEMENT_INT;
            } else {
                row[i] = similarityScore.apply(l, r) >= 0.92 ? LEVELS - 1 : 0;
            }
        }
        row[N_LINK_FIELDS] = rowNumber;
        return row;
    }

    // Assumption:  consumer offset already set to postion to read from.
    private ArrayList<int[]> getGammaMatrix(final MyKafkaConsumerByPartition<String, BatchEntity> consumer,
                                            final long nRecords) {
        final var jaroWinklerSimilarity = new JaroWinklerSimilarity();
        final var gamma = new ArrayList<int[]>();
        final var patients = new ArrayList<CustomPatient>();
        final int[] rowNumber = {0};

        boolean busy = true;
        final int[] count = {0};
        LOGGER.debug("{} {} {}", busy, count, nRecords);
        while (busy && count[0] < nRecords) {
            var records = consumer.poll(Duration.ofMillis(200));
            if (records.isEmpty()) {
                LOGGER.info("No records");
                busy = false;
            } else {
                records.forEach(r -> {
                    if (r.value().entityType() == BatchEntity.EntityType.BATCH_RECORD && count[0] < nRecords) {
                        count[0] += 1;
                        final var v = r.value();
                        final var patient = new CustomPatient(v.entity());
//                        patients.forEach(p -> {
//                            var k = 0;
//                            if (k >= 1) {
//                                final String[] left = {patient.col1(), patient.col2(),
//                                                       patient.genderAtBirth(), patient.dateOfBirth()};
//                                final String[] right = {p.col1(), p.col2(), p.genderAtBirth(),
//                                                        p.dateOfBirth()};
//                                gamma.add(setRowLevels(rowNumber[0]++, jaroWinklerSimilarity, left, right));
//                            }
//});
                        patients.add(patient);
                    }
                });
            }
        }
        return gamma;
    }

    private double[] expectation(final double[] mHat, final double[] uHat, final double pHat,
                                 final ArrayList<int[]> gammaMatrix) {
        final long startTime = System.currentTimeMillis();
        final int nRecords = gammaMatrix.size();
        final int nFields = gammaMatrix.get(0).length - 1;
        final double[] gHat = new double[nRecords];
        for (int j = 0; j < nRecords; j++) {
            final var gRow = gammaMatrix.get(j);
            double numerator = pHat;
            double denominator = 1.0 - pHat;
            for (int i = 0; i < nFields; i++) {
                final int gamma = gRow[i];
                if (gamma != MISSING_ELEMENT_INT) {
                    final double m = mHat[i];
                    final double u = uHat[i];
                    numerator *= (gamma == (LEVELS - 1) ? m : (1.0 - m));
                    denominator *= (gamma == (LEVELS - 1) ? u : (1.0 - u));
                }
            }
            gHat[j] = numerator / (numerator + denominator);
        }
        final long endTime = System.currentTimeMillis();
        final long totalTime = endTime - startTime;
        LOGGER.debug("Expectation step  : {} ms", totalTime);
        return gHat;
    }

    private double maximization(final double[] mHat, final double[] uHat, final double[] gHat,
                                final ArrayList<int[]> gammaMatrix) {
        final long startTime = System.currentTimeMillis();
        final int nRecords = gammaMatrix.size();
        final int nLinkFields = gammaMatrix.get(0).length - 1;
        final double[] mNumerator = new double[nLinkFields];
        final double[] mDenominator = new double[nLinkFields];
        final double[] uNumerator = new double[nLinkFields];
        final double[] uDenominator = new double[nLinkFields];
        for (int j = 0; j < nRecords; j++) {
            final var rowGammas = gammaMatrix.get(j);
            final var gJ = gHat[j];
            for (int i = 0; i < nLinkFields; i++) {
                final int gamma = rowGammas[i];
                if (gamma != MISSING_ELEMENT_INT) {
                    if (gamma == LEVELS - 1) {
                        mNumerator[i] += gJ;
                        uNumerator[i] += (1.0 - gJ);
                    }
                    mDenominator[i] += gJ;
                    uDenominator[i] += (1.0 - gJ);
                }
            }
        }
        for (int i = 0; i < nLinkFields; i++) {
            mHat[i] = mNumerator[i] / mDenominator[i];
            uHat[i] = uNumerator[i] / uDenominator[i];
        }
        final var gHatSum = Arrays.stream(gHat).sum();
        final long endTime = System.currentTimeMillis();
        final long totalTime = endTime - startTime;
        LOGGER.debug("Maximization step : {} ms", totalTime);
        return gHatSum / nRecords;
    }

    private double calcLogLikelihood(final ArrayList<int[]> gammaMatrix, final double[] gHat, final double[] mHat,
                                     final double[] uHat, final double pHat) {
        final long startTime = System.currentTimeMillis();
        final int nRecords = gammaMatrix.size();
        double logLikelihood = 0.0;
        for (int j = 0; j < nRecords; j++) {
            final var gRow = gammaMatrix.get(j);
            var mProduct = pHat;
            var uProduct = 1.0 - pHat;
            for (int i = 0; i < N_LINK_FIELDS; i++) {
                final int gamma = gRow[i];
                if (gamma != MISSING_ELEMENT_INT) {
                    final var m = mHat[i];
                    final var u = uHat[i];
                    mProduct *= (gamma == (LEVELS - 1) ? m : 1.0 - m);
                    uProduct *= (gamma == (LEVELS - 1) ? u : 1.0 - u);
                }
            }
            logLikelihood += (gHat[j] * (log(mProduct) / LOG2) + (1.0 - gHat[j] * (log(uProduct) / LOG2)));
        }
        final long endTime = System.currentTimeMillis();
        final long totalTime = endTime - startTime;
        LOGGER.debug("LogLikelihood step: {} ms", totalTime);
        return logLikelihood;
    }

    public boolean doIt(final long startOffset, final long nRecords) {
        LOGGER.debug("doIt: {} {}", startOffset, nRecords);

        var topic = GlobalConstants.TOPIC_PATIENT_EM;
        var consumer = new MyKafkaConsumerByPartition<>(topic, stringDeserializer(), entityJsonValueDeserializer(),
                                                        AppConfig.KAFKA_CLIENT_ID + topic,
                                                        AppConfig.KAFKA_GROUP_ID + topic, 500, 10);
        try {
            consumer.setOffset(0, startOffset);
        } catch (Exception e) {
            LOGGER.error(e.getLocalizedMessage(), e);
            return false;
        }

        final var gammaMatrix = getGammaMatrix(consumer, nRecords);
        consumer.close();

        if (gammaMatrix.isEmpty()) {
            LOGGER.warn("Empty gamma matrix");
            return false;
        }

        LOGGER.debug("gammaMatrix[{}][{}]", gammaMatrix.size(), gammaMatrix.get(0).length);

        final double[] mHat = new double[N_LINK_FIELDS];
        final double[] uHat = new double[N_LINK_FIELDS];

        mHat[IDX_GIVEN_NAME] = 0.78;
        uHat[IDX_GIVEN_NAME] = 0.05;
        mHat[IDX_FAMILY_NAME] = 0.84;
        uHat[IDX_FAMILY_NAME] = 0.07;
        mHat[IDX_GENDER] = 0.90;
        uHat[IDX_GENDER] = 0.50;
        mHat[IDX_DOB] = 0.97;
        uHat[IDX_DOB] = 0.01;
        mHat[IDX_CITY] = 0.88;
        uHat[IDX_CITY] = 0.79;
        mHat[IDX_PHONE_NUMBER] = 0.99;
        uHat[IDX_PHONE_NUMBER] = 0.01;

        double pHat = 0.5;

        final double[] logLikelihood = {1.0, 2.0};
        for (int loop = 0; abs((logLikelihood[0] - logLikelihood[1])) > (0.00001 * logLikelihood[0]) && loop < 30; loop++) {
            final var gHat = expectation(mHat, uHat, pHat, gammaMatrix);
            pHat = maximization(mHat, uHat, gHat, gammaMatrix);
            logLikelihood[0] = logLikelihood[1];
            logLikelihood[1] = calcLogLikelihood(gammaMatrix, gHat, mHat, uHat, pHat);
            LOGGER.debug("pHat: {}", pHat);
            LOGGER.debug("mHat: {}", mHat);
            LOGGER.debug("uHat: {}", uHat);
            LOGGER.debug("{}", logLikelihood);
        }

        LOGGER.debug("pHat: {}", pHat);
        LOGGER.debug("mHat: {}", mHat);
        LOGGER.debug("uHat: {}", uHat);
        LOGGER.debug("{}", gammaMatrix.size());
        try {
            sendToKafka(mHat, uHat);
            return true;
        } catch (InterruptedException e) {
            LOGGER.error(e.getLocalizedMessage());
        }
        return false;

    }

}
