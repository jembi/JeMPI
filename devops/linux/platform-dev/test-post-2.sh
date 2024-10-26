!/bin/bash
set -e
set -m


curl --location 'http://localhost:50000/JeMPI/crLink' \
--header 'Content-Type: application/json' \
--data '{
    "matchThreshold": 0.9,
    "sourceId": {
        "facility": "fac1",
        "patient": "pat1"
    },
    "uniqueInteractionData": {
        "auxDateCreated": "2016-10-30T14:22:25.285Z",
        "auxId": "rec-0000000001-01",
        "auxClinicalData": "RANDOM DATA"
    },
    "demographicData": {
        "givenName": "aaa",
        "familyName": "bbb",
        "gender": "male",
        "dob": "20000202",
        "city": "Cape Town",
        "phoneNumber": "12345556",
        "nationalId": "123456789012345"
    }
}
'
