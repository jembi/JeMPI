import csv
import json
import os
import random
import threading
from datetime import datetime

import wx

import JeMPI_Config
import JeMPI_HTTP

EVT_PROGRESS_ID = wx.NewId()


class ProgressEvent(wx.PyEvent):

    def __init__(self, size, n):
        wx.PyEvent.__init__(self)
        self.SetEventType(EVT_PROGRESS_ID)
        self.n = n
        self.size = size


class DialogImport(wx.ProgressDialog):

    def __init__(self, parent, path_name):
        super(DialogImport, self). \
            __init__(os.path.basename(path_name), 'Importing', maximum=100, parent=None,
                     style=wx.PD_APP_MODAL | wx.PD_ELAPSED_TIME | wx.PD_ESTIMATED_TIME | wx.PD_REMAINING_TIME)
        self._parent = parent
        self._path_name = path_name
        self._worker = None
        self._size = None
        wx.CallAfter(self._on_after_init)

    def _on_after_init(self):
        if not self._worker:
            self.Connect(-1, -1, EVT_PROGRESS_ID, self._on_progress)
            self._worker = Worker(self, self._path_name)
            self._worker.start_import()

    def _on_progress(self, progress):
        if self._size is None:
            self._size = progress.size
            self.SetRange(self._size)
        if progress.n == self._size:
            self.EndModal(wx.ID_OK)
        else:
            self.Update(progress.n)


class Worker(threading.Thread):

    def __init__(self, notify_window, path_name, **kwds):
        threading.Thread.__init__(self, **kwds)
        self._path_name = path_name
        self._size = os.path.getsize(self._path_name)
        self._notify_window = notify_window
        self.setDaemon(True)

    def start_import(self):
        self.start()

    def run(self):
        external_link_count = 0
        with open(self._path_name) as csv_file:
            line_count = sum(1 for _ in csv_file) - 1
            csv_file.seek(0)
            csv_reader = csv.reader(csv_file)
            _ = next(csv_reader)
            n = 0
            now = datetime.now()
            stan_date = now.strftime('%d/%m/%Y %H:%M:%S')
            row_index = 0
            facility_list = ['CLINIC', 'PHARMACY', 'LABORATORY']
            for row in csv_reader:
                source_id_facility = random.choice(facility_list)
                source_id_patient = row[7]
                if source_id_patient is None or source_id_patient == "":
                    source_id_patient = 'ANON'
                source_id = {'facility': source_id_facility, 'patient': source_id_patient}
                row = row[0:1] + ['source_id'] + row[1:]
                doc = {}
                for field in JeMPI_Config.MY_ENTITY_FIELDS:
                    if field.col_idx > 1 and len(row[field.col_idx - 2]) > 0:
                        if field.field_name == 'sourceId':
                            doc[field.field_name] = source_id
                        else:
                            doc[field.field_name] = row[field.col_idx - 2]
                row_index += 1
                body = {'externalLinkRange': {'low': 0.50, 'high': 0.70},
                        'matchThreshold': 0.65,
                        'stan': stan_date + ' ' + str(row_index),
                        'entity': doc}
                response = JeMPI_HTTP.http_post_link_entity(body)
                if response.status_code == 200:
                    json_response = response.json()
                    link_info = json_response['linkInfo']
                    external_link_candidate_list = json_response['externalLinkCandidateList']
                    if link_info is not None:
                        if link_info['score'] < 0.70:
                            print("ERROR")
                            print(link_info)
                    elif external_link_candidate_list is not None:
                        linked = False
                        for external_link_candidate in external_link_candidate_list:
                            golden_record = external_link_candidate['goldenRecord']
                            print(row[0] + ' ' + golden_record['auxId'])
                            link_it = (row[0])[:13] == (golden_record['auxId'])[:13]
                            print(row[0] +
                                  " " +
                                  golden_record['auxId'] + ' ' + golden_record['uid'] +
                                  (" <--- LINK " + str(external_link_count + 1) if link_it else ""))
                            if not linked and link_it:
                                body = {'stan': stan_date + ' ' + str(row_index),
                                        'entity': doc,
                                        'gid': golden_record['uid']}
                                response = JeMPI_HTTP.http_post_link_entity_to_gid(body)
                                linked = response.status_code == 200
                                print('link to old ' + str(response.status_code) + ' ' + json.dumps(response.json()))
                                external_link_count = external_link_count + 1
                        if not linked:
                            body = {'stan': stan_date + ' ' + str(row_index),
                                    'entity': doc,
                                    'gid': None}
                            response = JeMPI_HTTP.http_post_link_entity_to_gid(body)
                            print('link to new ' + str(response.status_code) + ' ' + json.dumps(response.json()))
                wx.PostEvent(self._notify_window, ProgressEvent(line_count, n))
                n += 1
            wx.PostEvent(self._notify_window, ProgressEvent(line_count, n))
