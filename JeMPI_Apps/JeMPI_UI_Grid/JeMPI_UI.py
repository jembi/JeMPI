import wx

import JeMPI_FrameMain


def main():
    app = wx.App()
    top = JeMPI_FrameMain.FrameMain(None, 'JeMPI UI')
    top.Show()
    app.MainLoop()


main()
