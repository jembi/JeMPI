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
  GridActionsCellItem,
  GridRowsProp,
  GridToolbarContainer
} from '@mui/x-data-grid'
import EditIcon from '@mui/icons-material/Edit'
import SaveIcon from '@mui/icons-material/Save'
import CancelIcon from '@mui/icons-material/Close'
import { useSnackbar } from 'notistack'
import { useEffect, useState } from 'react'
import { randomId } from 'utils/helpers'

interface EditToolbarProps {
  setRows: (newRows: (oldRows: GridRowsProp) => GridRowsProp) => void
  setRowModesModel: (
    newModel: (oldModel: GridRowModesModel) => GridRowModesModel
  ) => void
}

const CommonSettings = ({ demographicData }: { demographicData: any }) => {
  4

  const [rows, setRows] = useState(demographicData)
  const [rowModesModel, setRowModesModel] = useState<GridRowModesModel>({})

  useEffect(() => {
    const rowsWithIds = demographicData.map((row: any, index: number) => ({
      ...row,
      id: index.toString() 
    }))
    setRows(rowsWithIds)
  }, [demographicData])

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

  const columns: GridColDef[] = [
    {
      field: 'fieldName',
      headerName: 'Name',
      width: 180,
      editable: true,
      align: 'center',
      headerAlign: 'center',
      valueGetter: params =>
        params.row.fieldName
          .replace(/_/g, ' ')
          .replace(/\b\w/g, (char: string) => char.toUpperCase())
    },
    {
      field: 'fieldType',
      headerName: 'Type',
      type: 'string',
      width: 180,
      align: 'center',
      headerAlign: 'center',
      editable: false
    },

    {
      field: 'indexGoldenRecord',
      headerName: 'Index',
      type: 'string',
      width: 180,
      align: 'center',
      headerAlign: 'center',
      editable: true,
      valueGetter: params => {
        const indexGoldenRecord = params.row.indexGoldenRecord
        if (indexGoldenRecord) {
          return indexGoldenRecord
            .replace(/@index\(|\)(?=, trigram|$)/g, ' ')
            .replace(/,/g, ', ')
        }
        return ''
      }
    },
    {
      field: 'm',
      headerName: 'Default M',
      width: 180,
      editable: true,
      type: 'number',
      align: 'center',
      headerAlign: 'center',
      valueGetter: params => {
        const linkMetaData = params.row.linkMetaData
        if (linkMetaData) {
          return linkMetaData.m.toFixed(1)
        }
        return ''
      }
    },
    {
      field: 'u',
      headerName: 'Default U',
      width: 180,
      editable: true,
      type: 'number',
      align: 'center',
      headerAlign: 'center',
      valueGetter: params => {
        const linkMetaData = params.row.linkMetaData
        if (linkMetaData) {
          return linkMetaData.u.toFixed(2)
        }
        return ''
      }
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
        width: '100%'
      }}
    >
      {demographicData && (
        <DataGrid
          rows={rows}
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

function EditToolbar(props: EditToolbarProps) {
  const { setRows, setRowModesModel } = props

  const handleClick = () => {
    const id = randomId()
    setRows(oldRows => [...oldRows, { id, name: '', age: '', isNew: true }])
    setRowModesModel(oldModel => ({
      ...oldModel,
      [id]: { mode: GridRowModes.Edit, fieldToFocus: 'name' }
    }))
  }

  return <GridToolbarContainer></GridToolbarContainer>
}

export default CommonSettings
