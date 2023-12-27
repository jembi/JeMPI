package org.jembi.jempi.shared.libs.m_and_u;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public final class MuModel {

    private HashMap<String, FieldEqualityPairMatchMatrix> fieldEqualityPairMatchMap = new HashMap<>();
    private final String linkerId;
    private final String kafkaBootstrapServer;
    public MuModel(final String linkerIdIn, final Map<String, FieldEqualityPairMatchMatrix> fieldEqulityPairMatchMap, final String kafkaBootstrapServerIn) {
        this.linkerId = linkerIdIn;
        this.fieldEqualityPairMatchMap = (HashMap<String, FieldEqualityPairMatchMatrix>) fieldEqulityPairMatchMap;
        this.kafkaBootstrapServer = kafkaBootstrapServerIn;
    }

    public void updateFieldEqualityPairMatchMatrix(final String field, final Boolean fieldsEqual, final Boolean pairMatch) {
        FieldEqualityPairMatchMatrix matrix = this.fieldEqualityPairMatchMap.get(field);
        if (Boolean.TRUE.equals(pairMatch)) {
            if (Boolean.TRUE.equals(fieldsEqual)) {
                matrix.updateFieldEqualPairMatch(1);
            } else {
                matrix.updateFieldNotEqualPairMatch(1);
            }
        } else {
            if (Boolean.TRUE.equals(fieldsEqual)) {
                matrix.updateFieldEqualPairNoMatch(1);
            } else {
                matrix.updateFieldNotEqualPairNoMatch(1);
            }
        }
    }
    public void saveToKafka() throws ExecutionException, InterruptedException {
        MuAccesor.getKafkaMUUpdater(this.linkerId, this.kafkaBootstrapServer).updateValue(fieldEqualityPairMatchMap);
    }
    public static MuModel fromDemographicData(final String linkerId, final Map<String, String> demographicData, final String kafkaBootstrapServer) {
        HashMap<String, FieldEqualityPairMatchMatrix> fieldEqualityPairMatchMap = new HashMap<>();

        for (String field: demographicData.keySet()) {
            fieldEqualityPairMatchMap.put(field, new FieldEqualityPairMatchMatrix());
        }
        return new MuModel(linkerId, fieldEqualityPairMatchMap, kafkaBootstrapServer);
    }
    public Map<String, FieldEqualityPairMatchMatrix> getFieldEqualityPairMatchMatrix() {
        return this.fieldEqualityPairMatchMap;
    }

    @Override
    public String toString() {
        StringBuilder matrixData = new StringBuilder("----- Field Data -----");
        for (Map.Entry<String, FieldEqualityPairMatchMatrix> fieldInfo: this.fieldEqualityPairMatchMap.entrySet()) {
            matrixData.append(String.format("\n-> %s", fieldInfo.getKey()));
            matrixData.append(fieldInfo.getValue().toString());
        }
        return matrixData.toString();
    }
}
