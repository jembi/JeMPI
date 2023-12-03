package org.jembi.jempi.linker.thresholdRangeProcessor.standardRangeProcessor.processors.FieldPairMatchMatrix;

public class FieldPairMatchMatrix extends RangeProcessor{

    Canidate pairMatchCanidate = null;
    List<Canidates> pairUnMatchedCanidates = null;

    construtor(){
        fieldPaithEqualityMatrix = new FEM(fieldds);
    }

    ProcessCanidates(canidates){
      this.setPairMatchUnMatchedCanidates();
      this.updateFieldEqulaityMatrix();
    }

    private setPairMatchUnMatchedCanidates(){
        aboveThresholdCanidates = canidate.filter(c => c.rangeLocations.getNames() in STANDARD_RANDG_TYPE.ABOVE_THRESHOLH &&  c.rangeLocation.getName() not in STANDARD_RANDG_TYPE.NOTIFICATION_WINDOW).sort()
        pairMatchCanidate = aboveThresholdCanidates.get(0); //TODO: Make into oen with flag
        pairUnMatchedCanidates = rest.aboveThresholdCanidates + canidate.filter(c => c.rangeLocations.getNames() in STANDARD_RANDG_TYPE.BELOW_THRESHOLD &&  c.rangeLocation.getName() not in STANDARD_RANDG_TYPE.NOTIFICATION_WINDOW)
    }

    private updateFieldEqulaityMatrix() {
        for (snetri: g.getEntries()){

        }
    }
}
