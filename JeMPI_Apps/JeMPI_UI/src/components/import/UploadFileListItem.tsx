import {
  Close as CloseIcon,
  UploadFile as UploadFileIcon
} from '@mui/icons-material'
import { Avatar, Box, Grid, LinearProgress, Typography } from '@mui/material'
import { FC } from 'react'
import { FileObj, UploadStatus } from '../../types/FileUpload'
import { formatBytesSize } from 'utils/formatters'

interface FileObjProps {
  fileObj: FileObj
  handleRemoveFile: (fileObj: FileObj) => void
}

const UploadFileListItem: FC<FileObjProps> = (props: FileObjProps) => {
  return (
    <Grid
      container
      justifyContent="center"
      alignItems="center"
      spacing={0}
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
        <Avatar sx={{ bgcolor: '#305982' }}>
          <UploadFileIcon />
        </Avatar>
      </Grid>
      <Grid
        item
        padding={'1.5rem'}
        xs={10}
        justifyContent="center"
        alignItems="center"
      >
        <Typography
          fontWeight="bold"
          fontSize="1rem"
          noWrap
          maxWidth="100%"
          overflow="hidden"
          textOverflow="ellipsis"
        >
          {props.fileObj.file.name}
        </Typography>
        <Box display="flex">
          Size:
          <Typography color="#00000099" fontSize="1rem" px="0.5rem">
            {formatBytesSize(props.fileObj.file.size)}
          </Typography>
          Status:
          <Typography color="#00000099" fontSize="1rem" px="0.5rem">
            {UploadStatus[props.fileObj.status]}
            {props.fileObj.progress > 0 && ` ${props.fileObj.progress}%`}
          </Typography>
        </Box>
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
          onClick={() => props.handleRemoveFile(props.fileObj)}
        >
          <CloseIcon />
        </Box>
      </Grid>
      <Grid item xs={10} justifyContent="center" alignItems="center">
        <LinearProgress
          variant="buffer"
          value={props.fileObj.progress}
          valueBuffer={100}
        />
      </Grid>
    </Grid>
  )
}
export default UploadFileListItem
