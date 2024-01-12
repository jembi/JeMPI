import { Box, Link, Typography } from '@mui/material'
import { GridRenderCellParams } from '@mui/x-data-grid'
import dayjs from 'dayjs'
import RecordTypeCellComponent from './RecordTypeCellComponent'
import SourceIdCellComponent from './SourceIdCellComponent'

const getCellComponent = (
  fieldName: string,
  params: GridRenderCellParams,
  handleRecordLink?: () => void
) => {
  switch (fieldName) {
    case 'actions':
      switch (params.row.type) {
        case 'Blocked':
        case 'Searched':
          return (
            <Link
              sx={{ ':hover': { cursor: 'pointer' } }}
              onClick={() => (handleRecordLink ? handleRecordLink() : null)}
            >
              Link
            </Link>
          )
        default:
          return <></>
      }
    case 'sourceId':
      return <SourceIdCellComponent content={params.row.sourceId} />
    case 'createdAt':
      return (
        <Box
          sx={{
            display: 'flex',
            flexDirection: 'column',
            alignItems: 'center'
          }}
        >
          <Typography fontSize={'1em'}>
            {dayjs(params.row.created).format('YYYY-MM-DD HH:mm:ss')}
          </Typography>
        </Box>
      )
    case 'recordType':
      return <RecordTypeCellComponent params={params} />
    default:
      break
  }
}

export default getCellComponent
