import numpy as np
from datetime import datetime
from dateutil import relativedelta
from collections.abc import Generator


def clinical_data_generator(seed, average_number_of_clinical_records_per_patient) -> Generator[(str, str, str), (str, str), None]:
    """
    Random source id generator

    Args:
        seed: random national_id generator's seed.

    Returns:
        yields a source id tuple
    """
    # 00-14 years
    clinic_for_children = [['MC1', 'MC2', 'MC3', 'MC4', 'MC5'],
                          ['FC1', 'FC2', 'FC3', 'FC4', 'FC5']]
    # 15-24 years
    clinic_for_youth = [['MY1', 'MY2', 'MY3', 'MY4', 'MY5'],
                        ['FY1', 'FY2', 'FY3', 'FY4', 'FY5']]
    # 25-64 years
    clinic_for_adults = [['MA1', 'MA2', 'MA3', 'MA4', 'MA5'],
                         ['FA1', 'FA2', 'FA3', 'FA4', 'FA5']]
    # 65 years and over
    clinic_for_seniors = [['MS1', 'MS2', 'MS3', 'MS4', 'MS5'],
                          ['FS1', 'FS2', 'FS3', 'FS4', 'FS5']]
    rng = np.random.default_rng(seed)
    y = None
    while True:
        meta = yield y
        gender = meta[0]
        base_date = meta[1]
        dob = meta[2]
        p_id = meta[3]

        base_datetime = datetime.strptime(base_date, '%Y-%m-%d')
        dob_datetime = datetime.strptime(dob, '%Y-%m-%d')
        years = relativedelta.relativedelta(base_datetime, dob_datetime).years
        if years < 15:
            age_clinic = rng.choice(clinic_for_children[0 if gender == 'male' else 1],
                                    rng.integers(1, average_number_of_clinical_records_per_patient * 2),
                                    p=[0.7, 0.3, 0.0, 0.0, 0.0])
        elif years < 25:
            age_clinic = rng.choice(clinic_for_youth[0 if gender == 'male' else 1],
                                    rng.integers(1, average_number_of_clinical_records_per_patient * 2),
                                    p=[0.5, 0.3, 0.2, 0.0, 0.0])
        elif years < 65:
            age_clinic = rng.choice(clinic_for_adults[0 if gender == 'male' else 1],
                                    rng.integers(1, average_number_of_clinical_records_per_patient * 2),
                                    p=[0.2, 0.2, 0.2, 0.2, 0.2])
        else:
            age_clinic = rng.choice(clinic_for_seniors[0 if gender == 'male' else 1],
                                    rng.integers(1, average_number_of_clinical_records_per_patient * 2),
                                    p=[0.7, 0.3, 0.0, 0.0, 0.0])
        y = [{"facility": item, "patient_id": p_id, "clinical_data": "RANDOM DATA(" + str(np.random.randint(1000)) + ")"} for item in list(age_clinic)]
