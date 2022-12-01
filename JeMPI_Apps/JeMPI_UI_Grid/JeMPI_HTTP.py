import requests

import JeMPI_Config


def http_patch_unlink(golden_id, doc_id):
    url = JeMPI_Config.API_SERVER + '/JeMPI/Unlink'
    params = {'goldenID': golden_id, 'docID': doc_id}
    return requests.patch(url, params=params)


def http_patch_link(golden_id, new_golden_id, doc_id, score):
    url = JeMPI_Config.API_SERVER + '/JeMPI/Link'
    params = {'goldenID': golden_id, 'newGoldenID': new_golden_id, 'docID': doc_id, 'score': score}
    return requests.patch(url, params=params)


def http_patch_predicate(uid, predicate, value):
    url = JeMPI_Config.API_SERVER + '/JeMPI/PatchGoldenRecordPredicate'
    params = {'uid': uid, 'predicate': predicate, 'value': value}
    return requests.patch(url, params=params)


def http_get_candidate_list(uid, mu):
    url = JeMPI_Config.API_SERVER + '/JeMPI/Candidates'
    params = {'uid': uid}
    print(url, params)
    headers = {'Content-type': 'application/json'}
    return requests.get(url, params=params, json=mu, headers=headers)


def http_get_golden_uid_list():
    url = JeMPI_Config.API_SERVER + '/JeMPI/GoldenIdList'
    return requests.get(url)


def http_get_number_of_records():
    url = JeMPI_Config.API_SERVER + '/JeMPI/NumberOfRecords'
    return requests.get(url)


def http_get_golden_record_documents(uid_list):
    url = JeMPI_Config.API_SERVER + '/JeMPI/GoldenRecordDocumentList'
    return requests.get(url, params={"uidList": ",".join(uid_list)})


def http_get_mu():
    url = JeMPI_Config.CONTROLLER_SERVER + '/JeMPI/mu'
    return requests.get(url)


def http_post_link_entity(data_dict):
    url = JeMPI_Config.CONTROLLER_SERVER + '/JeMPI/link_entity'
    return requests.post(url, json=data_dict)


def http_post_link_entity_to_gid(data_dict):
    url = JeMPI_Config.CONTROLLER_SERVER + '/JeMPI/link_entity_to_gid'
    return requests.post(url, json=data_dict)
