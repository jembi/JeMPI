package org.jembi.jempi.api;

import akka.actor.testkit.typed.javadsl.ActorTestKit;
import akka.actor.testkit.typed.javadsl.TestProbe;
import akka.actor.typed.ActorRef;
import io.vavr.control.Either;
import org.jembi.jempi.libmpi.LibMPI;
import org.jembi.jempi.libmpi.MpiGeneralError;
import org.jembi.jempi.libmpi.MpiServiceError;
import org.jembi.jempi.shared.models.*;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

import static org.mockito.Mockito.*;

class CustomAPIBackEndTest {

   private static LibMPI libMPI;

   private static ActorTestKit testKit;

   final String error = "test error";

   @BeforeAll
   public static void setUp() {
      testKit = ActorTestKit.create();
      libMPI = mock(LibMPI.class);
   }

   @AfterAll
   public static void cleanup() {
      testKit.shutdownTestKit();
   }

   @Test
   void testEventGetGoldenRecordDocumentsHandler() {
      GoldenRecord goldenRecord1 = new GoldenRecord(
            new Interaction(
                  "1234",
                  new SourceId("f1fa7b5c", "4e71", "11ec-8d3d-0242ac130003"),
                  new CustomDemographicData(
                        "auxId",
                        "John",
                        "Doe",
                        "Male",
                        "1990-01-01",
                        "New York",
                        "555-1234",
                        "123-45-6789")
            ));

      List<InteractionWithScore> interactionWithScoreList1 = List.of(
            new InteractionWithScore(null, null),
            new InteractionWithScore(null, null));

      ExpandedGoldenRecord expandedGoldenRecord1 = new ExpandedGoldenRecord(goldenRecord1, interactionWithScoreList1);

      GoldenRecord goldenRecord2 = new GoldenRecord(
            new Interaction(
                  "5678",
                  new SourceId("d0d38f0e", "4e71", "11ec-8d3d-0242ac130003"),
                  new CustomDemographicData(
                        "auxId2",
                        "Jane",
                        "Smith",
                        "Female",
                        "1985-05-10",
                        "Los Angeles",
                        "555-5678",
                        "987-65-4321")
            ));

      List<InteractionWithScore> interactionWithScoreList2 = List.of(
            new InteractionWithScore(null, null),
            new InteractionWithScore(null, null));

      ExpandedGoldenRecord expandedGoldenRecord2 = new ExpandedGoldenRecord(goldenRecord2, interactionWithScoreList2);

      GoldenRecord goldenRecord3 = new GoldenRecord(
            new Interaction(
                  "9012",
                  new SourceId("c44a67f6", "4e71", "11ec-8d3d-0242ac130003"),
                  new CustomDemographicData(
                        "auxId3",
                        "Robert",
                        "Johnson",
                        "Male",
                        "1975-12-18",
                        "Chicago",
                        "555-9012",
                        "456-78-9012")
            ));

      List<InteractionWithScore> interactionWithScoreList3 = List.of(
            new InteractionWithScore(null, null),
            new InteractionWithScore(null, null));

      ExpandedGoldenRecord expandedGoldenRecord3 = new ExpandedGoldenRecord(goldenRecord3, interactionWithScoreList3);

      List<ExpandedGoldenRecord> expandedGoldenRecords =
            List.of(expandedGoldenRecord1, expandedGoldenRecord2, expandedGoldenRecord3);
      final var uids = List.of("1234", "5678", "9012");

      // mock LibMPI interface
      final var libMPI = mock(LibMPI.class);
      when(libMPI.findExpandedGoldenRecords(uids)).thenReturn(expandedGoldenRecords);

      // create ActorSystem and ActorRef using ActorTestKit
      ActorTestKit testKit = ActorTestKit.create();
      ActorRef<BackEnd.Event> myActorRef = testKit.spawn(BackEnd.create(libMPI));

      // create TestProbe to receive response from actor
      TestProbe<BackEnd.FindExpandedGoldenRecordsResponse> replyTo = testKit.createTestProbe();

      // send request to actor
      myActorRef.tell(new BackEnd.FindExpandedGoldenRecordsRequest(replyTo.getRef(), uids));

      // assert that response matches expected value
      replyTo.expectMessage(new BackEnd.FindExpandedGoldenRecordsResponse(Either.right(expandedGoldenRecords)));
   }

