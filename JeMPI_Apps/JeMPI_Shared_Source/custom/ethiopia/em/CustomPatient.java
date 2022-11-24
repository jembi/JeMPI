package org.jembi.jempi.em;


import org.jembi.jempi.shared.models.CustomEntity;

record CustomPatient(String col1, String col1Phonetic,
               String col2, String col2Phonetic,
               String col3, String col3Phonetic,
               String col4, String col4Phonetic,
               String col5, String col5Phonetic,
               String genderAtBirth,
               String dateOfBirth,
               String city, String cityPhonetic,
               String phoneNumber) {

    CustomPatient(final CustomEntity entity) {
        this(entity.nameGiven(), CustomEMTask.getPhonetic(entity.nameGiven()),
             entity.nameFather(), CustomEMTask.getPhonetic(entity.nameFather()),
             entity.nameFathersFather(), CustomEMTask.getPhonetic(entity.nameFathersFather()),
             entity.nameMother(), CustomEMTask.getPhonetic(entity.nameMother()),
             entity.nameMothersFather(), CustomEMTask.getPhonetic(entity.nameMothersFather()),
             entity.gender(),
             entity.dob(),
             entity.city(), CustomEMTask.getPhonetic(entity.city()),
             entity.phoneNumber());
    }
}