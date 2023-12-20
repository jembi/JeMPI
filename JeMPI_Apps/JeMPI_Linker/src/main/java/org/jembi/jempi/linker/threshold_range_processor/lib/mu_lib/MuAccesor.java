package org.jembi.jempi.linker.threshold_range_processor.lib.mu_lib;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class MuAccesor {

    private static Map<String,MUKGlobalStoreInstance> accessorInstances = new HashMap<>() {};

    public static MUKGlobalStoreInstance GetKafkaMUUpdater(String linkerId, String kafkaBootstrapServer) throws ExecutionException, InterruptedException {
        if (!accessorInstances.containsKey(linkerId)) {
            accessorInstances.put(linkerId, (MUKGlobalStoreInstance) new MUKGlobalStoreFactory(kafkaBootstrapServer).getCreate(String.format("linker_mu_tally_%s", linkerId), Object.class)); //TODO: Object.class <- change
        }
        return accessorInstances.get(linkerId);
    }
    static Object GetKafkaMu(String linkerId, String kafkaBootstrapServer) throws ExecutionException, InterruptedException {
        return GetKafkaMUUpdater(linkerId, kafkaBootstrapServer).getValue();
    }
}
