package org.jembi.jempi.shared.mapper;

import ca.uhn.fhir.context.FhirContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hl7.fhir.r4.model.*;
import org.jembi.jempi.shared.models.CustomDemographicData;
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

    public static String mapToPatientFhir(final String resourceId, final CustomDemographicData demographicData, final String sourceId) {
        Patient patient = new Patient();
            try {
                Identifier identifier = new Identifier();
                identifier.setValue(resourceId);
                patient.addIdentifier(identifier);
                for (Field demoField : CustomDemographicData.class.getDeclaredFields()) {
                    demoField.setAccessible(true);
                    String demoFieldName = demoField.getName();
                    String fieldValue = (String) demoField.get(demographicData);
                    if (fieldValue != null) {
                        String fhirPath = getFhirPath(demoFieldName);
                        if (fhirPath != null) {
                            processField(patient, fieldValue, fhirPath);
                        }
                    }
                }
                if (sourceId.length() != 0) {
                        Organization organization = new Organization();
                        organization.setId(sourceId);
                        patient.getManagingOrganization().setResource(organization);
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                LOGGER.debug(e);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        FhirContext ctx = FhirContext.forR4();
        // Serialize the patient object to FHIR JSON
        String patientJson = ctx.newJsonParser().setPrettyPrint(true).encodeResourceToString(patient);
        return patientJson;
    }
}
