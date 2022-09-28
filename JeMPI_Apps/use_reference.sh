#!/bin/bash

set -e
set -u

PROJECT=reference


# JeMPI_Test_01
pushd JeMPI_Test_01/src/main/java/org/jembi/jempi/shared/models
  rm -f CustomTesterPatientRecord.java
  ln -s ../../../../../../../../../JeMPI_Shared_Source/custom/$PROJECT/CustomTesterPatientRecord.java CustomTesterPatientRecord.java
popd
pushd JeMPI_Test_01/src/main/java/org/jembi/jempi/test
  rm -f CustomMain.java
  ln -s ../../../../../../../../JeMPI_Shared_Source/custom/$PROJECT/test-01/CustomMain.java CustomMain.java
popd

# JeMPI_Test_02
pushd JeMPI_Test_02/src/main/java/org/jembi/jempi/test
  rm -f CustomMain.java
  ln -s ../../../../../../../../JeMPI_Shared_Source/custom/$PROJECT/test-02/CustomMain.java CustomMain.java
popd

# JeMPI_Staging_01
pushd JeMPI_Staging_01/src/main/java/org/jembi/jempi/shared/models
  rm -f CustomTesterPatientRecord.java
  ln -s ../../../../../../../../../JeMPI_Shared_Source/custom/$PROJECT/CustomTesterPatientRecord.java CustomTesterPatientRecord.java
popd
pushd JeMPI_Staging_01/src/main/java/org/jembi/jempi/staging
  rm -f CustomFrontEndStream.java
  ln -s ../../../../../../../../JeMPI_Shared_Source/custom/$PROJECT/staging-01/CustomFrontEndStream.java CustomFrontEndStream.java
popd

# JeMPI_EM
pushd JeMPI_EM/src/main/java/org/jembi/jempi/em
  rm -f CustomEMTask.java
  rm -f CustomPatient.java
  ln -s ../../../../../../../../JeMPI_Shared_Source/custom/$PROJECT/em/CustomEMTask.java CustomEMTask.java  
  ln -s ../../../../../../../../JeMPI_Shared_Source/custom/$PROJECT/em/CustomPatient.java CustomPatient.java
popd

# JeMPI_Stats
pushd JeMPI_Stats/src/main/java/org/jembi/jempi/stats
  rm -f CustomMain.java
  ln -s ../../../../../../../../JeMPI_Shared_Source/custom/$PROJECT/stats/CustomMain.java CustomMain.java
popd

