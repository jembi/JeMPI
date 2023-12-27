package org.jembi.jempi.shared.libs.m_and_u;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class MuAccesor {
    protected MuAccesor() { }
    private static final Map<String, MUKGlobalStoreInstance> ACCESSOR_INSTANCE = new HashMap<>() { };

    public static MUKGlobalStoreInstance getKafkaMUUpdater(final String linkerId, final String kafkaBootstrapServer) throws ExecutionException, InterruptedException {
        if (!ACCESSOR_INSTANCE.containsKey(linkerId)) {
            ACCESSOR_INSTANCE.put(linkerId, (MUKGlobalStoreInstance) new MUKGlobalStoreFactory(kafkaBootstrapServer).getCreate(String.format("linker_mu_tally_%s", linkerId), Object.class));
        }
        return ACCESSOR_INSTANCE.get(linkerId);
    }
    static Object getKafkaMu(final String linkerId, final String kafkaBootstrapServer) throws ExecutionException, InterruptedException {
        return getKafkaMUUpdater(linkerId, kafkaBootstrapServer).getValue();
    }
}
