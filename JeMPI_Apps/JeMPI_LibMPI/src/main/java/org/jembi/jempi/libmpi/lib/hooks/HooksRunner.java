package org.jembi.jempi.libmpi.lib.hooks;

import io.vavr.control.Option;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jembi.jempi.libmpi.LibMPIClientInterface;
import org.jembi.jempi.libmpi.MpiHookError;
import org.jembi.jempi.libmpi.MpiServiceError;

import java.util.List;

public final class HooksRunner {

    private LibMPIClientInterface client;
    private List<LibMPIHooksInterface> hooks;

    private static final Logger LOGGER = LogManager.getLogger(HooksRunner.class);
    public HooksRunner(final LibMPIClientInterface clientIn) {
        this.client = clientIn;
        this.hooks = HooksRegistry.get(client);
    }

    private void processResults(final Option<MpiHookError> result, final List<MpiHookError> hookResults) {
        if (!result.isEmpty()) {
            MpiHookError error = result.get();
            LOGGER.error(error);
            hookResults.add(error);
        }
    }
    public List<MpiHookError> beforeLinkingHook() {
        List<MpiHookError> hookResults = List.of();

        for (LibMPIHooksInterface hook : this.hooks) {
            processResults(hook.beforeLinkingHook(), hookResults);
        }
        return hookResults;
    }

    public List<MpiHookError> afterLinkingHook() {
        List<MpiHookError> hookResults = List.of();
        for (LibMPIHooksInterface hook : this.hooks) {
            processResults(hook.beforeLinkingHook(), hookResults);
        }
        return hookResults;
    }
}
