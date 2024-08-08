---
description: How it works
---

# Architecture

The JeMPI Client Registry is a system that incorporates a microservice architecture, each microservice has a specific task such as data cleaning, data storing, etc. These various services communicate through a Kafka message bus, meaning that every service is storing and retrieving data from a specific Kafka topic.

**Below the synchronous and asynchronous flow diagram.**

#### [Asynchronous flow](https://drive.google.com/file/d/1G3_-BZNwRSOeriad6IbR6rFERQUnx1AK/view?usp=sharing) <a href="#_2v012h2bohjt" id="_2v012h2bohjt"></a>

![Asynchronous Flow Diagram](.gitbook/assets/0)

## JeMPI_AsyncReceiver <a href="#_6om7ih1t1k41" id="_6om7ih1t1k41"></a>

**Description:** A microservice that sends the content of an uploaded csv file to the JeMPI_ETL service. the JeMPI_AsyncReciever service produces kafka messages where each message has a row from the CSV file uploaded. it will then be saved under a kafka topic.

The base version uses a reference implementation with the fields below:

### String Fields
**String** auxId,\
**String** givenName,\
**String** familyName,\
**String** gender,\
**String** dob,\
**String** city,\
**String** phoneNumber,\
**String** nationalId

**Input**

1. A CSV file located in the JeMPI_AsyncReciever associated volume, under \*/app/csv\_ directory (this can be done through HTTP request).\
   Example of input file:

```
ID,Given_Name,Family_Name,Gender_at_Birth,Date_of_Birth,City,Phone_Number,National_ID
rec-00000000-aaa-0,Endalekachew,Onyango,male,20171114,Nairobi,091-749-4674,198804042874913
rec-00000001-aaa-0,Fikadu,Mwendwa,male,19840626,Nairobi,022-460-8846,199403050409528
rec-00000002-bbb-0,Biniyam,Maalim,male,20191022,Nairobi,098-119-7244,200006231841948
```

**Output**

The service will save the data from the CSV file, one line at a time.\
Kafka topic: _TOPIC_INTERACTION_ETL="JeMPI-interactions-etl"_

## JeMPI_ETL <a href="#_r783bgaxx08b" id="_r783bgaxx08b"></a>

**Description:** A microservice that pocesses the input coming from the JeMPI_AsyncReceiver. The JeMPI_ETL service will perform some data trasformation e.g. lower case the values for name of the patient or unformat the date for the date of birth. The resulting data will be sent as JSON (JSON Streaming) to the JeMPI_Controller service.

**Input:**

Data coming from the JeMPI_AsyncReciever service.\
Kafka topic: \_TOPIC_INTERACTION_ETL="JeMPI-interaction-etl"\*

**Output:**

Data transformed into JSON that will be sent to the JeMPI_Controller. It will be stored in the Kafka topic: \_TOPIC_INTERACTION_CONTROLLER="JeMPI-interaction-controller"\*

Example or a Kafka message coming from the interaction controller topic:

<figure><img src=".gitbook/assets/3" alt=""><figcaption></figcaption></figure>

```json
{
  "contentType": "BATCH_INTERACTION",
  "tag": "import-5334297603633827819uploadConfig",
  "stan": "2024/08/07 08:24:07:0000001",
  "interaction": {
    "sourceId": {
      "facility": "FA2",
      "patient": "patient_id"
    },
    "uniqueInteractionData": {
      "auxDateCreated": "2024-08-07T08:24:08.174750419",
      "auxUserFields": [
        {
          "scTag": "aux_id",
          "tag": "auxId",
          "value": "rec-0000000708-02"
        },
        {
          "scTag": "aux_clinical_data",
          "tag": "auxClinicalData",
          "value": "RANDOM DATA(865)"
        }
      ]
    },
    "demographicData": {
      "fields": [
        {
          "tag": "given_name",
          "value": "patricia"
        },
        {
          "tag": "family_name",
          "value": "solis"
        },
        {
          "tag": "gender",
          "value": "female"
        },
        {
          "tag": "dob",
          "value": "19821106"
        },
        {
          "tag": "city",
          "value": "chicago"
        },
        {
          "tag": "phone_number",
          "value": "0133705553"
        },
        {
          "tag": "national_id",
          "value": "198211065001099"
        }
      ]
    }
  },
  "sessionMetadata": {
    "commonMetaData": {
      "stan": "2024/08/07 08:24:07:0000001",
      "uploadConfig": {
        "reportingRequired": false,
        "uploadWorkflow": 0,
        "minThreshold": 0.65,
        "linkThreshold": 0.7,
        "maxThreshold": 0.75,
        "marginWindowSize": 0.1
      }
    },
    "uiMetadata": {
      "timeStamp": null
    },
    "asyncReceiverMetadata": {
      "timeStamp": "2024/08/07 08:24:08"
    },
    "etlMetadata": {
      "timeStamp": "2024/08/07 08:27:05"
    },
    "controllerMetadata": {
      "timeStamp": null
    },
    "linkerMetadata": {
      "timeStamp": null
    }
  }
}
```

