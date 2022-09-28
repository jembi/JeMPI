import wx
import wx.lib.mixins.listctrl as listmix
from wx.lib.agw import ultimatelistctrl as ULC

import JeMPI_Config
import JeMPI_HTTP

_MY_APP_WINDOW_WIDTH = 1600
_MY_APP_WINDOW_HEIGHT = 800


class DialogLink(wx.Dialog, listmix.ColumnSorterMixin):

    def __init__(self, parent, title, doc, golden_id, candidates):
        super(DialogLink, self).__init__(parent, title=title, size=(_MY_APP_WINDOW_WIDTH, _MY_APP_WINDOW_HEIGHT),
                                         style=wx.CLOSE_BOX | wx.RESIZE_BORDER | wx.MINIMIZE_BOX)
        self._doc = doc
        self._golden_id = golden_id
        self._selected_index = None

        print(doc)
        print(golden_id)
        print(candidates)
        print(len(candidates))

        rows = []
        for i in range(len(candidates)):
            candidate = candidates[i]['goldenRecord']
            print(candidate)
            my_list = []
            for _ in JeMPI_Config.MY_EXTRA_FIELDS:
                my_list.append(str(i + 1))
            for field in JeMPI_Config.MY_ENTITY_FIELDS:
                my_list.append(candidate[field.field_name])
            my_list.append(str(candidates[i]['score']))
            rows.append(tuple(my_list))
        self._list_ctrl = ULC.UltimateListCtrl(self, -1, agwStyle=ULC.ULC_REPORT | wx.LC_VRULES | wx.LC_HRULES)
        self._list_ctrl.SetFont(wx.Font(10, wx.FONTFAMILY_SWISS, wx.FONTSTYLE_NORMAL, wx.FONTWEIGHT_NORMAL))
        for field in JeMPI_Config.MY_EXTRA_FIELDS:
            self._list_ctrl.InsertColumn(field.col_idx, field.col_name)
        for field in JeMPI_Config.MY_ENTITY_FIELDS:
            self._list_ctrl.InsertColumn(field.col_idx, field.col_name)
        for field in JeMPI_Config.MY_DOCUMENT_FIELDS:
            self._list_ctrl.InsertColumn(field.col_idx, field.col_name)
        for row_index, data in enumerate(rows):
            for col_index, col_data in enumerate(data):
                if col_index == 0:
                    self._list_ctrl.InsertStringItem(row_index, col_data)
                else:
                    self._list_ctrl.SetStringItem(row_index, col_index,  "{}".format(col_data))
            self._list_ctrl.SetItemData(row_index, data)
        rows_ = map("{}".format, rows)
        self.itemDataMap = {data: data for data in rows_}
        for c in range(JeMPI_Config.MY_DISPLAY_GRID_COLS):
            self._list_ctrl.SetColumnWidth(c, -1)
        listmix.ColumnSorterMixin.__init__(self, JeMPI_Config.MY_DISPLAY_GRID_COLS)
        self.Bind(wx.EVT_LIST_COL_CLICK, self._on_col_click, self._list_ctrl)
        self._list_ctrl.Bind(wx.EVT_LIST_ITEM_SELECTED, self._on_item_selected)
        self._list_ctrl.Refresh()
        self._list_ctrl.Enable()
        cancel_button = wx.Button(self, wx.ID_CANCEL, label="CANCEL")
        new_master_button = wx.Button(self, wx.ID_OK, label="NEW MASTER")
        link_button = wx.Button(self, wx.ID_OK, label="LINK")
        v_box = wx.BoxSizer(wx.VERTICAL)
        v_box_doc = wx.BoxSizer(wx.VERTICAL)
        h_box = wx.BoxSizer(wx.HORIZONTAL)
        h_box_buttons = wx.BoxSizer(wx.HORIZONTAL)

        self._rot_text = []
        k = 0
        for field in JeMPI_Config.MY_ENTITY_FIELDS:
            text1 = wx.StaticText(self, label=field.col_name, style=wx.ST_NO_AUTORESIZE)
            text2 = wx.TextCtrl(self, value=doc[field.field_name], style=wx.TE_READONLY)
            self._rot_text.append(
                wx.TextCtrl(self, value=self._list_ctrl.GetItem(0, field.col_idx).GetText(), style=wx.TE_READONLY))
            sub_sizer = wx.BoxSizer(wx.HORIZONTAL)
            sub_sizer.Add(text1, proportion=1, flag=wx.ALL, border=5)
            sub_sizer.Add(text2, proportion=2, flag=wx.ALL, border=5)
            sub_sizer.Add(self._rot_text[k], proportion=2, flag=wx.ALL, border=5)
            v_box_doc.Add(sub_sizer, flag=wx.TOP | wx.LEFT | wx.EXPAND, border=10)
            k += 1

        h_box.Add(self._list_ctrl, proportion=2, flag=wx.ALL | wx.EXPAND, border=5)
        h_box.Add(v_box_doc, proportion=1, flag=wx.ALL | wx.EXPAND, border=5)
        v_box.Add(h_box, proportion=1, flag=wx.ALL | wx.EXPAND, border=5)

        h_box_buttons.Add(cancel_button, flag=wx.ALIGN_CENTRE | wx.TOP | wx.BOTTOM, border=10)
        h_box_buttons.Add(new_master_button, flag=wx.ALIGN_CENTRE | wx.TOP | wx.BOTTOM, border=10)
        h_box_buttons.Add(link_button, flag=wx.ALIGN_CENTRE | wx.TOP | wx.BOTTOM, border=10)
        v_box.Add(h_box_buttons, flag=wx.ALIGN_CENTRE | wx.TOP | wx.BOTTOM, border=10)
        self.SetSizer(v_box)
        cancel_button.Bind(wx.EVT_BUTTON, self._on_close)
        new_master_button.Bind(wx.EVT_BUTTON, self._on_new_master_click)
        link_button.Bind(wx.EVT_BUTTON, self._on_link_click)
        self.Refresh()

    def _on_close(self, _):
        self.EndModal(wx.ID_CANCEL)

    def GetListCtrl(self):
        return self._list_ctrl

    def _on_col_click(self, event):
        pass

    def _on_item_selected(self, event):
        self._selected_index = event.GetIndex()
        k = 0
        for field in JeMPI_Config.MY_ENTITY_FIELDS:
            self._rot_text[k].SetValue(self._list_ctrl.GetItem(self._selected_index, field.col_idx).GetText())
            k += 1

    def _on_new_master_click(self, event):
        response = JeMPI_HTTP.http_patch_unlink(golden_id=self._golden_id, doc_id=self._doc['uid'])
        wx.MessageBox('Code : ' + str(response.status_code) + '\n' + 'Body : ' + response.text, 'JeMPI Response',
                      wx.OK | wx.ICON_INFORMATION)
        event.Skip()

    def _on_link_click(self, event):
        item = self._list_ctrl.GetItem(self._selected_index, 1)
        new_golden_id = item.GetText()
        response = JeMPI_HTTP.http_patch_link(self._golden_id, new_golden_id, self._doc['uid'], 2.0)
        wx.MessageBox('Code : ' + str(response.status_code) + '\n' + 'Body : ' + response.text, 'JeMPI Response',
                      wx.OK | wx.ICON_INFORMATION)
        self.EndModal(wx.ID_OK)
        event.Skip()
