package org.jembi.jempi.linker;

import akka.actor.testkit.typed.javadsl.ActorTestKit;
import akka.actor.typed.ActorRef;
import org.jembi.jempi.libmpi.LibMPI;
import org.jembi.jempi.shared.models.CustomDemographicData;
import org.jembi.jempi.shared.models.ExpandedGoldenRecord;
import org.jembi.jempi.shared.models.GoldenRecord;
import org.jembi.jempi.shared.models.Notification;
import org.jembi.jempi.shared.models.PatientRecord;
import org.jembi.jempi.shared.models.PatientRecordWithScore;
import org.jembi.jempi.shared.models.SourceId;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class LinkerTests {
/*

    ActorTestKit testKit;
    ActorRef<BackEnd.Event> myActorRef;
    CustomDemographicData goldenRecordDemographicData;
    List<PatientRecordWithScore> patientRecords;
    PatientRecordWithScore patientRecordWithScore1;

    @Mock
    private LibMPI libMPI;
    private ExpandedGoldenRecord expandedGoldenRecord;

    private PatientRecord patientRecord;

    private List<GoldenRecord> goldenRecords;

    @BeforeEach
    public void init(){
        testKit = ActorTestKit.create();
        myActorRef = testKit.spawn(BackEnd.create(libMPI));
        goldenRecordDemographicData = new CustomDemographicData("1", "Jane", "Doe", "F", "1985-05-05", "New York", "555-1234", "123456789");
        patientRecord = new PatientRecord("2", new SourceId("1", null, null), new CustomDemographicData("2", "Jane", "Doe", "F", "1985-05-05", "Boston", "555-5678", "987654321"));
        patientRecords = new ArrayList<>();
        patientRecordWithScore1 = new PatientRecordWithScore(patientRecord, 0.14604087F);
        patientRecords.add(patientRecordWithScore1);
        expandedGoldenRecord = new ExpandedGoldenRecord(new GoldenRecord("1", null, goldenRecordDemographicData), patientRecords);
        goldenRecords = new ArrayList<>();
        goldenRecords.add(expandedGoldenRecord.goldenRecord());
    }

    @AfterAll
    public void tearDown() {
        testKit = null;
        myActorRef = null;
        goldenRecordDemographicData = null;
        patientRecord = null;
        patientRecords = null;
        patientRecordWithScore1 = null;
        expandedGoldenRecord = null;
        goldenRecords = null;

    }

    @Test
    public void updateGoldenRecordField_Success(){

        // Arrange
        when(libMPI.updateGoldenRecordField(eq("1"), eq("city"), anyString())).thenReturn(true);

        BackEnd.updateGoldenRecordField(expandedGoldenRecord, "city", "New York", CustomDemographicData::city);
        // Assert
        verify(libMPI).updateGoldenRecordField(eq("1"), eq("city"), anyString());
    }

    @Test
    public void updateGoldenRecordField_expandedGoldenRecordNull_returnSuccess() {
        BackEnd.updateGoldenRecordField(null, "city", "New York", CustomDemographicData::city);

        verify(libMPI, never()).updateGoldenRecordField(anyString(), anyString(), anyString());
    }

    @Test
    public void testCandidatesForReview() throws Exception{
        when(libMPI.getCandidates(any(CustomDemographicData.class), anyBoolean())).thenReturn(goldenRecords);
        ArrayList<Notification.MatchData> list = BackEnd.getCandidatesMatchDataForPatientRecord(patientRecord);

        assertEquals(1, list.size());
    }
*/
}
