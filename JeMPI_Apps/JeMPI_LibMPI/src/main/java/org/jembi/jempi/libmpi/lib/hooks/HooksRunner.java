package org.jembi.jempi.libmpi.lib.hooks;

import io.vavr.control.Option;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jembi.jempi.libmpi.LibMPIClientInterface;
import org.jembi.jempi.libmpi.MpiGeneralError;

import java.util.List;

public final class HooksRunner {

    private final List<LibMPIHooksInterface> hooks;

    private static final Logger LOGGER = LogManager.getLogger(HooksRunner.class);
    public HooksRunner(final LibMPIClientInterface clientIn) {
        this.hooks = HooksRegistry.get(clientIn);
    }

    private void processResults(final Option<MpiGeneralError> result, final List<MpiGeneralError> hookResults) {
        if (!result.isEmpty()) {
            MpiGeneralError error = result.get();
            LOGGER.error(error);
            hookResults.add(error);
        }
    }
    public List<MpiGeneralError> beforeLinkingHook() {
        List<MpiGeneralError> hookResults = List.of();

        for (LibMPIHooksInterface hook : this.hooks) {
            processResults(hook.beforeLinkingHook(), hookResults);
        }
        return hookResults;
    }

    public List<MpiGeneralError> afterLinkingHook() {
        List<MpiGeneralError> hookResults = List.of();
        for (LibMPIHooksInterface hook : this.hooks) {
            processResults(hook.beforeLinkingHook(), hookResults);
        }
        return hookResults;
    }
}
