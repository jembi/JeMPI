package org.jembi.jempi.shared.libs.interactionProcessor.processors.linkerProgress;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

// todo: A lot of repetition here, refactor
public class LinkerProgressAccessor {

    protected LinkerProgressAccessor() { }
    private static final Map<String, LinkerProgressKGlobalStoreInstance> ACCESSOR_INSTANCE = new HashMap<>() { };

    public static LinkerProgressKGlobalStoreInstance getKafkaLinkerProgressUpdater(final String linkerId, final String kafkaBootstrapServer) throws ExecutionException, InterruptedException {
        if (!ACCESSOR_INSTANCE.containsKey(linkerId)) {
            ACCESSOR_INSTANCE.put(linkerId, (LinkerProgressKGlobalStoreInstance) new LinkerProgressKGlobalStoreFactory(kafkaBootstrapServer).getCreate(String.format("linker_linker_progress_%s", linkerId), LinkerProgressData.class));
        }
        return ACCESSOR_INSTANCE.get(linkerId);
    }
}
