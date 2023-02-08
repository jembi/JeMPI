---
description: API endpoints documentation
---

# API


## GET /fields
The following endpoint returns the fields configuration needed by the frontend (JeMPI-UI) in order to properly display patient record data according to a specific 
implementation. This endpoint returns a JSON array. Below a sample of the response :
```json
[
  {
    "fieldName": "type",
    "accessLevel": [],
    "fieldLabel": "Record Type",
    "scope": [
      "/patient/:uid",
      "/match-details",
      "/patient/:uid/linked-records"
    ],
    "groups": [
      "demographics",
      "linked_records"
    ],
    "fieldType": "String"
  },
  ...
]
```

For each field we have a set of attributes, as defined below :

| Attribute   | Description                                                                                                     | Used by            |
|-------------|-----------------------------------------------------------------------------------------------------------------|--------------------|
| fieldName   | A "camel-case" field name which will be used when accessing a patient record data structure                       | Backend + Frontend |
| fieldLabel  | A string that is a human readable name for the field                                                            | Frontend           |
| scope       | Array of URL paths that tells the frontend UI in which pages should the field appear                            | Frontend           |
| groups      | Array of strings which identifies in which section within a frontend UI page should the field be displayed      | Frontend           |
| FieldType   | A string that identifies the type of field, could be String, Date, ...(useful for formatting for example)       | Frontend + Backend |
| accessLevel | An array of string that identifies which user roles are permitted to access a given field (NOT YET IMPLEMENTED) | Frontend           |
| readOnly    | Tells if the field can be editable.                                                                             | Frontend           |

The fields should be configured in the json file for each implementation and should not be updated in production : `JeMPI_Apps/JeMPI_Configuration/config-reference.json`

There's two type of fields : 
- Custom fields : Indexed by the key "fields", contains all the fields that are specific to the implementation. Examples : givenName, nationalId, ...
- System fields : Indexed by the key "systemFields", it contains all the fields that are readonly fields and do not change across the implementation. Example : uid, record type, score, ...

> ! IMPORTANT : The `fieldName` in `config-reference.json` should be set in snake-case, but it's returned in camel-case by the API. 

## GET /[patient|golden]-record/:uid
The following endpoint returns a golden or patient record given a uid is supplied. This endpoint returns a object. Below a sample of the response :
```json
// GET /patient-record/0x4
{
  "document": {
    "auxId": "rec-00000000-aaa-0",
    "city": "Nairobi",
    "dob": "20171114",
    "familyName": "Onyango",
    "gender": "male",
    "givenName": "Endalekachew",
    "nationalId": "198804042874913",
    "phoneNumber": "091-749-4674",
    "sourceId": {
      "facility": "LABORATORY",
      "patient": "198804042874913",
      "uid": "0x3"
    },
    "uid": "0x4"
  }
}
```

## POST /search/(golden|patient)
The following endpoint is used for the simple search either for golden or patient records. 

Below a sample of the request body :
```json
// POST /search/golden
{
   "parameters":[
      {
         "fieldName":"givenName",
         "value":"",
         "distance":0
      },
      {
         "fieldName":"familyName",
         "value":"",
         "distance":0
      },
      {
         "fieldName":"dob",
         "value":"",
         "distance":0
      },
      {
         "fieldName":"nationalId",
         "value":"198804042874913",
         "distance":0
      }
   ],
   "sortBy":"givenName",
   "sortAsc":true,
   "offset":0,
   "limit":10
}

```
### POST /search/golden
When the request is sent to the url `/search/golden` the response payload will contain the list of golden records along with the linked records and the result set total (useful for pagination) :
```json
{
   "records":{
      "data":[
         {
            "customGoldenRecord":{
               "auxId":"rec-00000000-aaa-0",
               "city":"Nairobi",
               "dob":"20171114",
               "familyName":"Onyango",
               "gender":"male",
               "givenName":"Endalekachew",
               "nationalId":"198804042874913",
               "phoneNumber":"091-749-4674",
               "sourceId":[
                  
               ],
               "uid":"0x5"
            },
            "mpiEntityList":[
               {
                  "entity":{
                     "auxId":"rec-00000000-aaa-0",
                     "city":"Nairobi",
                     "dob":"20171114",
                     "familyName":"Onyango",
                     "gender":"male",
                     "givenName":"Endalekachew",
                     "nationalId":"198804042874913",
                     "phoneNumber":"091-749-4674",
                     "uid":"0x4"
                  }
               },
               {
                  "entity":{
                     "auxId":"rec-00000000-bbb-0",
                     "city":"Nairobi",
                     "dob":"20171114",
                     "familyName":"Onyango",
                     "gender":"male",
                     "givenName":"Endalekachew",
                     "nationalId":"198804042874913",
                     "phoneNumber":"091-749-4674",
                     "uid":"0x6"
                  }
               }
            ]
         }
      ],
      "pagination":{
         "total": 100
      }
   }
}

```

### POST /search/patient
When the request is performed against the url `/search/patient` the response payload will contain the list of patient records along with the golden record uid and the search result total count :

```json
{
   "records":{
      "data":[
         {
            "auxId":"rec-00000000-aaa-0",
            "city":"Nairobi",
            "dob":"20171114",
            "familyName":"Onyango",
            "gender":"male",
            "givenName":"Endalekachew",
            "nationalId":"198804042874913",
            "phoneNumber":"091-749-4674",
            "uid":"0x4"
         },
         {
            "auxId":"rec-00000000-bbb-0",
            "city":"Nairobi",
            "dob":"20171114",
            "familyName":"Onyango",
            "gender":"male",
            "givenName":"Endalekachew",
            "nationalId":"198804042874913",
            "phoneNumber":"091-749-4674",
            "uid":"0x6"
         },
         {
            "auxId":"rec-00000000-bbb-1",
            "city":"Nairobi",
            "dob":"20171114",
            "familyName":"Onyango",
            "gender":"male",
            "givenName":"Endalekachew",
            "nationalId":"198804042874913",
            "phoneNumber":"091-749-4674",
            "uid":"0x8"
         },
      ],
      "pagination":{
         "total": 11
      }
   }
}

```

## POST /custom-search/(golden|patient)
The following endpoint is used for the custom search either for golden or patient records. 

Below a sample of the request body :

```json
{
  "$or": [
    {
      "parameters": [
        {
          "fieldName": "givenName",
          "value": "Jhon",
          "distance": 0
        },
        {
          "fieldName": "familyName",
          "value": "Doe",
          "distance": 3
        }
      ]
    },
    {
      "parameters": [
        {
          "fieldName": "nationalId",
          "value": "XXXXXXXXXXXXXX",
          "distance": 0
        }
      ]
    }
  ],
  "sortBy": "given_name",
  "sortAsc": true,
  "offset": 0,
  "limit": 10
}
```
The response payload is similar to the one returned by the simple search API endpoint. 