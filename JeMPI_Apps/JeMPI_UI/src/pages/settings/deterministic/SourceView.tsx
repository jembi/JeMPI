import React, { useState, useEffect } from 'react'
import Box from '@mui/material/Box'
import {
  DataGrid,
  GridColDef,
  GridEventListener,
  GridRowEditStopReasons,
  GridRowId,
  GridRowModel,
  GridRowModesModel,
  GridActionsCellItem
} from '@mui/x-data-grid'
import EditIcon from '@mui/icons-material/Edit'
import AddIcon from '@mui/icons-material/Add'
import IconButton from '@mui/material/IconButton'

export interface RowData {
  id: number
  ruleNumber: number
  ruleText: string
}

interface SourceViewProps {
  data: RowData[]
  onEditRow: (row: RowData) => void
  onAddUndefinedRule: () => void
  hasUndefinedRule: boolean
}

const SourceView: React.FC<SourceViewProps> = ({
  data,
  onEditRow,
  onAddUndefinedRule,
  hasUndefinedRule
}) => {
  const [rows, setRows] = useState<RowData[]>([])
  const [rowModesModel, setRowModesModel] = useState<GridRowModesModel>({})

  useEffect(() => {
    setRows(data)
  }, [data])

  const handleEditClick = (id: GridRowId) => () => {
    const row = rows.find(row => row.id === id)
    if (row) {
      onEditRow(row)
    }
  }

  const processRowUpdate = (newRow: GridRowModel) => {
    const { id, ...updatedRow } = newRow
    setRows(rows.map(row => (row.id === id ? (updatedRow as RowData) : row)))
    return updatedRow as RowData
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
      field: 'ruleNumber',
      headerName: 'Rule Number',
      width: 150,
      align: 'left',
      headerAlign: 'center',
      editable: false
    },
    {
      field: 'ruleText',
      headerName: 'Rule',
      width: 500,
      align: 'left',
      headerAlign: 'left',
      editable: true
    },
    {
      field: 'actions',
      type: 'actions',
      headerName: 'Actions',
      width: 150,
      align: 'center',
      headerAlign: 'center',
      cellClassName: 'actions',
      getActions: ({ id }) => {
        return [
          <GridActionsCellItem
            icon={<EditIcon />}
            key={`action-item-${id}`}
            id={`edit-button-${id}`}
            data-testid={`edit-button-${id}`}
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
        },
        '& .MuiDataGrid-columnHeaders': {
          display: 'none'
        },
        '& .MuiDataGrid-root': {
          borderLeft: 'none',
          borderRight: 'none',
          borderBottom: 'none'
        }
      }}
    >
      {hasUndefinedRule ? (
        <Box
          sx={{
            width: '100%',
            display: 'flex',
            flexDirection: 'column',
            gap: 2,
            justifyContent: 'center',
            alignItems: 'center'
          }}
        >
          <IconButton key="add-undefined-rule" onClick={onAddUndefinedRule}>
            <AddIcon />
          </IconButton>
        </Box>
      ) : (
        <DataGrid
          rows={rows}
          columns={columns}
          editMode="row"
          rowModesModel={rowModesModel}
          onRowModesModelChange={handleRowModesModelChange}
          onRowEditStop={handleRowEditStop}
          processRowUpdate={processRowUpdate}
          hideFooter
          disableColumnSelector
        />
      )}
    </Box>
  )
}

export default SourceView
