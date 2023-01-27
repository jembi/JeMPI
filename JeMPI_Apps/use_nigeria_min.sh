#!/bin/bash

set -e
set -u

pushd JeMPI_Configuration
  sbt "run config-nigeria-gender-dob.json"
popd

PROJECT=nigeria-gender-dob

# JeMPI_AsyncReceiver
pushd JeMPI_AsyncReceiver/src/main/java/org/jembi/jempi/shared/models
  rm -f CustomSourceRecord.java
  ln -s ../../../../../../../../../JeMPI_Shared_Source/custom/$PROJECT/CustomSourceRecord.java CustomSourceRecord.java
popd
pushd JeMPI_AsyncReceiver/src/main/java/org/jembi/jempi/async_receiver
  rm -f CustomMain.java
  ln -s ../../../../../../../../JeMPI_Shared_Source/custom/$PROJECT/async-receiver/CustomMain.java CustomMain.java
popd

# JeMPI_SyncReceiver
# pushd JeMPI_SyncReceiver/src/main/java/org/jembi/jempi/sync_receiver
#   rm -f CustomMain.java
#   ln -s ../../../../../../../../JeMPI_Shared_Source/custom/$PROJECT/sync_receiver/CustomMain.java CustomMain.java
# popd

# JeMPI_Preprocessor
pushd JeMPI_PreProcessor/src/main/java/org/jembi/jempi/shared/models
  rm -f CustomSourceRecord.java
  ln -s ../../../../../../../../../JeMPI_Shared_Source/custom/$PROJECT/CustomSourceRecord.java CustomSourceRecord.java
popd
pushd JeMPI_PreProcessor/src/main/java/org/jembi/jempi/pre_processor
  rm -f CustomSourceRecordStream.java
  ln -s ../../../../../../../../JeMPI_Shared_Source/custom/$PROJECT/pre-processor/CustomSourceRecordStream.java CustomSourceRecordStream.java
popd
pushd JeMPI_PreProcessor/src/main/java/org/jembi/jempi/pre_processor
  rm -f CustomFHIRsyncReceiver.java
  ln -s ../../../../../../../../JeMPI_Shared_Source/custom/$PROJECT/pre-processor/CustomFHIRsyncReceiver.java
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

