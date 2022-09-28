import wx

import JeMPI_DialogBrowse
import JeMPI_DialogImport

_MY_ID_BUTTON_BROWSE_MPI = wx.NewIdRef()
_MY_ID_BUTTON_IMPORT_MPI = wx.NewIdRef()
_MY_ID_BUTTON_EXIT = wx.NewIdRef()

_MY_APP_WINDOW_WIDTH = 750
_MY_APP_WINDOW_HEIGHT = 500


class FrameMain(wx.Frame, wx.Panel):

    def __init__(self, parent, title):
        super(FrameMain, self).__init__(parent, title=title, size=(_MY_APP_WINDOW_WIDTH, _MY_APP_WINDOW_HEIGHT))
        self.Centre()
        image = wx.Image('JeMPI.png', wx.BITMAP_TYPE_ANY)
        image = image.Scale(_MY_APP_WINDOW_WIDTH, _MY_APP_WINDOW_HEIGHT - 95, wx.IMAGE_QUALITY_HIGH)
        import_button = wx.Button(self, wx.ID_CANCEL, label="IMPORT")
        browse_button = wx.Button(self, wx.ID_CANCEL, label="BROWSE")
        close_button = wx.Button(self, wx.ID_OK, label="CLOSE")
        import_button.Bind(wx.EVT_BUTTON, self._on_import_click)
        browse_button.Bind(wx.EVT_BUTTON, self._on_browse_click)
        close_button.Bind(wx.EVT_BUTTON, self._on_close_click)
        image_panel = wx.Panel(self)
        wx.StaticBitmap(image_panel, -1, wx.Bitmap(image))
        v_box = wx.BoxSizer(wx.VERTICAL)
        h_box = wx.BoxSizer(wx.HORIZONTAL)
        h_box.Add(import_button, flag=wx.ALIGN_CENTRE | wx.ALIGN_TOP | wx.BOTTOM, border=10)
        h_box.Add(browse_button, flag=wx.ALIGN_CENTRE | wx.ALIGN_TOP | wx.BOTTOM, border=10)
        h_box.Add(close_button, flag=wx.ALIGN_CENTRE | wx.ALIGN_TOP | wx.BOTTOM, border=10)
        v_box.Add(image_panel, proportion=1, flag=wx.ALIGN_CENTRE | wx.ALIGN_TOP | wx.BOTTOM | wx.EXPAND, border=5)
        v_box.Add(h_box, flag=wx.ALIGN_CENTRE | wx.ALIGN_TOP | wx.ALIGN_BOTTOM)
        self.SetSizer(v_box)
        self.Refresh()

    def _on_import_click(self, event):
        with wx.FileDialog(self, "Open csv file", wildcard="CSV files (*.csv)|*.csv",
                           style=wx.FD_OPEN | wx.FD_FILE_MUST_EXIST) as fileDialog:
            if fileDialog.ShowModal() == wx.ID_CANCEL:
                return
            path_name = fileDialog.GetPath()
            _ = JeMPI_DialogImport.DialogImport(self, path_name).ShowModal()
        event.Skip()

    def _on_browse_click(self, event):
        _ = JeMPI_DialogBrowse.DialogBrowse(self, 'Browse MPI').ShowModal()
        event.Skip()

    def _on_close_click(self, event):
        self.Close()
        event.Skip()
