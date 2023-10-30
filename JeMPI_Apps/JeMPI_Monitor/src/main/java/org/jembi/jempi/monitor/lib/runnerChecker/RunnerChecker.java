package org.jembi.jempi.monitor.lib.runnerChecker;

import org.jembi.jempi.monitor.utils.HttpRequestor;

import java.net.ConnectException;

public class RunnerChecker {
    private final String apiUrl;
    public RunnerChecker(final String apiHost, final int apiPort) {
        this.apiUrl = String.format("http://%s:%d/JeMPI/health", apiHost, apiPort);
    }

    public boolean IsJeMPIRunning() throws Exception {
        try{
            HttpRequestor.GetRequest(this.apiUrl);
            return true;
        } catch (ConnectException e){
            return false;
        }

    }
}
