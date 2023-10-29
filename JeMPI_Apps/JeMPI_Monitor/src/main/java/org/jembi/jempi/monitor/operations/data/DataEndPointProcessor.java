package org.jembi.jempi.monitor.operations.data;

import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.server.Route;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jembi.jempi.monitor.BaseResponse;
import org.jembi.jempi.monitor.lib.LibRegistry;
import org.jembi.jempi.monitor.operations.BaseProcessor;

import java.util.Objects;
import java.util.concurrent.Callable;

import static akka.http.javadsl.server.Directives.complete;


public class DataEndPointProcessor extends BaseProcessor {
    private static final Logger LOGGER = LogManager.getLogger(DataEndPointProcessor.class);
    public DataEndPointProcessor(LibRegistry libRegistry) {
        super(libRegistry);
    }
    public Route deleteAll(String dbType, String tableName, Boolean force){
        if (this.libRegistry.runnerChecker.IsJeMPIRunning() && !force){
           return complete(StatusCodes.FORBIDDEN,
                   new BaseResponse("Cannot delete data whilst JeMPI is running. Please stop jempi services first (or append the url with /force) ", true),
                   JSON_MARSHALLER);
        }

        try {
            Callable<Boolean> runFunc;
            if (!Objects.equals(tableName, "__all")){
                runFunc = () -> this.libRegistry.postgres.deleteTableData(tableName);
            }
            else{
                runFunc = () -> this.libRegistry.postgres.deleteAllData();
            }

            if (runFunc.call()){
                return complete(StatusCodes.OK, new BaseResponse("Success", false), JSON_MARSHALLER);
            }
            return complete(StatusCodes.INTERNAL_SERVER_ERROR, new BaseResponse("Was unable to delete table data", true), JSON_MARSHALLER);

        } catch (Exception e){
            return complete(StatusCodes.INTERNAL_SERVER_ERROR, new BaseResponse("An error occurred whilst try to delete the data. See server logs for more details", true), JSON_MARSHALLER);
        }

    }




}
