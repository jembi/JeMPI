package org.jembi.jempi.bootstrapper.data.cli;

import org.jembi.jempi.bootstrapper.data.BaseDataBootstrapperCommand;
import org.jembi.jempi.bootstrapper.data.DataBootstrapper;

import java.util.concurrent.Callable;


public abstract class BaseCLICommand extends BaseDataBootstrapperCommand<DataBootstrapper> implements Callable<Integer> {
    @Override
    public BaseCLICommand init() throws Exception {
        super.init();
        return this;
    }

    @Override
    protected DataBootstrapper getBootstrapper(String configPath) {
        return null;
    }

    protected Integer callMultiple(BaseDataBootstrapperCommand<DataBootstrapper> [] bootstrapperCommands) throws Exception{
        Integer execResult = 0;
        for (BaseDataBootstrapperCommand<DataBootstrapper> b : bootstrapperCommands) {
            execResult += b.setConfigPath(this.config).init().call();
        }
        return execResult;
    }
}