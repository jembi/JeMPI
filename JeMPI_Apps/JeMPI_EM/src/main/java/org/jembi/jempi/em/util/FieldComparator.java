package org.jembi.jempi.em.util;

import org.apache.commons.text.similarity.JaroWinklerDistance;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.jembi.jempi.shared.models.GoldenRecord;
import org.jembi.jempi.shared.models.Interaction;

public class FieldComparator {

    private boolean compareField(final String field1, final String field2, final FieldType fieldType) {
        double similarity = 0.0;
        switch (fieldType) {
            case GIVEN_NAME:
            case FAMILY_NAME:
                similarity = new JaroWinklerDistance().apply(field1, field2);
                break;
            case NATIONAL_ID:
                similarity = field1.equals(field2) ? 1.0 : 0.0;
                break;
            case DATE_OF_BIRTH:
                // Use a suitable date comparison library to compare the datesbreak;
            case CITY:
                similarity = LevenshteinDistance.getDefaultInstance().apply(field1, field2);
                break;
            case PHONE_NUMBER:
                similarity = LevenshteinDistance.getDefaultInstance().apply(field1, field2);
                break;
            default:
                //to be handled
                break;
        }

        return similarity >= 0.5;
    }

    public final boolean compareFields(final Interaction patient, final GoldenRecord candidate) {
        boolean givenNameAgreement = compareField(patient.demographicData().getGivenName(), candidate.demographicData().getGivenName(), FieldType.GIVEN_NAME);
        boolean familyNameAgreement = compareField(patient.demographicData().getFamilyName(), candidate.demographicData().getFamilyName(), FieldType.FAMILY_NAME);
        boolean dobAgreement = compareField(patient.demographicData().getDob(), candidate.demographicData().getDob(), FieldType.DATE_OF_BIRTH);
        boolean cityAgreement = compareField(patient.demographicData().getCity(), candidate.demographicData().getCity(), FieldType.CITY);
//        boolean nationalIdAgreement = compareField(patient.demographicData().nationalId, candidate.demographicData().getNationalId(), FieldType.NATIONAL_ID);

        return givenNameAgreement && familyNameAgreement && dobAgreement && cityAgreement; //  && nationalIdAgreement;
    }
    public enum FieldType {
        GIVEN_NAME("given_name"),
        FAMILY_NAME("family_name"),
        NATIONAL_ID("national_id"),
        DATE_OF_BIRTH("date_of_birth"),
        CITY("city"),
        PHONE_NUMBER("phone_number");

        private final String fieldName;

        FieldType(final String someFieldName) {
            this.fieldName = someFieldName;
        }

        public String getFieldName() {
            return fieldName;
        }
    }

}
