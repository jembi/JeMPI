---
description: How it works
---

# Architecture

JeMPI is a package that is composed of different microservices.

**Below the synchronous and asynchronous flow diagram.**

#### [Asynchronous flow](https://drive.google.com/file/d/1rcbF3UJ5Lh-4bjXl8GVpJVnYxA1diRjl/view?usp=sharing) <a href="#_2v012h2bohjt" id="_2v012h2bohjt"></a>

![](.gitbook/assets/0)

#### [Synchronous flow](https://drive.google.com/file/d/10n82D46bQuhGogYGwdpB5jakGRgtD0aU/view?usp=sharing) <a href="#_hntouyz6p7yj" id="_hntouyz6p7yj"></a>

![](.gitbook/assets/1)

## JeMPI\_AsyncReceiver <a href="#_6om7ih1t1k41" id="_6om7ih1t1k41"></a>

**Description:** A microservice to transform the input format (csv) composed of a bunch of lines, to one single line at a time for the preprocessor.

There's only support for 10 columns in the following order **\[for the current version]**:

**String** uid,\
**SourceId** sourceId,\
**String** auxId,\
**String** givenName,\
**String** familyName,\
**String** gender,\
**String** dob,\
**String** city,\
**String** phoneNumber,\
**String** nationalId

**Input**

1. Saving csv files under _/app/csv_ that contains data (can be done through HTTP request)\
   Example of input file:

```
ID,Given_Name,Family_Name,Gender_at_Birth,Date_of_Birth,City,Phone_Number,National_ID,Dummy1,Dummy2,Dummy3
rec-00000000-aaa-0,Endalekachew,Onyango,male,20171114,Nairobi,091-749-4674,198804042874913,19940613,19781023,19660406
rec-00000001-aaa-0,Fikadu,Mwendwa,male,19840626,Nairobi,022-460-8846,199403050409528,20190317,19400321,20190104
rec-00000002-bbb-0,Biniyam,Maalim,male,20191022,Nairobi,098-119-7244,200006231841948,,20190302,
```

1. Sending a JSON patient through HTTP to to JeMPI\_AsyncReceiver (without send the ExternalLinkRange param)

**NB: **_**ExternalLinkRange** param is a parameter that has two values: low and high. It is used in the calculation of the probable match in the Linker. Shortly, any patient with a score greater than the ExternalLinkRange low value and lower than the ExternalLinkRange high value will be considered as a probable match._

**Output**

Kafka topic: _TOPIC\_PATIENT\_ASYNC\_PREPROCESSOR="JeMPI-async-preprocessor"_

<figure><img src=".gitbook/assets/2" alt=""><figcaption></figcaption></figure>

## JeMPI\_SyncReceiver <a href="#_yi7au41svo51" id="_yi7au41svo51"></a>

**Description:** A microservice that routes the data to the preprocessor.

**Input**

Sending a FHIR patient (JSON) through HTTP. It should have an _ExternalLinkRange param._

**NB: **_**ExternalLinkRange** param is a parameter that has two values: low and high. It is used in the calculation of the probable match in the Linker. Shortly, any patient with a score greater than the ExternalLinkRange low value and lower than the ExternalLinkRange high value will be considered as a probable match._

**Output**

The data is sent to the next microservice JeMPI\_PreProcessor.

## JeMPI\_PreProcessor <a href="#_r783bgaxx08b" id="_r783bgaxx08b"></a>

**Description:** A microservice to transform and preprocess the input format from the receiver, to one single line JSON (JSON Streaming) to the controller:

* For async flow:\
  Send it to the Kafka topic for the Controller.
* For sync flow:\
  Proxy the HTTP request coming from the Sync\_receiver to the Controller.\
  Get the response coming from the Controller and transform it to FHIR and then send it to the Sync\_receiver.

**Input:**

* For async flow:\
  Kafka topic: _TOPIC\_PATIENT\_ASYNC\_PREPROCESSOR="JeMPI-async-preprocessor"_
* For sync flow:\
  HTTP request that is coming from the Sync\_receiver.\
  HTTP response coming from the Controller.

**Output:**

The resulted data next will be sent to:

* For async flow:\
  Kafka topic: _TOPIC\_PATIENT\_CONTROLLER="JeMPI-patient-controller"_
* For sync flow:\
  HTTP request proxied to the microservice JeMPI\_Controller.\
  HTTP request to the Sync\_receiver containing the response.

Example:

<figure><img src=".gitbook/assets/3" alt=""><figcaption></figcaption></figure>

```json
{
    "entityType": "BATCH_RECORD",
    "stan": "2022/11/24 13:35:02:0000003",
    "entity": {
        "sourceId": {
            "facility": "LABORATORY",
            "patient": "200006231841948"
        },js
        "auxId": "rec-00000002-bbb-0",
        "givenName": "Biniyam",
        "familyName": "Maalim",
        "gender": "male",
        "dob": "20191022",
        "city": "Nairobi",
        "phoneNumber": "098-119-7244",
        "nationalId": "200006231841948"
    }
}
```

