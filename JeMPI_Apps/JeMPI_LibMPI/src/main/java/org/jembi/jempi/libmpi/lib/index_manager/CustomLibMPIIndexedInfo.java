package org.jembi.jempi.libmpi.lib.index_manager;

import java.util.List;

public final class CustomLibMPIIndexedInfo {
    private CustomLibMPIIndexedInfo() { }
    public static Boolean shouldUpdateLinkingIndexes() {
        return true;
    }

    public static List<String> getLinkingIndexes() {
        return List.of();
    }

    public static List<String> defaultFieldIndexes() {
        return List.of();
    }
}
