package org.jembi.jempi.linker.thresholdRangeProcessor.lib.muLib;

import java.util.concurrent.ExecutionException;

public class MuAccesor {

    private static MUKGlobalStoreInstance accessorInstance = null;
    static void GetKafkaMu(String linkerId) throws ExecutionException, InterruptedException {
        if (MuAccesor.accessorInstance == null){
            accessorInstance = (MUKGlobalStoreInstance) new MUKGlobalStoreFactory("").get(linkerId+"_mu", Object.class);
        }

        accessorInstance.getValue();
    }
}
