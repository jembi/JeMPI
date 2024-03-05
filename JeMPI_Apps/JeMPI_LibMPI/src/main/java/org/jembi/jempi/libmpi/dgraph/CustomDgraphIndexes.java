package org.jembi.jempi.libmpi.dgraph;

public final class CustomDgraphIndexes {

    public static Boolean shouldUpdateLinkingIndexes() {
        return true;
    }
    static final String LOAD_DEFAULT_INDEXES =
            """
            
            GoldenRecord.given_name:               string    @index(exact,trigram)              .
            GoldenRecord.family_name:              string    @index(exact,trigram)              .
            GoldenRecord.gender:                   string    @index(exact,trigram)              .
            GoldenRecord.city:                     string    @index(trigram)                    .
            GoldenRecord.phone_number:             string    @index(exact,trigram)              .
            GoldenRecord.national_id:              string    @index(exact,trigram)              .
            Interaction.given_name:                string    @index(exact,trigram)              .
            Interaction.family_name:               string    @index(exact,trigram)              .
            Interaction.national_id:               string    @index(exact,trigram)              .
            
            """;

    static final String LOAD_LINKING_INDEXES =
            """
            
            GoldenRecord.national_id:              string    @index(exact,trigram)              .
            
            """;
    static final String REMOVE_ALL_INDEXES =
            """

            GoldenRecord.given_name:               string                                       .
            GoldenRecord.family_name:              string                                       .
            GoldenRecord.gender:                   string                                       .
            GoldenRecord.city:                     string                                       .
            GoldenRecord.phone_number:             string                                       .
            GoldenRecord.national_id:              string                                       .
            Interaction.given_name:                string                                       .
            Interaction.family_name:               string                                       .
            Interaction.national_id:               string                                       .
            
            """;


}
