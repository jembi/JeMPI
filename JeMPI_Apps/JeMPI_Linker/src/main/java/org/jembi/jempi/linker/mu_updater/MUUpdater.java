package org.jembi.jempi.linker.mu_updater;

public class MUUpdater {

    public MUUpdater(){

    }

    public void updateMandUs(){


        FieldPairEqualityMatrix localMandU = getLocalMandU();
        updateGlobalManU(localMandU);

    }

    private FieldPairEqualityMatrix getFieldPairEqualityMatrix(){

    }

    private void updateGlobalFieldPairEqualityMatrix(){
        var globalMandU = GlobalKafkaContext.Get<FieldPairEqualityMatrix>("global_m_and_u");

        // TOOD: Add

    }


}
