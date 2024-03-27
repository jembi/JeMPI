package org.jembi.jempi.linker.backend;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.vavr.control.Either;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jembi.jempi.libmpi.LibMPI;
import org.jembi.jempi.libmpi.LibMPIClientInterface;
import org.jembi.jempi.libmpi.MpiGeneralError;
import org.jembi.jempi.libmpi.MpiServiceError;
import org.jembi.jempi.shared.models.*;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.jembi.jempi.shared.utils.AppUtils.OBJECT_MAPPER;

final class LinkerCR {
   private static final Logger LOGGER = LogManager.getLogger(LinkerCR.class);

   private LinkerCR() {
   }

   static Either<MpiGeneralError, List<GoldenRecord>> crFind(
         final LibMPI libMPI,
         final ApiModels.ApiCrFindRequest crFindData) {
      if (LOGGER.isTraceEnabled()) {
         LOGGER.trace("{}", crFindData);
      }
      return libMPI.apiCrFindGoldenRecords(crFindData);
   }

   static List<GoldenRecord> crCandidates(
         final LibMPI libMPI,
         final ApiModels.ApiCrCandidatesRequest crCandidatesData) {
      if (LOGGER.isTraceEnabled()) {
         LOGGER.trace("{}", crCandidatesData.demographicData());
      }
      final var matchedCandidates =
            crMatchedCandidates(libMPI, crCandidatesData.candidateThreshold(), crCandidatesData.demographicData());
      LOGGER.trace("size = {}", matchedCandidates.size());
      return matchedCandidates;
   }

   private static List<GoldenRecord> crMatchedCandidates(
         final LibMPI libMPI,
         final float candidateThreshold,
         final CustomDemographicData demographicData) {
      final var candidates = libMPI.findLinkCandidates(demographicData);
      if (candidates.isEmpty()) {
         return List.of();
      } else {
         return candidates.parallelStream()
                          .unordered()
                          .map(candidate -> new WorkCandidate(candidate,
                                                              LinkerUtils.calcNormalizedScore(candidate.demographicData(),
                                                                                              demographicData)))
                          .sorted((o1, o2) -> Float.compare(o2.score(), o1.score()))
                          .filter(x -> x.score >= candidateThreshold)
                          .map(x -> x.goldenRecord)
                          .collect(Collectors.toCollection(ArrayList::new));
      }
   }

   private static Interaction applyAutoCreateFunctions(final Interaction interaction) {
      return new Interaction(interaction.interactionId(),
                             interaction.sourceId(),
                             interaction.uniqueInteractionData(),
                             new CustomDemographicData(interaction.demographicData()));
   }

   static Either<MpiGeneralError, LinkInfo> crRegister(
         final LibMPI libMPI,
         final ApiModels.ApiCrRegisterRequest crRegister) {
      if (LOGGER.isTraceEnabled()) {
         LOGGER.trace("{}", crRegister.demographicData());
      }
      if (crRegister.uniqueInteractionData().auxDateCreated() == null) {
         return Either.left(new MpiServiceError.CRMissingFieldError("auxDateCreated"));
      } else {
         final var matchedCandidates = crMatchedCandidates(libMPI, crRegister.candidateThreshold(), crRegister.demographicData());
         if (matchedCandidates.isEmpty()) {
            final var interaction =
                  new Interaction(null, crRegister.sourceId(), crRegister.uniqueInteractionData(), crRegister.demographicData());
            final var linkInfo =
                  libMPI.createInteractionAndLinkToClonedGoldenRecord(applyAutoCreateFunctions(interaction),
                                                                      1.0F);
            return Either.right(linkInfo);
         } else {
            return Either.left(new MpiServiceError.CRClientExistsError(matchedCandidates.stream()
                                                                                        .map(GoldenRecord::demographicData)
                                                                                        .toList(), crRegister.demographicData()));
         }
      }
   }

