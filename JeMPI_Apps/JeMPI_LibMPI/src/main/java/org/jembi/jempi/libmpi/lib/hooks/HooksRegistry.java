package org.jembi.jempi.libmpi.lib.hooks;

import org.jembi.jempi.libmpi.LibMPIClientInterface;
import org.jembi.jempi.libmpi.lib.hooks.indexes.LibMPIIndexesHooks;

import java.util.List;

public final class HooksRegistry {

    private HooksRegistry() { }
    public static List<LibMPIHooksInterface> get(final LibMPIClientInterface clientIn) {
        return List.of(
                new LibMPIIndexesHooks(clientIn)
        );
    }
}
