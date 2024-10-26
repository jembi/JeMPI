#!/bin/bash
set -e
set -m


curl --location 'http://localhost:5001/fhir/Patient' \
--header 'Authorization: custom test' \
--header 'Content-Type: application/fhir+json' \
--data '{
    "resourceType": "Patient",
    "id": "{{patientId1}}",
    "identifier": [
        {
            "system": "http://example.org/patient-ids",
            "value": "56b79056-98a8-4327-bf65-834dd647c0c9"
        },
        {
            "system": "NationalID",
            "value": "cd353dc3-ab4a-46c0-88b9-655a178d3865"
        }
    ],
    "name": [
        {
            "family": "Doe",
            "given": [
                "John"
            ]
        }
    ],
    "gender": "male",
    "birthDate": "1970-01-01",
    "telecom": [
        {
            "value": "000000110",
            "system": "phone"
        }
    ],
    "address": [
        {
            "city": "Test city"
        }
    ]
}'
