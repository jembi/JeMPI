package org.jembi.jempi.libmpi.lib.index_manager;

import org.jembi.jempi.libmpi.LibMPIClientInterface;

import java.util.List;

public final class LibMPIIndexesManager {
    private final LibMPIClientInterface client;
    public LibMPIIndexesManager(final LibMPIClientInterface clientIn) {
        client = clientIn;
    }
    public Boolean resetIndexes(final List<String> indexesToCreate) {
        return client.deleteAllIndexes()
                && client.createIndexes(indexesToCreate);
    }
    public Boolean updateBeforeLinking() {
        if (CustomLibMPIIndexedInfo.shouldUpdateLinkingIndexes()) {
            return resetIndexes(CustomLibMPIIndexedInfo.getLinkingIndexes());
        }
        return true;
    }

    public Boolean updateAfterLinking() {
        return resetIndexes(CustomLibMPIIndexedInfo.defaultFieldIndexes());
    }
}