   static Either<MpiGeneralError, LinkInfo> crLinkUpdate(
         final LibMPI libMPI,
         final ApiModels.ApiCrLinkUpdateRequest crLinkUpdate) {
      try {
         LOGGER.debug("{}", OBJECT_MAPPER.writeValueAsString(crLinkUpdate.demographicData()));
      } catch (JsonProcessingException e) {
         LOGGER.error(e.getLocalizedMessage(), e);
      }
      if (crLinkUpdate.uniqueInteractionData().auxDateCreated() == null) {
         return Either.left(new MpiServiceError.CRMissingFieldError("auxDateCreated"));
      } else {
         libMPI.startTransaction();
         final var grec = libMPI.findGoldenRecord(crLinkUpdate.gid());
         if (grec.isLeft()) {
            return Either.left(grec.getLeft());
         }
         final var goldenRecord = grec.get();
         try {
            LOGGER.debug("{}", OBJECT_MAPPER.writeValueAsString(goldenRecord));
         } catch (JsonProcessingException e) {
            LOGGER.error(e.getLocalizedMessage(), e);
            return Either.left(new MpiServiceError.CRGidDoesNotExistError(crLinkUpdate.gid()));
         }
         try {
            if (goldenRecord == null || goldenRecord.demographicData() == null || checkNull(goldenRecord.demographicData())) {
               LOGGER.warn("No golden record: {}", crLinkUpdate.gid());
               return Either.left(new MpiServiceError.CRGidDoesNotExistError(crLinkUpdate.gid()));
            }
         } catch (IllegalAccessException e) {
            LOGGER.error(e.getLocalizedMessage(), e);
            return Either.left(new MpiServiceError.CRGidDoesNotExistError(crLinkUpdate.gid()));
         }
         try {
            LOGGER.debug("{}", OBJECT_MAPPER.writeValueAsString(goldenRecord.demographicData()));
         } catch (JsonProcessingException e) {
            LOGGER.error("{}", e.getLocalizedMessage(), e);
         }
         final var validated1 =
               CustomLinkerDeterministic.validateDeterministicMatch(goldenRecord.demographicData(),
                                                                    crLinkUpdate.demographicData());
         final var validated2 =
               CustomLinkerProbabilistic.validateProbabilisticScore(goldenRecord.demographicData(),
                                                                    crLinkUpdate.demographicData());
         LOGGER.debug("Validate: {} {}", validated1, validated2);
         final var interaction = new Interaction(null,
                                                 crLinkUpdate.sourceId(),
                                                 crLinkUpdate.uniqueInteractionData(),
                                                 crLinkUpdate.demographicData());
         final var linkInfo = libMPI.createInteractionAndLinkToExistingGoldenRecord(
               interaction,
               new LibMPIClientInterface.GoldenIdScore(crLinkUpdate.gid(), 1.0F),
               validated1,
               validated2,
               LinkingRule.ADMIN);
         try {
            LOGGER.debug("{}", OBJECT_MAPPER.writeValueAsString(linkInfo));
         } catch (JsonProcessingException e) {
            LOGGER.error(e.getLocalizedMessage(), e);
         }
         if (linkInfo != null) {
            syncGoldenRecordFields(libMPI,
                                   crLinkUpdate.gid(),
                                   goldenRecord.demographicData(),
                                   linkInfo.interactionUID(),
                                   crLinkUpdate.demographicData());
            try {
               LOGGER.debug("{}", OBJECT_MAPPER.writeValueAsString(linkInfo));
            } catch (JsonProcessingException e) {
               LOGGER.error(e.getLocalizedMessage(), e);
            }
            return Either.right(linkInfo);
         } else {
            return Either.left(new MpiServiceError.CRLinkUpdateError(interaction.demographicData()));
         }
      }
   }

   static Either<MpiGeneralError, BackEnd.CrUpdateFieldResponse.UpdateFieldResponse> crUpdateField(
         final LibMPI libMPI,
         final ApiModels.ApiCrUpdateFieldsRequest crUpdateFields) {
      if (LOGGER.isTraceEnabled()) {
         LOGGER.trace("{} {}", crUpdateFields.goldenId(), crUpdateFields.fields());
      }
      final var fail = new ArrayList<String>();
      final var pass = new ArrayList<String>();
      if (StringUtils.isBlank(crUpdateFields.goldenId())) {
         crUpdateFields.fields().forEach(field -> fail.add(field.name()));
         return Either.left(new MpiServiceError.CRUpdateFieldError(null, fail));
      }
      final boolean[] success = new boolean[1];
      crUpdateFields.fields().forEach(field -> {
         if (libMPI.updateGoldenRecordField(crUpdateFields.goldenId(), field.name(), field.value())) {
            pass.add(field.name());
         } else {
            fail.add(field.name());
         }
      });
      LOGGER.debug("{}", success);
      if (fail.isEmpty()) {
         return Either.right(new BackEnd.CrUpdateFieldResponse.UpdateFieldResponse(crUpdateFields.goldenId(), pass, fail));
      } else {
         return Either.left(new MpiServiceError.CRUpdateFieldError(crUpdateFields.goldenId(), fail));
      }
   }

   private static void syncGoldenRecordFields(
         final LibMPI libMPI,
         final String gid,
         final CustomDemographicData goldenRecord,
         final String iid,
         final CustomDemographicData interaction) {
      for (Field f : CustomDemographicData.class.getDeclaredFields()) {
         LOGGER.debug("{}", f.getName());
         String gField = null;
         String iField = null;
         try {
            gField = f.get(goldenRecord).toString();
         } catch (IllegalAccessException | NullPointerException e) {
         }
         try {
            iField = f.get(interaction).toString();
         } catch (IllegalAccessException | NullPointerException e) {
         }
         if ((!StringUtils.isBlank(gField) && !StringUtils.isBlank(iField)
              && !gField.equals(iField))
             ||
             (StringUtils.isBlank(gField) && !StringUtils.isEmpty(iField))) {
            LOGGER.debug("{} {} {}", f.getName(), gField, iField);
            libMPI.updateGoldenRecordField(iid, gid, f.getName(), gField, iField);
         }
      }
      LOGGER.debug("done with sync");
   }

   private static boolean checkNull(final CustomDemographicData demographicData) throws IllegalAccessException {
      for (Field f : demographicData.getClass().getDeclaredFields()) {
         if (f.get(demographicData) != null) {
            return false;
         }
      }
      return true;
   }

   private record WorkCandidate(
         GoldenRecord goldenRecord,
         float score) {
   }

}
