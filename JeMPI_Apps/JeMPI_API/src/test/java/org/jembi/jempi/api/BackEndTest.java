package org.jembi.jempi.api;
import akka.actor.testkit.typed.javadsl.TestProbe;
import akka.actor.typed.ActorRef;
import akka.actor.testkit.typed.javadsl.ActorTestKit;
import org.jembi.jempi.libmpi.LibMPI;
import org.jembi.jempi.libmpi.MpiEntity;
import org.jembi.jempi.libmpi.MpiExpandedGoldenRecord;
import org.jembi.jempi.shared.models.CustomEntity;
import org.jembi.jempi.shared.models.CustomGoldenRecord;
import org.jembi.jempi.shared.models.SourceId;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class BackEndTest {

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
        CustomGoldenRecord customGoldenRecord1 = new CustomGoldenRecord(
                "1234",
                List.of(new SourceId("f1fa7b5c", "4e71", "11ec-8d3d-0242ac130003")),
                "auxId",
                "John",
                "Doe",
                "Male",
                "1990-01-01",
                "New York",
                "555-1234",
                "123-45-6789"
        );
        List<MpiEntity> mpiEntityList1 = List.of(
                new MpiEntity(null, null),
                new MpiEntity(null, null)
        );
        MpiExpandedGoldenRecord expandedGoldenRecord1 = new MpiExpandedGoldenRecord(customGoldenRecord1, mpiEntityList1);

        CustomGoldenRecord customGoldenRecord2 = new CustomGoldenRecord(
                "5678",
                List.of(new SourceId("d0d38f0e", "4e71", "11ec-8d3d-0242ac130003")),
                "auxId2",
                "Jane",
                "Smith",
                "Female",
                "1985-05-10",
                "Los Angeles",
                "555-5678",
                "987-65-4321"
        );
        List<MpiEntity> mpiEntityList2 = List.of(
                new MpiEntity(null, null),
                new MpiEntity(null, null)
        );
        MpiExpandedGoldenRecord expandedGoldenRecord2 = new MpiExpandedGoldenRecord(customGoldenRecord2, mpiEntityList2);

        CustomGoldenRecord customGoldenRecord3 = new CustomGoldenRecord(
                "9012",
                List.of(new SourceId("c44a67f6", "4e71", "11ec-8d3d-0242ac130003")),
                "auxId3",
                "Robert",
                "Johnson",
                "Male",
                "1975-12-18",
                "Chicago",
                "555-9012",
                "456-78-9012"
        );
        List<MpiEntity> mpiEntityList3 = List.of(
                new MpiEntity( null, null),
                new MpiEntity( null, null)
        );
        MpiExpandedGoldenRecord expandedGoldenRecord3 = new MpiExpandedGoldenRecord(customGoldenRecord3, mpiEntityList3);
        List<MpiExpandedGoldenRecord> expandedGoldenRecords = List.of(expandedGoldenRecord1, expandedGoldenRecord2, expandedGoldenRecord3);
        final var uids = List.of("1234", "5678", "9012");

        // mock LibMPI interface
        final var libMPI = mock(LibMPI.class);
        when(libMPI.getMpiExpandedGoldenRecordList(uids)).thenReturn(expandedGoldenRecords);

        // create ActorSystem and ActorRef using ActorTestKit
        ActorTestKit testKit = ActorTestKit.create();
        ActorRef<BackEnd.Event> myActorRef = testKit.spawn(BackEnd.create(libMPI));

        // create TestProbe to receive response from actor
        TestProbe<BackEnd.EventGetGoldenRecordDocumentsRsp> replyTo = testKit.createTestProbe();

        // send request to actor
        myActorRef.tell(new BackEnd.EventGetGoldenRecordDocumentsReq(replyTo.getRef(), uids));

        // assert that response matches expected value
        replyTo.expectMessage(new BackEnd.EventGetGoldenRecordDocumentsRsp(expandedGoldenRecords));


    }

    @Test
    public void testFindPatientByUidEventHandler() {
        // Create a mock libMPI instance

        final var libMPI = mock(LibMPI.class);

        // Create an instance of the actor to be tested
        final var backend = testKit.spawn(BackEnd.create(libMPI));

        // Create a TestProbe to receive responses
        final var probe = testKit.createTestProbe(BackEnd.EventFindPatientRecordByUidResponse.class);

        // Create an EventFindPatientByUidRequest message with a specific UID value
        final var request = new BackEnd.EventFindPatientByUidRequest(probe.getRef(), "1234");

        // Stub the mock libMPI instance to return a CustomEntity object when `getDocument()` is called
        final var customEntity = new CustomEntity(
                "1234", // uid
                new SourceId("f1fa7b5c", "4e71", "11ec-8d3d-0242ac130003"), // sourceId
                "auxId", // auxId
                "John", // givenName
                "Doe", // familyName
                "Male", // gender
                "1990-01-01", // dob
                "New York", // city
                "555-1234", // phoneNumber
                "123-45-6789" // nationalId
        );
        when(libMPI.getDocument("1234")).thenReturn(customEntity);

        // Send the message to the actor
        backend.tell(request);

        // Expect to receive the expected response
        final var response = probe.receiveMessage();
        assertEquals(customEntity, response.document());

        // Verify that libMPI was called with the correct arguments
        verify(libMPI).startTransaction();
        verify(libMPI).getDocument("1234");
        verify(libMPI).closeTransaction();
    }


}