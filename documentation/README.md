---
description: Pronounced 'Jem-P-I'
---

# 💎 JeMPI

JeMPI is an entity matching (commonly used for patient matching) and linking technology that supports batch and transactional matching via advanced techniques though a combination of deterministic and probabilistic matching.

## Overview

The Jembi MPI, also known as JeMPI, is a standards-based client registry (CR) or master patient index (MPI).

JeMPI facilitates the exchange of patient information between different systems and holds patient identifiers that may include patient demographic information. This is a necessary tool for public health to help manage patients, monitor outcomes, and conduct case-based surveillance.

JeMPI’s primary goal is to act as a tool in order to solve the issue of multiple or duplicated patient records that are submitted from multiple point of service systems such as electronic medical records, lab systems, radiology systems and other health information systems.

This is achieved by matching the various patient records from different systems under a Master Patient record with a unique ID. This allows for downstream applications, such as surveillance, to accurately display data and information on patient records without the worry that the data contains multiple records for the same patient.

## Privacy and Applicable Laws

JeMPI is an open-source Master Patient Index (MPI) application that stores patient identity and demographic information and assigns a golden ID for internal, unique identification. It was designed to assist with compliance with standard protection of personally identifiable information (PII) legislation and regulations by separating personal identification information from clinical information in a health information exchange.

JeMPI assists with separating personally identifiable information from clinical information for individual data stored in a health information exchange. JeMPI can implement encryption in flight and at rest (for dgraph). However, the at-rest functionality might need a license. Security and privacy have to be provided by the health information exchange infrastructure (e.g. the OpenHIM platform).

Data protection acts like the Kenya Data Protection Act and South Africa's Protection of Personal Information Act (POPIA) emphasize protecting personal information, including clinical data, and require consent for its processing and disclosure. They also address separating clinical information from personal identification information, stressing the need for secure handling and de-identification when necessary.

In Kenya, where JeMPI is presently used for patient matching, linking and de-duplication, the Digital Health Act (2023), in conjunction with the Data Protection Act (2019), envisages a health information exchange environment and pseudo-anonymization. It provides a framework for collecting, processing, and using health data, including de-identification and secure storage provisions. The Act emphasizes the need to separate clinical information from personal identification data, particularly for research purposes, and outlines the requirements for safe handling and de-identification. 
