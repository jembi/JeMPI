import numpy as np
import pandas as pd

from src.ClinicalDataGenerator import MinimalClinicalDataGenerator
from src.CorrupterGenerator import Corrupters
from src.DemographicDataGenerator import PatientGenerator
from src import helper, basefunctions


def generate_dataset():

    csv_file_name = "results/dataset-6-002000-05-50.csv"

    config = \
        {"BaseDate": "2022-01-01",
         "NumberOfPatients": 2_000,
         "AverageNumberOfClinicalRecordsPerPatient": 5,
         "PercentageOfCorruptedRecords": 0.5,
         "fields": [
             {"name": "given_name",
              "weight": 0.2,
              "corrupter": {
                  "type": ["missing_value_corrupter", "keyboard_corrupter", "edit1_corrupter", "edit2_corrupter",
                           "phonetic_corrupter", "ocr_corrupter"],
                  "weight": [0.2, 0.2, 0.3, 0.0, 0.15, 0.15]}},
             {"name": "family_name",
              "weight": 0.2,
              "corrupter": {
                  "type": ["missing_value_corrupter", "keyboard_corrupter", "edit1_corrupter", "edit2_corrupter",
                           "phonetic_corrupter", "ocr_corrupter"],
                  "weight": [0.2, 0.3, 0.2, 0.0, 0.1, 0.2]}},
             {"name": "gender",
              "weight": 0.1,
              "corrupter": {
                  "type": ["missing_value_corrupter", "keyboard_corrupter", "edit1_corrupter", "edit2_corrupter",
                           "phonetic_corrupter", "ocr_corrupter"],
                  "weight": [1.0, 0.0, 0.0, 0.0, 0.0, 0.0]}},
             {"name": "dob",
              "weight": 0.1,
              "corrupter": {
                  "type": ["missing_value_corrupter", "keyboard_corrupter", "edit1_corrupter", "edit2_corrupter",
                           "phonetic_corrupter", "ocr_corrupter"],
                  "weight": [1.0, 0.0, 0.0, 0.0, 0.0, 0.0]}},
             {"name": "phone_number",
              "weight": 0.1,
              "corrupter": {
                  "type": ["missing_value_corrupter", "keyboard_corrupter", "edit1_corrupter", "edit2_corrupter",
                           "phonetic_corrupter", "ocr_corrupter"],
                  "weight": [0.3, 0.3, 0.0, 0.3, 0.0, 0.1]}},
             {"name": "city",
              "weight": 0.2,
              "corrupter": {
                  "type": ["missing_value_corrupter", "keyboard_corrupter", "edit1_corrupter", "edit2_corrupter",
                           "phonetic_corrupter", "ocr_corrupter"],
                  "weight": [1.0, 0.0, 0.0, 0.0, 0.0, 0.0]}},
             {"name": "national_id",
              "weight": 0.1,
              "corrupter": {
                  "type": ["missing_value_corrupter", "keyboard_corrupter", "edit1_corrupter", "edit2_corrupter",
                           "phonetic_corrupter", "ocr_corrupter"],
                  "weight": [0.3, 0.3, 0.0, 0.3, 0.0, 0.1]}}
         ]}

    fields = config["fields"]
    field_name_list = []
    field_weight_list = []
    field_corrupter_name_list = {}
    field_corrupter_weight_list = {}
    for f_idx in range(len(fields)):
        field_name_list.append(fields[f_idx]['name'])
        field_weight_list.append(fields[f_idx]['weight'])
        field_corrupter_name_list[fields[f_idx]['name']] = fields[f_idx]['corrupter']['type']
        field_corrupter_weight_list[fields[f_idx]['name']] = fields[f_idx]['corrupter']['weight']

    base_date = config['BaseDate']
    seed = 123456
    rng = np.random.default_rng(seed)
    gender_generator = PatientGenerator.gender_generator(seed, 0.50)
    given_name_male_generator = PatientGenerator.name_generator(seed, 'metadata/private/zambia-male-given-names.csv')
    given_name_female_generator = PatientGenerator.name_generator(seed, 'metadata/private/zambia-female-given-names.csv')
    family_name_female_generator = PatientGenerator.name_generator(seed, 'metadata/private/zambia-female-family-names.csv')
    family_name_male_generator = PatientGenerator.name_generator(seed, 'metadata/private/zambia-male-family-names.csv')
    dob_generator = PatientGenerator.date_generator(seed, base_date, 'gumbel', 35, 12 * 10)
    phone_number_generator = PatientGenerator.phone_number_generator(seed, 'metadata/phone_area_codes.csv')
    city_generator = PatientGenerator.city_generator(seed, 'metadata/cities.csv')
    national_id_generator = PatientGenerator.national_id_generator(seed)
    clinical_data_generator = MinimalClinicalDataGenerator.clinical_data_generator(
        seed,
        config['AverageNumberOfClinicalRecordsPerPatient'])

    corrupter_dict = {'ocr_corrupter': Corrupters.ocr_corrupter('metadata/ocr-variations.csv', False, None,
                                                                Corrupters.position_mod_normal),
                      'missing_value_corrupter': Corrupters.missing_value_corrupter(),
                      'keyboard_corrupter': Corrupters.keyboard_corrupter(0.5, 0.5, Corrupters.position_mod_normal),
                      'edit1_corrupter': Corrupters.edit_corrupter(Corrupters.position_mod_normal,
                                                                   basefunctions.char_set_ascii,
                                                                   0.50, 0.50, 0.00, 0.00),
                      'edit2_corrupter': Corrupters.edit_corrupter(Corrupters.position_mod_uniform,
                                                                   basefunctions.char_set_ascii,
                                                                   0.25, 0.25, 0.25, 0.25),
                      'phonetic_corrupter': Corrupters.phonetic_corrupter('metadata/phonetic-variations.csv',
                                                                          False, 'utf_8')}

    number_of_patients = config.get("NumberOfPatients")
    data = []
    next(national_id_generator)
    next(phone_number_generator)
    next(clinical_data_generator)
    next(corrupter_dict['missing_value_corrupter'])
    next(corrupter_dict['ocr_corrupter'])
    next(corrupter_dict['keyboard_corrupter'])
    next(corrupter_dict['edit1_corrupter'])
    next(corrupter_dict['edit2_corrupter'])
    next(corrupter_dict['phonetic_corrupter'])
    k = 0
    for i in range(0, number_of_patients):

        gender = next(gender_generator)
        given_name = next(given_name_female_generator)[1] if gender == 'female' else next(given_name_male_generator)[1]
        family_name = next(family_name_female_generator)[1] if gender == 'female' else next(family_name_male_generator)[1]
        dob = next(dob_generator)
        dob = np.datetime_as_string(dob, unit='D')
        city = next(city_generator)[1]
        phone_number = phone_number_generator.send(city)
        national_id = national_id_generator.send((dob, gender))
        clinical_data = clinical_data_generator.send((gender, base_date, dob, "patient_id"))
        for j in range(0, len(clinical_data)):
            rec_num = "rec-%010d-%02d" % (i + 1, j)
            facility = clinical_data[j]['facility']
            patient_id = clinical_data[j]['patient_id']
            c_data = clinical_data[j]['clinical_data']
            # data.append([rec_num, given_name, family_name, gender, dob, city, phone_number, national_id,
            #              facility, patient_id, c_data])
            # data.append([rec_num, gender, dob, city, facility, patient_id, c_data])
            data.append([rec_num, given_name, family_name, gender, dob, phone_number, city, national_id, facility, patient_id, c_data ])
        k = k + 1
        if k % 1000 == 0:
            print(k)

    # df = pd.DataFrame(data, columns=['rec_num', 'given_name', 'family_name', 'gender', 'dob',
    #                                  'city', 'phone_number', 'national_id',
    #                                  'src_id_facility', 'src_id_patient', 'clinical_data'])
    # df = pd.DataFrame(data, columns=['rec_num', 'gender', 'dob',
    #                                  'city',
    #                                  'src_id_facility', 'src_id_patient', 'clinical_data'])
    df = pd.DataFrame(data, columns=['rec_num', 'given_name', 'family_name', 'gender', 'dob',
                                     'phone_number', 'city', 'national_id',
                                     'src_id_facility', 'src_id_patient', 'clinical_data'])
    df['corrupted'] = False
    number_of_records = df.shape[0]
    percentage_of_corrupted_records = config['PercentageOfCorruptedRecords']
    number_of_corrupted_records = int(number_of_records * percentage_of_corrupted_records)
    for i in range(0, number_of_corrupted_records):
        candidate_not_corrupted = False
        row_to_corrupt = None
        while not candidate_not_corrupted:
            row_to_corrupt = rng.integers(0, number_of_records)
            already_corrupted = df.loc[row_to_corrupt]['corrupted']
            if not already_corrupted:
                df.at[row_to_corrupt, 'corrupted'] = True
                candidate_not_corrupted = True

        columns_to_corrupt = rng.choice(a=field_name_list,
                                        p=field_weight_list,
                                        size=1, # rng.integers(1, len(field_weight_list) + 1),
                                        replace=False)
        # print('row to corrupt: %s' % row_to_corrupt)
        for column_to_corrupt in columns_to_corrupt:
            corrupter_names = field_corrupter_name_list[column_to_corrupt]
            corrupter_weights = field_corrupter_weight_list[column_to_corrupt]
            corrupter = rng.choice(a=corrupter_names, p=corrupter_weights, size=1, replace=False)[0]
            # print('col to corrupt: %s, %s' % (column_to_corrupt, corrupter))
            value_to_corrupt = df.at[row_to_corrupt, column_to_corrupt]
            corrupter_value = corrupter_dict[corrupter].send(value_to_corrupt)
            df.at[row_to_corrupt, column_to_corrupt] = corrupter_value
        # print()
    df = df.drop('corrupted', axis=1)
    df.to_csv(csv_file_name, index=False, encoding='utf-8')


def main():
    generate_dataset()


main()
