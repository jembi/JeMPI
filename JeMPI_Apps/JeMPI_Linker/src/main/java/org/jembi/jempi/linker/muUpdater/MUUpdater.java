package org.jembi.jempi.linker.muUpdater;

import java.util.concurrent.ExecutionException;

public class MUUpdater {

    private MUKGlobalStoreInstance muGlobalKStore;
    public MUUpdater(final String tableName) throws ExecutionException, InterruptedException {
       muGlobalKStore = (MUKGlobalStoreInstance) new MUKGlobalStoreFactory("").get(tableName, FieldPairEqualityMatrix.class);
    }

    public void updateMandUs(){


//        FieldPairEqualityMatrix localMandU = getLocalMandU();
//        updateGlobalManU(localMandU);

    }

    private FieldPairEqualityMatrix getFieldPairEqualityMatrix(){
        return null;
    }

    private void updateGlobalFieldPairEqualityMatrix(FieldPairEqualityMatrix newMatrix) throws ExecutionException, InterruptedException {
        muGlobalKStore.updateValue(newMatrix);
    }


}
