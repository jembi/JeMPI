package org.jembi.jempi.libmpi;

import java.util.List;

public class LibMPIIndexesManager {

    public resetIndexes(client){
        client.deleteAllIndexes();
        client.createIndexes(CustomLibMPIIndexedInfo.getLinkingIndexes());
    }
    public Boolean updateBeforeLinking(){
        if (CustomLibMPIIndexedInfo.shouldUpdateLinkingIndexes()){
            //client.deleteAllIndexes();
            //cleint.createIndexes(List)
            // client.deleteIndexes(List)
            //client.resetIndexes(List)
            resetIndexes(client, CustomLibMPIIndexedInfo.getLinkingIndexes());
        }
    }

    public Boolean updateAfterLinking(){
        resetIndexes(client, CustomLibMPIIndexedInfo.defaultFieldIndexes());
    }
}
