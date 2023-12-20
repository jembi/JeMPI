package org.jembi.jempi.linker.threshold_range_processor.utls;

import org.jembi.jempi.linker.threshold_range_processor.mocks.dynamic.MockInteractionsDic;
import org.jembi.jempi.shared.models.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class MockInteractionCreator {

    public static CustomDemographicData getMockDemographicData(){
        return MockInteractionsDic.interactionsDic.get(new Random().ints(0, MockInteractionsDic.interactionsDic.size()).findFirst().getAsInt()).originalInteractionDemographics();
    }

    public static Interaction interactionFromIdRef(final int interactionId){
        return new Interaction(String.format("ID_%s", interactionId),
                new CustomSourceId(UUID.randomUUID().toString(), null, null),
                new CustomUniqueInteractionData( LocalDateTime.now(), null, null),
                MockInteractionsDic.interactionsDic.get(interactionId).originalInteractionDemographics());
    }
    public static Interaction interactionFromDemographicData(final String interactionId, final CustomDemographicData demographicData){
        return new Interaction(interactionId == null ?  UUID.randomUUID().toString() : interactionId,
                new CustomSourceId(UUID.randomUUID().toString(), null, null),
                new CustomUniqueInteractionData( LocalDateTime.now(), null, null),
                demographicData == null ? getMockDemographicData() : demographicData);
    }
    public static GoldenRecord goldenRecordFromDemographicData(final String goldenId, final CustomDemographicData demographicData){
        return new GoldenRecord(goldenId == null ?  UUID.randomUUID().toString() : goldenId,
                List.of(new CustomSourceId(UUID.randomUUID().toString(), null, null)),
                new CustomUniqueGoldenRecordData( LocalDateTime.now(), null, null),
                demographicData == null ? getMockDemographicData() : demographicData);
    }
}