## JeMPI_Controller <a href="#_lpn0tn79g4ka" id="_lpn0tn79g4ka"></a>

**Description:** The JeMPI_Controller service has multiple tasks:

- Send the data coming from the JeMPI_ETL to either the JeMPI_Linker or the JeMPI_EM services, based on the workflow selection made by user on import. The data will be stored in their respective Kafka topics accessed (consumed) by those service.

**Input:**
Data coming from the JeMPI_ETL service
Kafka topic: \_TOPIC_INTERACTION_CONTROLLER="JeMPI-interaction-controller"\*.

Values of the M & U computed in the JeMPI_EM service
Kafka topic: \_TOPIC_MU_CONTROLLER="JeMPI-mu-controller"\*

**Output:**
1. Send the data to the JeMPI_EM
- Kafka topic: _TOPIC_INTERACTION_EM="JeMPI-interaction-em"_
- MU process: Kafka topic: _TOPIC_MU_LINKER="JeMPI-mu-linker"_

![JeMPI-interaction-linker Kafka Topic](.gitbook/assets/4) ![JeMPI-interaction-em Kafka Topic](.gitbook/assets/5)

2. Send the data to the JeMPI_Linker
- Kafka topic: _TOPIC_INTERACTION_LINKER="JeMPI-interaction-linker"_

## JeMPI_EM <a href="#_7tf3t1atn1ab" id="_7tf3t1atn1ab"></a>

**Description:** A microservice that will create an object containing m\&u of a patient against patient records that go into the EM algorithm (quality (m) and the uniqueness (u) per field). This object is used in the linker for matching patients. It uses a machine learning called Estimation maximisation (EM) algorithm to optimize that value, it is launched after receiving a number of records specified in the configuration.

**Input:** Kafka topic: _TOPIC_INTERACTION_LINKER="JeMPI-interaction-linker"_

**Output:** Kafka topic: _TOPIC_MU_CONTROLLER="JeMPI-mu-controller"_

## JeMPI_Linker <a href="#_111ah0ssrp64" id="_111ah0ssrp64"></a>

**Description:** A microservice that will interact with Dgraph database to do the matching of the patients. The Linker uses thresholds to drive the linking and notifications for review processes. These thresholds are the following:

- **A single match or no match threshold :** the interaction will automatically be linked to the highest golden record candidate above the threshold. If no candidate has a score above the threshold, a new golden record is created. This is typically used for fully autonomous linking.
- **Window around the match/no match threshold :** if the highest score generated for the candidates falls within this window, a notification is sent for Admin to review the interaction.
- **Margin threshold :** if another candidate falls within a margin from the highest score and this highest score is above the match threshold, a notification for review is sent for the Admin to review the linked interaction.

**Input:**
Kafka topic: _TOPIC_INTERACTION_EM="JeMPI-interaction-em"_\

**Output:**

- Interact with the Dgraph database using GraphQL queries/mutations, save the interactions and the links.
- Send response of either the link info or the list of candidates to the Controller
- Save response to Kafka topic: _TOPIC_notifications=”JeMPI_notifications”_

## JeMPI_Dgraph <a href="#_kb1wgk9uafqz" id="_kb1wgk9uafqz"></a>

**Description:** The Dgraph database used for JeMPI to store the patient records. it is a graph database.

Component linked:

- **Dgraph Ratel:** A tool for data visualization and cluster management. Ratel can be used with Dgraph to manage cluster settings, run DQL queries and mutations and see results of the mentioned operations.
- **Dgraph Alpha:** Expose and host endpoints of the indexes.
- **Dgraph Zero:** it is like a Zookeeper/KRaft in Kafka, it will control the instances of Alpha by assigning them to a group, and re-balances the data between them.

## JeMPI_Kafka <a href="#_lhpqpufx5pyy" id="_lhpqpufx5pyy"></a>

**Description:** Kafka the message queue bus, it contains all the topics used previously in the other components.

## JeMPI_API <a href="#_ioszcxv7tpj" id="_ioszcxv7tpj"></a>

**Description:** The JeMPI_API service contains the endpoints needed to interact with JeMPI.

It performs the following functions:

- Read data from the Kafka topic _TOPIC_notifications=”JeMPI_notifications”_
- Save data related to the administration in PostgeSQL DB
- Get the data from PostgreSQL when the JeMPI Web requests data.
