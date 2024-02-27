package org.jembi.jempi.libmpi.queries.default_settings;

import org.jembi.jempi.libmpi.LibMPI;
import org.jembi.jempi.libmpi.queries.utils.QueryUtilities;
import org.jembi.jempi.libmpi.utils.Utilities;
import org.jembi.jempi.shared.models.CustomDemographicData;
import org.jembi.jempi.shared.models.GoldenRecord;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.BeforeEach;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

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

    void testCanFindCandidatesMultipleeterministically() {

    }

    void testCanFindCandidatesProbalistically() {

    }
}