   @Test
   void testFindPatientByUidEventHandler() {
      // Create a mock libMPI instance
      final var libMPI = mock(LibMPI.class);

      // Create an instance of the actor to be tested
      final var backend = testKit.spawn(BackEnd.create(libMPI));

      // Create a TestProbe to receive responses
      final var probe = testKit.createTestProbe(BackEnd.FindPatientRecordResponse.class);

      // Create an EventFindPatientByUidRequest message with a specific UID value
      final var request = new BackEnd.FindPatientRecordRequest(probe.getRef(), "1234");

      // Stub the mock libMPI instance to return a CustomEntity object when `getDocument()` is called
      final var patientRecord = new Interaction(
            "1234", // uid
            new SourceId("f1fa7b5c", "4e71", "11ec-8d3d-0242ac130003"), // sourceId
            new CustomDemographicData(
                  "auxId", // auxId
                  "John", // givenName
                  "Doe", // familyName
                  "Male", // gender
                  "1990-01-01", // dob
                  "New York", // city
                  "555-1234", // phoneNumber
                  "123-45-6789")); // nationalId

      when(libMPI.findInteraction("1234")).thenReturn(patientRecord);

      // Send the message to the actor
      backend.tell(request);

      // Expect to receive the expected response
      final var response = probe.receiveMessage();
      assertEquals(patientRecord, response.patient().get());

      // Verify that libMPI was called with the correct arguments
      verify(libMPI).startTransaction();
      verify(libMPI).findInteraction("1234");
      verify(libMPI).closeTransaction();
   }

   //@Test TODO: find solution to handle exception test
   public void findPatientRecordHandler_whenFindPatientRecordThrowsException_ReturnSuccess() {
      final var libMPI = mock(LibMPI.class);
      String patientId = "12344";

      when(libMPI.findInteraction(patientId)).thenThrow(new Exception("patient record does not exist"));

      ActorTestKit testKit = ActorTestKit.create();
      ActorRef<BackEnd.Event> myActorRef = testKit.spawn(BackEnd.create(libMPI));
      TestProbe<BackEnd.FindPatientRecordResponse> replyTo = testKit.createTestProbe();

      myActorRef.tell(new BackEnd.FindPatientRecordRequest(replyTo.getRef(), patientId));

      replyTo.expectMessage(new BackEnd.FindPatientRecordResponse(Either.left(new MpiServiceError.PatientIdDoesNotExistError(
            "Patient not found",
            patientId))));
   }

   @Test
   public void findPatientRecordHandler_whenFindPatientRecordReturnsNull_ReturnNotFound() {
      final var libMPI = mock(LibMPI.class);
      String patientId = "12345";
      when(libMPI.findInteraction(patientId)).thenReturn(null);

      ActorTestKit testKit = ActorTestKit.create();
      ActorRef<BackEnd.Event> myActorRef = testKit.spawn(BackEnd.create(libMPI));
      TestProbe<BackEnd.FindPatientRecordResponse> replyTo = testKit.createTestProbe();

      myActorRef.tell(new BackEnd.FindPatientRecordRequest(replyTo.getRef(), patientId));

      replyTo.expectMessage(new BackEnd.FindPatientRecordResponse(Either.left(new MpiServiceError.PatientIdDoesNotExistError(
            "Patient not found",
            patientId))));
   }

   @Test
   public void findPatientRecordHandler_whenFindPatientRecordSuccess_ReturnSuccess() {
      final var libMPI = mock(LibMPI.class);
      String patientId = "9012";
      Interaction interaction = new Interaction(
            patientId,
            new SourceId("c44a67f6", "4e71", "11ec-8d3d-0242ac130003"),
            new CustomDemographicData(
                  "auxId3",
                  "Robert",
                  "Johnson",
                  "Male",
                  "1975-12-18",
                  "Chicago",
                  "555-9012",
                  "456-78-9012")
      );

      when(libMPI.findInteraction(patientId)).thenReturn(interaction);

      ActorTestKit testKit = ActorTestKit.create();
      ActorRef<BackEnd.Event> myActorRef = testKit.spawn(BackEnd.create(libMPI));
      TestProbe<BackEnd.FindPatientRecordResponse> replyTo = testKit.createTestProbe();

      myActorRef.tell(new BackEnd.FindPatientRecordRequest(replyTo.getRef(), patientId));

      replyTo.expectMessage(new BackEnd.FindPatientRecordResponse(Either.right(interaction)));
   }

