import requests
import json
from datetime import datetime
import os
from dotenv import dotenv_values
import sys

env_vars = dotenv_values('../conf.env')
host = env_vars['NODE1_IP']
port = "50010"
backup_path = env_vars['DGRAPH_BACKUP_DIRECTORY']

if len(sys.argv) >= 1:
    current_datetime = sys.argv[1]
else:
    current_datetime = datetime.now().strftime('%Y%m%d_%H%M%S')

def create_folder_if_not_exists(folder_path):
    if not os.path.exists(folder_path):
        os.makedirs(folder_path)


# Function to fetch data for a single ID
def fetch_data_for_id(gid):
    get_expanded_golden_record = f'http://{host}:{port}/JeMPI/expandedGoldenRecord'
    payload = json.dumps({"gid": gid})
    headers = {'Content-Type': 'application/json'}
    response = requests.post(get_expanded_golden_record, headers=headers, data=payload)
    return response.json() if response.status_code == 200 else None


# Function to backup Dgraph data
def backup_dgraph_data():
    get_gid_url = f'http://{host}:{port}/JeMPI/gidsAll'
    print("Fetching list of all golden record id's")
    response = requests.get(get_gid_url)
    if response.status_code == 200:
        new_golden_records = response.json()
        backup_data = []
        for gid in new_golden_records.get("records"):
            golden_record_data = fetch_data_for_id(gid)
            if golden_record_data:
                backup_data.append(golden_record_data)

        file_name = f'dgraph_backup_{current_datetime}.json'
        print(f'Total {str(len(backup_data))} Golden records backed up.')

        backup_path_folder = create_backup_json(backup_data, file_name)

        print(f'All data saved to {backup_path_folder + "/" + file_name}')
    else:
        print('Failed to retrieve list of IDs from the API')


def create_backup_json(backup_data, file_name):
    backup_path_folder = os.path.join(backup_path, current_datetime)
    create_folder_if_not_exists(backup_path_folder)
    with open(os.path.join(backup_path_folder, file_name), 'w') as json_file:
        json.dump(backup_data, json_file, indent=4)
    return backup_path_folder


backup_dgraph_data()
