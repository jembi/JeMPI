package org.jembi.jempi.libmpi.lib.hooks;

import org.jembi.jempi.libmpi.LibMPIClientInterface;


public abstract class BaseHook implements LibMPIHooksInterface {

    protected LibMPIClientInterface client;
    public BaseHook(final LibMPIClientInterface clientIn) {
        this.client = clientIn;
    }
}