   //@Test TODO: find solution to handle exception test
   public void findExpandedGoldenRecordHandler_whenFindExpandedGoldenRecordThrowsException_ReturnSuccess() {
      final var libMPI = mock(LibMPI.class);
      when(libMPI.findExpandedGoldenRecord(anyString())).thenThrow(new Exception("golden record does not exist"));

      ActorTestKit testKit = ActorTestKit.create();
      ActorRef<BackEnd.Event> myActorRef = testKit.spawn(BackEnd.create(libMPI));
      TestProbe<BackEnd.FindExpandedGoldenRecordResponse> replyTo = testKit.createTestProbe();

      myActorRef.tell(new BackEnd.FindExpandedGoldenRecordRequest(replyTo.getRef(), anyString()));

      replyTo.expectMessage(new BackEnd.FindExpandedGoldenRecordResponse(null));
   }

   @Test
   public void findExpandedGoldenRecordHandler_whenFindExpandedGoldenRecordReturnsNull_ReturnSuccess() {
      String goldenId = "54321";

      final var libMPI = mock(LibMPI.class);
      when(libMPI.findExpandedGoldenRecord(goldenId)).thenReturn(null);

      ActorTestKit testKit = ActorTestKit.create();
      ActorRef<BackEnd.Event> myActorRef = testKit.spawn(BackEnd.create(libMPI));
      TestProbe<BackEnd.FindExpandedGoldenRecordResponse> replyTo = testKit.createTestProbe();

      myActorRef.tell(new BackEnd.FindExpandedGoldenRecordRequest(replyTo.getRef(), goldenId));

      replyTo.expectMessage(new BackEnd.FindExpandedGoldenRecordResponse(Either.left(new MpiServiceError.GoldenIdDoesNotExistError(
            "Golden Record does not exist",
            goldenId))));
   }

   @Test
   public void findExpandedGoldenRecordHandler_whenFindExpandedGoldenRecordSuccess_ReturnSuccess() {
      final var libMPI = mock(LibMPI.class);
      String goldenId = "9010";
      GoldenRecord goldenRecord = new GoldenRecord(
            new Interaction(
                  "9013",
                  new SourceId("c44a67f6", "4e71", "11ec-8d3d-0242ac130003"),
                  new CustomDemographicData(
                        "auxId3",
                        "Robert",
                        "Johnson",
                        "Male",
                        "1975-12-18",
                        "Chicago",
                        "555-9012",
                        "456-78-9012")
            ));
      List<InteractionWithScore> interactionWithScoreList = List.of(
            new InteractionWithScore(null, null),
            new InteractionWithScore(null, null));
      ExpandedGoldenRecord expandedGoldenRecord = new ExpandedGoldenRecord(goldenRecord, interactionWithScoreList);

      when(libMPI.findExpandedGoldenRecord(goldenId)).thenReturn(expandedGoldenRecord);

      ActorTestKit testKit = ActorTestKit.create();
      ActorRef<BackEnd.Event> myActorRef = testKit.spawn(BackEnd.create(libMPI));
      TestProbe<BackEnd.FindExpandedGoldenRecordResponse> replyTo = testKit.createTestProbe();

      myActorRef.tell(new BackEnd.FindExpandedGoldenRecordRequest(replyTo.getRef(), goldenId));

      replyTo.expectMessage(new BackEnd.FindExpandedGoldenRecordResponse(Either.right(expandedGoldenRecord)));
   }

