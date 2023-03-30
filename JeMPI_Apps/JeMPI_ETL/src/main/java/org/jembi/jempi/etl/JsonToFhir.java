package org.jembi.jempi.etl;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.fhirpath.IFhirPath;
import ca.uhn.fhir.parser.IParser;
import org.hl7.fhir.instance.model.api.IBase;
import org.hl7.fhir.r4.model.*;
import org.jembi.jempi.shared.models.PatientRecord;
import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.text.WordUtils.capitalizeFully;

public class JsonToFhir {
    private static String getFhirPath(String fieldName, JSONObject config) {
        JSONArray fields = config.getJSONArray("fields");
        for (int i = 0; i < fields.length(); i++) {
            JSONObject field = fields.getJSONObject(i);
            if (fieldName.equalsIgnoreCase(capitalizeFully(field.getString("fieldName")).replace("_", ""))) {
                return field.getString("fhirPath");
            }
        }
        return null;
    }

    public static Patient mapToPatientFhir(PatientRecord patientRecord, String configReference) {
        JSONObject config = new JSONObject(configReference);
        Patient patient = new Patient();
        FhirContext fhirContext = FhirContext.forR4();

        for (Field field : patientRecord.getClass().getDeclaredFields()) {
            String fieldName = field.getName();
            String fieldValue = null;
            try {
                fieldValue = (String) field.get(patientRecord);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }

            if (fieldValue != null) {
                String fhirPath = getFhirPath(fieldName, config);
                if (fhirPath != null) {

                    FhirContext ctx = FhirContext.forR4();
                    IFhirPath fhirPathEngine = ctx.newFhirPath();
                    List<Base> matchingElements = fhirPathEngine.evaluate(patient, fhirPath, Base.class);

                    switch (fhirPath) {
                        case "Patient.identifier":
                            Identifier identifier = new Identifier();
                            identifier.setValue(fieldValue);
                            patient.addIdentifier(identifier);
                            System.out.println("I am in the identifier");
                            break;
                        case "name.given":
                            HumanName name = new HumanName();
                            name.addGiven(fieldValue);
                            patient.addName(name);
                            System.out.println("in the Human name");
                            break;
                        case "name.family":
                            HumanName namee = new HumanName();
                            namee.setFamily(fieldValue);
                            patient.addName(namee);
                            System.out.println("in the Human name");
                            break;
                        case "address.city":
                            Address address = new Address();
                            address.setCity(fieldValue);
                            patient.addAddress(address);
                            break;
                        case "birthDate":
                            DateType birthDate = new DateType(fieldValue);
                            patient.setBirthDateElement(birthDate);
                            break;
                        default:
                            List<Base> values = new ArrayList<>();
                            values.add(new StringType(fieldValue));
                            IFhirPath fhirPathElement = fhirContext.newFhirPath();
                            IParser parser = fhirContext.newJsonParser();
                            List<IBase> elements = fhirPathElement.evaluate(patient, fhirPath, IBase.class);
                            break;
                    }
                }
            }

        }

        return patient;
    }
}
