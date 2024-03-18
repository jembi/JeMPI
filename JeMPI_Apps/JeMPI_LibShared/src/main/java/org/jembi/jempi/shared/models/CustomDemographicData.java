package org.jembi.jempi.shared.models;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class CustomDemographicData {

    public static final int GIVEN_NAME = 0;
    public static final int FAMILY_NAME = 1;
    public static final int GENDER = 2;
    public static final int DOB = 3;
    public static final int CITY = 4;
    public static final int PHONE_NUMBER = 5;
    public static final int NATIONAL_ID = 6;

    public final List<Field> fields;
   
    public CustomDemographicData() {
        fields = new ArrayList<>();
    }

    public CustomDemographicData(final CustomDemographicData demographicData) {
        fields = demographicData.fields.stream().toList();
    }

    private CustomDemographicData(final List<Field> fields) {
        this.fields = fields;
    }

    public CustomDemographicData clean() {
        return new CustomDemographicData(fields.stream()
                                               .map(x -> new Field(x.tag, x.value.trim()
                                                                                 .toLowerCase()
                                                                                 .replaceAll("\\W", "")))
                                               .toList());
    }

    public CustomDemographicData(
        final String givenName,
        final String familyName,
        final String gender,
        final String dob,
        final String city,
        final String phoneNumber,
        final String nationalId) {
      fields = new ArrayList<>(Arrays.asList(
            new Field("givenName", givenName),
            new Field("familyName", familyName),
            new Field("gender", gender),
            new Field("dob", dob),
            new Field("city", city),
            new Field("phoneNumber", phoneNumber),
            new Field("nationalId", nationalId)));
    }

   public record Field(
         String tag,
         String value) {
   }
}