   @Test
   public void findCandidatesHandler_whenFindPatientRecordSuccessAndFindCandidatesSuccess_ReturnSuccess() {
      final var libMPI = mock(LibMPI.class);
      CustomMU customMU = new CustomMU(new double[]{0.8, 0.8, 0.8, 0.8, 0.8, 0.8, 0.8},
                                       new double[]{0.002, 0.002, 0.002, 0.002, 0.002, 0.002, 0.002});
      float score = 0.71428573F;
      String patientId = "9014";
      Interaction interaction = new Interaction(
            "1235",
            new SourceId("f1fa7b5c", "4e71", "11ec-8d3d-0242ac130003"),
            new CustomDemographicData(
                  "auxId",
                  "John",
                  "Doe",
                  "Male",
                  "1990-01-01",
                  "Johannesburg",
                  "555-1234",
                  "123-45-6789")
      );

      GoldenRecord goldenRecord = new GoldenRecord(
            new Interaction(
                  "1236",
                  new SourceId("f1fa7b5d", "4e71", "11ec-8d3d-0242ac130004"),
                  new CustomDemographicData(
                        "auxId",
                        "Jonathan",
                        "Doe",
                        "Male",
                        "1990-01-01",
                        "Cape Town",
                        "555-1234",
                        "123-45-6789")
            ));

      List<GoldenRecord> goldenRecords = new ArrayList<>();
      goldenRecords.add(goldenRecord);

      List<BackEnd.FindCandidatesResponse.Candidate> candidates = new ArrayList<>();
      BackEnd.FindCandidatesResponse.Candidate candidate =
            new BackEnd.FindCandidatesResponse.Candidate(goldenRecord, score);
      candidates.add(candidate);

      when(libMPI.findInteraction(patientId)).thenReturn(interaction);
      when(libMPI.getCandidates(any(CustomDemographicData.class), anyBoolean())).thenReturn(goldenRecords);

      ActorTestKit testKit = ActorTestKit.create();
      ActorRef<BackEnd.Event> myActorRef = testKit.spawn(BackEnd.create(libMPI));
      TestProbe<BackEnd.FindCandidatesResponse> replyTo = testKit.createTestProbe();

      myActorRef.tell(new BackEnd.FindCandidatesRequest(replyTo.getRef(), patientId, customMU));

      replyTo.expectMessage(new BackEnd.FindCandidatesResponse(Either.right(candidates)));
   }

   @Test
   public void findCandidatesHandler_whenFindPatientRecordThrowException_ReturnNotFound() {
      String patientId = "9015";

      MpiGeneralError notFoundError = new MpiServiceError.PatientIdDoesNotExistError(
            "Patient not found",
            patientId);

      CustomMU customMU = new CustomMU(new double[]{0.8, 0.8, 0.8, 0.8, 0.8, 0.8, 0.8},
                                       new double[]{0.002, 0.002, 0.002, 0.002, 0.002, 0.002, 0.002});

      GoldenRecord goldenRecord = new GoldenRecord(
            new Interaction(
                  "1236",
                  new SourceId("f1fa7b5d", "4e71", "11ec-8d3d-0242ac130004"),
                  new CustomDemographicData(
                        "auxId",
                        "Jonathan",
                        "Doe",
                        "Male",
                        "1990-01-01",
                        "Cape Town",
                        "555-1234",
                        "123-45-6789")
            ));

      List<GoldenRecord> goldenRecords = new ArrayList<>();
      goldenRecords.add(goldenRecord);

      final var libMPI = mock(LibMPI.class);
      when(libMPI.findInteraction(patientId)).thenThrow(new RuntimeException());
      when(libMPI.getCandidates(any(CustomDemographicData.class), anyBoolean())).thenReturn(goldenRecords);

      ActorTestKit testKit = ActorTestKit.create();
      ActorRef<BackEnd.Event> myActorRef = testKit.spawn(BackEnd.create(libMPI));
      TestProbe<BackEnd.FindCandidatesResponse> replyTo = testKit.createTestProbe();

      myActorRef.tell(new BackEnd.FindCandidatesRequest(replyTo.getRef(), patientId, customMU));

      replyTo.expectMessage(new BackEnd.FindCandidatesResponse(Either.left(notFoundError)));
   }

