import React from 'react'
import Box from '@mui/material/Box'
import Button from '@mui/material/Button'
import AddIcon from '@mui/icons-material/Add'
import EditIcon from '@mui/icons-material/Edit'
import DeleteIcon from '@mui/icons-material/DeleteOutlined'
import SaveIcon from '@mui/icons-material/Save'
import CancelIcon from '@mui/icons-material/Close'
import {
  GridRowsProp,
  GridRowModesModel,
  GridRowModes,
  DataGrid,
  GridColDef,
  GridToolbarContainer,
  GridActionsCellItem,
  GridEventListener,
  GridRowId,
  GridRowModel,
  GridRowEditStopReasons,
  GridValueSetterParams
} from '@mui/x-data-grid'
import { useSnackbar } from 'notistack'

const randomTraderName = () => {
  return Math.random().toString(36).substring(2, 7)
}

const randomId = () => {
  return Math.random().toString(36).substring(2, 9)
}

const initialRows: GridRowsProp = [
  {
    id: randomId(),
    name: randomTraderName(),
    type: 'string',
    index: 'exact, trigram',
    m: 0.8,
    u: 0.3
  },
  {
    id: randomId(),
    name: randomTraderName(),
    type: 'string',
    index: 'exact, trigram',
    m: 0.8,
    u: 0.3
  },
  {
    id: randomId(),
    name: randomTraderName(),
    type: 'string',
    index: 'exact, trigram',
    m: 0.8,
    u: 0.3
  }
]

interface EditToolbarProps {
  setRows: (newRows: (oldRows: GridRowsProp) => GridRowsProp) => void
  setRowModesModel: (
    newModel: (oldModel: GridRowModesModel) => GridRowModesModel
  ) => void
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

const CommonSettings = () => {
  const [rows, setRows] = React.useState(initialRows)
  const { enqueueSnackbar } = useSnackbar()
  const [rowModesModel, setRowModesModel] = React.useState<GridRowModesModel>(
    {}
  )

  const handleRowEditStop: GridEventListener<'rowEditStop'> = (
    params,
    event
  ) => {
    if (params.reason === GridRowEditStopReasons.rowFocusOut) {
      event.defaultMuiPrevented = true
    }
  }

  const handleEditClick = (id: GridRowId) => () => {
    setRowModesModel({ ...rowModesModel, [id]: { mode: GridRowModes.Edit } })
  }

  const handleSaveClick = (id: GridRowId) => () => {
    setRowModesModel({ ...rowModesModel, [id]: { mode: GridRowModes.View } })
  }

  const handleDeleteClick = (id: GridRowId) => () => {
    setRows(rows.filter(row => row.id !== id))
  }

  const handleCancelClick = (id: GridRowId) => () => {
    setRowModesModel({
      ...rowModesModel,
      [id]: { mode: GridRowModes.View, ignoreModifications: true }
    })

    const editedRow = rows.find(row => row.id === id)
    if (editedRow!.isNew) {
      setRows(rows.filter(row => row.id !== id))
    }
  }

  const processRowUpdate = (newRow: GridRowModel) => {
    const updatedRow = { ...newRow, isNew: false }
    setRows(rows.map(row => (row.id === newRow.id ? updatedRow : row)))
    return updatedRow
  }

  const handleRowModesModelChange = (newRowModesModel: GridRowModesModel) => {
    setRowModesModel(newRowModesModel)
  }

  const columns: GridColDef[] = [
    {
      field: 'name',
      headerName: 'Name',
      width: 180,
      editable: true,
      align: 'center',
      headerAlign: 'center'
    },
    {
      field: 'type',
      headerName: 'Type',
      type: 'string',
      width: 180,
      align: 'center',
      headerAlign: 'center',
      editable: false
    },
    {
      field: 'index',
      headerName: 'Index',
      type: 'string',
      width: 180,
      align: 'center',
      headerAlign: 'center',

      editable: true
    },
    {
      field: 'm',
      headerName: 'Default M',

      width: 180,
      editable: true,
      type: 'number',
      align: 'center',
      headerAlign: 'center',
      valueSetter: (params: GridValueSetterParams) => {
        if (params.value == null || params.value < 0 || params.value > 1) {
          enqueueSnackbar('Value must be between 0 and 1', { variant: 'error' })
          return false
        }
        return true
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
      valueSetter: (params: GridValueSetterParams) => {
        if (params.value == null || params.value < 0 || params.value > 1) {
          enqueueSnackbar('Value must be between 0 and 1', { variant: 'error' })
          return false
        }
        return true
      }
    },
    {
      field: 'actions',
      type: 'actions',
      headerName: 'Actions',
      align: 'center',
      headerAlign: 'center',

      width: 180,
      cellClassName: 'actions',
      getActions: ({ id }) => {
        const isInEditMode = rowModesModel[id]?.mode === GridRowModes.Edit
        if (isInEditMode) {
          return [
            <GridActionsCellItem
              icon={<SaveIcon />}
              label="Save"
              sx={{
                color: 'white'
              }}
              onClick={handleSaveClick(id)}
            />,
            <GridActionsCellItem
              icon={<CancelIcon />}
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
    </Box>
  )
}

export default CommonSettings
