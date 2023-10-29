package org.jembi.jempi.monitor.operations;
import akka.http.javadsl.marshallers.jackson.Jackson;
import akka.http.javadsl.marshalling.Marshaller;
import akka.http.javadsl.model.RequestEntity;
import org.jembi.jempi.monitor.lib.LibRegistry;

import static org.jembi.jempi.shared.utils.AppUtils.OBJECT_MAPPER;

public class BaseProcessor {
    protected static final Marshaller<Object, RequestEntity> JSON_MARSHALLER = Jackson.marshaller(OBJECT_MAPPER);
    protected LibRegistry libRegistry;
    public BaseProcessor(final LibRegistry libRegistry){
        this.libRegistry = libRegistry;
    }
}