   @Test
   public void findCandidatesHandler_whenFindPatientRecordSuccessAndFindCandidatesThrowsException_ReturnNotFound() {
      String patientId = "9016";

      MpiGeneralError notFoundError = new MpiServiceError.CandidatesNotFoundError(
            "Candidates(golden records) not found with demographic data for patientId",
            patientId);

      CustomMU customMU = new CustomMU(new double[]{0.8, 0.8, 0.8, 0.8, 0.8, 0.8, 0.8},
                                       new double[]{0.002, 0.002, 0.002, 0.002, 0.002, 0.002, 0.002});

      Interaction interaction = new Interaction(
            patientId,
            new SourceId("f1fa7b5c", "4e71", "11ec-8d3d-0242ac130003"),
            new CustomDemographicData(
                  "auxId",
                  "John",
                  "Doe",
                  "Male",
                  "1990-01-01",
                  "Johannesburg",
                  "555-1234",
                  "123-45-6789")
      );

      final var libMPI = mock(LibMPI.class);
      when(libMPI.findInteraction(patientId)).thenReturn(interaction);
      when(libMPI.getCandidates(any(CustomDemographicData.class), anyBoolean())).thenThrow(new RuntimeException());

      ActorTestKit testKit = ActorTestKit.create();
      ActorRef<BackEnd.Event> myActorRef = testKit.spawn(BackEnd.create(libMPI));
      TestProbe<BackEnd.FindCandidatesResponse> replyTo = testKit.createTestProbe();

      myActorRef.tell(new BackEnd.FindCandidatesRequest(replyTo.getRef(), patientId, customMU));

      replyTo.expectMessage(new BackEnd.FindCandidatesResponse(Either.left(notFoundError)));
   }

   @Test
   public void findCandidatesHandler_whenFindPatientRecordSuccessAndFindCandidatesReturnEmpty_ReturnEmpty() {
      String patientId = "9017";

      MpiGeneralError notFoundError = new MpiServiceError.CandidatesNotFoundError(
            "Candidates(golden records) not found with demographic data for patientId",
            patientId);

      CustomMU customMU = new CustomMU(new double[]{0.8, 0.8, 0.8, 0.8, 0.8, 0.8, 0.8},
                                       new double[]{0.002, 0.002, 0.002, 0.002, 0.002, 0.002, 0.002});

      Interaction interaction = new Interaction(
            patientId,
            new SourceId("f1fa7b5c", "4e71", "11ec-8d3d-0242ac130003"),
            new CustomDemographicData(
                  "auxId",
                  "John",
                  "Doe",
                  "Male",
                  "1990-01-01",
                  "Johannesburg",
                  "555-1234",
                  "123-45-6789")
      );

      List<GoldenRecord> goldenRecords = new ArrayList<>();

      final var libMPI = mock(LibMPI.class);
      when(libMPI.findInteraction(patientId)).thenReturn(interaction);
      when(libMPI.getCandidates(any(CustomDemographicData.class), anyBoolean())).thenReturn(goldenRecords);

      ActorTestKit testKit = ActorTestKit.create();
      ActorRef<BackEnd.Event> myActorRef = testKit.spawn(BackEnd.create(libMPI));
      TestProbe<BackEnd.FindCandidatesResponse> replyTo = testKit.createTestProbe();

      myActorRef.tell(new BackEnd.FindCandidatesRequest(replyTo.getRef(), patientId, customMU));

      replyTo.expectMessage(new BackEnd.FindCandidatesResponse(Either.right(new ArrayList<>())));
   }

   @Test
   public void findExpandedPatientRecordsHandler_whenFindExpandedPatientRecordsSuccess_ReturnSuccess() {
      final var libMPI = mock(LibMPI.class);
      String patientId = "9018";
      Interaction interaction = new Interaction(
            patientId,
            new SourceId("c44a67f6", "4e71", "11ec-8d3d-0242ac130003"),
            new CustomDemographicData(
                  "auxId3",
                  "Robert",
                  "Johnson",
                  "Male",
                  "1975-12-18",
                  "Chicago",
                  "555-9012",
                  "456-78-9012")
      );
      ExpandedInteraction expandedInteraction = new ExpandedInteraction(interaction, null);
      List<ExpandedInteraction> expandedInteractions = new ArrayList<>() {{add(expandedInteraction);}};
      List<String> patientIds = new ArrayList<>() {{add(patientId);}};

      when(libMPI.findExpandedPatientRecords(patientIds)).thenReturn(expandedInteractions);

      ActorTestKit testKit = ActorTestKit.create();
      ActorRef<BackEnd.Event> myActorRef = testKit.spawn(BackEnd.create(libMPI));
      TestProbe<BackEnd.FindExpandedPatientRecordsResponse> replyTo = testKit.createTestProbe();

      myActorRef.tell(new BackEnd.FindExpandedPatientRecordsRequest(replyTo.getRef(), patientIds));

      replyTo.expectMessage(new BackEnd.FindExpandedPatientRecordsResponse(Either.right(expandedInteractions)));
   }

