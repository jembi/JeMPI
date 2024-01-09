package org.jembi.jempi.shared.libs.tptn;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

// todo: A lot of repetition here, refactor
public class TPTNAccessor {

    protected TPTNAccessor() { }
    private static final Map<String, TPTNKGlobalStoreInstance> ACCESSOR_INSTANCE = new HashMap<>() { };

    public static TPTNKGlobalStoreInstance getKafkaTPTNUpdater(final String linkerId, final String kafkaBootstrapServer) throws ExecutionException, InterruptedException {
        if (!ACCESSOR_INSTANCE.containsKey(linkerId)) {
            ACCESSOR_INSTANCE.put(linkerId, (TPTNKGlobalStoreInstance) new TPTNKGlobalStoreFactory(kafkaBootstrapServer).getCreate(String.format("linker_tptn_%s", linkerId), TPTNMatrix.class));
        }
        return ACCESSOR_INSTANCE.get(linkerId);
    }
}
