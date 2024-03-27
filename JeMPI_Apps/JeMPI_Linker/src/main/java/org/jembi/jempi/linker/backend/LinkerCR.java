package org.jembi.jempi.linker.backend;

import io.vavr.control.Either;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jembi.jempi.libmpi.LibMPI;
import org.jembi.jempi.libmpi.MpiGeneralError;
import org.jembi.jempi.libmpi.MpiServiceError;
import org.jembi.jempi.shared.models.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

final class LinkerCR {
   private static final Logger LOGGER = LogManager.getLogger(LinkerCR.class);

   private LinkerCR() {
   }

   static Either<List<GoldenRecord>, MpiGeneralError> crFind(
         final LibMPI libMPI,
         final ApiModels.ApiCrFindRequest crFindData) {
      if (LOGGER.isTraceEnabled()) {
         LOGGER.trace("{}", crFindData);
      }
      return libMPI.findGoldenRecords(crFindData);
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

   private record WorkCandidate(
         GoldenRecord goldenRecord,
         float score) {
   }

}
