package org.jembi.jempi.libmpi.lib.hooks.indexes;

import io.vavr.control.Option;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jembi.jempi.libmpi.LibMPIClientInterface;
import org.jembi.jempi.libmpi.MpiGeneralError;
import org.jembi.jempi.libmpi.lib.hooks.BaseHook;

public final class LibMPIIndexesHooks extends BaseHook {

    private static final Logger LOGGER = LogManager.getLogger(LibMPIIndexesHooks.class);
    public LibMPIIndexesHooks(final LibMPIClientInterface clientIn) {
        super(clientIn);
    }

    @Override
    public Option<MpiGeneralError> beforeLinkingHook() {
        if (client.shouldUpdateLinkingIndexes()) {
            LOGGER.info("Updating indexes for linking.");
            return client.loadLinkingIndexes();
        }
        return Option.none();
    }

    @Override
    public Option<MpiGeneralError> afterLinkingHook() {
        if (client.shouldUpdateLinkingIndexes()) {
            LOGGER.info("Adding default indexes after linking.");
            return client.loadDefaultIndexes();
        }
        return Option.none();
    }
}
