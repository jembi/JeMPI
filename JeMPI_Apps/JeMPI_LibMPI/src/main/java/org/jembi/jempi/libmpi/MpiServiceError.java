package org.jembi.jempi.libmpi;

public sealed interface MpiServiceError extends MpiGeneralError {

   record PatientUIDDoesNotExistError(
         String error,
         String uid) implements MpiServiceError {}

   record GoldenUIDDoesNotExistError(
         String error,
         String uid) implements MpiServiceError {}

   record GoldenUIDPatientConflictError(
         String error,
         String goldenUID,
         String patientUID) implements MpiServiceError {}

   record DeletePredicateError(
         String uid,
         String predicate) implements MpiServiceError {}

   record GeneralError(String error) implements MpiServiceError {}

}
