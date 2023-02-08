package org.jembi.jempi.em;


import org.jembi.jempi.shared.models.CustomEntity;

record CustomPatient(String genderAtBirth,
                     String dateOfBirth) {

   CustomPatient(final CustomEntity entity) {
      this(entity.gender(), entity.dob());
   }
}


