package org.jembi.jempi.linker.linker_processor.processors;

import org.jembi.jempi.libmpi.LibMPI;

import java.util.concurrent.ExecutionException;

public interface IDashboardDataProducer<T> extends ISubProcessor {
    T getDashboardData(LibMPI libMPI) throws ExecutionException;
    String getDashboardDataName();
}
