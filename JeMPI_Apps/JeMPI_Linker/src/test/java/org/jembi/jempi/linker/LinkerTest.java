//package org.jembi.jempi.linker;
//
//import akka.actor.testkit.typed.javadsl.ActorTestKit;
//import akka.actor.typed.ActorRef;
//import org.jembi.jempi.libmpi.LibMPI;
//import org.jembi.jempi.shared.models.*;
//import org.junit.jupiter.api.*;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import java.util.ArrayList;
//import java.util.List;
//import static org.mockito.Mockito.*;
//import static org.junit.jupiter.api.Assertions.assertThrows;
//
//@ExtendWith(MockitoExtension.class)
//class LinkerTests {
//
//    @Mock
//    private LibMPI libMPI;
//    private ExpandedGoldenRecord expandedGoldenRecord;
//
//    @BeforeEach
//    public void init(){
//        CustomDemographicData goldenRecordDemographicData = new CustomDemographicData("1", "John", "Doe", "M", "1980-01-01", "New York", "555-1234", "123456789");
//        PatientRecord patientRecord = new PatientRecord("2", new SourceId("1", null, null), new CustomDemographicData("2", "Jane", "Doe", "F", "1985-05-05", "Boston", "555-5678", "987654321"));
//        List<PatientRecordWithScore> patientRecords = new ArrayList<>();
//        PatientRecordWithScore patientRecordWithScore1 = new PatientRecordWithScore(patientRecord, 0.14604087F);
//        patientRecords.add(patientRecordWithScore1);
//        expandedGoldenRecord = new ExpandedGoldenRecord(new GoldenRecord("1", null, goldenRecordDemographicData), patientRecords);
//    }
//
//    @Test
//    public void updateGoldenRecordField_Success(){
//        ActorTestKit testKit = ActorTestKit.create();
//        ActorRef<BackEnd.Event> myActorRef = testKit.spawn(BackEnd.create(libMPI));
//
//        // Arrange
//        when(libMPI.updateGoldenRecordField(eq("1"), eq("city"), anyString())).thenReturn(true);
//        when(libMPI.setScore(eq("2"), eq("1"), anyFloat())).thenReturn(true);
//
//        BackEnd.updateGoldenRecordField(expandedGoldenRecord, "city", "New York", CustomDemographicData::city);
//        // Assert
//        verify(libMPI).updateGoldenRecordField(eq("1"), eq("city"), anyString());
//        verify(libMPI).setScore(eq("2"), eq("1"), eq( 0.14604087F));
//    }
//
//    @Test
//    public void updateGoldenRecordField_fiedNameNull_returnsIllegalArgumentException() {
//        String fieldName = null;
//        assertThrows(IllegalArgumentException.class, () -> {
//            BackEnd.updateGoldenRecordField(expandedGoldenRecord, fieldName, "New York", CustomDemographicData::city);
//        });
//    }
//
//    @Test
//    public void updateGoldenRecordField_fieldNameEmpty_returnsIllegalArgumentException() {
//        String fieldName = "";
//        assertThrows(IllegalArgumentException.class, () -> {
//            BackEnd.updateGoldenRecordField(expandedGoldenRecord, fieldName, "New York", CustomDemographicData::city);
//        });
//    }
//
//    @Test
//    public void updateGoldenRecordField_expandedGoldenRecordNull_returnsIllegalArgumentException() {
//        expandedGoldenRecord = null;
//        assertThrows(IllegalArgumentException.class, () -> {
//            BackEnd.updateGoldenRecordField(expandedGoldenRecord, "city", "New York", CustomDemographicData::city);
//        });
//    }
//}
