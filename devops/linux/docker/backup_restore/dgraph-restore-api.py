import json
import datetime
import sys
import os


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


def process_json_data(data):
    for item in data:
        golden_record = item.get('goldenRecord')
        if golden_record:
            uid = golden_record.get('uid', 'N/A')
            demographic_data = golden_record.get('demographicData', {})
            given_name = demographic_data.get('givenName', 'N/A')
            print(f"UID: {uid}, Given Name: {given_name}")

            source_ids = golden_record.get('sourceId', [])
            if isinstance(source_ids, list):
                for source_id in source_ids:
                    facility = source_id.get('facility', 'N/A')
                    patient = source_id.get('patient', 'N/A')
                    print(f"  Facility: {facility}, Patient: {patient}")
            else:
                print("  No source IDs found or source IDs not in expected format.")

        interactions_with_score = item.get('interactionsWithScore', [])
        if isinstance(interactions_with_score, list):
            for interaction in interactions_with_score:
                interaction_data = interaction.get('interaction', {})
                interaction_uid = interaction_data.get('uid', 'N/A')
                interaction_demographic_data = interaction_data.get('demographicData', {})
                interaction_given_name = interaction_demographic_data.get('givenName', 'N/A')
                print(f"  Interaction UID: {interaction_uid}, Given Name: {interaction_given_name}")
        else:
            print("  No interactions found or interactions not in expected format.")


if __name__ == "__main__":
    # Check if a JSON file path is provided as an argument
    if len(sys.argv) < 2:
        print("Usage: python read_json.py <path_to_json_file>")
        sys.exit(1)

    json_file = sys.argv[1]
    main(json_file)
