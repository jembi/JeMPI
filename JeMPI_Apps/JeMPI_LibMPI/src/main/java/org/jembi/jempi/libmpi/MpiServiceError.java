package org.jembi.jempi.libmpi;

public sealed interface MpiServiceError extends MpiGeneralError {

   record EntityIDDoesNotExistError(String error, String uid) implements MpiServiceError {}

   record GoldenIDDoesNotExistError(String error, String uid) implements MpiServiceError {}

   record GoldenIDEntityConflictError(String error, String goldenID, String entityID) implements MpiServiceError {}

   record DeletePredicateError(String uid, String predicate) implements MpiServiceError {}

   record GeneralError(String error) implements MpiServiceError {}

}
