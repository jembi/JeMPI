package org.jembi.jempi.linker.backend;

import io.vavr.control.Either;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;
import org.jembi.jempi.AppConfig;
import org.jembi.jempi.libmpi.LibMPI;
import org.jembi.jempi.libmpi.LibMPIClientInterface;
import org.jembi.jempi.libmpi.MpiGeneralError;
import org.jembi.jempi.libmpi.MpiServiceError;
import org.jembi.jempi.shared.config.Config;
import org.jembi.jempi.shared.config.FieldsConfig;
import org.jembi.jempi.shared.models.*;
import org.jembi.jempi.shared.utils.AppUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

final class LinkerCR {
   private static final Logger LOGGER;

   static {
      LOGGER = LogManager.getLogger(LinkerCR.class);
      Configurator.setLevel(Map.of(LinkerCR.class.getName(), Level.TRACE));
   }

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
            crMatchedCandidates(libMPI,
                                crCandidatesData.candidateThreshold(),
                                DemographicData.fromCustomDemographicData(crCandidatesData.demographicData()));
      LOGGER.trace("size = {}", matchedCandidates.size());
      return matchedCandidates;
   }

   private static List<GoldenRecord> crMatchedCandidates(
         final LibMPI libMPI,
         final float candidateThreshold,
         final DemographicData demographicData) {
      final var candidates = libMPI.findLinkCandidates(demographicData);
      if (candidates.isEmpty()) {
         return List.of();
      } else {
         return candidates.parallelStream()
                          .unordered()
                          .map(candidate -> new WorkCandidate(candidate,
                                                              LinkerUtils.calcNormalizedLinkScore(candidate.demographicData(),
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
                             interaction.auxInteractionData(),
                             new DemographicData(interaction.demographicData()));
   }

   static Either<MpiGeneralError, LinkInfo> crRegister(
         final LibMPI libMPI,
         final ApiModels.ApiCrRegisterRequest crRegister) {
      if (LOGGER.isTraceEnabled()) {
         LOGGER.trace("{}", crRegister.demographicData());
      }
      if (crRegister.auxInteractionData().get(FieldsConfig.INTERACTION_AUX_DATE_CREATED_FIELD_NAME_CC).isMissingNode()) {
         return Either.left(new MpiServiceError.CRMissingFieldError("auxDateCreated"));
      } else {
         final var matchedCandidates = crMatchedCandidates(libMPI,
                                                           crRegister.candidateThreshold(),
                                                           DemographicData.fromCustomDemographicData(crRegister.demographicData()));
         if (matchedCandidates.isEmpty()) {
            final var interaction =
                  new Interaction(null,
                                  crRegister.sourceId(),
                                  AuxInteractionData.fromCustomAuxInteractionData(crRegister.auxInteractionData()),
                                  DemographicData.fromCustomDemographicData(crRegister.demographicData()));
            final var linkInfo =
                  libMPI.createInteractionAndLinkToClonedGoldenRecord(applyAutoCreateFunctions(interaction),
                                                                      1.0F);
            return Either.right(linkInfo);
         } else {
            return Either.left(new MpiServiceError.CRClientExistsError(matchedCandidates.stream()
                                                                                        .map(GoldenRecord::demographicData)
                                                                                        .toList(),
                                                                       DemographicData.fromCustomDemographicData(crRegister.demographicData())));
         }
      }
   }

   static Either<MpiGeneralError, LinkInfo> crLinkToGidUpdate(
         final LibMPI libMPI,
         final ApiModels.ApiCrLinkToGidUpdateRequest req) {
      if (req.auxInteractionData().get(FieldsConfig.INTERACTION_AUX_DATE_CREATED_FIELD_NAME_CC).isMissingNode()) {
         return Either.left(new MpiServiceError.CRMissingFieldError("auxDateCreated"));
      } else {
         final var reqDemographicData = DemographicData.fromCustomDemographicData(req.demographicData());
         final var grec = libMPI.findGoldenRecord(req.gid());
         if (grec.isLeft()) {
            return Either.left(grec.getLeft());
         }
         final var goldenRecord = grec.get();
         if (goldenRecord == null
             || goldenRecord.demographicData() == null
             || checkNull(goldenRecord.demographicData())
             || AppUtils.isNullOrEmpty(goldenRecord.demographicData().fields)) {
            LOGGER.warn("No golden record: {}", req.gid());
            return Either.left(new MpiServiceError.CRGidDoesNotExistError(req.gid()));
         }
         final var validated1 =
               LinkerDeterministic.validateDeterministicMatch(goldenRecord.demographicData(), reqDemographicData);
         final var validated2 =
               LinkerProbabilistic.validateProbabilisticScore(goldenRecord.demographicData(), reqDemographicData);
         final var interaction = new Interaction(null,
                                                 req.sourceId(),
                                                 AuxInteractionData.fromCustomAuxInteractionData(req.auxInteractionData()),
                                                 reqDemographicData);
         final var linkInfo = libMPI.createInteractionAndLinkToExistingGoldenRecord(
               interaction,
               new LibMPIClientInterface.GoldenIdScore(req.gid(), 1.0F),
               validated1,
               validated2,
               LinkingRule.EXPLICIT_GID);
         if (linkInfo != null) {
            syncGoldenRecordToInteraction(libMPI,
                                          req.gid(),
                                          goldenRecord.demographicData(),
                                          linkInfo.interactionUID(),
                                          reqDemographicData);
            return Either.right(linkInfo);
         } else {
            return Either.left(new MpiServiceError.CRLinkUpdateError(interaction.demographicData()));
         }
      }
   }

   private static Either<MpiGeneralError, LinkInfo> crLinkBySourceIdUpdate(
         final LibMPI libMPI,
         final boolean syncGoldenRecord,
         final SourceId sourceId,
         final AuxInteractionData auxInteractionData,
         final DemographicData demographicData) {
      if (auxInteractionData.auxDateCreated() == null) {
         return Either.left(new MpiServiceError.CRMissingFieldError("auxDateCreated"));
      } else {
         final var grec = libMPI.findExpandedSourceIdList(sourceId.facility(), sourceId.patient());
         if (AppUtils.isNullOrEmpty(grec) || grec.isEmpty() || grec.getFirst().goldenRecords().isEmpty()) {
            final var linkInfo = LinkerDWH.linkInteraction(libMPI,
                                                           new Interaction(null,
                                                                           sourceId,
                                                                           auxInteractionData,
                                                                           demographicData),
                                                           null,
                                                           AppConfig.LINKER_MATCH_THRESHOLD,
                                                           "STAN");

            return Either.right(linkInfo.get());
         }
         final var goldenRecord = grec.getFirst().goldenRecords().getFirst();
         final var gid = goldenRecord.goldenId();
         if (goldenRecord.demographicData() == null
             || checkNull(goldenRecord.demographicData())
             || AppUtils.isNullOrEmpty(goldenRecord.demographicData().fields)) {
            LOGGER.warn("No golden record: {}", gid);
            return Either.left(new MpiServiceError.CRGidDoesNotExistError(gid));
         }
         final var validated1 =
               LinkerDeterministic.validateDeterministicMatch(goldenRecord.demographicData(), demographicData);
         final var validated2 =
               LinkerProbabilistic.validateProbabilisticScore(goldenRecord.demographicData(), demographicData);
         final var interaction = new Interaction(null, sourceId, auxInteractionData, demographicData);
         final var linkInfo = libMPI.createInteractionAndLinkToExistingGoldenRecord(
               interaction,
               new LibMPIClientInterface.GoldenIdScore(gid, 1.0F),
               validated1,
               validated2,
               LinkingRule.EXPLICIT_SOURCE_ID);
         if (linkInfo != null) {
            if (syncGoldenRecord) {
               syncGoldenRecordToInteraction(libMPI,
                                             gid,
                                             goldenRecord.demographicData(),
                                             linkInfo.interactionUID(),
                                             demographicData);
            }
            return Either.right(linkInfo);
         } else {
            return Either.left(new MpiServiceError.CRLinkUpdateError(interaction.demographicData()));
         }
      }
   }

   static Either<MpiGeneralError, LinkInfo> crLinkBySourceId(
         final LibMPI libMPI,
         final ApiModels.ApiCrLinkBySourceIdRequest req) {
      return crLinkBySourceIdUpdate(libMPI,
                                    false,
                                    req.sourceId(),
                                    AuxInteractionData.fromCustomAuxInteractionData(req.auxInteractionData()),
                                    DemographicData.fromCustomDemographicData(req.demographicData()));
   }

   static Either<MpiGeneralError, LinkInfo> crLinkBySourceIdUpdate(
         final LibMPI libMPI,
         final ApiModels.ApiCrLinkBySourceIdUpdateRequest req) {

      return crLinkBySourceIdUpdate(libMPI,
                                    true,
                                    req.sourceId(),
                                    AuxInteractionData.fromCustomAuxInteractionData(req.auxInteractionData()),
                                    DemographicData.fromCustomDemographicData(req.demographicData()));
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
      crUpdateFields.fields().forEach(field -> {
         final var i = Config.FIELDS_CONFIG.findIndexOfDemographicField(field.name());
         if (i < 0) {
            LOGGER.error("{} {}", field.name(), field.value());
         } else {
            if (libMPI.updateGoldenRecordField(crUpdateFields.goldenId(), "demographic_field_%02d".formatted(i), field.value())) {
               pass.add(field.name());
            } else {
               fail.add(field.name());
            }
         }
      });
      if (fail.isEmpty()) {
         return Either.right(new BackEnd.CrUpdateFieldResponse.UpdateFieldResponse(crUpdateFields.goldenId(), pass, fail));
      } else {
         return Either.left(new MpiServiceError.CRUpdateFieldError(crUpdateFields.goldenId(), fail));
      }
   }

   private static void syncGoldenRecordToInteraction(
         final LibMPI libMPI,
         final String gid,
         final DemographicData goldenRecord,
         final String iid,
         final DemographicData interaction) {
      for (int i = 0; i < Config.DGRAPH_CONFIG.demographicDataFields.size(); i++) {
         final String gField = goldenRecord.fields.get(i).value();
         final String iField = interaction.fields.get(i).value();
         if ((!StringUtils.isBlank(gField) && !StringUtils.isBlank(iField) && !gField.equals(iField))
             ||
             (StringUtils.isBlank(gField) && !StringUtils.isEmpty(iField))) {
            libMPI.updateGoldenRecordField(iid,
                                           gid,
                                           Config.DGRAPH_CONFIG.demographicDataFields.get(i).getLeft(),
                                           gField,
                                           iField,
                                           Config.DGRAPH_CONFIG.demographicDataFields.get(i).getLeft());
         }
      }
   }

   private static boolean checkNull(final DemographicData demographicData) {

      for (int i = 0; i < demographicData.fields.size(); i++) {
         if (demographicData.fields.get(i) != null) {
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
