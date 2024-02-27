package org.jembi.jempi.libmpi.queries.extended_pid;

import org.jembi.jempi.libmpi.LibMPI;
import org.jembi.jempi.libmpi.queries.utils.QueryUtilities;
import org.jembi.jempi.libmpi.utils.Utilities;
import org.jembi.jempi.shared.models.CustomDemographicData;
import org.jembi.jempi.shared.models.GoldenRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.List;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class QueriesTests {

    @BeforeEach
    void resetAllData(){
        Utilities.ResetData();
    }
    @Test
    void testCanFindCandidatesDeterministicallyOnPid() {
        Utilities.AddData(List.of(
                new CustomDemographicData(
                            "name1",
                            "familyName1",
                            "male",
                            "dob1",
                            "city1",
                            "phoneNumber1",
                            "123456"),
                new CustomDemographicData(
                        "name2",
                        "familyName2",
                        "male",
                        "dob2",
                        "city2",
                        "phoneNumber2",
                        "785445")));

        LibMPI libMPI = Utilities.getLibMPI();
        List<GoldenRecord>  gRS = libMPI.findLinkCandidates(new CustomDemographicData(
                                    "unknown",
                                    "unknown",
                                   "unknown",
                                    "unknown",
                                    "unknown",
                                    "unknown",
                                    "123456"));

        QueryUtilities.expectGRSearchResultToMatch(gRS, List.of(new CustomDemographicData(
                "name1",
                "familyName1",
                "male",
                "dob1",
                "city1",
                "phoneNumber1",
                "123456")));
    }

    @Test
    void testCanFindCandidatesDeterministicallyAlternatively() {
        Utilities.AddData(List.of(
                new CustomDemographicData(
                        "name1",
                        "familyName1",
                        "male",
                        "dob1",
                        "city1",
                        "phoneNumber1",
                        "123456"),
                new CustomDemographicData(
                        "name2",
                        "familyName2",
                        "male",
                        "dob2",
                        "city2",
                        "phoneNumber2",
                        "785445"),
                new CustomDemographicData(
                        "name3",
                        "familyName3",
                        "male",
                        "dob3",
                        "city3",
                        "phoneNumber3",
                        "")));


        LibMPI libMPI = Utilities.getLibMPI();

        // Deterministically givenName, familyName, phoneNumber
        List<GoldenRecord>  gRS = libMPI.findLinkCandidates(new CustomDemographicData(
                "unknown",
                "unknown",
                "unknown",
                "unknown",
                "unknown",
                "phoneNumber3",
                "unknown"));

        QueryUtilities.expectGRSearchResultToMatch(gRS, List.of(new CustomDemographicData(
                "name3",
                "familyName3",
                "male",
                "dob3",
                "city3",
                "phoneNumber3",
                "")));


    }
    @Test
    void testCanFindCandidatesProbabilistically() {
        Utilities.AddData(List.of(
                new CustomDemographicData(
                        "name1",
                        "familyName1",
                        "male",
                        "dob1",
                        "city1",
                        "phoneNumber1",
                        "123456"),
                new CustomDemographicData(
                        "name2",
                        "familyName2",
                        "male",
                        "dob2",
                        "city2",
                        "phoneNumber2",
                        "785445")));

        LibMPI libMPI = Utilities.getLibMPI();
        // Deterministically givenName, familyName, phoneNumber - 0
        List<GoldenRecord> gRS2 = libMPI.findLinkCandidates(new CustomDemographicData(
                "name",
                "familyName",
                "male",
                "unknown",
                "city",
                "unknown",
                "unknown"));

        QueryUtilities.expectGRSearchResultToMatch(gRS2, List.of(
                new CustomDemographicData(
                        "name1",
                        "familyName1",
                        "male",
                        "dob1",
                        "city1",
                        "phoneNumber1",
                        "123456"),
                new CustomDemographicData(
                        "name2",
                        "familyName2",
                        "male",
                        "dob2",
                        "city2",
                        "phoneNumber2",
                        "785445")
        ));
    }
}
