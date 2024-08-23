import { useEffect, useState } from 'react'
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
import { processIndex, toSnakeCase, transformFieldName } from 'utils/helpers'
import { useConfiguration } from 'hooks/useUIConfiguration'
import { Configuration, LinkMetaData } from 'types/Configuration'
import { RowData } from '../deterministic/SourceView'
import { Switch } from '@mui/material'

const CommonSettings = () => {
  const [rows, setRows] = useState<any>([])
  const { configuration, setConfiguration } = useConfiguration()
  const [rowModesModel, setRowModesModel] = useState<GridRowModesModel>({})


  useEffect(() => {
    if (configuration && configuration.demographicFields) {
      const rowData = configuration?.demographicFields.map(
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

  const handleUpdateConfiguration = (updatedRow: any, rowIndex: number) => {
    if (!configuration) return
    const updatedConfiguration = getUpdatedConfiguration(
      updatedRow,
      rowIndex,
      configuration
    )
    localStorage.setItem('configuration', JSON.stringify(updatedConfiguration))
    setConfiguration(updatedConfiguration)
    setRows((prevRows: any) =>
      prevRows.map((row: any) =>
        row.id === updatedRow.id ? { ...updatedRow } : row
      )
    )
  }

  const getUpdatedConfiguration = (
    updatedRow: any,
    rowIndex: number,
    currentConfiguration: Configuration
  ): Configuration => {
    const newConfiguration = { ...currentConfiguration } 
    const fieldName = toSnakeCase(updatedRow.fieldName)
    if (!newConfiguration.demographicFields) {
      return currentConfiguration
    }

    const fieldToUpdate = { ...newConfiguration.demographicFields[rowIndex] }

    if (!fieldToUpdate) {
      return currentConfiguration
    }

    fieldToUpdate.fieldName = fieldName

    if (updatedRow?.indexGoldenRecord) {
      fieldToUpdate.indexGoldenRecord = `@index(${updatedRow.indexGoldenRecord.replace(
        ' ',
        ''
      )})`
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

    newConfiguration.demographicFields[rowIndex] = fieldToUpdate

    return newConfiguration
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

  const processRowUpdate = (newRow: GridRowModel) => {
    const { id, ...updatedRow } = newRow
    const updatedRows = rows.map((row: { id: any }) =>
      row.id === id ? ({ ...updatedRow, id } as RowData) : row
    )
    setRows(updatedRows)
    handleUpdateConfiguration(updatedRow, updatedRow.rowIndex)
    return { ...updatedRow, id } as RowData
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
          return linkMetaData.m.toFixed(7)
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
          return linkMetaData.u.toFixed(7)
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
    },
    {
      field: 'disable',
      headerName: '',
      width: 90,
      align: 'center',
      headerAlign: 'center',
      renderCell: (params) => {
        const label = { inputProps: { 'aria-label': 'Switch demo' } };
        return <Switch {...label} checked={params.value ?? true} />;
      },
    }
  ]

  return (
    <Box
      sx={{
        height: 500,
        width: '100%'
      }}
    >
      {configuration && (
        <DataGrid
          rows={rows}
          columns={columns}
          editMode="row"
          rowModesModel={rowModesModel}
          onRowModesModelChange={handleRowModesModelChange}
          processRowUpdate={processRowUpdate}
          onRowEditStop={handleRowEditStop}
          getRowId={row => row.id}
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

export default CommonSettings
