---
description: API endpoints documentation
---

# API

## GET /fieldsConfiguration

The following endpoint returns the fields configuration needed by the frontend (JeMPI-UI) in order to properly display interactions data according to a specific
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
    "fieldType": "String",
    "validation": {
      "regex":"/(Golden|Interaction)/",
      "required": true,
      "onErrorMessage": "This field cannot be empty"
    }
  },
  ...
]
```

For each field we have a set of attributes, as defined below :

| Attribute   | Description                                                                                                     | Used by            |
| ----------- | --------------------------------------------------------------------------------------------------------------- | ------------------ |
| fieldName   | A "camel-case" field name which will be used when accessing a patient record data structure                     | Backend + Frontend |
| fieldLabel  | A string that is a human readable name for the field                                                            | Frontend           |
| scope       | Array of URL paths that tells the frontend UI in which pages should the field appear                            | Frontend           |
| groups      | Array of strings which identifies in which section within a frontend UI page should the field be displayed      | Frontend           |
| fieldType   | A string that identifies the type of field, could be String, Date, ...(useful for formatting for example)       | Frontend + Backend |
| accessLevel | An array of string that identifies which user roles are permitted to access a given field (NOT YET IMPLEMENTED) | Frontend           |
| readOnly    | Tells if the field can be editable.                                                                             | Frontend           |
| validation  | An Object used for validating the field.                                                                        | Frontend           |

The fields should be configured manually in the config json file : `devops/linux/docker/data-config/config-reference-link-dp-api.json`

There's two type of fields :

- Custom fields : Indexed by the key "fields", contains all the fields that are specific to the implementation. Examples : givenName, nationalId, ...
- System fields : Indexed by the key "systemFields", it contains all the fields that are readonly fields and do not change across the implementation. Example : uid, record type, score, ...

> ! IMPORTANT : The `fieldName` in `config-reference-link-dp-api.json` should be set in snake-case, but it's returned in camel-case by the API.

## POST /notifications

Below is a sample of the body you are to send :

```json
{
    "limit": 25,
    "offset":10 ,
    "startDate":"2024-04-15T23:59:59" ,
    "endDate": "2024-04-15T23:59:59",
    "states": ["OPEN"]
}
```

The following endpoint returns notifications. notifications are used to inform the user about potential interaction linking to golden records and are generated when a certain case is triggerd. the response contains parameters (`count`, `skippedRecords` ) that are useful for pagination.

Below are the necessary parameters to get the notifications list

| Parameter | Description                                                                                               | Type   |
| --------- | --------------------------------------------------------------------------------------------------------- | ------ |
| limit     | Number of notification that the user want to get from the API                                             | Number |
| date      | A date limit, the user will get the data from the oldest to that particualar date limit (YYYY-MM-DD)      | Date   |
| offset    | A pagination parameter                                                                                    | Number |
| state     | The state of the notificaitons that we want to fetch interaction (OPEN, CLOSED) | String |

Below is a sample of the response

```json
{
  "count": 10,
  "skippedRecords": 773,
  "records": [
    {
      "score": 0.6156526,
      "candidates": [
        {
          "score": 0.6156526,
          "golden_id": "0x2a"
        }
      ],
      "golden_id": "0x2a",
      "names": "katanewa",
      "patient_id": "0x2b",
      "created": "2023-08-31",
      "id": "a448a79e-09c1-4e84-b7ed-0d0c3c4d3f61",
      "state": "New",
      "type": "THRESHOLD"
    },
    {
      "score": 1.0,
      "candidates": [
        {
          "score": 0.556004,
          "golden_id": "0xb"
        }
      ],
      "golden_id": "0xd",
      "names": "zuli,ezther",
      "patient_id": "0xc",
      "created": "2023-08-31",
      "id": "3b0f6dcf-428b-4c07-9b61-48220633569f",
      "state": "New",
      "type": "THRESHOLD"
    }
  ]
}
```

## POST /interaction

The following endpoint returns an interaction given a `uid` is supplied. This endpoint returns a object. 

Below is a sample of the body:

```json
{
    "uid":"0x4"
}
```

Below is a sample of the response:

```json
{
  "uid": "0x4",
  "sourceId": {
    "uid": "0x3",
    "facility": "MA2",
    "patient": "198702145001118"
  },
  "uniqueInteractionData": {
    "auxDateCreated": "2023-08-31T13:44:44.576006034",
    "auxId": "rec-0000000001--0",
    "auxClinicalData": "RANDOM DATA(645)"
  },
  "demographicData": {
    "givenName": "gift",
    "familyName": "zulu",
    "gender": "male",
    "dob": "19870214",
    "city": "lusaka",
    "phoneNumber": "0100563200",
    "nationalId": "198702145001118"
  }
}
```

## POST /expandedGoldenRecord

Given a supplied `uid`, The following endpoint returns an expanded golden records=, meaning a golden record with interactions linked to it. This endpoint returns an object. 

Below is a sample of the body you are to send :

```json
{
    "gid":"0x2b20"
}
```

Below a sample of the response :

```json
{
  "goldenRecord": {
    "uid": "0x2e",
    "sourceId": [
      {
        "uid": "0x31",
        "facility": "MA1",
        "patient": "199508125001143"
      }
    ],
    "uniqueGoldenRecordData": {
      "auxDateCreated": "2023-08-31T13:44:47.655570503",
      "auxAutoUpdateEnabled": true,
      "auxId": "rec-0000000005--4"
    },
    "demographicData": {
      "givenName": "katanekwa",
      "familyName": "simukungwe",
      "gender": "male",
      "dob": "1995o812",
      "city": "kitwe",
      "phoneNumber": "0112302557",
      "nationalId": "199508125001143"
    }
  },
  "interactionsWithScore": [
    {
      "interaction": {
        "uid": "0x32",
        "sourceId": {
          "uid": "0x31",
          "facility": "MA1",
          "patient": "199508125001143"
        },
        "uniqueInteractionData": {
          "auxDateCreated": "2023-08-31T13:44:44.653638164",
          "auxId": "rec-0000000005--6",
          "auxClinicalData": "RANDOM DATA(998)"
        },
        "demographicData": {
          "givenName": "katanekwa",
          "familyName": "simukungwe",
          "gender": "male",
          "dob": "19950812",
          "city": "kitwe",
          "phoneNumber": "0112302557",
          "nationalId": "199508125001143"
        }
      },
      "score": 1.0
    },
    {
      "interaction": {
        "uid": "0x33",
        "sourceId": {
          "uid": "0x31",
          "facility": "MA1",
          "patient": "199508125001143"
        },
        "uniqueInteractionData": {
          "auxDateCreated": "2023-08-31T13:44:44.657099612",
          "auxId": "rec-0000000005--7",
          "auxClinicalData": "RANDOM DATA(779)"
        },
        "demographicData": {
          "givenName": "katanekwa",
          "familyName": "simukungwe",
          "gender": "male",
          "dob": "",
          "city": "kitwie",
          "phoneNumber": "011230z557",
          "nationalId": "199508125001143"
        }
      },
      "score": 1.0
    }
  ]
}
```

## POST /expandedGoldenRecords

The following endpoint will return a list of expanded golden records given a list of golden Ids (`GIDS_LIST`).

Below is a sample of the body you are to send :

```json
{
    "uidList": [
        "0x4d03",
        "0x3a07",
        "0x2b20"
    ]
}
```

Below is a sample of the response:

```json
[
  {
    "goldenRecord": {
      "uid": "0x4d03",
      "sourceId": [
        {
          "uid": "0x4d01",
          "facility": "MA3",
          "patient": "198304285002129"
        },
        {
          "uid": "0x4d08",
          "facility": "MA1",
          "patient": "198304285002129"
        },
        {
          "uid": "0x4d0a",
          "facility": "MA4",
          "patient": "198304285002129"
        }
      ],
      "uniqueGoldenRecordData": {
        "auxDateCreated": "2023-09-01T07:28:06.846286719",
        "auxAutoUpdateEnabled": true,
        "auxId": "rec-0000000997--0"
      },
      "demographicData": {
        "givenName": "davison",
        "familyName": "nyambe",
        "gender": "male",
        "dob": "19830428",
        "city": "mbala",
        "phoneNumber": "0332675512",
        "nationalId": "198304285002129"
      }
    },
    "interactionsWithScore": [
      {
        "interaction": {
          "uid": "0x4d02",
          "sourceId": {
            "uid": "0x4d01",
            "facility": "MA3",
            "patient": "198304285002129"
          },
          "uniqueInteractionData": {
            "auxDateCreated": "2023-09-01T07:26:04.152254576",
            "auxId": "rec-0000000997--0",
            "auxClinicalData": "RANDOM DATA(693)"
          },
          "demographicData": {
            "givenName": "davxison",
            "familyName": "nyambe",
            "gender": "",
            "dob": "19820428",
            "city": "mbala",
            "phoneNumber": "",
            "nationalId": "1983o4285002129"
          }
        },
        "score": 0.631448
      }
    ]
  }
]
```

## GET /gidsAll

The following endpoint returns a list of the saved (created) Golden record Ids.

```json
{
  "records": [
    "0x2715",
    "0x2718",
    "0x271b",
    "0x271d",
    "0x2726",
    "0x2729",
    "0x272c",
    "0x2733",
    "0x2737",
    "0x273a",
    "0x273e",
    "0x2748",
    "0x274f",
    "0x2752",
    "0x2754",
    "0x275c",
    "0x2761"
  ]
}
```

## POST /gidsPaged

The following endpoint returns a list of gids paginated, given parameters `OFFSET` and a `LENGTH`.

Below is a sample of the body you are to send :

```json
{
    "offset":0,
    "length":10
}
```

Below is a sample of the response:

```json
{
  "goldenIds": [
    "0x2a",
    "0x27",
    "0x23",
    "0x1c",
    "0x19",
    "0x16",
    "0xd",
    "0xb",
    "0x8",
    "0x5"
  ]
}
```

## POST /goldenRecordAuditTrail

The Following endpoint returns the audit trail for a given Golden Record with a Golden_Id `GOLDEN_ID`

Below is a sample of the body to send :

```json
{
    "gid":"0x2b20"
}
```

Below a sample of the response :

```json
{
  "entries": [
    {
      "inserted_at": "2023-09-04 08:37:02.000665",
      "created_at": "2023-09-04 08:37:02.000657",
      "interaction_id": "0x4c",
      "golden_id": "0x4d",
      "entry": "Interaction -> New GoldenRecord (1.000000)"
    },
    {
      "inserted_at": "2023-09-04 08:37:02.000732",
      "created_at": "2023-09-04 08:37:02.000725",
      "interaction_id": "0x4f",
      "golden_id": "0x4d",
      "entry": "Interaction -> Existing GoldenRecord (0.72150)"
    }
  ]
}
```

## POST /interactionAuditTrail

The Following endpoint returns the audit trail for a given Interaction with Interaction Id `INTERACTION_ID`

Below a sample of the request body:

```json
{
    "uid":"0x2b1f"
}
```
Below a sample of the response:

```json
{
  "entries": [
    {
      "inserted_at": "2023-09-04 08:37:02.000665",
      "created_at": "2023-09-04 08:37:02.000657",
      "interaction_id": "0x4c",
      "golden_id": "0x4d",
      "entry": "Interaction -> New GoldenRecord (1.000000)"
    }
  ]
}
```

## GET /countGoldenRecords

The following endpoint returns the golden record count available in the database

```json
{
  "count": 1911
}
```

## GET /countInteractions

The following endpoint returns the interaction count available in the database

```json
{
  "count": 5035
}
```

## GET /countRecords

The following endpoint returns the record count available in the database. bellow is a sample of the response

```json
{
  "goldenRecords": 1911,
  "interactions": 5035
}
```

## POST /notificationRequest

The following endpoint update the notification state given a notification Id and a state.
Below a sample of the request:

```json
{
  "notificationId": "53512711-aca5-4bfb-a239-51e438d74d36",
  "state": "Pending"
}
```

## POST /search/(golden|patient)

The following endpoint is used for the simple search either for golden or interactions.

Below a sample of the request body :

```json
{
  "parameters": [
    {
      "fieldName": "givenName",
      "value": "John",
      "distance": 0
    },
    {
      "fieldName": "familyName",
      "value": "Doe",
      "distance": 0
    },
    {
      "fieldName": "dob",
      "value": "19870423",
      "distance": 0
    },
    {
      "fieldName": "nationalId",
      "value": "198804042874913",
      "distance": 0
    }
  ],
  "sortBy": "givenName",
  "sortAsc": true,
  "offset": 0,
  "limit": 10
}
```

### POST /search/golden

When the request is sent to the url `/search/golden` the response payload will contain the list of golden records along with the linked records and the result set total (useful for pagination) :

```json
{
  "data": [
    {
      "goldenRecord": {
        "uid": "0x1019",
        "sourceId": [
          {
            "uid": "0x1017",
            "facility": "FA3",
            "patient": "198704115001082"
          }
        ],
        "uniqueGoldenRecordData": {
          "auxDateCreated": "2023-09-04T14:39:52.258643516",
          "auxAutoUpdateEnabled": true,
          "auxId": "rec-0000000425--0"
        },
        "demographicData": {
          "givenName": "jan",
          "familyName": "daka",
          "gender": "",
          "dob": "19879411",
          "city": "mongu",
          "phoneNumber": "",
          "nationalId": "898704115001082"
        }
      },
      "interactionsWithScore": [
        {
          "interaction": {
            "uid": "0x1018",
            "sourceId": {
              "uid": "0x1017",
              "facility": "FA3",
              "patient": "198704115001082"
            },
            "uniqueInteractionData": {
              "auxDateCreated": "2023-09-04T14:38:48.901033856",
              "auxId": "rec-0000000425--0",
              "auxClinicalData": "RANDOM DATA(290)"
            },
            "demographicData": {
              "givenName": "jan",
              "familyName": "daka",
              "gender": "",
              "dob": "19879411",
              "city": "mongu",
              "phoneNumber": "",
              "nationalId": "898704115001082"
            }
          },
          "score": 1.0
        }
      ]
    },
    {
      "goldenRecord": {
        "uid": "0x1220",
        "sourceId": [
          {
            "uid": "0x121e",
            "facility": "FA5",
            "patient": "198308125001080"
          }
        ],
        "uniqueGoldenRecordData": {
          "auxDateCreated": "2023-09-04T14:39:59.44475916",
          "auxAutoUpdateEnabled": true,
          "auxId": "rec-0000000480--2"
        },
        "demographicData": {
          "givenName": "jan",
          "familyName": "dakq",
          "gender": "",
          "dob": "",
          "city": "",
          "phoneNumber": "",
          "nationalId": "1983081q5001080"
        }
      },
      "interactionsWithScore": [
        {
          "interaction": {
            "uid": "0x121f",
            "sourceId": {
              "uid": "0x121e",
              "facility": "FA5",
              "patient": "198308125001080"
            },
            "uniqueInteractionData": {
              "auxDateCreated": "2023-09-04T14:38:49.755542027",
              "auxId": "rec-0000000480--2",
              "auxClinicalData": "RANDOM DATA(424)"
            },
            "demographicData": {
              "givenName": "jan",
              "familyName": "dakq",
              "gender": "",
              "dob": "",
              "city": "",
              "phoneNumber": "",
              "nationalId": "1983081q5001080"
            }
          },
          "score": 1.0
        }
      ]
    }
  ],
  "pagination": {
    "total": 7
  }
}
```

### POST /search/patient

When the request is performed against the url `/search/patient` the response payload will contain the list of interactions along with the search result total count :

```json
{
  "data": [
    {
      "uid": "0x1018",
      "sourceId": {
        "uid": "0x1017",
        "facility": "FA3",
        "patient": "198704115001082"
      },
      "uniqueInteractionData": {
        "auxDateCreated": "2023-09-04T14:38:48.901033856",
        "auxId": "rec-0000000425--0",
        "auxClinicalData": "RANDOM DATA(290)"
      },
      "demographicData": {
        "givenName": "jan",
        "familyName": "daka",
        "gender": "",
        "dob": "19879411",
        "city": "mongu",
        "phoneNumber": "",
        "nationalId": "898704115001082"
      }
    },
    {
      "uid": "0xaf4",
      "sourceId": {
        "uid": "0xaf3",
        "facility": "FA2",
        "patient": "199201285001099"
      },
      "uniqueInteractionData": {
        "auxDateCreated": "2023-09-04T14:38:46.01708765",
        "auxId": "rec-0000000297--0",
        "auxClinicalData": "RANDOM DATA(147)"
      },
      "demographicData": {
        "givenName": "jan",
        "familyName": "daia",
        "gender": "female",
        "dob": "",
        "city": "mwens3",
        "phoneNumber": "06841w1573",
        "nationalId": "199201285001099"
      }
    }
  ],
  "pagination": {
    "total": 18
  }
}
```

## POST /customSearch/(golden|patient)

The following endpoint is used for the custom search either for golden or interactions.

Below a sample of the request body :

```json
{
  "$or": [
    {
      "parameters": [
        {
          "fieldName": "givenName",
          "value": "John",
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

## POST /upload

The following endpoint is used to upload file into JeMPI. the file uploaded will be put into the `async_reciever`'s storage under the `/csv` directory.


## POST /calculateScores

The following endpoint is used to calculate the score between an interaction and a set of golden records

Below a sample of the request body

```json
{
  "goldenIds": ["0x26bc"],
  "interactionId": "0x26bb"
}
```

## POST /filterGids

The following endpoint returns a paginated Golden Ids list a request body illustrated in the example below

Note: this endpoint is similar to the `search/(golden|patient)`

```json
{
  "parameters": [
    {
      "fieldName": "givenName",
      "value": "John",
      "distance": 0
    },
    {
      "fieldName": "familyName",
      "value": "Doe",
      "distance": 3
    }
  ],
  "sortBy": "givenName",
  "sortAsc": true,
  "offset": 0,
  "limit": 10
}
```

## POST /crRegister

This endpoint is used for the patient registration process, in that it checks if the patient already exists, preventing duplicate patient registrations. This relies on the threshold provided in the request.

Below is a sample of the request body:

```json
{
  "candidateThreshold": 0.9,
  "sourceId": {
    "facility": "fac1",
    "patient": "12345689"
  },
  "uniqueInteractionData": {
    "auxDateCreated": "2016-10-30T14:22:25.00Z",
    "auxClinicalData" : "REGISTER DATA",
    "auxId" : "1235",
    "auxIid" : "0x9"
  },
  "demographicData": {
    "givenName": "Sanket",
    "familyName": "",
    "gender": "male",
    "dob": "",
    "city": "Kolhapur",
    "nationalId": ""
  }
}
```

## POST /crFind

This endpoint is used to find register patient golden records

Below is a sample of the request body:

```json
{
  "operand": {
    "fn": "eq",
    "name": "givenName",
    "value": "Sanket"
  },
  "operands": [
    {
      "operator": "and",
      "operand": {
        "fn": "match",
        "distance": 1,
        "name": "familyName",
        "value": "Shevare"
      }
    }
  ]
}
```

## POST /crCandidates

The following endpoint returns the list of candidate golden record given demographic data of a record and a threshold
Below a sample of the request body

```json
{
  "candidateThreshold": 0.9,
  "demographicData": {
    "givenName": "XX",
    "familyName": "YYY",
    "dob": "20000101",
    "gender": "female",
    "phoneNumber": "123456789",
    "city": "Cape Town",
    "nationalId": "123456789"
  }
}
```

Below a sample of the response body for this endpoint

```json
{
  "goldenRecords": [
    {
      "goldenId": "0x9ed",
      "sourceId": [
        {
          "uid": "0x9e9"
        }
      ],
      "customUniqueGoldenRecordData": {
        "auxDateCreated": "2023-09-04T14:39:26.351509536",
        "auxAutoUpdateEnabled": true,
        "auxId": "rec-0000000272--1"
      },
      "demographicData": {
        "givenName": "alx",
        "familyName": "swna",
        "gender": "",
        "dob": "",
        "city": "ndolad",
        "phoneNumber": "",
        "nationalId": ""
      }
    },
    {
      "goldenId": "0x1220",
      "sourceId": [
        {
          "uid": "0x121e"
        }
      ],
      "customUniqueGoldenRecordData": {
        "auxDateCreated": "2023-09-04T14:39:59.44475916",
        "auxAutoUpdateEnabled": true,
        "auxId": "rec-0000000480--2"
      },
      "demographicData": {
        "givenName": "jan",
        "familyName": "dakq",
        "gender": "",
        "dob": "",
        "city": "",
        "phoneNumber": "",
        "nationalId": "1983081q5001080"
      }
    },
    {
      "goldenId": "0x1845",
      "sourceId": [
        {
          "uid": "0x1841"
        }
      ],
      "customUniqueGoldenRecordData": {
        "auxDateCreated": "2023-09-04T14:40:25.363902833",
        "auxAutoUpdateEnabled": true,
        "auxId": "rec-0000000643--2"
      },
      "demographicData": {
        "givenName": "james",
        "familyName": "bansa",
        "gender": "female",
        "dob": "",
        "city": "kansanvshi",
        "phoneNumber": "",
        "nationalId": "198602245001086"
      }
    }
  ]
}
```

## POST /newlink

This endpoint is used to create a new golden record for an interaction, that the MPI identified as a possible match to another golden record

Below is a sample of the request body:

```json
{
  "notificationId": "432fd5fe-e7db-4d3c-8a4a-fb23e8b9b45f",
  "notificationType": "MARGIN",
  "interactionId": "0xfb5",
  "currentGoldenId": "0xa5f",
  "resolutionState": "RELINKED_NEW",
  "currentCandidates": [
    "0xfaa"
  ],
  "newGoldenId": "",
  "score": 1
}
```

Below is a sample of the response:

```json
{
  "goldenUID":"0x34ab",
  "interactionUID":"0xfb5",
  "sourceUID":"0x30",
  "score":2.0
}
```

## POST /relink

This endpoint point is used to link an interaction, identified by the MPI as a possible link to a golden record

Below is a sample of the request body:

```json
{
  "notificationId": "64c04a03-179f-4696-91ed-881dffc8f818",
  "notificationType": "MARGIN",
  "interactionId": "0x1f89",
  "currentGoldenId": "0xdda",
  "resolutionState": "RELINKED",
  "currentCandidates": [
    "0x1f7c"
  ],
  "newGoldenId": "0x1f7c",
  "score": 1
}
```

Below is a sample of the response:

```json
{
  "goldenUID":"0x1f7c",
  "interactionUID":"0x1f89",
  "sourceUID":null,
  "score":1.0
}
```

## POST /updateGoldenRecordFieldsForId

The following endpoint updates the fields of a golden record. This endpoint returns an object.
Below a sample of the request :

```json
{
  "goldenId":"0x2aeb",
  "fields": [
    {
      "name": "givenName",
      "value": "John"
    },
    {
      "name": "familyName",
      "value": "Doe"
    }
  ]
}
```

When at least one of the fields update is successful, the endpoint will return an array of the fields that have been successfully updated. If none of the fields has been updated, the endpoint will return a "400 Bad Request" response. The endpoint will return a "500 Internal Server Error" if the update failed for a different reason.
