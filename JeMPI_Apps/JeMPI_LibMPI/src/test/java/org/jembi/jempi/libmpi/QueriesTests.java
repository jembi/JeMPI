package org.jembi.jempi.libmpi;

import org.jembi.jempi.libmpi.utils.Utilities;
import org.jembi.jempi.shared.models.CustomDemographicData;
import org.jembi.jempi.shared.models.GoldenRecord;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;
import java.util.List;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class QueriesTests {


    void expectGRToMatch(List<GoldenRecord> queryGoldenRecords, List<CustomDemographicData> expectedDemographicData){
        int[] idx = { 0 };

        if (queryGoldenRecords.isEmpty()){
            assertTrue(expectedDemographicData.isEmpty());
        }

        Class<? extends CustomDemographicData> clazz = queryGoldenRecords.get(0).demographicData().getClass();
        Field[] fields = clazz.getDeclaredFields();

        queryGoldenRecords.stream().map(GoldenRecord::demographicData).forEach(g -> {
            CustomDemographicData expectedObject = expectedDemographicData.get(idx[0]++);
            for (Field field : fields) {
                try {
                    assertEquals(field.get(g), field.get(expectedObject));
                } catch (IllegalAccessException e){
                    fail();
                }
            }

        });
        //assertSame(queryGoldenRecords.stream().map(GoldenRecord::demographicData).toArray(), expectedDemographicData.toArray());
    }
    @Test
    void testCanFindCandidatesDeterministically() {
        Utilities.SetRules("default");
        Utilities.ResetData();
        Utilities.AddData(new CustomDemographicData(
                "givenName",
                "familyName",
                "gender",
                "dob",
                "city",
                "phoneNumber",
                "nationalId"));



        LibMPI libMPI = Utilities.getLibMPI();
        List<GoldenRecord>  gRS = libMPI.findLinkCandidates(new CustomDemographicData(
                                    "givasdasdenName",
                                    "famdsfdilyName",
                                   "gefsdfnder",
                                    "dosdfb",
                                    "cifsdfty",
                                    "phoneNgffdgdfgdfumber",
                                    "nationdadasdalId"));

        expectGRToMatch(gRS, List.of(new CustomDemographicData(
                "givenName",
                "familyName",
                "gender",
                "dob",
                "city",
                "phoneNumber",
                "nationalId")));
    }

    void testCanFindCandidatesMultipleeterministically() {

    }

    void testCanFindCandidatesProbalistically() {

    }
}
