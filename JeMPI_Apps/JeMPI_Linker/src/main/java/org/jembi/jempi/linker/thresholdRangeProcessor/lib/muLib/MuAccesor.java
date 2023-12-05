package org.jembi.jempi.linker.thresholdRangeProcessor.lib.muLib;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class MuAccesor {

    private static Map<String,MUKGlobalStoreInstance> accessorInstances = new HashMap<>() {};

    static MUKGlobalStoreInstance GetKafkaMUUpdater(String linkerId) throws ExecutionException, InterruptedException {
        if (!accessorInstances.containsKey(linkerId)) {
            accessorInstances.put(linkerId, (MUKGlobalStoreInstance) new MUKGlobalStoreFactory("").get(linkerId + "_mu", Object.class)); //TODO: Object.class
        }
        return accessorInstances.get(linkerId);
    }
    static Object GetKafkaMu(String linkerId) throws ExecutionException, InterruptedException {
        return GetKafkaMUUpdater(linkerId).getValue();
    }
}
