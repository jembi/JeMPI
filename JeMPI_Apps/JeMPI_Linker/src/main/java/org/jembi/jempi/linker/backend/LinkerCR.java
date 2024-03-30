package org.jembi.jempi.linker.backend;

import io.vavr.control.Either;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jembi.jempi.libmpi.LibMPI;
import org.jembi.jempi.libmpi.LibMPIClientInterface;
import org.jembi.jempi.libmpi.MpiGeneralError;
import org.jembi.jempi.libmpi.MpiServiceError;
import org.jembi.jempi.shared.models.*;
import org.jembi.jempi.shared.utils.AppUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
      final var res = libMPI.apiCrFindGoldenRecords(crFindData);
      return res;
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

   static Either<MpiGeneralError, LinkInfo> crLinkToGidUpdate(
         final LibMPI libMPI,
         final ApiModels.ApiCrLinkToGidUpdateRequest req) {
      if (req.uniqueInteractionData().auxDateCreated() == null) {
         return Either.left(new MpiServiceError.CRMissingFieldError("auxDateCreated"));
      } else {
         final var grec = libMPI.findGoldenRecord(req.gid());
         if (grec.isLeft()) {
            return Either.left(grec.getLeft());
         }
         final var goldenRecord = grec.get();
         try {
            if (goldenRecord == null || goldenRecord.demographicData() == null || checkNull(goldenRecord.demographicData())) {
               LOGGER.warn("No golden record: {}", req.gid());
               return Either.left(new MpiServiceError.CRGidDoesNotExistError(req.gid()));
            }
         } catch (IllegalAccessException e) {
            LOGGER.error(e.getLocalizedMessage(), e);
            return Either.left(new MpiServiceError.CRGidDoesNotExistError(req.gid()));
         }
         final var validated1 =
               CustomLinkerDeterministic.validateDeterministicMatch(goldenRecord.demographicData(),
                                                                    req.demographicData());
         final var validated2 =
               CustomLinkerProbabilistic.validateProbabilisticScore(goldenRecord.demographicData(),
                                                                    req.demographicData());
         final var interaction = new Interaction(null, req.sourceId(), req.uniqueInteractionData(), req.demographicData());
         final var linkInfo = libMPI.createInteractionAndLinkToExistingGoldenRecord(
               interaction,
               new LibMPIClientInterface.GoldenIdScore(req.gid(), 1.0F),
               validated1,
               validated2,
               LinkingRule.ADMIN);
         if (linkInfo != null) {
            syncGoldenRecordFields(libMPI,
                                   req.gid(),
                                   goldenRecord.demographicData(),
                                   linkInfo.interactionUID(),
                                   req.demographicData());
            return Either.right(linkInfo);
         } else {
            return Either.left(new MpiServiceError.CRLinkUpdateError(interaction.demographicData()));
         }
      }
   }

   static Either<MpiGeneralError, LinkInfo> crLinkBySourceId(
         final LibMPI libMPI,
         final ApiModels.ApiCrLinkBySourceIdRequest req) {
      if (req.uniqueInteractionData().auxDateCreated() == null) {
         return Either.left(new MpiServiceError.CRMissingFieldError("auxDateCreated"));
      } else {
         final var grec = libMPI.findExpandedSourceIdList(req.sourceId().facility(), req.sourceId().patient());
         if (AppUtils.isNullOrEmpty(grec) || grec.isEmpty() || grec.getFirst().goldenRecords().isEmpty()) {
            return Either.left(new MpiServiceError.CRGidDoesNotExistError("No Golden Record"));
         }
         final var goldenRecord = grec.getFirst().goldenRecords().getFirst();
         final var gid = goldenRecord.goldenId();
         try {
            if (goldenRecord == null || goldenRecord.demographicData() == null || checkNull(goldenRecord.demographicData())) {
               LOGGER.warn("No golden record: {}", gid);
               return Either.left(new MpiServiceError.CRGidDoesNotExistError(gid));
            }
         } catch (IllegalAccessException e) {
            LOGGER.error(e.getLocalizedMessage(), e);
            return Either.left(new MpiServiceError.CRGidDoesNotExistError(gid));
         }
         final var validated1 =
               CustomLinkerDeterministic.validateDeterministicMatch(goldenRecord.demographicData(),
                                                                    req.demographicData());
         final var validated2 =
               CustomLinkerProbabilistic.validateProbabilisticScore(goldenRecord.demographicData(),
                                                                    req.demographicData());
         final var interaction = new Interaction(null, req.sourceId(), req.uniqueInteractionData(), req.demographicData());
         final var linkInfo = libMPI.createInteractionAndLinkToExistingGoldenRecord(
               interaction,
               new LibMPIClientInterface.GoldenIdScore(gid, 1.0F),
               validated1,
               validated2,
               LinkingRule.ADMIN);
         if (linkInfo != null) {
            return Either.right(linkInfo);
         } else {
            return Either.left(new MpiServiceError.CRLinkUpdateError(interaction.demographicData()));
         }
      }
   }

   static Either<MpiGeneralError, LinkInfo> crLinkBySourceIdUpdate(
         final LibMPI libMPI,
         final ApiModels.ApiCrLinkBySourceIdUpdateRequest req) {
      if (req.uniqueInteractionData().auxDateCreated() == null) {
         return Either.left(new MpiServiceError.CRMissingFieldError("auxDateCreated"));
      } else {
         final var grec = libMPI.findExpandedSourceIdList(req.sourceId().facility(), req.sourceId().patient());
         if (AppUtils.isNullOrEmpty(grec) || grec.isEmpty() || grec.getFirst().goldenRecords().isEmpty()) {
            return Either.left(new MpiServiceError.CRGidDoesNotExistError("No Golden Record"));
         }
         final var goldenRecord = grec.getFirst().goldenRecords().getFirst();
         final var gid = goldenRecord.goldenId();
         try {
            if (goldenRecord.demographicData() == null || checkNull(goldenRecord.demographicData())) {
               LOGGER.warn("No golden record: {}", gid);
               return Either.left(new MpiServiceError.CRGidDoesNotExistError(gid));
            }
         } catch (IllegalAccessException e) {
            LOGGER.error(e.getLocalizedMessage(), e);
            return Either.left(new MpiServiceError.CRGidDoesNotExistError(gid));
         }
         final var validated1 =
               CustomLinkerDeterministic.validateDeterministicMatch(goldenRecord.demographicData(),
                                                                    req.demographicData());
         final var validated2 =
               CustomLinkerProbabilistic.validateProbabilisticScore(goldenRecord.demographicData(),
                                                                    req.demographicData());
         LOGGER.debug("Validate: {} {}", validated1, validated2);
         final var interaction = new Interaction(null, req.sourceId(), req.uniqueInteractionData(), req.demographicData());
         final var linkInfo = libMPI.createInteractionAndLinkToExistingGoldenRecord(
               interaction,
               new LibMPIClientInterface.GoldenIdScore(gid, 1.0F),
               validated1,
               validated2,
               LinkingRule.ADMIN);
         if (linkInfo != null) {
            syncGoldenRecordFields(libMPI,
                                   gid,
                                   goldenRecord.demographicData(),
                                   linkInfo.interactionUID(),
                                   req.demographicData());
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
            libMPI.updateGoldenRecordField(iid, gid, f.getName(), gField, iField);
         }
      }
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