   @Test
   public void findExpandedPatientRecordsHandler_whenFindExpandedPatientRecordsReturnNull_ReturnNotFound() {
      final var libMPI = mock(LibMPI.class);
      String patientId = "9019";
      List<String> patientIds = new ArrayList<>() {{add(patientId);}};

      when(libMPI.findExpandedPatientRecords(patientIds)).thenReturn(null);

      ActorTestKit testKit = ActorTestKit.create();
      ActorRef<BackEnd.Event> myActorRef = testKit.spawn(BackEnd.create(libMPI));
      TestProbe<BackEnd.FindExpandedPatientRecordsResponse> replyTo = testKit.createTestProbe();

      myActorRef.tell(new BackEnd.FindExpandedPatientRecordsRequest(replyTo.getRef(), patientIds));

      replyTo.expectMessage(new BackEnd.FindExpandedPatientRecordsResponse(Either.left(new MpiServiceError.PatientIdDoesNotExistError(
            "Patient Records do not exist",
            List.of(patientIds).toString()))));
   }

   @Test
   public void getGoldenRecordCountHandler_whenGetGoldenRecordCountSuccess_ReturnSuccess() {
      long count = 50;

      when(libMPI.countGoldenRecords()).thenReturn(count);

      ActorTestKit testKit = ActorTestKit.create();
      ActorRef<BackEnd.Event> myActorRef = testKit.spawn(BackEnd.create(libMPI));
      TestProbe<BackEnd.GetGoldenRecordCountResponse> replyTo = testKit.createTestProbe();

      myActorRef.tell(new BackEnd.GetGoldenRecordCountRequest(replyTo.getRef()));

      replyTo.expectMessage(new BackEnd.GetGoldenRecordCountResponse(Either.right(count)));
   }

   @Test
   public void getGoldenRecordCountHandler_whenGetGoldenRecordCountThrowsException_ReturnGeneralError() {

      when(libMPI.countGoldenRecords()).thenThrow(new RuntimeException(error));

      ActorTestKit testKit = ActorTestKit.create();
      ActorRef<BackEnd.Event> myActorRef = testKit.spawn(BackEnd.create(libMPI));
      TestProbe<BackEnd.GetGoldenRecordCountResponse> replyTo = testKit.createTestProbe();

      myActorRef.tell(new BackEnd.GetGoldenRecordCountRequest(replyTo.getRef()));

      replyTo.expectMessage(new BackEnd.GetGoldenRecordCountResponse(Either.left(new MpiServiceError.GeneralError(error))));
   }

   @Test
   public void getPatientRecordCountHandler_whenGetPatientRecordCountSuccess_ReturnSuccess() {
      long count = 100;

      when(libMPI.countInteractions()).thenReturn(count);

      ActorTestKit testKit = ActorTestKit.create();
      ActorRef<BackEnd.Event> myActorRef = testKit.spawn(BackEnd.create(libMPI));
      TestProbe<BackEnd.GetPatientRecordCountResponse> replyTo = testKit.createTestProbe();

      myActorRef.tell(new BackEnd.GetPatientRecordCountRequest(replyTo.getRef()));

      replyTo.expectMessage(new BackEnd.GetPatientRecordCountResponse(Either.right(count)));
   }

   @Test
   public void getPatientRecordCountHandler_whenGetPatientRecordCountThrowsException_ReturnGeneralError() {

      when(libMPI.countInteractions()).thenThrow(new RuntimeException(error));

      ActorTestKit testKit = ActorTestKit.create();
      ActorRef<BackEnd.Event> myActorRef = testKit.spawn(BackEnd.create(libMPI));
      TestProbe<BackEnd.GetPatientRecordCountResponse> replyTo = testKit.createTestProbe();

      myActorRef.tell(new BackEnd.GetPatientRecordCountRequest(replyTo.getRef()));

      replyTo.expectMessage(new BackEnd.GetPatientRecordCountResponse(Either.left(new MpiServiceError.GeneralError(error))));
   }
}