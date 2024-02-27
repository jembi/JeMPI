package org.jembi.jempi.libmpi.queries.utils;

import org.jembi.jempi.shared.models.CustomDemographicData;
import org.jembi.jempi.shared.models.GoldenRecord;

import java.lang.reflect.Field;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class QueryUtilities {

    public static void expectGRSearchResultToMatch(List<GoldenRecord> queryGoldenRecords, List<CustomDemographicData> expectedDemographicData){
        int[] idx = { 0 };

        if (queryGoldenRecords.isEmpty()){
            assertTrue(expectedDemographicData.isEmpty());
        }

        assertEquals(queryGoldenRecords.size(), expectedDemographicData.size());
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
    }
}
