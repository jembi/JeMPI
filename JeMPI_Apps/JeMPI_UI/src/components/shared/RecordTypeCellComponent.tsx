import { Typography } from '@mui/material'
import { GridRenderCellParams } from '@mui/x-data-grid'
import React from 'react'
interface RecordTypeCellComponentProps {
  params: GridRenderCellParams
}

const RecordTypeCellComponent: React.FC<RecordTypeCellComponentProps> = ({
  params
}) => {
  switch (params.row.type) {
    case 'Current':
      return (
        <>
          <Typography>Patient</Typography>
        </>
      )
    case 'Golden':
      return (
        <Typography color="#D79B01" fontWeight={700}>
          Golden
        </Typography>
      )
    case 'Candidate':
      if (params.row.searched) {
        return <Typography>Searched</Typography>
      } else {
        return <Typography>Blocked</Typography>
      }
    default:
      return <></>
  }
}

export default RecordTypeCellComponent
