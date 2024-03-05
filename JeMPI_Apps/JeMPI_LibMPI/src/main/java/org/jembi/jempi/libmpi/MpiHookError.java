package org.jembi.jempi.libmpi;

public sealed interface MpiHookError extends MpiGeneralError {

    record NotImplementedError(
            String error) implements MpiHookError {
    }
}
