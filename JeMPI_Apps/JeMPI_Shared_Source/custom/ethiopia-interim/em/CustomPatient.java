package org.jembi.jempi.em;


import org.jembi.jempi.shared.models.CustomEntity;

record CustomPatient(String col1, String col1Phonetic,
               String col2, String col2Phonetic,
               String genderAtBirth,
               String dateOfBirth,
               String city, String cityPhonetic,
               String phoneNumber) {

    CustomPatient(final CustomEntity entity) {
        this(entity.nameGiven(), CustomEMTask.getPhonetic(entity.nameGiven()),
             entity.nameFather(), CustomEMTask.getPhonetic(entity.nameFather()),
             entity.gender(),
             entity.dob(),
             entity.city(), CustomEMTask.getPhonetic(entity.city()),
             entity.phoneNumber());
    }
}


