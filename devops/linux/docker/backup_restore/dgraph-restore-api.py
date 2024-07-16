import json
import requests
import sys
import os
from datetime import datetime
from dotenv import dotenv_values

env_vars = dotenv_values('../conf.env')

host = env_vars['NODE1_IP']
port = "50010"


def main(json_file):
    # Ensure JSON file exists
    if not os.path.exists(json_file):
        print(f"File {json_file} not found.")
        sys.exit(1)

    # Read the JSON file
    with open(json_file, 'r') as file:
        data = json.load(file)

    # Check if the data is a list
    if not isinstance(data, list):
        print(f"Expected a list in the JSON file but got {type(data).__name__}.")
        sys.exit(1)

    # Process the data
    process_json_data(data)

    print("JSON data processed successfully.")


def convert_datetime_format(date_str):
    input_formats = [
        "%Y-%m-%dT%H:%M:%S.%fZ",         # With microseconds and Z timezone
        "%Y-%m-%dT%H:%M:%S.%f",          # With microseconds
        "%Y-%m-%dT%H:%M:%S.%f%z",        # With microseconds and UTC offset
        "%Y-%m-%dT%H:%M:%S",             # Without microseconds
        "%Y-%m-%dT%H:%M:%S%z",           # Without microseconds and UTC offset
        "%Y-%m-%dT%H:%M:%S.%fZ",         # With microseconds and Z timezone (redundant but kept for completeness)
        "%Y-%m-%dT%H:%M:%S.%f%Z",        # With microseconds and full timezone name
        "%Y-%m-%dT%H:%M:%S.%f %Z",       # With microseconds and space before timezone name
        "%Y-%m-%dT%H:%M:%S.%f %z",       # With microseconds and space before UTC offset
        "%Y-%m-%dT%H:%M",                # Without seconds
        "%Y-%m-%dT%H",                   # Without minutes
    ]
    for input_format in input_formats:
        try:
            dt = datetime.strptime(date_str[:26], input_format)  # Take only the first 26 characters to match the format
            break
        except ValueError:
            continue
    else:
        return date_str  # If the format is not correct, return the original string
    
    output_format = "%Y-%m-%dT%H:%M:%S.%fZ"
    output_str = dt.strftime(output_format)
    output_str = output_str[:26] + 'Z'  # Keep only the first 2 decimal places of the seconds part
    return output_str

def process_json_data(golden_records):
    
    for golden_record in golden_records:
        
        golden_record['goldenRecord']['uniqueGoldenRecordData']['auxDateCreated'] = convert_datetime_format(golden_record['goldenRecord']['uniqueGoldenRecordData']['auxDateCreated'])
        unique_golden_record_data = golden_record['goldenRecord']['uniqueGoldenRecordData']
        if not unique_golden_record_data.get('auxGid'):
            unique_golden_record_data['auxGid'] = golden_record['goldenRecord']['uid']
            
        for interaction in golden_record['interactionsWithScore']:
            interaction['interaction']['uniqueInteractionData']['auxDateCreated'] = convert_datetime_format(
                interaction['interaction']['uniqueInteractionData']['auxDateCreated'])
            unique_interaction_data = interaction['interaction']['uniqueInteractionData']
            if not unique_interaction_data.get('auxIid'):
                unique_interaction_data['auxIid'] = interaction['interaction']['uid']
        print("------------------------------------------------------")
        print(golden_record)
        response = send_golden_record_to_api(golden_record)
        print(response.text)
    
def send_golden_record_to_api(golden_record_payload):
    get_expanded_golden_record_url = f'http://{host}:{port}/JeMPI/restoreGoldenRecord'
    payload = json.dumps(golden_record_payload)
    headers = {
        'Content-Type': 'application/json'
    }
    response = requests.post(get_expanded_golden_record_url, headers=headers, data=payload)
    return response

if __name__ == "__main__":
    # Check if a JSON file path is provided as an argument
    if len(sys.argv) < 2:
        print("Usage: python read_json.py <path_to_json_file>")
        sys.exit(1)

    json_file = sys.argv[1]
    main(json_file)
