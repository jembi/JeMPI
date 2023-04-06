package org.jembi.jempi.shared.mapper;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hl7.fhir.r4.model.*;
import org.jembi.jempi.shared.models.CustomDemographicData;
import org.jembi.jempi.shared.models.PatientRecord;
import org.jembi.jempi.shared.utils.JsonFieldsConfig;
import org.json.simple.JSONObject;

import java.lang.reflect.Field;

public final class JsonToFhir {

    private static JsonFieldsConfig jsonFieldsConfig = new JsonFieldsConfig();

    private JsonToFhir() throws Exception {

    }

    private static final Logger LOGGER = LogManager.getLogger(JsonToFhir.class);
    private static String getFhirPath(final String fieldName) {
        try {
            for (int i = 0; i < jsonFieldsConfig.fields.size(); i++) {
                JSONObject field = (JSONObject) jsonFieldsConfig.fields.get(i);
                if (fieldName.equalsIgnoreCase((String) field.get("fieldName"))) {
                    return (String) field.get("fhirPath");
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    private static void processField(final Patient patient, final String fieldValue, final String fhirPath) {
        switch (fhirPath) {
            case "Patient.identifier" -> {
                Identifier identifier = new Identifier();
                identifier.setValue(fieldValue);
                patient.addIdentifier(identifier);
            }
            case "name.given" -> {
                HumanName name = new HumanName();
                name.addGiven(fieldValue);
                patient.addName(name);
            }
            case "name.family" -> {
                HumanName familyName = new HumanName();
                familyName.setFamily(fieldValue);
                patient.addName(familyName);
            }
            case "address.city" -> {
                Address address = new Address();
                address.setCity(fieldValue);
                patient.addAddress(address);
            }
            case "birthDate" -> {
                DateType birthDate = new DateType(fieldValue);
                patient.setBirthDateElement(birthDate);
            }
            default -> {
                // to be implemented
            }
        }
    }

    public static Patient mapToPatientFhir(final PatientRecord patientRecord) {
        Patient patient = new Patient();
        CustomDemographicData demographicData = patientRecord.demographicData();

        for (Field field : PatientRecord.class.getDeclaredFields()) {
            field.setAccessible(true);
            String fieldName = field.getName();
            String fieldValue;
            try {
                if (fieldName.equals("demographicData")) {
                    for (Field demoField : CustomDemographicData.class.getDeclaredFields()) {
                        demoField.setAccessible(true);
                        String demoFieldName = demoField.getName();
                        fieldValue = (String) demoField.get(demographicData);
                        if (fieldValue != null) {
                            String fhirPath = getFhirPath(demoFieldName);
                            if (fhirPath != null) {
                                processField(patient, fieldValue, fhirPath);
                            }
                        }
                    }
                } else if (fieldName.equals("patientId")) {
                    fieldValue = (String) field.get(patientRecord);
                    Identifier identifier = new Identifier();
                    identifier.setValue(fieldValue);
                    patient.addIdentifier(identifier);

                } else if (fieldName.equals("sourceId")) {
                           LOGGER.debug("sourceId");
                } else {
                    fieldValue = (String) field.get(patientRecord);
                    if (fieldValue != null) {
                        String fhirPath = getFhirPath(fieldName);
                        if (fhirPath != null) {
                            processField(patient, fieldValue, fhirPath);
                        }
                    }
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                LOGGER.debug(e);

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        return patient;
    }
}
