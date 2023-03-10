package org.jembi.jempi.libmpi;

public sealed interface MpiServiceError extends MpiGeneralError {

   record PatientIdDoesNotExistError(
         String error,
         String patientId) implements MpiServiceError {}

   record GoldenIdDoesNotExistError(
         String error,
         String goldenId) implements MpiServiceError {}

   record GoldenIdPatientConflictError(
         String error,
         String goldenId,
         String patientId) implements MpiServiceError {}

   record DeletePredicateError(
         String uid,
         String predicate) implements MpiServiceError {}

   record GeneralError(String error) implements MpiServiceError {}

   record CandidatesNotFoundError(
         String error,
         String patientId) implements MpiServiceError {}

}
