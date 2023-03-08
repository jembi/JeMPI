package org.jembi.jempi.linker;


import org.jembi.jempi.shared.models.CustomMU;
import org.jembi.jempi.shared.models.CustomDemographicData;

public final class CustomLinkerProbabilistic {

  private CustomLinkerProbabilistic() {
  }

  public static float probabilisticScore(final CustomDemographicData goldenRecord,
                                         final CustomDemographicData patientRecord) {
    return 0.0F;
  }

  public static void updateMU(final CustomMU mu) {
  }

  public static void checkUpdatedMU() {
  }

  static CustomMU getMU() {
    return new CustomMU(null);
  }

}
