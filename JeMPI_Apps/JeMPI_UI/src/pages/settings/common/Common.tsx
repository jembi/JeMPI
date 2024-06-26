import { useCallback, useEffect, useState } from 'react'
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
import { EditToolbar } from 'components/shared/EditToolBar'
import { processIndex, transformFieldName } from 'utils/helpers'
import { useConfiguration } from 'hooks/useUIConfiguration'
import { Configuration, LinkMetaData } from 'types/Configuration'

const toSnakeCase = (str: string) => {
  return str
    .trim()
    .replace(/\s+/g, '_')
    .replace(/([a-z])([A-Z])/g, '$1_$2')
    .toLowerCase()
}

const CommonSettings = ({ demographicData }: { demographicData: any }) => {
  const { configuration, setConfiguration } = useConfiguration()
  const [rowModesModel, setRowModesModel] = useState<GridRowModesModel>({})
  const [rows, setRows] = useState<any[]>(() =>
    demographicData.map((row: any, rowIndex: number) => {
      return { id: rowIndex + 1, ...row, rowIndex }
    })
  )

  const handleEditClick = useCallback((id: GridRowId) => {
    setRowModesModel(prevRowModesModel => ({
      ...prevRowModesModel,
      [id]: { mode: GridRowModes.Edit }
    }))
  }, [])

  const handleSaveClick = (id: GridRowId) => {
    const updatedRow = rows.find(row => row.id === id)
    if (updatedRow) {
      const newRowModesModel = {
        ...rowModesModel,
        [id]: { mode: GridRowModes.View }
      }
      setRowModesModel(newRowModesModel)
      handleUpdateConfiguration(updatedRow, updatedRow.rowIndex)
    }
  }

  const handleUpdateConfiguration = (updatedRow: any, rowIndex: number) => {
    setConfiguration(previousConfiguration => {
      if (!previousConfiguration) return previousConfiguration
      const updatedConfiguration = getUpdatedConfiguration(
        updatedRow,
        rowIndex,
        previousConfiguration
      )
      localStorage.setItem(
        'configuration',
        JSON.stringify(updatedConfiguration)
      )
      return updatedConfiguration
    })
  }

  const getUpdatedConfiguration = (
    updatedRow: any,
    rowIndex: number,
    currentConfiguration: Configuration
  ): Configuration => {
    const fieldName = toSnakeCase(updatedRow.fieldName)

    const fieldToUpdate = currentConfiguration.demographicFields[rowIndex]

    fieldToUpdate.fieldName = fieldName

    if (updatedRow?.indexGoldenRecord) {
      fieldToUpdate.indexGoldenRecord = `@index(${updatedRow.indexGoldenRecord})`
    }

    if (updatedRow?.m) {
      fieldToUpdate.linkMetaData = {
        ...fieldToUpdate.linkMetaData,
        m: Number(updatedRow.m)
      } as LinkMetaData
    }

    if (updatedRow?.u) {
      fieldToUpdate.linkMetaData = {
        ...fieldToUpdate.linkMetaData,
        u: Number(updatedRow.u)
      } as LinkMetaData
    }
    currentConfiguration.demographicFields[rowIndex] = fieldToUpdate

    return currentConfiguration
  }

  const handleCancelClick = (id: GridRowId) => () => {
    setRowModesModel(prevRowModesModel => {
      const newRowModesModel = { ...prevRowModesModel }
      delete newRowModesModel[id]
      return newRowModesModel
    })
  }

  const handleRowEditStop: GridEventListener<'rowEditStop'> = ({ reason }) =>
    reason === GridRowEditStopReasons.rowFocusOut

  const handleRowModesModelChange = (newRowModesModel: GridRowModesModel) => {
    setRowModesModel(newRowModesModel)
  }

  const handleProcessRowUpdate = (
    newRow: GridRowModel,
    oldRow: GridRowModel
  ) => {
    setRows(
      rows.map(row =>
        row.id === oldRow.id ? { ...newRow, rowIndex: oldRow.rowIndex } : row
      )
    )
    return newRow
  }

  const columns: GridColDef[] = [
    {
      field: 'fieldName',
      headerName: 'Name',
      width: 180,
      editable: true,
      align: 'left',
      headerAlign: 'left',
      valueGetter: params => transformFieldName(params)
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
        return processIndex(indexGoldenRecord)
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
        if (linkMetaData && typeof linkMetaData.m === 'number') {
          return linkMetaData.m.toFixed(1)
        }
        return
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
        if (linkMetaData && typeof linkMetaData.u === 'number') {
          return linkMetaData.u.toFixed(2)
        }
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
        const rowMode = rowModesModel[id]
        if (rowMode && rowMode.mode === GridRowModes.Edit) {
          return [
            <GridActionsCellItem
              icon={<SaveIcon />}
              id="save-button"
              label="Save"
              sx={{
                color: 'white'
              }}
              onClick={() => handleSaveClick(id)}
            />,
            <GridActionsCellItem
              icon={<CancelIcon />}
              id="cancel-button"
              label="Cancel"
              className="textPrimary"
              onClick={() => handleCancelClick(id)}
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
            onClick={() => handleEditClick(id)}
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
          processRowUpdate={handleProcessRowUpdate}
          onRowEditStop={handleRowEditStop}
          components={{
            Toolbar: EditToolbar
          }}
          componentsProps={{
            toolbar: { setRows, setRowModesModel }
          }}
        />
      )}
    </Box>
  )
}

export default CommonSettings
