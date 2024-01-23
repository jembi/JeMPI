package org.jembi.jempi.shared.libs.interactionProcessor.processors.m_and_u;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jembi.jempi.shared.kafka.global_context.store_processor.StoreProcessor;
import org.jembi.jempi.shared.kafka.global_context.store_processor.StoreUpdaterProcessor;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public final class MUKGlobalStoreInstance extends StoreProcessor<Object> {

    public MUKGlobalStoreInstance(final String bootStrapServers, final String topicName, final String sinkName, final Class<Object> serializeCls) throws InterruptedException, ExecutionException {
        super(bootStrapServers, topicName, sinkName, serializeCls);
    }
    @Override
    public HashMap<String, FieldEqualityPairMatchMatrix> getValue() {
        Object storedValue = super.getValue();
        HashMap<String, FieldEqualityPairMatchMatrix> storeValue = new HashMap<>();

        if (storedValue == null) {
            return storeValue;
        }

        for (Map.Entry<String, Object> value: ((LinkedHashMap<String, Object>) storedValue).entrySet()) {
            storeValue.put(value.getKey(), convertToFieldEqualityPairMatchMatrix(value.getValue()));
        }

        return storeValue;
    }

    protected FieldEqualityPairMatchMatrix convertToFieldEqualityPairMatchMatrix(final Object value) {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.convertValue(value, FieldEqualityPairMatchMatrix.class);
    }

    @Override
    protected StoreUpdaterProcessor<Object, Object, Object> getValueUpdater() {
        return (final Object globalValue, final Object currentValue) -> {
            LinkedHashMap<String, Object> currentMapValue = (LinkedHashMap<String, Object>) currentValue;
            LinkedHashMap<String, Object> currentGlobalMapValue = (LinkedHashMap<String, Object>) globalValue;

            if (currentGlobalMapValue == null) {
                currentGlobalMapValue = new LinkedHashMap<>();
            }

            for (Map.Entry<String, Object> current: currentMapValue.entrySet()) {
                if (!currentGlobalMapValue.containsKey(current.getKey())) {
                    currentGlobalMapValue.put(current.getKey(), current.getValue());
                } else {
                    FieldEqualityPairMatchMatrix currentParsedValue = convertToFieldEqualityPairMatchMatrix(current.getValue());
                    FieldEqualityPairMatchMatrix currentGlobalParsedValue = convertToFieldEqualityPairMatchMatrix(currentGlobalMapValue.get(current.getKey()));

                    currentGlobalMapValue.put(current.getKey(), currentGlobalParsedValue.merge(currentGlobalParsedValue, currentParsedValue));
                }

            }

            return currentGlobalMapValue;
        };
    }
}
