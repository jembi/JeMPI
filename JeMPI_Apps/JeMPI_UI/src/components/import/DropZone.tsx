import { UploadFile as UploadFileIcon } from '@mui/icons-material'
import {
  Avatar,
  Box,
  Card,
  CardContent,
  FormControlLabel,
  Grid,
  Radio,
  RadioGroup,
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
    handleChange: handleImportFormChange,
    handleSubmit,
    values: FormValues,
    setFieldValue
  } = useFormik({
    initialValues: {
      reporting: false,
      computing: 0,
      leftMargin: 0.2,
      rightMargin: 0.6,
      threshold: 0.9,
      windowSize: 0.1
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
    <Card>
      <CardContent>
        <Grid container direction="row" paddingX={{ lg: '1rem', xs: '0.5rem' }}>
          <Grid item xs={12} lg={6}>
            <form>
              <Grid container alignItems="center">
                <Grid item xs={9}>
                  <Typography
                    fontWeight="bold"
                    fontSize="1rem"
                    paddingY={{ lg: '0.5rem' }}
                  >
                    Machine Learning Configuration
                  </Typography>
                </Grid>
                <Grid
                  item
                  xs={12}
                  paddingX={{ lg: '1rem', xs: '0.5rem' }}
                  border={'1px solid grey'}
                  borderRadius={'5px'}
                >
                  <FormControlLabel
                    control={
                      <Radio
                        name="computing"
                        value={0}
                        onChange={() => {
                          handleImportFormChange({
                            target: { name: 'computing', value: 0 }
                          })
                        }}
                        checked={FormValues.computing === 0}
                      />
                    }
                    label={
                      <Typography fontSize={'0.9rem'}>
                        {
                          " Use current M & U's (computed periodically, only using the Client Registry)."
                        }
                      </Typography>
                    }
                  />
                  <FormControlLabel
                    control={
                      <Radio
                        name="computing"
                        value={1}
                        onChange={() =>
                          handleImportFormChange({
                            target: { name: 'computing', value: 1 }
                          })
                        }
                        checked={FormValues.computing === 1}
                      />
                    }
                    label={
                      <Typography fontSize={'0.9rem'}>
                        {
                          ' Before linking, compute M & U values using the interactions from the CSV file.'
                        }
                      </Typography>
                    }
                  />
                  <FormControlLabel
                    control={
                      <Radio
                        name="computing"
                        value={2}
                        onChange={() => {
                          handleImportFormChange({
                            target: { name: 'computing', value: 2 }
                          })
                        }}
                        checked={FormValues.computing === 2}
                      />
                    }
                    label={
                      <Typography fontSize={'0.9rem'}>
                        {
                          " Before linking, Compute M & U values using the interactions from the CSV file & the CR's golden records."
                        }
                      </Typography>
                    }
                  />
                </Grid>
                <Grid item xs={12} paddingY={{ lg: '1rem', xs: '0.5rem' }}>
                  <Typography fontWeight="bold" fontSize="1rem">
                    Threshold
                  </Typography>
                </Grid>
                <Grid item xs={12} paddingX={{ lg: '1rem', xs: '0.5rem' }}>
                  <Slider
                    onChange={(_: Event, value: number | number[]) => {
                      if (!Array.isArray(value)) return
                      const [leftMargin, threshold, rightMargin] = value
                      if (
                        0 < threshold &&
                        threshold < 1 &&
                        threshold > leftMargin &&
                        threshold < rightMargin
                      ) {
                        setFieldValue('threshold', threshold)
                      }
                      if (threshold > leftMargin)
                        setFieldValue('leftMargin', leftMargin)

                      if (threshold < rightMargin)
                        setFieldValue('rightMargin', rightMargin)
                    }}
                    getAriaValueText={(e: number) => e.toString()}
                    valueLabelDisplay="auto"
                    step={0.05}
                    marks
                    min={0.19}
                    max={0.96}
                    value={[
                      FormValues.leftMargin,
                      FormValues.threshold,
                      FormValues.rightMargin
                    ]}
                    defaultValue={[
                      FormValues.leftMargin,
                      FormValues.threshold,
                      FormValues.rightMargin
                    ]}
                    sx={{
                      '& .MuiSlider-thumb': {
                        "&[data-index='0']": {
                          backgroundColor: 'red'
                        },
                        "&[data-index='1']": {
                          backgroundColor: 'green'
                        }
                      }
                    }}
                    track={false}
                  />
                </Grid>
                <Grid item xs={12} md={4} sx={{ padding: { xs: '0.5rem' } }}>
                  <TextField
                    name="leftMargin"
                    type="number"
                    size="small"
                    variant="outlined"
                    label="Minimum Threshold  Review"
                    value={FormValues.leftMargin}
                    onChange={e => {
                      if (+e.target.value < FormValues.threshold) {
                        handleImportFormChange(e)
                      }
                    }}
                    inputProps={{
                      min: 0.19,
                      max: FormValues.threshold,
                      step: 0.01
                    }}
                    InputLabelProps={{
                      style: { color: 'red' }
                    }}
                    fullWidth
                  />
                </Grid>
                <Grid item xs={12} md={4} sx={{ padding: { xs: '0.5rem' } }}>
                  <TextField
                    name="threshold"
                    type="number"
                    size="small"
                    variant="outlined"
                    label="Link Threshold"
                    value={FormValues.threshold}
                    onChange={e => {
                      if (
                        +e.target.value > FormValues.leftMargin &&
                        +e.target.value < FormValues.rightMargin
                      ) {
                        handleImportFormChange(e)
                      }
                    }}
                    inputProps={{ min: 0.2, max: 0.95, step: 0.01 }}
                    InputLabelProps={{
                      style: { color: 'green' }
                    }}
                    fullWidth
                  />
                </Grid>
                <Grid item xs={12} md={4} sx={{ padding: { xs: '0.5rem' } }}>
                  <TextField
                    name="rightMargin"
                    type="number"
                    size="small"
                    variant="outlined"
                    label="Maximum Review Threshold "
                    value={FormValues.rightMargin}
                    onChange={e => {
                      if (+e.target.value > FormValues.threshold) {
                        handleImportFormChange(e)
                      }
                    }}
                    inputProps={{
                      min: FormValues.threshold,
                      max: 0.96,
                      step: 0.01
                    }}
                    InputLabelProps={{
                      style: { color: '#1976D2' }
                    }}
                    fullWidth
                  />
                </Grid>
                <Grid item xs={12} md={4} sx={{ padding: { xs: '0.5rem' } }}>
                  <Slider
                    value={FormValues.windowSize}
                    getAriaValueText={(e: number) => e.toString()}
                    valueLabelDisplay="auto"
                    onChange={handleImportFormChange}
                    name="windowSize"
                    step={0.01}
                    marks
                    min={0}
                    max={0.2}
                  />
                  <Box sx={{ paddingY: { xs: '0.5rem' } }}>
                    <TextField
                      name="windowSize"
                      type="number"
                      size="small"
                      variant="outlined"
                      label="Margin Window size"
                      value={FormValues.windowSize}
                      onChange={handleImportFormChange}
                      inputProps={{ min: 0, max: 0.2, step: 0.01 }}
                      fullWidth
                    />
                  </Box>
                </Grid>
                <Grid item xs={12} paddingY={'1rem'}>
                  <Typography
                    fontWeight="bold"
                    fontSize="1rem"
                    id="import-report-radio"
                  >
                    Reports
                  </Typography>
                </Grid>
                <Grid
                  item
                  xs={12}
                  paddingX={'1rem'}
                  border={'1px solid grey'}
                  borderRadius={'5px'}
                >
                  <RadioGroup
                    aria-labelledby="import-report-radio"
                    defaultValue="false"
                    name="reporting"
                  >
                    <FormControlLabel
                      value="false"
                      control={<Radio />}
                      label={
                        <Typography fontSize={'0.9rem'}>
                          {' Link records only (do not generate report).'}
                        </Typography>
                      }
                    />
                    <FormControlLabel
                      value="true"
                      control={<Radio />}
                      label={
                        <Typography fontSize={'0.9rem'}>
                          {
                            ' Create CSV report and send notification when input file is created.'
                          }
                        </Typography>
                      }
                    />
                  </RadioGroup>
                </Grid>
              </Grid>
            </form>
          </Grid>
          <Grid
            item
            container
            xs={12}
            lg={6}
            padding={1}
            direction={'column'}
            gap={8}
            sx={{
              display: 'flex',
              justifyContent: ' center',
              alignItems: 'center'
            }}
          >
            <Grid
              item
              xs={8}
              border={'2px dashed #305982 '}
              borderRadius={'1rem'}
              sx={{
                display: 'flex',
                justifyContent: 'center',
                alignItems: 'center'
              }}
            >
              {!fileObjs?.file ? (
                <Box
                  sx={{
                    padding: '3rem',
                    display: 'flex',
                    flexDirection: 'column',
                    alignContent: 'center',
                    alignItems: 'center',
                    cursor: 'pointer'
                  }}
                  {...getRootProps()}
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
            </Grid>
            <Grid item>
              <Box
                sx={{
                  display: 'flex',
                  gap: '2rem'
                }}
              >
                <Button
                  variant="outlined"
                  onClick={handleCancel}
                  disabled={!fileObjs}
                >
                  Cancel
                </Button>
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
                  Submit
                </Button>
              </Box>
            </Grid>
          </Grid>
        </Grid>
      </CardContent>
    </Card>
  )
}
export default DropZone
