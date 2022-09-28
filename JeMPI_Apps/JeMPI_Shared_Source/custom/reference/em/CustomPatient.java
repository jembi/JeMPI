package org.jembi.jempi.em;


import org.jembi.jempi.shared.models.CustomEntity;

record CustomPatient(String col1, String col1Phonetic,
               String col2, String col2Phonetic,
               String genderAtBirth,
               String dateOfBirth,
               String city, String cityPhonetic,
               String phoneNumber,
               String nationalID) {

    CustomPatient(final CustomEntity entity) {
        this(entity.givenName(), CustomEMTask.getPhonetic(entity.givenName()),
             entity.familyName(), CustomEMTask.getPhonetic(entity.familyName()),
             entity.gender(),
             entity.dob(),
             entity.city(), CustomEMTask.getPhonetic(entity.city()),
             entity.phoneNumber(),
             entity.nationalId());
    }
}


