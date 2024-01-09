package org.jembi.jempi.linker.linker_processor.processors;

import org.jembi.jempi.libmpi.LibMPI;

public interface IDashboardDataProducer<T> extends ISubProcessor {
    T getDashboardData(LibMPI libMPI) throws Exception;
    String getDashboardDataName();
}
