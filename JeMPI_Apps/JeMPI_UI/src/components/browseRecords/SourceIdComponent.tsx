import styled from '@emotion/styled'
import {
  TooltipProps,
  Tooltip,
  tooltipClasses,
  Box,
  Typography
} from '@mui/material'
import React from 'react'
import { SourceId } from 'types/PatientRecord'

const CustomTooltip = styled(({ className, ...props }: TooltipProps) => (
  <Tooltip {...props} classes={{ popper: className }} />
))(() => ({
  [`& .${tooltipClasses.tooltip}`]: {
    backgroundColor: '#f5f5f9',
    color: 'rgba(0, 0, 0, 0.87)',
    maxWidth: 220,
    fontSize: '1em',
    border: '1px solid #dadde9'
  }
}))

const SourceIdElement: React.FC<{ sourceId: SourceId }> = ({ sourceId }) => {
  return (
    <Box sx={{ display: 'flex', flexDirection: 'column' }}>
      <Typography
        fontSize={'1em'}
      >{`Facility-ID: ${sourceId.facility}`}</Typography>

      <Typography
        fontSize={'1em'}
      >{`Patient-ID: ${sourceId.patient}`}</Typography>
    </Box>
  )
}

const SourceIdComponent: React.FC<{ content: SourceId | Array<SourceId> }> = ({
  content
}) => {
  if (Array.isArray(content)) {
    if (content.length === 1) {
      return <SourceIdElement sourceId={content[0]} />
    }
    return (
      <CustomTooltip
        title={
          <Box sx={{ display: 'flex', flexDirection: 'column' }}>
            {content.slice(1).map((value: SourceId, idx: number) => (
              <React.Fragment key={value.uid}>
                <Typography fontSize={'1em'}>{`Facility-ID ${idx + 2}: ${
                  value.facility
                }`}</Typography>
                <Typography fontSize={'1em'}>{`Patient-ID ${idx + 2}: ${
                  value.patient
                }`}</Typography>
              </React.Fragment>
            ))}
          </Box>
        }
        arrow
      >
        <div>
          <SourceIdElement sourceId={content[0]} />
        </div>
      </CustomTooltip>
    )
  } else {
    return <SourceIdElement sourceId={content} />
  }
}

export default SourceIdComponent
