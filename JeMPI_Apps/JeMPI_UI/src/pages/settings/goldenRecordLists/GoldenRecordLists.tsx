import Box from '@mui/material/Box'
import {
  DataGrid,
  GridColDef,
  GridEventListener,
  GridRowEditStopReasons,
  GridRowId,
  GridRowModel,
  GridRowModes,
  GridRowModesModel,
  GridActionsCellItem
} from '@mui/x-data-grid'
import EditIcon from '@mui/icons-material/Edit'
import SaveIcon from '@mui/icons-material/Save'
import CancelIcon from '@mui/icons-material/Close'
import { useEffect, useState } from 'react'
import { EditToolbar } from 'components/shared/EditToolBar'
import { formatNodeName, toUpperCase } from 'utils/helpers'


const GoldenRecordLists = ({goldenRecordList }: { goldenRecordList: any }) => {
  const [rows, setRows] = useState(goldenRecordList)
  const [rowModesModel, setRowModesModel] = useState<GridRowModesModel>({})

  useEffect(() => {
    const rowsWithIds = goldenRecordList.map((row: any, index: number) => ({
      ...row,
      id: index.toString() 
    }))
    setRows(rowsWithIds)
  }, [goldenRecordList])

  const handleEditClick = (id: GridRowId) => () => {
    setRowModesModel({ ...rowModesModel, [id]: { mode: GridRowModes.Edit } })
  }

  const handleSaveClick = (id: GridRowId) => () => {
    setRowModesModel({ ...rowModesModel, [id]: { mode: GridRowModes.View } })
  }

  const handleDeleteClick = (id: any) => () => {
    setRows(rows?.filter((row: { id: any }) => row.id !== id))
  }

  const handleCancelClick = (id: GridRowId) => () => {
    setRowModesModel({
      ...rowModesModel,
      [id]: { mode: GridRowModes.View, ignoreModifications: true }
    })

    const editedRow = rows.find((row: { id: GridRowId }) => row.id === id)
    if (editedRow!.isNew) {
      setRows(rows.filter((row: { id: GridRowId }) => row.id !== id))
    }
  }

  const processRowUpdate = (newRow: GridRowModel) => {
    const { isNew, ...updatedRow } = newRow
    setRows(
      rows.map((row: { id: any }) => (row.id === newRow.id ? updatedRow : row))
    )
    return updatedRow
  }

  const handleRowModesModelChange = (newRowModesModel: GridRowModesModel) => {
    setRowModesModel(newRowModesModel)
  }

  const handleRowEditStop: GridEventListener<'rowEditStop'> = (
    params,
    event
  ) => {
    if (params.reason === GridRowEditStopReasons.rowFocusOut) {
      event.defaultMuiPrevented = true
    }
  }
  const rowsWithIds = goldenRecordList.flatMap((node: { fields: any[]; nodeName: string }) => {
    return node.fields.map((field, index) => {
      return {
        id: node.nodeName + '_' + index,
        nodeName: node.nodeName,
        fieldName: field.fieldName,
        fieldType: field.fieldType,
        csvCol: field.csvCol,
      };
    });
  });

  const columns: GridColDef[] = [
    {
      field: 'Name',
      headerName: 'List Name',
      width: 300,
      editable: true,
      align: 'left',
      headerAlign: 'left',
      valueGetter: params => { 
        return formatNodeName(params.row.nodeName);}
    },
    {
      field: 'fieldName',
      headerName: 'Property Name',
      type: 'string',
      width: 300,
      align: 'center',
      headerAlign: 'center',
      editable: false,
      valueGetter: params => toUpperCase(params.row.fieldName)
    },
    {
      field: 'fieldType',
      headerName: 'Type',
      type: 'string',
      width: 300,
      align: 'center',
      headerAlign: 'center',
      editable: false,
      valueGetter: params => params.row.fieldType
    },
    {
      field: 'actions',
      type: 'actions',
      headerName: 'Actions',
      align: 'center',
      headerAlign: 'center',
      width: 300,
      cellClassName: 'actions',
      getActions: ({ id }) => {
        const isInEditMode = rowModesModel[id]?.mode === GridRowModes.Edit
        if (isInEditMode) {
          return [
            <GridActionsCellItem
              icon={<SaveIcon />}
              id="save-button"
              label="Save"
              sx={{
                color: 'white'
              }}
              onClick={handleSaveClick(id)}
            />,
            <GridActionsCellItem
              icon={<CancelIcon />}
              id="cancel-button"
              label="Cancel"
              className="textPrimary"
              onClick={handleCancelClick(id)}
              color="inherit"
            />
          ]
        }

        return [
          <GridActionsCellItem
            icon={<EditIcon />}
            id="edit-button"
            label="Edit"
            className="textPrimary"
            onClick={handleEditClick(id)}
            color="inherit"
          />
        ]
      }
    }
  ]

  return (
    <Box
      sx={{
        height: 500,
        width: '100%',
        '& .actions': {
          color: '#fff'
        },
        '& .textPrimary': {
          color: '#fff'
        }
      }}
    >
      {goldenRecordList && (
        <DataGrid
          rows={rowsWithIds}
          columns={columns}
          editMode="row"
          rowModesModel={rowModesModel}
          onRowModesModelChange={handleRowModesModelChange}
          onRowEditStop={handleRowEditStop}
          processRowUpdate={processRowUpdate}
          slots={{
            toolbar: EditToolbar
          }}
          slotProps={{
            toolbar: { setRows, setRowModesModel }
          }}
        />
      )}
    </Box>
  )
}

export default GoldenRecordLists
