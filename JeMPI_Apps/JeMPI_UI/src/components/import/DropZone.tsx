import { UploadFile as UploadFileIcon } from '@mui/icons-material'
import {
  Box,
  CardActions,
  Checkbox,
  Container,
  FormControl,
  FormControlLabel,
  Radio,
  RadioGroup,
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
import './Import.css'
import UploadFileListItem from './UploadFileListItem'
import { formatBytesSize } from 'utils/formatters'

// default max upload file size is 128 MO => '128 * 1024 * 1024'
const MAX_UPLOAD_FILE_SIZE_IN_BYTES =
  process.env.REACT_APP_MAX_UPLOAD_CSV_SIZE_IN_BYTES ?? '128 * 1024 * 1024'
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
    maxSize: eval(MAX_UPLOAD_FILE_SIZE_IN_BYTES)
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
        if (progressEvent.total) {
          const progress = (progressEvent.loaded / progressEvent.total) * 100
          updateFileUploadProgress(fileObj, progress)
        }
      }
    }
  }

  const updateFileUploadProgress = (
    fileUploadObj: FileObj,
    progress: number
  ) => {
    if (fileObjs)
      setFilesObj((prev: FileObj | undefined) => {
        if (prev?.file.name === fileUploadObj.file.name) {
          return { ...prev, progress, status: getFileUploadStatus(fileObjs) }
        }
      })
  }

  const setUploadStatus = (fileUploadObj: FileObj, status: UploadStatus) => {
    setFilesObj((prev: FileObj | undefined) => {
      if (prev?.file.name === fileUploadObj.file.name) {
        prev.status = status
        if (status === UploadStatus.Failed) {
          prev.progress = 0
        }
      }
      return prev
    })
  }

  const getFileUploadStatus = (fileObj: FileObj) => {
    if (fileObj.progress === 0) {
      return UploadStatus.Pending
    } else if (fileObj.progress > 0 && fileObj.progress < 100) {
      return UploadStatus.Loading
    } else if (fileObj.progress === 100) {
      return UploadStatus.Complete
    } else {
      return UploadStatus.Failed
    }
  }

  const uploadFileMutation = useMutation({
    mutationFn: uploadFile,
    onSuccess: (data, fileObj) => {
      setUploadStatus(fileObj, UploadStatus.Complete)
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
      setUploadStatus(data, UploadStatus.Failed)
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
    <Container>
      <Box className="dropzone" {...getRootProps()}>
        <div className="dropzone-inner">
          <input {...getInputProps()} />
          <Box className="import__upload-icon">
            <UploadFileIcon />
          </Box>
          <Typography fontSize="16px">
            <a>Click to upload</a> or drag and drop
          </Typography>
          <Typography color="#00000099" fontSize="1rem">
            CSV (max. {formatBytesSize(eval(MAX_UPLOAD_FILE_SIZE_IN_BYTES))})
          </Typography>
        </div>
      </Box>
      {uploadList}
      <Box
        display={'flex'}
        justifyContent="start"
        gap={'0.5rem'}
        alignItems="center"
        padding={'0.5rem'}
      >
        <Typography fontWeight={'bold'} fontSize={'1.2rem'}>
          TB 1
        </Typography>
        <TextField placeholder="TB 1" size="small" />
      </Box>
      <Box
        display={'flex'}
        justifyContent="start"
        gap={'0.5rem'}
        alignItems={'center '}
        padding={'0.5rem'}
      >
        <Typography fontWeight={'bold'} fontSize={'1.2rem'}>
          CB
        </Typography>
        <FormControlLabel
          value="CB2"
          control={<Checkbox />}
          label="CB1"
          labelPlacement="start"
        />
        <FormControlLabel
          value="CB2"
          control={<Checkbox />}
          label="CB2"
          labelPlacement="start"
        />
      </Box>
      <Box
        display={'flex'}
        gap={'1.3rem'}
        alignItems={'center'}
        padding={'0.5rem'}
      >
        <Typography fontWeight={'bold'} fontSize={'1.2rem'}>
          RB
        </Typography>
        <FormControl>
          <RadioGroup
            aria-labelledby="demo-radio-buttons-group-label"
            defaultValue="Opt1"
            name="radio-buttons-group"
          >
            <FormControlLabel value="Opt1" control={<Radio />} label="Opt1" />
            <FormControlLabel value="Opt2" control={<Radio />} label="Opt2" />
            <FormControlLabel value="Opt3" control={<Radio />} label="Opt3" />
          </RadioGroup>
        </FormControl>
      </Box>
      <CardActions
        sx={{ display: 'block', textAlign: 'center', marginTop: '5%' }}
      >
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
        <Button variant="outlined" onClick={handleCancel} disabled={!fileObjs}>
          Cancel
        </Button>
      </CardActions>
    </Container>
  )
}
export default DropZone
