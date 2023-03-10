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
            new PatientRecord(
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

      List<PatientRecordWithScore> patientRecordWithScoreList1 = List.of(
            new PatientRecordWithScore(null, null),
            new PatientRecordWithScore(null, null));

      ExpandedGoldenRecord expandedGoldenRecord1 = new ExpandedGoldenRecord(goldenRecord1, patientRecordWithScoreList1);

      GoldenRecord goldenRecord2 = new GoldenRecord(
            new PatientRecord(
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

      List<PatientRecordWithScore> patientRecordWithScoreList2 = List.of(
            new PatientRecordWithScore(null, null),
            new PatientRecordWithScore(null, null));

      ExpandedGoldenRecord expandedGoldenRecord2 = new ExpandedGoldenRecord(goldenRecord2, patientRecordWithScoreList2);

      GoldenRecord goldenRecord3 = new GoldenRecord(
            new PatientRecord(
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

      List<PatientRecordWithScore> patientRecordWithScoreList3 = List.of(
            new PatientRecordWithScore(null, null),
            new PatientRecordWithScore(null, null));

      ExpandedGoldenRecord expandedGoldenRecord3 = new ExpandedGoldenRecord(goldenRecord3, patientRecordWithScoreList3);

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
      replyTo.expectMessage(new BackEnd.FindExpandedGoldenRecordsResponse(expandedGoldenRecords));
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
      final var patientRecord = new PatientRecord(
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

      when(libMPI.findPatientRecord("1234")).thenReturn(Either.right(patientRecord));

      // Send the message to the actor
      backend.tell(request);

      // Expect to receive the expected response
      final var response = probe.receiveMessage();
      assertEquals(patientRecord, response.patient().get());

      // Verify that libMPI was called with the correct arguments
      verify(libMPI).startTransaction();
      verify(libMPI).findPatientRecord("1234");
      verify(libMPI).closeTransaction();
   }

   //@Test TODO: find solution to handle exception test
   public void findPatientRecordHandler_whenFindPatientRecordThrowsException_ReturnSuccess() {
      final var libMPI = mock(LibMPI.class);
      String patientId = "12344";

      when(libMPI.findPatientRecord(patientId)).thenThrow(new Exception("patient record does not exist"));

      ActorTestKit testKit = ActorTestKit.create();
      ActorRef<BackEnd.Event> myActorRef = testKit.spawn(BackEnd.create(libMPI));
      TestProbe<BackEnd.FindPatientRecordResponse> replyTo = testKit.createTestProbe();

      myActorRef.tell(new BackEnd.FindPatientRecordRequest(replyTo.getRef(), patientId));

      replyTo.expectMessage(new BackEnd.FindPatientRecordResponse(Either.left(new MpiServiceError.PatientIdDoesNotExistError(
            "Patient not found",
            patientId))));
   }

   @Test
   public void findPatientRecordHandler_whenFindPatientRecordReturnNotFound_ReturnNotFound() {
      final var libMPI = mock(LibMPI.class);
      String patientId = "12345";
      Either<MpiGeneralError, PatientRecord> response = Either.left(new MpiServiceError.PatientIdDoesNotExistError(
            "Patient not found",
            patientId));
      when(libMPI.findPatientRecord(patientId)).thenReturn(response);

      ActorTestKit testKit = ActorTestKit.create();
      ActorRef<BackEnd.Event> myActorRef = testKit.spawn(BackEnd.create(libMPI));
      TestProbe<BackEnd.FindPatientRecordResponse> replyTo = testKit.createTestProbe();

      myActorRef.tell(new BackEnd.FindPatientRecordRequest(replyTo.getRef(), patientId));

      replyTo.expectMessage(new BackEnd.FindPatientRecordResponse(response));
   }

   @Test
   public void findPatientRecordHandler_whenFindPatientRecordReturnsNull_ReturnNotFound() {
      final var libMPI = mock(LibMPI.class);
      String patientId = "12345";
      when(libMPI.findPatientRecord(patientId)).thenReturn(null);

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
      PatientRecord patientRecord = new PatientRecord(
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

      when(libMPI.findPatientRecord(patientId)).thenReturn(Either.right(patientRecord));

      ActorTestKit testKit = ActorTestKit.create();
      ActorRef<BackEnd.Event> myActorRef = testKit.spawn(BackEnd.create(libMPI));
      TestProbe<BackEnd.FindPatientRecordResponse> replyTo = testKit.createTestProbe();

      myActorRef.tell(new BackEnd.FindPatientRecordRequest(replyTo.getRef(), patientId));

      replyTo.expectMessage(new BackEnd.FindPatientRecordResponse(Either.right(patientRecord)));
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

      replyTo.expectMessage(new BackEnd.FindExpandedGoldenRecordResponse(null));
   }

   @Test
   public void findExpandedGoldenRecordHandler_whenFindExpandedGoldenRecordSuccess_ReturnSuccess() {
      final var libMPI = mock(LibMPI.class);
      String goldenId = "9010";
      GoldenRecord goldenRecord = new GoldenRecord(
            new PatientRecord(
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
      List<PatientRecordWithScore> patientRecordWithScoreList = List.of(
            new PatientRecordWithScore(null, null),
            new PatientRecordWithScore(null, null));
      ExpandedGoldenRecord expandedGoldenRecord = new ExpandedGoldenRecord(goldenRecord, patientRecordWithScoreList);

      when(libMPI.findExpandedGoldenRecord(goldenId)).thenReturn(expandedGoldenRecord);

      ActorTestKit testKit = ActorTestKit.create();
      ActorRef<BackEnd.Event> myActorRef = testKit.spawn(BackEnd.create(libMPI));
      TestProbe<BackEnd.FindExpandedGoldenRecordResponse> replyTo = testKit.createTestProbe();

      myActorRef.tell(new BackEnd.FindExpandedGoldenRecordRequest(replyTo.getRef(), goldenId));

      replyTo.expectMessage(new BackEnd.FindExpandedGoldenRecordResponse(expandedGoldenRecord));
   }

   @Test
   public void findCandidatesHandler_whenFindPatientRecordSuccessAndFindCandidatesSuccess_ReturnSuccess() {
      final var libMPI = mock(LibMPI.class);
      CustomMU customMU = new CustomMU(new double[]{0.8, 0.8, 0.8, 0.8, 0.8, 0.8, 0.8},
                                       new double[]{0.002, 0.002, 0.002, 0.002, 0.002, 0.002, 0.002});
      float score = 0.71428573F;
      String patientId = "9014";
      PatientRecord patientRecord = new PatientRecord(
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
            new PatientRecord(
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

      when(libMPI.findPatientRecord(patientId)).thenReturn(Either.right(patientRecord));
      when(libMPI.getCandidates(any(CustomDemographicData.class), anyBoolean())).thenReturn(goldenRecords);

      ActorTestKit testKit = ActorTestKit.create();
      ActorRef<BackEnd.Event> myActorRef = testKit.spawn(BackEnd.create(libMPI));
      TestProbe<BackEnd.FindCandidatesResponse> replyTo = testKit.createTestProbe();

      myActorRef.tell(new BackEnd.FindCandidatesRequest(replyTo.getRef(), patientId, customMU));

      replyTo.expectMessage(new BackEnd.FindCandidatesResponse(Either.right(candidates)));
   }

   @Test
   public void findCandidatesHandler_whenFindPatientRecordNotFound_ReturnNotFound() {
      String patientId = "9015";

      MpiGeneralError notFoundError = new MpiServiceError.PatientIdDoesNotExistError(
            "Patient not found",
            patientId);

      CustomMU customMU = new CustomMU(new double[]{0.8, 0.8, 0.8, 0.8, 0.8, 0.8, 0.8},
                                       new double[]{0.002, 0.002, 0.002, 0.002, 0.002, 0.002, 0.002});

      GoldenRecord goldenRecord = new GoldenRecord(
            new PatientRecord(
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
      when(libMPI.findPatientRecord(patientId)).thenReturn(Either.left(notFoundError));
      when(libMPI.getCandidates(any(CustomDemographicData.class), anyBoolean())).thenReturn(goldenRecords);

      ActorTestKit testKit = ActorTestKit.create();
      ActorRef<BackEnd.Event> myActorRef = testKit.spawn(BackEnd.create(libMPI));
      TestProbe<BackEnd.FindCandidatesResponse> replyTo = testKit.createTestProbe();

      myActorRef.tell(new BackEnd.FindCandidatesRequest(replyTo.getRef(), patientId, customMU));

      replyTo.expectMessage(new BackEnd.FindCandidatesResponse(Either.left(notFoundError)));
   }

   @Test
   public void findCandidatesHandler_whenFindPatientRecordSuccessAndFindCandidatesReturnNull_ReturnNotFound() {
      String patientId = "9016";

      MpiGeneralError notFoundError = new MpiServiceError.CandidatesNotFoundError(
            "Candidates(golden records) not found with demographic data for patientId",
            patientId);

      CustomMU customMU = new CustomMU(new double[]{0.8, 0.8, 0.8, 0.8, 0.8, 0.8, 0.8},
                                       new double[]{0.002, 0.002, 0.002, 0.002, 0.002, 0.002, 0.002});

      PatientRecord patientRecord = new PatientRecord(
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
      when(libMPI.findPatientRecord(patientId)).thenReturn(Either.right(patientRecord));
      when(libMPI.getCandidates(any(CustomDemographicData.class), anyBoolean())).thenReturn(null);

      ActorTestKit testKit = ActorTestKit.create();
      ActorRef<BackEnd.Event> myActorRef = testKit.spawn(BackEnd.create(libMPI));
      TestProbe<BackEnd.FindCandidatesResponse> replyTo = testKit.createTestProbe();

      myActorRef.tell(new BackEnd.FindCandidatesRequest(replyTo.getRef(), patientId, customMU));

      replyTo.expectMessage(new BackEnd.FindCandidatesResponse(Either.left(notFoundError)));
   }

   @Test
   public void findCandidatesHandler_whenFindPatientRecordSuccessAndFindCandidatesReturnEmpty_ReturnNotFound() {
      String patientId = "9017";

      MpiGeneralError notFoundError = new MpiServiceError.CandidatesNotFoundError(
            "Candidates(golden records) not found with demographic data for patientId",
            patientId);

      CustomMU customMU = new CustomMU(new double[]{0.8, 0.8, 0.8, 0.8, 0.8, 0.8, 0.8},
                                       new double[]{0.002, 0.002, 0.002, 0.002, 0.002, 0.002, 0.002});

      PatientRecord patientRecord = new PatientRecord(
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
      when(libMPI.findPatientRecord(patientId)).thenReturn(Either.right(patientRecord));
      when(libMPI.getCandidates(any(CustomDemographicData.class), anyBoolean())).thenReturn(goldenRecords);

      ActorTestKit testKit = ActorTestKit.create();
      ActorRef<BackEnd.Event> myActorRef = testKit.spawn(BackEnd.create(libMPI));
      TestProbe<BackEnd.FindCandidatesResponse> replyTo = testKit.createTestProbe();

      myActorRef.tell(new BackEnd.FindCandidatesRequest(replyTo.getRef(), patientId, customMU));

      replyTo.expectMessage(new BackEnd.FindCandidatesResponse(Either.left(notFoundError)));
   }
}