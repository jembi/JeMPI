package org.jembi.jempi.linker;

import akka.actor.testkit.typed.javadsl.ActorTestKit;
import akka.actor.typed.ActorRef;
import org.jembi.jempi.libmpi.LibMPI;
import org.jembi.jempi.shared.models.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.ArrayList;
import java.util.List;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LinkerTest {

    @Mock
    private LibMPI libMPI;
    @Test
    public void testUpdateGoldenRecordField() {
        ActorTestKit testKit = ActorTestKit.create();
        ActorRef<BackEnd.Event> myActorRef = testKit.spawn(BackEnd.create(libMPI));
        // Arrange
        CustomDemographicData goldenRecord = new CustomDemographicData("1", "John", "Doe", "M", "1980-01-01", "New York", "555-1234", "123456789");
        List<PatientRecordWithScore> patientRecords = new ArrayList<>();
        PatientRecord patientRecord1 = new PatientRecord("2", new SourceId("1", null, null), new CustomDemographicData("2", "Jane", "Doe", "F", "1985-05-05", "Boston", "555-5678", "987654321"));
        PatientRecordWithScore patientRecordWithScore1 = new PatientRecordWithScore(patientRecord1, 0.14604087F);
        patientRecords.add(patientRecordWithScore1);

        ExpandedGoldenRecord expandedGoldenRecord = new ExpandedGoldenRecord(new GoldenRecord("1", null, goldenRecord), patientRecords);

        when(libMPI.updateGoldenRecordField(eq("1"), eq("city"), anyString())).thenReturn(true);
        when(libMPI.reComputeScores(eq("2"), eq("1"), anyFloat())).thenReturn(true);

        BackEnd.updateGoldenRecordField(expandedGoldenRecord, "city", "New York", CustomDemographicData::city);
        // Assert
        verify(libMPI).updateGoldenRecordField(eq("1"), eq("city"), anyString());
        verify(libMPI).reComputeScores(eq("2"), eq("1"), eq( 0.14604087F));

    }
}
