package org.jembi.jempi.linker.threshold_range_processor.lib.mu_lib;

import org.jembi.jempi.AppConfig;
import org.jembi.jempi.libmpi.LibMPI;
import org.jembi.jempi.linker.threshold_range_processor.utls.MockLibMPI;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static java.lang.Thread.sleep;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class MuModelTest {
    LibMPI libMPI = null;
    @BeforeAll
    void setLibMPI(){
        libMPI = MockLibMPI.getLibMPI();
    }
    @Test
    void testCanAddMatrixToKafka() throws ExecutionException, InterruptedException {
        HashMap<String, FieldEqualityPairMatchMatrix> fieldMap = new HashMap<>();
        fieldMap.put("firstName", new FieldEqualityPairMatchMatrix());
        fieldMap.put("lastName", new FieldEqualityPairMatchMatrix());

        MuModel muModal = new MuModel("testlinker", fieldMap, AppConfig.KAFKA_BOOTSTRAP_SERVERS);

        muModal.saveToKafka();
        assertEquals(fieldMap, MuAccesor.getKafkaMUUpdater("testlinker",  AppConfig.KAFKA_BOOTSTRAP_SERVERS).getValue());
    }

    @Test
    void testCanAddMatrixWithDataToKafka(){

    }

    HashMap<String, FieldEqualityPairMatchMatrix> getDefaultFieldMatrix(){
        HashMap<String, FieldEqualityPairMatchMatrix> fieldMap = new HashMap<>();

        fieldMap.put("firstName", new FieldEqualityPairMatchMatrix());
        fieldMap.put("lastName", new FieldEqualityPairMatchMatrix());

        return  fieldMap;
    }

    public void assertKafkaMatrixMatch(HashMap<String, FieldEqualityPairMatchMatrix> resultMatrix, Map<String, List<Integer>> compareMatrix){

        for (Map.Entry<String, List<Integer>> field: compareMatrix.entrySet()){
            assertTrue(resultMatrix.containsKey(field.getKey()));

            FieldEqualityPairMatchMatrix fieldResultMatrix = resultMatrix.get(field.getKey());
            List<Integer> expectedResults = field.getValue();

            assertEquals(expectedResults.get(0), fieldResultMatrix.getFieldEqualPairMatch());
            assertEquals(expectedResults.get(1), fieldResultMatrix.getFieldEqualPairNoMatch());
            assertEquals(expectedResults.get(2), fieldResultMatrix.getFieldNotEqualPairMatch());
            assertEquals(expectedResults.get(3), fieldResultMatrix.getFieldNotEqualPairNoMatch());
        }

    }
    @Test
    void testKafkaMatrixAccumulatesValues() throws ExecutionException, InterruptedException {

        HashMap<String, FieldEqualityPairMatchMatrix> fieldMap = getDefaultFieldMatrix();
        MuModel muModal = new MuModel("testlinker", fieldMap, AppConfig.KAFKA_BOOTSTRAP_SERVERS);

        muModal.updateFieldEqualityPairMatchMatrix("firstName",true, true);
        muModal.updateFieldEqualityPairMatchMatrix("lastName",false, true);

        //muModal.saveToKafka();
        //muModal.saveToKafka();
        sleep(5000);
        assertKafkaMatrixMatch(MuAccesor.getKafkaMUUpdater("testlinker",  AppConfig.KAFKA_BOOTSTRAP_SERVERS).getValue(),
                                Map.ofEntries(
                                        Map.entry("firstName", List.of(1, 0, 0, 0)),
                                        Map.entry("lastName", List.of(0, 0, 1, 0))
                                ));



//        fieldMap = getDefaultFieldMatrix();
//        muModal = new MuModel("testlinker", fieldMap, AppConfig.KAFKA_BOOTSTRAP_SERVERS);
//
//        muModal.updateFieldEqualityPairMatchMatrix("firstName",false, false);
//        muModal.updateFieldEqualityPairMatchMatrix("lastName",true, false);
//
//        muModal.updateFieldEqualityPairMatchMatrix("firstName",true, false);
//        muModal.updateFieldEqualityPairMatchMatrix("lastName",true, false);
//
//        muModal.updateFieldEqualityPairMatchMatrix("firstName",true, true);
//        muModal.updateFieldEqualityPairMatchMatrix("lastName",false, true);
//
//        muModal.saveToKafka();
//
//        assertKafkaMatrixMatch(MuAccesor.GetKafkaMUUpdater("testlinker",  AppConfig.KAFKA_BOOTSTRAP_SERVERS).getValue(),
//                Map.ofEntries(
//                        Map.entry("firstName", List.of(2, 1, 0, 1)),
//                        Map.entry("lastName", List.of(0, 2, 2, 0))
//                ));

    }
}
