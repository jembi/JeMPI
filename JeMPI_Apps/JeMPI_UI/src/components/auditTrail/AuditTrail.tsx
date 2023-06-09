import { Person, Warning } from '@mui/icons-material'
import SearchIcon from '@mui/icons-material/Search'
import { Card, Divider, Grid, Link, Typography } from '@mui/material'
import { Container } from '@mui/system'
import { GridColDef } from '@mui/x-data-grid'
import { DataGrid } from '@mui/x-data-grid/DataGrid'
import { useMatch } from '@tanstack/react-location'
import { useAppConfig } from '../../hooks/useAppConfig'
import { useAuditTrailQuery } from '../../hooks/useAuditTrailQuery'
import { ACTION_TYPE } from '../../utils/constants'
import { formatDate } from '../../utils/formatters'
import Loading from '../common/Loading'
import ApiErrorMessage from '../error/ApiErrorMessage'
import NotFound from '../error/NotFound'
import Button from '../shared/Button'
import PageHeader from '../shell/PageHeader'

const AUDIT_TRAIL_COLUMNS: GridColDef[] = [
  {
    field: 'process',
    headerName: 'Process',
    sortable: false,
    disableColumnMenu: true,
    flex: 1
  },
  {
    field: 'actionTaken',
    headerName: 'Action taken',
    renderCell: ({ value }) => ACTION_TYPE[value],
    sortable: false,
    disableColumnMenu: true,
    flex: 1
  },
  {
    field: 'links',
    headerName: 'Links',
    renderCell: ({ value }) =>
      value.map((link: string) => (
        <Link
          key={link}
          href={`patient/${link}`}
          display="block"
          whiteSpace="nowrap"
        >
          {link}
        </Link>
      )),
    sortable: false,
    disableColumnMenu: true,
    flex: 1
  },
  {
    field: 'when',
    headerName: 'When',
    valueFormatter: ({ value }) => formatDate(value),
    sortable: false,
    disableColumnMenu: true,
    flex: 1
  },
  {
    field: 'changedBy',
    headerName: 'Changed By',
    sortable: false,
    disableColumnMenu: true,
    flex: 1
  },
  {
    field: 'comment',
    headerName: 'Comment',
    sortable: false,
    disableColumnMenu: true,
    flex: 1
  }
]

const AuditTrail = () => {
  const {
    data: { uid }
  } = useMatch()
  const { getPatientName } = useAppConfig()
  const { patient, auditTrail, isLoading, error } = useAuditTrailQuery()

  if (isLoading) {
    return <Loading />
  }

  if (error) {
    return <ApiErrorMessage error={error} />
  }

  if (!auditTrail) {
    return <NotFound />
  }

  return (
    <Container>
      <Grid container sx={{ display: 'flex', justifyContent: 'space-between' }}>
        <Grid item>
          {patient && (
            <PageHeader
              description={
                <Typography fontSize="16px" letterSpacing="0.15px">
                  {patient.uid}
                </Typography>
              }
              title={getPatientName(patient)}
              breadcrumbs={[
                {
                  icon: <SearchIcon />,
                  title: 'Search Results'
                },
                {
                  icon: <Person />,
                  title: `Record Details`
                },
                {
                  icon: <Warning />,
                  title: 'Audit Trail'
                }
              ]}
            />
          )}
        </Grid>
        <Grid item>
          <Button variant="header" href={`patient/${uid}`}>
            BACK TO RECORD
          </Button>
        </Grid>
      </Grid>
      <Divider />
      <Card
        sx={{
          marginTop: '33px',
          background: '#FFFFFF',
          boxShadow: '0px 0px 0px 1px #E0E0E0',
          borderRadius: '4px'
        }}
      >
        <DataGrid
          getRowId={({ process }) => process}
          columns={AUDIT_TRAIL_COLUMNS}
          rows={auditTrail}
          autoHeight
          sx={{
            '& .MuiDataGrid-columnHeaders': {
              backgroundColor: '#F3F3F3'
            },
            '& .MuiDataGrid-columnSeparator': {
              display: 'none'
            }
          }}
        />
      </Card>
    </Container>
  )
}

export default AuditTrail
