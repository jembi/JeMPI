package org.jembi.jempi.linker.threshold_range_processor;

import org.jembi.jempi.libmpi.LibMPI;

import java.util.concurrent.ExecutionException;

public interface IDashboardDataProducer<T> {
    T getDashboardData(LibMPI libMPI) throws ExecutionException;
    String getDashboardDataName();
}
