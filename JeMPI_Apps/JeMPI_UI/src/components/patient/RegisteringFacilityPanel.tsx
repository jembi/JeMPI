import { Paper, Typography } from '@mui/material'
import { DataGrid } from '@mui/x-data-grid'
import { FC } from 'react'
import { SourceId } from '../../types/PatientRecord'

const RegisteringFacilityPanel: FC<{
  data: SourceId[]
}> = ({ data }) => {
  const columns = [
    {
      field: 'patient',
      headerName: 'Site code',
      flex: 1,
      sortable: false,
      disableColumnMenu: true
    },
    {
      field: 'facility',
      headerName: 'Facility Name',
      flex: 1,
      sortable: false,
      disableColumnMenu: true
    }
  ]

  return (
    <Paper sx={{ p: 1 }}>
      <Typography variant="h6">Registering Facility</Typography>
      <DataGrid
        getRowId={({ uid }) => uid}
        columns={columns}
        rows={data}
        autoHeight={true}
        hideFooter={true}
      />
    </Paper>
  )
}

export default RegisteringFacilityPanel
