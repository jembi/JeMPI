package org.jembi.jempi.libmpi;

import org.jembi.jempi.shared.models.CustomDemographicData;

import java.util.List;

public sealed interface MpiServiceError extends MpiGeneralError {

   record NotImplementedError(
         String error) implements MpiServiceError {
   }

   record InteractionIdDoesNotExistError(
         String error,
         String interactionID) implements MpiServiceError {
   }

   record GoldenIdDoesNotExistError(
         String error,
         String goldenID) implements MpiServiceError {
   }

   record GoldenIdInteractionConflictError(
         String error,
         String goldenID,
         String interactionID) implements MpiServiceError {
   }

   record DeletePredicateError(
         String uid,
         String predicate) implements MpiServiceError {
   }

   record CRClientExistsError(
         List<CustomDemographicData> exists,
         CustomDemographicData request) implements MpiServiceError {
   }

   record CRUpdateFieldError(
         String goldenId,
         String name,
         String value) implements MpiServiceError {
   }

   record GeneralError(String error) implements MpiServiceError {
   }

   record CandidatesNotFoundError(
         String error,
         String interactionID) implements MpiServiceError {
   }

}
