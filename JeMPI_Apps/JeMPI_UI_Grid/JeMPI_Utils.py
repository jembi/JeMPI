import wx


def on_cancel_pressed(event):
    event.Skip()


def set_grid_cursor(grid, row, col):
    grid.GoToCell(row, col)
    pixels_per_unit = grid.GetScrollPixelsPerUnit()
    cell_coords = grid.CellToRect(row, col)
    y = cell_coords.y / pixels_per_unit[1]
    scroll_page_size = grid.GetScrollPageSize(wx.VERTICAL)
    scroll_coords = (0, y - scroll_page_size / 2)
    grid.Scroll(scroll_coords)


def set_cell(grid, row, col, value, align, text_colour, background_colour):
    grid.SetCellValue(row, col, value)
    grid.SetCellTextColour(row, col, text_colour)
    grid.SetCellBackgroundColour(row, col, background_colour)
    grid.SetCellAlignment(row, col, align, wx.ALIGN_CENTRE)
    grid.SetReadOnly(row, col)
