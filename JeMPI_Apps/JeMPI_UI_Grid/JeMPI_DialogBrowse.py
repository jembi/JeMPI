import wx
import wx.grid as wx_grid

import JeMPI_Config
import JeMPI_DialogGoldenRecord
import JeMPI_DialogLink
import JeMPI_HTTP
import JeMPI_Utils

MY_ID_MENU_LOAD_MPI = wx.NewIdRef()
MY_ID_MENU_EXIT = wx.NewIdRef()

MY_APP_WINDOW_WIDTH = 1500
MY_APP_WINDOW_HEIGHT = 1000
MY_DISPLAY_GRID_ROWS = 32

MY_GRID_MAX_ROT_PAGE_SIZE = 100

MY_GRID_ROT_TEXT_COLOUR = 'white'
MY_GRID_ROT_BACKGROUND_COLOUR = 'blue'
MY_GRID_DOC_TEXT_COLOUR = 'black'
MY_GRID_DOC_BACKGROUND_COLOUR = 'white'


class DialogBrowse(wx.Dialog):

    def __init__(self, parent, title):
        super(DialogBrowse, self).__init__(parent, title=title, size=(MY_APP_WINDOW_WIDTH, MY_APP_WINDOW_HEIGHT),
                                           style=wx.CLOSE_BOX | wx.RESIZE_BORDER | wx.MINIMIZE_BOX)

        self.Centre()
        self._clicked_row = None

        self._golden_uid_list = None
        self._offset = None
        self._golden_records = None

        self._grid = wx_grid.Grid(self)
        self._grid.CreateGrid(MY_DISPLAY_GRID_ROWS, JeMPI_Config.MY_DISPLAY_GRID_COLS)
        self._grid.Bind(wx.EVT_KEY_DOWN, self._on_grid_key_down_event)
        self._grid.Bind(wx.grid.EVT_GRID_CELL_LEFT_DCLICK, self._on_cell_left_dclick)
        self._grid.SetFont(wx.Font(10, wx.FONTFAMILY_SWISS, wx.FONTSTYLE_NORMAL, wx.FONTWEIGHT_NORMAL))
        for field in JeMPI_Config.MY_EXTRA_FIELDS:
            self._grid.SetColLabelValue(field.col_idx, field.col_name)
        for field in JeMPI_Config.MY_ENTITY_FIELDS:
            self._grid.SetColLabelValue(field.col_idx, field.col_name)
        for field in JeMPI_Config.MY_DOCUMENT_FIELDS:
            self._grid.SetColLabelValue(field.col_idx, field.col_name)
        self._grid.Disable()
        self._get_golden_uid_list(True)
        self._get_golden_record_documents()
        self._display_entities(True)
        self._grid.SetFocus()

        close_button = wx.Button(self, label='Close')
        reload_button = wx.Button(self, label='Reload')

        v_box = wx.BoxSizer(wx.VERTICAL)
        h_box = wx.BoxSizer(wx.HORIZONTAL)
        h_box.Add(reload_button, flag=wx.ALIGN_CENTRE | wx.TOP | wx.BOTTOM, border=10)
        h_box.Add(close_button, flag=wx.ALIGN_CENTRE | wx.TOP | wx.BOTTOM, border=10)
        v_box.Add(self._grid, proportion=1, flag=wx.ALL | wx.EXPAND, border=5)
        v_box.Add(h_box, flag=wx.ALIGN_CENTRE | wx.TOP | wx.BOTTOM, border=10)
        self.SetSizer(v_box)
        close_button.Bind(wx.EVT_BUTTON, self._on_close)
        reload_button.Bind(wx.EVT_BUTTON, self._on_reload)

    def _on_reload(self, event):
        self._grid.Disable()
        self._get_golden_uid_list(True)
        self._get_golden_record_documents()
        self._display_entities(True)
        self._grid.SetFocus()
        event.Skip()

    def _on_close(self, _):
        self.EndModal(wx.ID_OK)

    def _on_cell_left_dclick(self, event):
        self._clicked_row = event.GetRow()
        if not self._grid.GetCellValue(self._clicked_row, 0).startswith('0x'):
            rot = {}
            for field in JeMPI_Config.MY_ENTITY_FIELDS:
                rot[field.field_name] = self._grid.GetCellValue(self._clicked_row, field.col_idx)
            res = JeMPI_DialogGoldenRecord.DialogGoldenRecord(self, 'Golden Record', rot).ShowModal()
            if res == wx.ID_OK:
                self._refresh_grid()
        else:
            doc = {}
            for field in JeMPI_Config.MY_ENTITY_FIELDS:
                doc[field.field_name] = self._grid.GetCellValue(self._clicked_row, field.col_idx)
            response = JeMPI_HTTP.http_get_mu()
            response = JeMPI_HTTP.http_get_candidate_list(uid=doc['uid'], mu=response.json())
            if response.status_code == 200:
                candidates = response.json()
                print(candidates)
                golden_id = self._grid.GetCellValue(self._clicked_row, 0)
                res = JeMPI_DialogLink.DialogLink(self, 'Link', doc, golden_id, candidates).ShowModal()
                if res == wx.ID_OK:
                    self._refresh_grid()
        event.Skip()

    def _refresh_grid(self):
        self._grid.Disable()
        self._get_golden_uid_list(False)
        self._get_golden_record_documents()
        self._display_entities(True)
        self._grid.SetFocus()
        wx.CallAfter(JeMPI_Utils.set_grid_cursor, self._grid, self._clicked_row, 0)

    def _on_grid_key_down_event(self, event):
        key_code = event.GetKeyCode()
        row_now = self._grid.GetGridCursorRow()
        if (key_code == wx.WXK_DOWN or key_code == wx.WXK_PAGEDOWN) \
                and row_now + 1 == self._grid.GetNumberRows() \
                and self._offset + MY_GRID_MAX_ROT_PAGE_SIZE < len(self._golden_uid_list):
            self._offset += MY_GRID_MAX_ROT_PAGE_SIZE
            self._get_golden_record_documents()
            self._display_entities(True)
        elif (key_code == wx.WXK_UP or key_code == wx.WXK_PAGEUP) and row_now == 0 and self._offset != 0:
            self._offset -= MY_GRID_MAX_ROT_PAGE_SIZE
            self._get_golden_record_documents()
            self._display_entities(False)
        else:
            event.Skip()

    def _get_entity_count(self):
        count = len(self._golden_records)
        print(count)
        for i in range(len(self._golden_records)):
            documents = self._golden_records[i]['mpiEntityList']
            k = len(documents)
            for _ in range(k):
                count += 1
        return count

    def _get_golden_uid_list(self, reset_offset):
        if reset_offset:
            self._offset = 0
        response = JeMPI_HTTP.http_get_golden_uid_list()
        if response.status_code == 200:
            json_object = response.json()
            self._golden_uid_list = json_object['records']
        else:
            wx.MessageBox('Code : ' + str(response.status_code) + '\n' + 'Body : ' + response.text,
                          'JeMPI Response',
                          wx.OK | wx.ICON_ERROR)

    def _get_golden_record_documents(self):
        golden_uid_list_size = len(self._golden_uid_list)
        if self._offset < golden_uid_list_size:
            grid_rot_page_size = min(MY_GRID_MAX_ROT_PAGE_SIZE, golden_uid_list_size - self._offset)
            uid_list = []
            if self._offset < golden_uid_list_size:
                for i in range(self._offset, self._offset + grid_rot_page_size):
                    uid_list.append(self._golden_uid_list[i])
            response = JeMPI_HTTP.http_get_golden_record_documents(uid_list)
            if response.status_code == 200:
                json_object = response.json()
                self._golden_records = json_object['goldenRecords']
            else:
                wx.MessageBox('Code : ' + str(response.status_code) + '\n' + 'Body : ' + response.text,
                              'JeMPI Response',
                              wx.OK | wx.ICON_ERROR)

    def _set_rot_cell(self, row, col, value, align):
        JeMPI_Utils.set_cell(self._grid, row, col, "{}".format(value), align, MY_GRID_ROT_TEXT_COLOUR,
                             MY_GRID_ROT_BACKGROUND_COLOUR)

    def _set_doc_cell(self, row, col, value, align):
        JeMPI_Utils.set_cell(self._grid, row, col, "{}".format(value), align, MY_GRID_DOC_TEXT_COLOUR,
                             MY_GRID_DOC_BACKGROUND_COLOUR)

    def _display_entities(self, place_cursor_at_top):
        n_entities = self._get_entity_count()

        self._grid.DeleteRows(numRows=self._grid.GetNumberRows())
        self._grid.AppendRows(n_entities)

        rows = 0
        for i in range(len(self._golden_records)):
            rot = self._golden_records[i]['customGoldenRecord']
            for field in JeMPI_Config.MY_EXTRA_FIELDS:
                self._set_rot_cell(rows, field.col_idx, self._offset + i + 1, field.align)
            for field in JeMPI_Config.MY_ENTITY_FIELDS:
                self._set_rot_cell(rows, field.col_idx, rot[field.field_name], field.align)
            rows += 1
            documents = self._golden_records[i]['mpiEntityList']
            k = len(documents)
            for j in range(k):
                document = documents[j]
                entity = document['entity']
                self._set_doc_cell(rows, 0, rot['uid'], wx.ALIGN_CENTRE)
                for field in JeMPI_Config.MY_ENTITY_FIELDS:
                    self._set_doc_cell(rows, field.col_idx, entity[field.field_name], field.align)
                for field in JeMPI_Config.MY_DOCUMENT_FIELDS:
                    self._set_doc_cell(rows, field.col_idx, document[field.field_name], field.align)
                rows += 1

        self._grid.AutoSizeColumns()
        wx.CallAfter(JeMPI_Utils.set_grid_cursor, self._grid, 0 if place_cursor_at_top else rows - 1, 0)
        self._grid.Refresh()
        self._grid.Enable()
        self.Refresh()
