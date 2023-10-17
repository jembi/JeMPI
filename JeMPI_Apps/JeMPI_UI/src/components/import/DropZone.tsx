import { UploadFile as UploadFileIcon } from '@mui/icons-material'
import {
  Avatar,
  Box,
  Card,
  CardActions,
  CardContent,
  Checkbox,
  Grid,
  Slider,
  TextField,
  Typography
} from '@mui/material'
import { useMutation } from '@tanstack/react-query'
import { AxiosError, AxiosProgressEvent, AxiosRequestConfig } from 'axios'
import { useSnackbar } from 'notistack'
import { FC, useRef, useState } from 'react'
import { FileRejection, useDropzone } from 'react-dropzone'
import ApiClient from '../../services/ApiClient'
import {
  FileObj,
  UploadStatus,
  importQueriesType
} from '../../types/FileUpload'
import Button from '../shared/Button'
import UploadFileListItem from './UploadFileListItem'
import { formatBytesSize, megabytesToBytes } from 'utils/formatters'
import { useFormik } from 'formik'

const MAX_UPLOAD_FILE_SIZE_IN_BYTES = megabytesToBytes(
  +(process.env.REACT_APP_MAX_UPLOAD_CSV_SIZE_IN_MEGABYTES ?? 128)
)

const DropZone: FC = () => {
  const { enqueueSnackbar } = useSnackbar()
  const [fileObjs, setFilesObj] = useState<FileObj | undefined>()
  const abortControllerRef = useRef<AbortController>(new AbortController())

  const {
    handleChange,
    handleSubmit,
    values: FormValues
  } = useFormik({
    initialValues: {
      reporting: false,
      computing: false,
      leftMargin: 0,
      rightMargin: 0,
      threshold: 0,
      windowSize: 0
    },
    onSubmit: () => {
      if (fileObjs?.file) {
        uploadFileMutation.mutate(fileObjs)
      }
    }
  })

  const onDrop = (
    acceptedFiles: File[],
    fileRejections: FileRejection[]
  ): void => {
    validate(fileRejections)
    setFilesObj({
      file: acceptedFiles[0],
      progress: 0,
      status: UploadStatus.Pending
    })
  }

  const validate = (fileRejections: FileRejection[]): void => {
    if (fileRejections.length > 0) {
      enqueueSnackbar(fileRejections[0].errors[0].message, {
        variant: 'error'
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

  const uploadFile = async (
    fileObj: FileObj,
    importQueries: importQueriesType
  ) => {
    return await ApiClient.uploadFile(
      createFileUploadAxiosConfig(fileObj, importQueries)
    )
  }

  const createFileUploadAxiosConfig = (
    fileObj: FileObj,
    importQueries: importQueriesType
  ): AxiosRequestConfig<FormData> => {
    const formData = new FormData()
    formData.set('csv', fileObj.file)
    formData.set('queries', JSON.stringify(importQueries))
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
    mutationFn: (fileObjs: FileObj) => uploadFile(fileObjs, FormValues),
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

  const handleRemoveFile = (): void => {
    setFilesObj(undefined)
  }

  const uploadList: JSX.Element = (
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
          <form>
            <Grid
              container
              spacing={2}
              sx={{ marginTop: '1rem' }}
              alignItems="center"
            >
              <Grid item xs={9}>
                <Typography fontWeight="bold" fontSize="1rem">
                  Compute M&U before linking:
                </Typography>
              </Grid>
              <Grid item xs={3}>
                <Checkbox
                  name="computing"
                  checked={FormValues.computing}
                  value={FormValues.computing}
                  onChange={handleChange}
                />
              </Grid>
              <Grid item xs={12}>
                <Typography fontWeight="bold" fontSize="1rem">
                  Linking:
                </Typography>
              </Grid>
              <Grid item xs={12} md={4} sx={{ padding: '1rem' }}>
                <Slider
                  value={FormValues.leftMargin}
                  onChange={handleChange}
                  getAriaValueText={(e: number) => e.toString()}
                  valueLabelDisplay="auto"
                  name="leftMargin"
                  step={0.1}
                  marks
                  min={0}
                  max={1}
                />
                <TextField
                  name="leftMargin"
                  type="number"
                  size="small"
                  variant="outlined"
                  label="Notification Left margin"
                  value={FormValues.leftMargin}
                  onChange={handleChange}
                  inputProps={{ min: 0, max: 100, step: 0.01 }}
                  fullWidth
                />
              </Grid>
              <Grid item xs={12} md={4} sx={{ padding: '1rem' }}>
                <Slider
                  value={FormValues.threshold}
                  onChange={handleChange}
                  getAriaValueText={(e: number) => e.toString()}
                  valueLabelDisplay="auto"
                  name="threshold"
                  step={0.1}
                  marks
                  min={0}
                  max={1}
                />
                <TextField
                  name="threshold"
                  type="number"
                  size="small"
                  variant="outlined"
                  label="Threshold"
                  value={FormValues.threshold}
                  onChange={handleChange}
                  inputProps={{ min: 0, max: 1, step: 0.01 }}
                  fullWidth
                />
              </Grid>
              <Grid item xs={12} md={4} sx={{ padding: '1rem' }}>
                <Slider
                  value={FormValues.rightMargin}
                  getAriaValueText={(e: number) => e.toString()}
                  valueLabelDisplay="auto"
                  onChange={handleChange}
                  name="rightMargin"
                  step={0.1}
                  marks
                  min={0}
                  max={1}
                />
                <TextField
                  name="rightMargin"
                  type="number"
                  size="small"
                  variant="outlined"
                  label="Notification right margin"
                  value={FormValues.rightMargin}
                  onChange={handleChange}
                  inputProps={{ min: 0, max: 1, step: 0.01 }}
                  fullWidth
                />
              </Grid>
              <Grid item xs={12}>
                <Typography fontWeight="bold" fontSize="1rem">
                  Notification for similar Candidates:
                </Typography>
              </Grid>
              <Grid item xs={12} md={4}>
                <Slider
                  value={FormValues.windowSize}
                  getAriaValueText={(e: number) => e.toString()}
                  valueLabelDisplay="auto"
                  onChange={handleChange}
                  name="windowSize"
                  step={0.1}
                  marks
                  min={0}
                  max={1}
                />
                <TextField
                  name="windowSize"
                  type="number"
                  size="small"
                  variant="outlined"
                  label="Window size"
                  value={FormValues.windowSize}
                  onChange={handleChange}
                  inputProps={{ min: 0, max: 100, step: 0.01 }}
                  fullWidth
                />
              </Grid>
              <Grid item xs={9}>
                <Typography fontWeight="bold" fontSize="1rem">
                  Generate Report:
                </Typography>
              </Grid>
              <Grid item xs={3}>
                <Checkbox
                  name="reporting"
                  checked={FormValues.reporting}
                  value={FormValues.reporting}
                  onChange={handleChange}
                />
              </Grid>
            </Grid>
          </form>
        </CardContent>
        <CardActions sx={{ display: 'block', textAlign: 'center' }}>
          <Button
            variant="contained"
            onClick={() => handleSubmit()}
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
