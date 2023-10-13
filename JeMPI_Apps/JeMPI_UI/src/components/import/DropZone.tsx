import { UploadFile as UploadFileIcon } from '@mui/icons-material'
import {
  Avatar,
  Box,
  Card,
  CardActions,
  CardContent,
  Checkbox,
  FormControlLabel,
  Grid,
  TextField,
  Typography
} from '@mui/material'
import { useMutation } from '@tanstack/react-query'
import { AxiosError, AxiosProgressEvent, AxiosRequestConfig } from 'axios'
import { useSnackbar } from 'notistack'
import { FC, useRef, useState } from 'react'
import { FileRejection, useDropzone } from 'react-dropzone'
import ApiClient from '../../services/ApiClient'
import { FileObj, UploadStatus } from '../../types/FileUpload'
import Button from '../shared/Button'
import UploadFileListItem from './UploadFileListItem'
import { formatBytesSize, megabytesToBytes } from 'utils/formatters'

const MAX_UPLOAD_FILE_SIZE_IN_BYTES = megabytesToBytes(
  +(process.env.REACT_APP_MAX_UPLOAD_CSV_SIZE_IN_MEGABYTES ?? 128)
)
const DropZone: FC = () => {
  const [fileObjs, setFilesObj] = useState<FileObj | undefined>()
  const abortControllerRef = useRef<AbortController>(new AbortController())
  const { enqueueSnackbar } = useSnackbar()

  const onDrop = (
    acceptedFiles: File[],
    fileRejections: FileRejection[]
  ): void => {
    validate(acceptedFiles, fileRejections)
    setFilesObj({
      file: acceptedFiles[0],
      progress: 0,
      status: UploadStatus.Pending
    })
  }

  const validate = (
    acceptedFiles: File[],
    fileRejections: FileRejection[]
  ): void => {
    if (fileRejections.length > 0) {
      enqueueSnackbar(fileRejections[0].errors[0].message, {
        variant: 'error'
      })
      return
    }

    if (uploadFileMutation.isLoading) {
      enqueueSnackbar('Please wait for current import to be completed', {
        variant: 'warning'
      })
      return
    }
  }

  const { getRootProps, getInputProps } = useDropzone({
    accept: { 'text/csv': ['.csv'] },
    onDrop,
    multiple: false,
    maxSize: MAX_UPLOAD_FILE_SIZE_IN_BYTES
  })

  const uploadFile = async (fileObj: FileObj) => {
    return await ApiClient.uploadFile(createFileUploadAxiosConfig(fileObj))
  }

  const createFileUploadAxiosConfig = (
    fileObj: FileObj
  ): AxiosRequestConfig<FormData> => {
    const formData = new FormData()
    formData.set('csv', fileObj.file)
    return {
      signal: abortControllerRef.current.signal,
      headers: {
        'content-type': 'multipart/form-data'
      },
      data: formData,
      onUploadProgress: (progressEvent: AxiosProgressEvent) => {
        setFilesObj((prev: FileObj | undefined) => {
          if (prev?.file.name === fileObj.file.name && progressEvent.total) {
            return {
              ...prev,
              progress: Math.round(
                (progressEvent.loaded * 100) / progressEvent.total
              ),
              status: UploadStatus.Loading
            }
          }
        })
      }
    }
  }

  const uploadFileMutation = useMutation({
    mutationFn: uploadFile,
    onSuccess: (_, fileObj) => {
      setFilesObj((prev: FileObj | undefined) =>
        prev ? { ...prev, status: UploadStatus.Complete } : undefined
      )
      enqueueSnackbar(`${fileObj.file.name} file imported`, {
        variant: 'success'
      })
      setFilesObj(undefined)
    },
    onError: (error: AxiosError, data) => {
      enqueueSnackbar(
        `Error importing ${data.file.name} file: ${error.message}`,
        {
          variant: 'error'
        }
      )
      setFilesObj((prev: FileObj | undefined) =>
        prev ? { ...prev, status: UploadStatus.Failed } : undefined
      )
    }
  })

  const handleCancel = (): void => {
    abortControllerRef.current.abort()
    abortControllerRef.current = new AbortController()
    uploadFileMutation.reset()
    setFilesObj(undefined)
  }

  const handleUpload = () => {
    if (
      fileObjs?.status === UploadStatus.Complete ||
      fileObjs?.status === UploadStatus.Failed
    ) {
      return
    }
    if (fileObjs) uploadFileMutation.mutate(fileObjs)
  }

  const handleRemoveFile = (): void => {
    setFilesObj(undefined)
  }

  const uploadList = (
    <>
      {fileObjs && (
        <UploadFileListItem
          fileObj={fileObjs}
          handleRemoveFile={handleRemoveFile}
          key={fileObjs?.file.name}
        />
      )}
    </>
  )

  return (
    <>
      <Card>
        <CardContent sx={{ width: { xs: '100%', lg: '40%' } }}>
          {!fileObjs?.file ? (
            <Box
              sx={{
                padding: '2rem',
                display: 'flex',
                flexDirection: 'column',
                alignContent: 'center',
                alignItems: 'center',
                cursor: 'pointer'
              }}
              {...getRootProps()}
              border={'2px dashed #305982 '}
              borderRadius={'1rem'}
            >
              <input {...getInputProps()} />
              <Avatar sx={{ bgcolor: '#305982' }}>
                <UploadFileIcon />
              </Avatar>
              <Typography fontSize="1rem">
                Click to upload or drag and drop
              </Typography>
              <Typography color="#00000099" fontSize="1rem">
                CSV (max. {formatBytesSize(MAX_UPLOAD_FILE_SIZE_IN_BYTES)})
              </Typography>
            </Box>
          ) : (
            uploadList
          )}
          <Grid
            container
            spacing={2}
            sx={{ marginTop: '1rem' }}
            alignItems="center"
          >
            <Grid item xs={9}>
              <Typography fontWeight="bold" fontSize="1rem">
                Generate Report:
              </Typography>
            </Grid>
            <Grid item xs={3}>
              <FormControlLabel
                value="false"
                control={<Checkbox />}
                label=""
                labelPlacement="start"
              />
            </Grid>
            <Grid item xs={9}>
              <Typography fontWeight="bold" fontSize="1rem">
                Compute M&U before linking:
              </Typography>
            </Grid>
            <Grid item xs={3}>
              <FormControlLabel
                value="true"
                control={<Checkbox />}
                label=""
                labelPlacement="start"
                checked={true}
              />
            </Grid>
            <Grid item xs={12}>
              <Typography fontWeight="bold" fontSize="1rem">
                Linking:
              </Typography>
            </Grid>
            <Grid item xs={12} md={4}>
              <TextField
                type="number"
                size="small"
                variant="outlined"
                label="Notification Left margin"
                inputProps={{ min: 0, max: 100, step: 0.01 }}
                fullWidth
              />
            </Grid>
            <Grid item xs={12} md={4}>
              <TextField
                type="number"
                size="small"
                variant="outlined"
                label="Threshold"
                inputProps={{ min: 0, max: 100, step: 0.01 }}
                fullWidth
              />
            </Grid>
            <Grid item xs={12} md={4}>
              <TextField
                type="number"
                size="small"
                variant="outlined"
                label="Notification right margin"
                inputProps={{ min: 0, max: 100, step: 0.01 }}
                fullWidth
              />
            </Grid>
            <Grid item xs={12}>
              <Typography fontWeight="bold" fontSize="1rem">
                Notification for similar Candidates:
              </Typography>
            </Grid>
            <Grid item xs={12} md={4}>
              <TextField
                type="number"
                size="small"
                variant="outlined"
                label="Window size"
                inputProps={{ min: 0, max: 100, step: 0.01 }}
                fullWidth
              />
            </Grid>
          </Grid>
        </CardContent>
        <CardActions sx={{ display: 'block', textAlign: 'center' }}>
          <Button
            variant="contained"
            onClick={handleUpload}
            disabled={
              uploadFileMutation.isLoading ||
              uploadFileMutation.isError ||
              !fileObjs ||
              fileObjs?.status === 'Failed'
            }
          >
            Upload
          </Button>
          <Button
            variant="outlined"
            onClick={handleCancel}
            disabled={!fileObjs}
          >
            Cancel
          </Button>
        </CardActions>
      </Card>
    </>
  )
}
export default DropZone
