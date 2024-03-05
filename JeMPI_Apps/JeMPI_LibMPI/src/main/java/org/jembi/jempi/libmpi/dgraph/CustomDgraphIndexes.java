package org.jembi.jempi.libmpi.dgraph;

public final class CustomDgraphIndexes {

    public static Boolean shouldUpdateLinkingIndexes() {
        return true;
    }
    static final String LOAD_DEFAULT_INDEXES =
            """
            """;

    static final String LOAD_LINKING_INDEXES =
            """
            """;
    static final String REMOVE_ALL_INDEXES =
            """

            GoldenRecord.given_name:               string                                       .
            GoldenRecord.family_name:              string                                       .
            GoldenRecord.gender:                   string                                       .
            GoldenRecord.city:                     string                                       .
            GoldenRecord.phone_number:             string                                       .
            GoldenRecord.national_id:              string                                       .

            """;


}
