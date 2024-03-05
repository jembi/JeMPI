package org.jembi.jempi.libmpi.lib.hooks;

import io.vavr.control.Option;
import org.jembi.jempi.libmpi.MpiGeneralError;

public interface LibMPIHooksInterface {
    Option<? extends MpiGeneralError> beforeLinkingHook();
    Option<? extends MpiGeneralError> afterLinkingHook();
}


