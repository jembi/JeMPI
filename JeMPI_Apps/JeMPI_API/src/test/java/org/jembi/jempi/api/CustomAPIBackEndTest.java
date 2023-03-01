package org.jembi.jempi.api;

import akka.actor.testkit.typed.javadsl.ActorTestKit;
import akka.actor.testkit.typed.javadsl.TestProbe;
import akka.actor.typed.ActorRef;
import org.jembi.jempi.libmpi.LibMPI;
import org.jembi.jempi.shared.models.*;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

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
   void testEventGetGoldenRecordDocumentsHandler() throws Exception {
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

      when(libMPI.findPatientRecord("1234")).thenReturn(patientRecord);

      // Send the message to the actor
      backend.tell(request);

      // Expect to receive the expected response
      final var response = probe.receiveMessage();
      assertEquals(patientRecord, response.patient());

      // Verify that libMPI was called with the correct arguments
      verify(libMPI).startTransaction();
      verify(libMPI).findPatientRecord("1234");
      verify(libMPI).closeTransaction();
   }
}