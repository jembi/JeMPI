import {
  Circle as CircleIcon,
  Close as CloseIcon,
  UploadFile as UploadFileIcon
} from '@mui/icons-material'
import { Box, Grid, LinearProgress, Typography } from '@mui/material'
import { FC } from 'react'
import { FileObj, UploadStatus } from '../../types/FileUpload'
import './Import.css'
import { truncateString } from 'utils/formatters'

interface FileObjProps {
  fileObj: FileObj
  handleRemoveFile: (fileObj: FileObj) => void
}

const UploadFileListItem: FC<FileObjProps> = (props: FileObjProps) => {
  const addStatusClass = (status: UploadStatus): string => {
    switch (status) {
      case UploadStatus.Failed:
        return 'failed'
      default:
        return ''
    }
  }

  return (
    <Grid
      container
      justifyContent="center"
      alignItems="center"
      spacing={0}
      className={`import__upload-list-item ${addStatusClass}`}
      key={props.fileObj.file.name}
    >
      <Grid
        item
        xs={1}
        justifyContent="center"
        alignItems="center"
        textAlign="center"
        padding={'0.5rem'}
      >
        <Box className="import__upload-icon">
          <UploadFileIcon />
        </Box>
      </Grid>
      <Grid
        item
        padding={'1.5rem'}
        xs={10}
        justifyContent="center"
        alignItems="center"
      >
        <Typography color="primary.disabled" fontSize="16px" noWrap>
          {truncateString(props.fileObj.file.name, 30)}
        </Typography>
        <Typography
          color="#00000099"
          fontSize="14px"
          className={`import__upload-list-item ${addStatusClass}`}
        >
          {Math.round(props.fileObj.file.size / 1000)}kb
          <CircleIcon sx={{ fontSize: '5px', alignItems: 'center' }} />
          {UploadStatus[props.fileObj.status]}
        </Typography>
      </Grid>
      <Grid
        item
        xs={1}
        justifyContent="center"
        alignItems="center"
        textAlign="center"
      >
        <Box
          display={props.fileObj.status !== 'Loading' ? 'block' : 'none'}
          className="import__close-icon"
          onClick={() => props.handleRemoveFile(props.fileObj)}
        >
          <CloseIcon />
        </Box>
      </Grid>
      <Grid item xs={10} justifyContent="center" alignItems="center">
        <LinearProgress
          className="import__upload-progress-bar"
          variant="buffer"
          value={props.fileObj.progress}
          valueBuffer={100}
        />
      </Grid>
    </Grid>
  )
}
export default UploadFileListItem
