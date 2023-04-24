import { UploadFile as UploadFileIcon } from '@mui/icons-material'
import { Box, CardActions, Container, Typography } from '@mui/material'
import { useMutation } from '@tanstack/react-query'
import { AxiosError, AxiosProgressEvent, AxiosRequestConfig } from 'axios'
import { useSnackbar } from 'notistack'
import { FC, useState } from 'react'
import { FileRejection, useDropzone } from 'react-dropzone'
import ApiClient from '../../services/ApiClient'
import { FileObj, UploadStatus } from '../../types/FileUpload'
import Button from '../shared/Button'
import './Import.css'
import UploadFileListItem from './UploadFileListItem'

const DropZone: FC = () => {
  const [fileObjs, setFilesObj] = useState<FileObj[]>([])
  const { enqueueSnackbar } = useSnackbar()

  const onDrop = (
    acceptedFiles: File[],
    fileRejections: FileRejection[]
  ): void => {
    validate(acceptedFiles, fileRejections)
    setFilesObj([
      ...fileObjs,
      { file: acceptedFiles[0], progress: 0, status: UploadStatus.Pending }
    ])
  }

  const validate = (
    acceptedFiles: File[],
    fileRejections: FileRejection[]
  ): void => {
    if (fileRejections.length > 0) {
      enqueueSnackbar('File type not supported', {
        variant: 'error'
      })
      return
    }

    if (fileObjs.some(x => x.file.name === acceptedFiles[0].name)) {
      enqueueSnackbar('File already queued', {
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

  const { getRootProps, getInputProps, isFocused, isDragAccept, isDragReject } =
    useDropzone({
      accept: { 'text/csv': ['.csv'] },
      onDrop,
      maxFiles: 1,
      multiple: true
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
    const fileUploadObjNewState = fileObjs.map(fileObj => {
      if (fileObj.file.name === fileUploadObj.file.name) {
        fileObj.progress = progress
        fileObj.status = getFileUploadStatus(fileObj)
      }
      return fileObj
    })
    setFilesObj(fileUploadObjNewState)
  }

  const setUploadStatus = (fileUploadObj: FileObj, status: UploadStatus) => {
    const fileUploadObjNewState = fileObjs.map(fileObj => {
      if (fileObj.file.name === fileUploadObj.file.name) {
        fileObj.status = status
        if (status === UploadStatus.Failed) {
          fileObj.progress = 0
        }
      }
      return fileObj
    })
    setFilesObj(fileUploadObjNewState)
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
    setFilesObj([])
  }

  const handleUpload = () => {
    fileObjs.forEach(async fileObj => {
      if (
        fileObj.status === UploadStatus.Complete ||
        fileObj.status === UploadStatus.Failed
      ) {
        return
      }
      uploadFileMutation.mutate(fileObj)
    })
  }

  const handleRemoveFile = (fileObjForDeletion: FileObj): void => {
    setFilesObj(
      fileObjs?.filter(x => x.file.name !== fileObjForDeletion.file.name)
    )
  }

  const uploadList = fileObjs.map(fileObj => (
    <UploadFileListItem
      fileObj={fileObj}
      handleRemoveFile={handleRemoveFile}
      key={fileObj.file.name}
    />
  ))

  return (
    <Container>
      <Box
        className="dropzone"
        {...getRootProps({
          isFocused,
          isDragAccept,
          isDragReject
        })}
      >
        <div className="dropzone-inner">
          <input {...getInputProps()} />
          <Box className="import__upload-icon">
            <UploadFileIcon />
          </Box>
          <Typography fontSize="16px">
            <a>Click to upload</a> or drag and drop
          </Typography>
          <Typography color="#00000099" fontSize="14px">
            CSV (max. 128MB)
          </Typography>
        </div>
      </Box>
      {uploadList}
      <CardActions
        sx={{ display: 'block', textAlign: 'center', marginTop: '5%' }}
      >
        <Button
          variant="contained"
          onClick={handleUpload}
          disabled={uploadFileMutation.isLoading}
        >
          Upload
        </Button>
        <Button variant="outlined" onClick={handleCancel}>
          Cancel
        </Button>
      </CardActions>
    </Container>
  )
}
export default DropZone
