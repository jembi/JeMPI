package org.jembi.jempi.linker.thresholdRangeProcessor.lib.muLib;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jembi.jempi.shared.kafka.globalContext.globalKTableWrapper.GlobalKTableWrapperInstance;
import org.jembi.jempi.shared.kafka.globalContext.globalKTableWrapper.TableUpdaterProcessor;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class MUKGlobalStoreInstance extends GlobalKTableWrapperInstance<Object> {

    public MUKGlobalStoreInstance(String bootStrapServers, String topicName, Class<Object> serializeCls) throws InterruptedException, ExecutionException {
        super(bootStrapServers, topicName, serializeCls);
    }
    @Override
    public HashMap<String, FieldEqualityPairMatchMatrix> getValue() {


        HashMap<String, FieldEqualityPairMatchMatrix> storeValue = new HashMap<>();

        for (Map.Entry<String, Object> value: ((LinkedHashMap<String, Object>) super.getValue()).entrySet()){
            storeValue.put(value.getKey(), ConvertTpFieldEqualityPairMatchMatrix(value.getValue()) );
        }

        return storeValue;
    }

    protected FieldEqualityPairMatchMatrix ConvertTpFieldEqualityPairMatchMatrix(Object value){
        ObjectMapper mapper = new ObjectMapper();
        return mapper.convertValue(value, FieldEqualityPairMatchMatrix.class);
    }

    protected TableUpdaterProcessor<Object, Object, Object> GetValueUpdater(){
        return (Object globalValue, Object currentValue) -> {
            LinkedHashMap<String, Object> currentMapValue = (LinkedHashMap<String, Object>) currentValue;
            LinkedHashMap<String, Object> currentGlobalMapValue = (LinkedHashMap<String, Object>) globalValue;

            for (Map.Entry<String, Object> current: currentMapValue.entrySet()){
                if (!currentGlobalMapValue.containsKey(current.getKey())){
                    currentGlobalMapValue.put(current.getKey(), current.getValue());
                }
                else{
                    FieldEqualityPairMatchMatrix currentParsedValue = ConvertTpFieldEqualityPairMatchMatrix(current.getValue());
                    FieldEqualityPairMatchMatrix currentGlobalParsedValue = ConvertTpFieldEqualityPairMatchMatrix(currentGlobalMapValue.get(current.getKey()));

                    currentGlobalMapValue.put(current.getKey(), currentGlobalParsedValue.merge(currentGlobalParsedValue, currentParsedValue));
                }

            }

            return currentGlobalMapValue;
        };
    }
}
