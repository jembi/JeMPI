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
import { useConfiguration } from 'hooks/useUIConfiguration'
import { Configuration } from 'types/Configuration'
import { toSnakeCase } from 'utils/helpers'

const UniqueToInteraction = () => {
  const [rows, setRows] = useState<any[]>([])
  const [rowModesModel, setRowModesModel] = useState<GridRowModesModel>({})
  const { configuration, setConfiguration } = useConfiguration()

  useEffect(() => {
    if (configuration && Array.isArray(configuration.auxInteractionFields)) {
      const rowData = configuration.auxInteractionFields.map(
        (row: any, rowIndex: number) => ({
          id: rowIndex + 1,
          ...row,
          rowIndex
        })
      )
      setRows(rowData)
    }
  }, [configuration])

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
    handleUpdateConfiguration(updatedRow, updatedRow.rowIndex)
    return updatedRow
  }

  const handleUpdateConfiguration = (updatedRow: any, rowIndex: number) => {
    if (!configuration) return

    const updatedConfiguration = getUpdatedConfiguration(
      updatedRow,
      rowIndex,
      configuration
    )

    localStorage.setItem(
      'configuration',
      JSON.stringify(updatedConfiguration)
    )
    setConfiguration(updatedConfiguration)
  }


  const getUpdatedConfiguration = (
    updatedRow: { fieldName: string; csvCol: undefined },
    rowIndex: number,
    currentConfig: Configuration
  ) => {
    const fieldName = toSnakeCase(updatedRow.fieldName)
    const csvCol = updatedRow.csvCol !== undefined ? updatedRow.csvCol : null

    const fieldToUpdate = currentConfig.auxInteractionFields[rowIndex]
    if (fieldToUpdate !== null) {
      fieldToUpdate.fieldName = fieldName
    }

    if (csvCol !== null) {
      fieldToUpdate.source = { ...fieldToUpdate.source, csvCol }
    }

    currentConfig.auxInteractionFields[rowIndex] = fieldToUpdate

    return currentConfig
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
      width: 300,
      editable: true,
      align: 'left',
      headerAlign: 'left',
      valueGetter: params =>
        params.row.fieldName
          .replace(/_/g, ' ')
          .replace(/\b\w/g, (char: string) => char.toUpperCase())
    },
    {
      field: 'fieldType',
      headerName: 'Type',
      type: 'string',
      width: 300,
      align: 'center',
      headerAlign: 'center',
      editable: false
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
      {configuration && (
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

export default UniqueToInteraction
