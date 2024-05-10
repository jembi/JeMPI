package org.jembi.jempi.backuprestoreapi;

import akka.http.javadsl.marshallers.jackson.Jackson;
import akka.http.javadsl.marshalling.Marshaller;
import akka.http.javadsl.model.RequestEntity;
import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.server.Route;
import org.jembi.jempi.libmpi.MpiGeneralError;
import org.jembi.jempi.libmpi.MpiServiceError;

import static akka.http.javadsl.server.Directives.complete;
import static org.jembi.jempi.shared.utils.AppUtils.OBJECT_MAPPER;

public final class MapError {

   private static final Marshaller<Object, RequestEntity> JSON_MARSHALLER = Jackson.marshaller(OBJECT_MAPPER);

   private MapError() {
   }

   public static Route mapError(final MpiGeneralError obj) {
      return switch (obj) {
         case MpiServiceError.InteractionIdDoesNotExistError e -> complete(StatusCodes.BAD_REQUEST, e, JSON_MARSHALLER);
         case MpiServiceError.GoldenIdDoesNotExistError e -> complete(StatusCodes.BAD_REQUEST, e, JSON_MARSHALLER);
         case MpiServiceError.GoldenIdInteractionConflictError e -> complete(StatusCodes.BAD_REQUEST, e, JSON_MARSHALLER);
         case MpiServiceError.DeletePredicateError e -> complete(StatusCodes.BAD_REQUEST, e, JSON_MARSHALLER);
         case MpiServiceError.InvalidFunctionError e -> complete(StatusCodes.UNPROCESSABLE_ENTITY, e, JSON_MARSHALLER);
         case MpiServiceError.InvalidOperatorError e -> complete(StatusCodes.UNPROCESSABLE_ENTITY, e, JSON_MARSHALLER);
         case MpiServiceError.NoScoreGivenError e -> complete(StatusCodes.PARTIAL_CONTENT, e, JSON_MARSHALLER);
         case MpiServiceError.NotImplementedError e -> complete(StatusCodes.NOT_IMPLEMENTED, e, JSON_MARSHALLER);
         case MpiServiceError.CRClientExistsError e -> complete(StatusCodes.CONFLICT, e, JSON_MARSHALLER);
         case MpiServiceError.CRUpdateFieldError e -> complete(StatusCodes.BAD_REQUEST, e, JSON_MARSHALLER);
         case MpiServiceError.CRGidDoesNotExistError e -> complete(StatusCodes.NOT_FOUND, e, JSON_MARSHALLER);
         case MpiServiceError.CRLinkUpdateError e -> complete(StatusCodes.BAD_REQUEST, e, JSON_MARSHALLER);
         case MpiServiceError.CRMissingFieldError e -> complete(StatusCodes.BAD_REQUEST, e, JSON_MARSHALLER);
         case MpiServiceError.GeneralError e -> complete(StatusCodes.INTERNAL_SERVER_ERROR, e, JSON_MARSHALLER);
         case MpiServiceError.InternalError e -> complete(StatusCodes.INTERNAL_SERVER_ERROR, e, JSON_MARSHALLER);
         default -> complete(StatusCodes.INTERNAL_SERVER_ERROR);
      };
   }

}
