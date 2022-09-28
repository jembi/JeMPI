import wx

import JeMPI_Config
import JeMPI_HTTP
import JeMPI_Utils


class DialogGoldenRecord(wx.Dialog):

    def __init__(self, parent, title, rot):
        self._rot = rot
        super(DialogGoldenRecord, self).__init__(parent, title=title,
                                                 size=(500, (len(JeMPI_Config.MY_ENTITY_FIELDS) + 1) * 2 * 30))
        panel = wx.Panel(self)
        self._sizer = wx.BoxSizer(wx.VERTICAL)
        self._text_ctrl_list = []
        for field in JeMPI_Config.MY_ENTITY_FIELDS:
            text1 = wx.StaticText(panel, label=field.col_name, style=wx.ST_NO_AUTORESIZE)
            text2 = wx.TextCtrl(panel, value=rot[field.field_name])
            self._text_ctrl_list.append(text2)
            sub_sizer = wx.BoxSizer(wx.HORIZONTAL)
            sub_sizer.Add(text1, proportion=1, flag=wx.ALL, border=5)
            sub_sizer.Add(text2, proportion=2, flag=wx.ALL, border=5)
            self._sizer.Add(sub_sizer, flag=wx.TOP | wx.LEFT | wx.EXPAND, border=10)

        button_cancel = wx.Button(panel, wx.ID_CANCEL, label='CANCEL')
        button_save = wx.Button(panel, wx.ID_OK, label='SAVE')
        button_save.Bind(wx.EVT_BUTTON, self._on_ok_pressed)
        button_cancel.Bind(wx.EVT_BUTTON, JeMPI_Utils.on_cancel_pressed)
        btn_sizer = wx.BoxSizer(wx.HORIZONTAL)
        btn_sizer.Add(button_cancel, proportion=1, flag=wx.TOP | wx.CENTRE, border=5)
        btn_sizer.Add(button_save, proportion=1, flag=wx.TOP | wx.CENTRE, border=5)
        self._sizer.Add(btn_sizer, flag=wx.TOP | wx.ALIGN_CENTRE | wx.ALL, border=10)
        panel.SetSizer(self._sizer)
        self.Centre()

    def _on_ok_pressed(self, event):
        for field in JeMPI_Config.MY_ENTITY_FIELDS:
            k = field.col_idx - 1
            if self._rot[field.field_name] != self._text_ctrl_list[k].GetValue():
                response = JeMPI_HTTP.http_patch_predicate(self._rot['uid'], field.predicate,
                                                           self._text_ctrl_list[k].GetValue())
                wx.MessageBox('Code : ' + str(response.status_code) + '\n' + 'Body : ' + response.text,
                              'JeMPI Response',
                              wx.OK | wx.ICON_INFORMATION)
        event.Skip()
