package org.jembi.jempi.libmpi.lib.hooks.indexes;

import io.vavr.control.Option;
import org.jembi.jempi.libmpi.LibMPIClientInterface;
import org.jembi.jempi.libmpi.MpiGeneralError;
import org.jembi.jempi.libmpi.lib.hooks.BaseHook;

public final class LibMPIIndexesHooks extends BaseHook {
    public LibMPIIndexesHooks(final LibMPIClientInterface clientIn) {
        super(clientIn);
    }

    @Override
    public Option<MpiGeneralError> beforeLinkingHook() {
        if (client.shouldUpdateLinkingIndexes()) {
            return client.loadLinkingIndexes();
        }
        return Option.none();
    }

    @Override
    public Option<MpiGeneralError> afterLinkingHook() {
        if (client.shouldUpdateLinkingIndexes()) {
            return client.loadDefaultIndexes();
        }
        return Option.none();
    }
}
