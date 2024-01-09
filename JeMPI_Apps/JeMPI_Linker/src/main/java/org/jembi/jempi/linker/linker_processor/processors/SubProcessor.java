package org.jembi.jempi.linker.linker_processor.processors;

import org.jembi.jempi.shared.models.GlobalConstants;

public class SubProcessor implements  ISubProcessor {
    protected String linkerId;

    public  SubProcessor() {
        this.linkerId = GlobalConstants.DEFAULT_LINKER_GLOBAL_STORE_NAME;
    }
    @Override
    public ISubProcessor setLinkerId(final String linkerIdIn) {
        this.linkerId = linkerIdIn;
        return this;
    }
}
