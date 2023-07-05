import os


def generate_log_filename(target_name: str):
    if not os.path.exists('results/' + target_name + '000.csv'):
        return target_name + '000.csv'
    i = 1
    while True:
        new_name = target_name + f'{i:03}' + '.csv'
        if not os.path.exists('Results/' + new_name):
            return new_name
        i += 1
