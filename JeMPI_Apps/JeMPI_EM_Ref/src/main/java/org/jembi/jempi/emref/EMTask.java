package org.jembi.jempi.emref;

import org.apache.commons.codec.language.DoubleMetaphone;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.similarity.JaroWinklerSimilarity;
import org.apache.commons.text.similarity.SimilarityScore;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.zip.GZIPInputStream;

import static java.lang.Math.abs;
import static java.lang.Math.log;

/*
 *
 * Implements the EM algorithm as specified in section 3 from
 * https://www.ons.gov.uk/methodology/methodologicalpublications/generalmethodology/onsworkingpaperseries
 * /developingstandardtoolsfordatalinkagefebruary2021
 *
 * */

class EMTask {

    private static final Logger LOGGER = LogManager.getLogger(EMTask.class);

    private static final int IDX_GIVEN_NAME = 0;
    private static final int IDX_FAMILY_NAME = 1;
    private static final int IDX_GENDER = 2;
    private static final int IDX_DOB = 3;
    private static final int IDX_CITY = 4;
    private static final int IDX_PHONE_NUMBER = 5;
    private static final int IDX_NATIONAL_ID = 6;
    private static final int N_LINK_FIELDS = 7;
    private static final int MISSING_ELEMENT_INT = -1; // Integer.MIN_VALUE;
    private static final int LEVELS = 3;
    private static final DoubleMetaphone DOUBLE_METAPHONE = new DoubleMetaphone();

    EMTask() {
        LOGGER.info("RUN EMTask");
    }


    private void sendToKafka(final double[] mHat, final double[] uHat) {
        LOGGER.info("mHat:{}", mHat);
        LOGGER.info("uHat:{}", uHat);
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

    private String getPhonetic(String s) {
        return s == null
               ? null
               : DOUBLE_METAPHONE.doubleMetaphone(s);
    }

    // Assumption:  consumer offset already set to postion to read from.
    private ArrayList<int[]> getGammaMatrix(final String fileName, final long startOffset, final long nRecords) throws IOException {

        // The matrix to return
        final var gamma = new ArrayList<int[]>();

        try (var stream = new FileInputStream(fileName)) {
            final InputStream gzipStream = new GZIPInputStream(stream);
            Reader decoder = new InputStreamReader(gzipStream, StandardCharsets.UTF_8);
            BufferedReader bufferedReader = new BufferedReader(decoder);

            bufferedReader.readLine(); // HEADER

            // skip to startOffset
            for (int i = 0; i < startOffset; i++) {
                bufferedReader.readLine();
            }

            final var jaroWinklerSimilarity = new JaroWinklerSimilarity();
            final var patients = new ArrayList<Patient>();
            final int[] rowNumber = {0};

            final int[] count = {0};
            while (count[0]++ < nRecords) {
                final var line = bufferedReader.readLine();
                final var fields = Arrays.asList(line.split(","));
                for (int i = 0; i < 8; i++) {
                    if (StringUtils.isBlank(fields.get(i))) {
                        fields.set(i, null);
                    }
                }
                final var patient = new Patient(fields.get(1),                        // givenName
                                                getPhonetic(fields.get(1)),           // givenNamePhonetic
                                                fields.get(2),                        // familyName
                                                getPhonetic(fields.get(2)),           // familyNamePhonetic
                                                fields.get(3),                        // genderAtBirth
                                                fields.get(4),                        // dateOfBirth
                                                fields.get(5),                        // city
                                                getPhonetic(fields.get(5)),           // cityPhonetic
                                                fields.get(6),                        // phoneNumber
                                                fields.get(7));                       // nationalID
                LOGGER.info("{} - {}", count[0], patient);
                patients.forEach(p -> {
                    var k = 0;
                    k += (patient.givenNamePhonetic == null
                          || !patient.givenNamePhonetic.equals(p.givenNamePhonetic)) ? 0 : 1;
                    k += (patient.familyNamePhonetic == null
                          || !patient.familyNamePhonetic.equals(p.familyNamePhonetic)) ? 0 : 1;
                    k += (patient.cityPhonetic == null
                          || !patient.cityPhonetic.equals(p.cityPhonetic)) ? 0 : 1;
                    if (k >= 1) {
                        String[] left = {
                                patient.givenName(), patient.familyName(), patient.genderAtBirth(),
                                patient.dateOfBirth(),
                                patient.city(), patient.phoneNumber(), patient.nationalID()};
                        String[] right = {
                                p.givenName(), p.familyName(), p.genderAtBirth(), p.dateOfBirth(),
                                p.city(), p.phoneNumber(), p.nationalID()};
                        gamma.add(setRowLevels(rowNumber[0]++, jaroWinklerSimilarity, left, right));
                    }
                });
                patients.add(patient);
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
        final long nRecords = gammaMatrix.size();
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
            logLikelihood += (gHat[j] * (log(mProduct) / log(2.0)) + (1.0 - gHat[j] * (log(uProduct) / log(2.0))));
        }
        final long endTime = System.currentTimeMillis();
        final long totalTime = endTime - startTime;
        LOGGER.debug("LogLikelihood step: {} ms", totalTime);
        return logLikelihood;
    }

    public boolean doIt(final String fileName, final long startOffset, final long nRecords)
            throws IOException {
        LOGGER.debug("doIt");

        final var gammaMatrix = getGammaMatrix(fileName, startOffset, nRecords);

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
        mHat[IDX_NATIONAL_ID] = 0.97;
        uHat[IDX_NATIONAL_ID] = 0.01;

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
        sendToKafka(mHat, uHat);
        return true;

    }

    private record Patient(
            String givenName,
            String givenNamePhonetic,
            String familyName,
            String familyNamePhonetic,
            String genderAtBirth,
            String dateOfBirth,
            String city,
            String cityPhonetic,
            String phoneNumber,
            String nationalID) {
    }

}
