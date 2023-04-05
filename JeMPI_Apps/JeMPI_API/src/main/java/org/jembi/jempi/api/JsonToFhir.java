package org.jembi.jempi.api;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hl7.fhir.r4.model.*;
import org.jembi.jempi.shared.models.CustomDemographicData;
import org.jembi.jempi.shared.models.PatientRecord;
import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Field;

import static org.apache.commons.text.WordUtils.capitalizeFully;

public final class JsonToFhir {

    private JsonToFhir() {
    }
    private static final Logger LOGGER = LogManager.getLogger(JsonToFhir.class);
    private static String getFhirPath(final String fieldName, final JSONObject config) {
        JSONArray fields = config.getJSONArray("fields");
        for (int i = 0; i < fields.length(); i++) {
            JSONObject field = fields.getJSONObject(i);
            if (fieldName.equalsIgnoreCase(capitalizeFully(field.getString("fieldName")).replace("_", ""))) {
                return field.getString("fhirPath");
            }
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

    public static Patient mapToPatientFhir(final PatientRecord patientRecord, final JSONObject config) {
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
                            String fhirPath = getFhirPath(demoFieldName, config);
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
                           // to be implemented 
                } else {
                    fieldValue = (String) field.get(patientRecord);
                    if (fieldValue != null) {
                        String fhirPath = getFhirPath(fieldName, config);
                        if (fhirPath != null) {
                            processField(patient, fieldValue, fhirPath);
                        }
                    }
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                LOGGER.debug(e);

            }
        }

        return patient;
    }
}
