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
import {
  FileObj,
  UploadStatus,
  importQueriesType
} from '../../types/FileUpload'
import Button from '../shared/Button'
import UploadFileListItem from './UploadFileListItem'
import { formatBytesSize, megabytesToBytes } from 'utils/formatters'
import { useConfig } from 'hooks/useConfig'
import { useFormik } from 'formik'
import Papa, { ParseResult } from 'papaparse'

const DropZone: FC = () => {
  const { enqueueSnackbar } = useSnackbar()
  const [fileObjs, setFilesObj] = useState<FileObj | undefined>()
  const abortControllerRef = useRef<AbortController>(new AbortController())
  const { apiClient, config } = useConfig()
  const MAX_UPLOAD_FILE_SIZE_IN_BYTES = megabytesToBytes(
    config.maxUploadCsvSize
  )
  const {
    handleChange: handleImportFormChange,
    handleSubmit,
    values: FormValues,
    setFieldValue
  } = useFormik({
    initialValues: {
      reportingRequired: false,
      uploadWorkflow: 0,
      minThreshold: 0.65,
      linkThreshold: 0.7,
      maxThreshold: 0.75,
      marginWindowSize: 0.1
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
    if (validate(fileRejections)) {
      setFilesObj({
        file: acceptedFiles[0],
        progress: 0,
        status: UploadStatus.Pending
      })
    }
  }

  const validate = (fileRejections: FileRejection[]): boolean => {
    if (fileRejections.length > 0) {
      enqueueSnackbar(fileRejections[0].errors[0].message, {
        variant: 'error'
      })
      return false
    }
    return true
  }

  const { getRootProps, getInputProps } = useDropzone({
    accept: { 'text/csv': ['.csv'] },
    onDrop,
    multiple: false,
    maxSize: MAX_UPLOAD_FILE_SIZE_IN_BYTES
  })

  const handleUpload = async (
    fileObj: FileObj,
    importQueries: importQueriesType
  ) => {
    if (!fileObj.file) return

    await Papa.parse(fileObj.file, {
      header: true,
      skipEmptyLines: true,
      complete: async function (results: ParseResult<Record<string, unknown>>) {
        const rows = results.data
        const headers = results.meta.fields
        const chunkSize = 10000

        for (let i = 0; i < rows.length; i += chunkSize) {
          const chunk = rows.slice(i, i + chunkSize)

          const csvContent = Papa.unparse(
            { fields: headers!, data: chunk },
            {
              delimiter: ',',
              header: true,
              newline: '\r\n'
            }
          )

          await uploadChunk(csvContent, importQueries, i)
        }
        console.log('Successfully uploaded data')
      }
    })
  }
  const uploadChunk = async (
    csvContent: string,
    importQueries: importQueriesType,
    index: number
  ) => {
    const blob = new Blob([csvContent], { type: 'text/csv' })
    const file = new File([blob], `chunk-${index}.csv`, { type: 'text/csv' })

    const formData = new FormData()
    formData.append('csv', file)
    formData.append('queries', JSON.stringify(importQueries))

    const config = {
      signal: abortControllerRef.current.signal,
      headers: {
        'Content-Type': 'multipart/form-data'
      },
      data: formData
    }
    return await apiClient.uploadFile(config)
  }

  const uploadFileMutation = useMutation({
    mutationFn: (fileObjs: FileObj) => handleUpload(fileObjs, FormValues),
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
                        name="uploadWorkflow"
                        value={0}
                        onChange={() => {
                          handleImportFormChange({
                            target: { name: 'uploadWorkflow', value: 0 }
                          })
                        }}
                        checked={FormValues.uploadWorkflow === 0}
                      />
                    }
                    label={
                      <Typography fontSize={'0.9rem'}>
                        {' Send to the linker and use the current M & U values'}
                      </Typography>
                    }
                  />
                  <br />
                  <FormControlLabel
                    control={
                      <Radio
                        name="uploadWorkflow"
                        value={1}
                        onChange={() =>
                          handleImportFormChange({
                            target: { name: 'uploadWorkflow', value: 1 }
                          })
                        }
                        checked={FormValues.uploadWorkflow === 1}
                      />
                    }
                    label={
                      <Typography fontSize={'0.9rem'}>
                        {' Send to EM task to compute new M & U values'}
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
                      const [minThreshold, linkThreshold, maxThreshold] = value
                      if (
                        0 < linkThreshold &&
                        linkThreshold < 1 &&
                        linkThreshold > minThreshold &&
                        linkThreshold < maxThreshold
                      ) {
                        setFieldValue('linkThreshold', linkThreshold)
                      }
                      if (linkThreshold > minThreshold)
                        setFieldValue('minThreshold', minThreshold)

                      if (linkThreshold < maxThreshold)
                        setFieldValue('maxThreshold', maxThreshold)
                    }}
                    getAriaValueText={(e: number) => e.toString()}
                    valueLabelDisplay="auto"
                    step={0.05}
                    marks
                    min={0.19}
                    max={0.96}
                    value={[
                      FormValues.minThreshold,
                      FormValues.linkThreshold,
                      FormValues.maxThreshold
                    ]}
                    defaultValue={[
                      FormValues.minThreshold,
                      FormValues.linkThreshold,
                      FormValues.maxThreshold
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
                    name="minThreshold"
                    type="number"
                    size="small"
                    variant="outlined"
                    label="Minimum Threshold Review"
                    value={FormValues.minThreshold}
                    onChange={e => {
                      if (+e.target.value < FormValues.linkThreshold) {
                        handleImportFormChange(e)
                      }
                    }}
                    inputProps={{
                      min: 0.19,
                      max: FormValues.linkThreshold,
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
                    name="linkThreshold"
                    type="number"
                    size="small"
                    variant="outlined"
                    label="Link Threshold"
                    value={FormValues.linkThreshold}
                    onChange={e => {
                      if (
                        +e.target.value > FormValues.minThreshold &&
                        +e.target.value < FormValues.maxThreshold
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
                    name="maxThreshold"
                    type="number"
                    size="small"
                    variant="outlined"
                    label="Maximum Review Threshold "
                    value={FormValues.maxThreshold}
                    onChange={e => {
                      if (+e.target.value > FormValues.linkThreshold) {
                        handleImportFormChange(e)
                      }
                    }}
                    inputProps={{
                      min: FormValues.linkThreshold,
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
                    value={FormValues.marginWindowSize}
                    getAriaValueText={(e: number) => e.toString()}
                    valueLabelDisplay="auto"
                    onChange={handleImportFormChange}
                    name="marginWindowSize"
                    step={0.01}
                    marks
                    min={0}
                    max={0.2}
                  />
                  <Box sx={{ paddingY: { xs: '0.5rem' } }}>
                    <TextField
                      name="marginWindowSize"
                      type="number"
                      size="small"
                      variant="outlined"
                      label="Margin Window size"
                      value={FormValues.marginWindowSize}
                      onChange={handleImportFormChange}
                      inputProps={{ min: 0, max: 0.2, step: 0.01 }}
                      fullWidth
                    />
                  </Box>
                </Grid>
                <Grid item xs={12} paddingY={'0.5rem'}>
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
