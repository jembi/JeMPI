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
        <Typography color="#D79B01" fontWeight={700}>
          Golden
        </Typography>
      )
    case 'Searched':
      return <Typography>Searched</Typography>

    case 'Blocked':
      return <Typography>Blocked</Typography>

    default:
      return (
        <>
          <Typography>Patient</Typography>
        </>
      )
  }
}

export default RecordTypeCellComponent
