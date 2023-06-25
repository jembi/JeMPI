import { Paper, Typography } from '@mui/material'
import { DataGrid, GridColDef } from '@mui/x-data-grid'
import { FC } from 'react'
import { useAppConfig } from '../../hooks/useAppConfig'
import { GoldenRecord, PatientRecord } from '../../types/PatientRecord'
import DataGridCellInput from './DataGridCellInput'

const RelationshipPanel: FC<{
  data: PatientRecord | GoldenRecord
  isEditable: boolean
  onChange: (newRow: PatientRecord | GoldenRecord) => unknown
}> = ({ data, isEditable, onChange }) => {
  const { getFieldsByGroup } = useAppConfig()
  const columns: GridColDef[] = getFieldsByGroup('relationships').map(
    ({ fieldName, fieldLabel, readOnly, isValid, formatValue }) => {
      return {
        field: fieldName,
        headerName: fieldLabel,
        flex: 1,
        valueFormatter: ({ value }) => formatValue(value),
        sortable: false,
        disableColumnMenu: true,
        editable: !readOnly && isEditable,
        // a Callback used to validate the user's input
        preProcessEditCellProps: ({ props }) => {
          return {
            ...props,
            error: !isValid(props.value)
          }
        },
        renderEditCell: props => <DataGridCellInput {...props} />
      }
    }
  )

  return (
    <Paper sx={{ p: 1 }}>
      <Typography variant="h6">Relationship Contacts</Typography>
      <DataGrid
        getRowId={({ uid }) => uid}
        columns={columns}
        rows={[data]}
        autoHeight={true}
        hideFooter={true}
        processRowUpdate={newRow => onChange(newRow)}
      />
    </Paper>
  )
}

export default RelationshipPanel