## JeMPI\_Controller <a href="#_lpn0tn79g4ka" id="_lpn0tn79g4ka"></a>

**Description:** A microservice that has different tasks:

* For async flow:\
  Send the data coming to the two kafka topics (For JeMPI\_EM & JeMPI\_Linker).
* For sync flow:\
  Send the data to one kafka topic for JeMPI\_EM and route it also to JeMPI\_Linker through HTTP.
* Control and manage the optimization of the MU calculation by activating/stopping the link process of the linker and getting the new value of MU from JeMPI\_EM to provide it to the linker.

**Input:**

* For async flow:\
  Kafka topic: _TOPIC\_PATIENT\_CONTROLLER="JeMPI-patient-controller"_.
* For sync flow:\
  HTTP request coming from the preprocessor.\
  HTTP response coming from the Linker.

MU process: Kafka topic: _TOPIC\_MU\_CONTROLLER="JeMPI-mu-controller"_

**Output:**

* For async flow:
  * Kafka topic: _TOPIC\_PATIENT\_EM="JeMPI-patient-em"_
  * Kafka topic: _TOPIC\_PATIENT\_LINKER="JeMPI-patient-linker"_
* For sync flow:
  * Kafka topic: _TOPIC\_PATIENT\_EM="JeMPI-patient-em"_
  * Routing the data to JeMPI\_Linker (HTTP)
  * Routing the HTTP response coming from the Linker to the Preprocessor.

MU process: Kafka topic: _TOPIC\_MU\_LINKER="JeMPI-mu-linker"_

![](.gitbook/assets/4) ![](.gitbook/assets/5)

## JeMPI\_EM <a href="#_7tf3t1atn1ab" id="_7tf3t1atn1ab"></a>

**Description:** A microservice that will create an object containing m\&u of a patient against patient records that go into the EM algorithm (quality (m) and the uniqueness (u) per field). This object is used in the linker for matching patients. It uses a machine learning algorithm to optimize that value, it is launched after receiving a number of records specified in the configuration.

**Input:** Kafka topic: _TOPIC\_PATIENT\_LINKER="JeMPI-patient-linker"_

**Output:** Kafka topic: _TOPIC\_MU\_CONTROLLER="JeMPI-mu-controller"_

## JeMPI\_Linker <a href="#_111ah0ssrp64" id="_111ah0ssrp64"></a>

**Description:** A microservice that will interact with Dgraph database to do the matching of the patients. It will get the candidates that have a similarity to the patient, and then:

* For async flow:\
  If the score of the candidate is superior than a _**threshold**_, it will link the patient to a master record.

Else, it will create a new patient with a new golden ID and if the score is between a certain range (probable match), the Linker will send a notification to the admin to check the probable match.

* For sync flow:\
  Same as the async flow except it will return the list of the candidates as a response to the controller.\
  If there is a match, it will return the information of the match as a response.

**NB:** The **threshold** used can be specified in the config.

**Input:**

* For async flow:\
  Kafka topic: _TOPIC\_PATIENT\_EM="JeMPI-patient-em"_\
  Kafka topic: _TOPIC\_MU\_LINKER="JeMPI-mu-linker"_
* For sync flow:\
  HTTP request coming from the Controller.

**Output:**

* Interact with the Dgraph database using GraphQL queries/mutations, save the patients and the links.
* Send response of either the link info or the list of candidates to the Controller
* Save response to Kafka topic: _TOPIC\_notifications=”JeMPI\_notifications”_

## JeMPI\_Dgraph <a href="#_kb1wgk9uafqz" id="_kb1wgk9uafqz"></a>

**Description:** The Dgraph database used for JeMPI.

Component linked:

* **Dgraph Ratel:** A tool for data visualization and cluster management can be used with Dgraph to manage cluster settings and run DQL queries and mutations, and see results.
* **Dgraph Alpha:** Expose and host endpoints of the indexes.
* **Dgraph Zero:** it is like a zookeeper in Kafka, it will control the instances of Alpha by assigning them to a group, and re-balances the data between them.

## JeMPI\_Kafka <a href="#_lhpqpufx5pyy" id="_lhpqpufx5pyy"></a>

**Description:** Kafka the message queue bus, it contains all the topics used previously in the other components.

## JeMPI\_API <a href="#_ioszcxv7tpj" id="_ioszcxv7tpj"></a>

**Description:** All the endpoints are in this component, it is used for the interaction with JeMPI WEB.

It will do the following actions:

* Read data from the Kafka topic _TOPIC\_notifications=”JeMPI\_notifications”_
* Save data related to the administration in PostgeSQL DB
* Get the data from PostgreSQL when the JeMPI Web requests data.
