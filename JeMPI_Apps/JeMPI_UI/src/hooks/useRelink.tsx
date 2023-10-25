import { useMutation } from '@tanstack/react-query'
import { AxiosError } from 'axios'
import { useSnackbar } from 'notistack'
import { useConfig } from './useConfig'

const useRelink = () => {
  const { enqueueSnackbar } = useSnackbar()
  const { apiClient } = useConfig()

  const createNewGoldenRecord = useMutation({
    mutationFn: apiClient.newGoldenRecord,
    onError: (error: AxiosError) => {
      enqueueSnackbar(`Error creating new golden record: ${error.message}`, {
        variant: 'error'
      })
    }
  })

  const linkRecords = useMutation({
    mutationFn: apiClient.linkRecord,
    onSuccess: () => {
      enqueueSnackbar('Golden record accepted and notification closed', {
        variant: 'success'
      })
    },
    onError: (error: AxiosError) => {
      enqueueSnackbar(`Error linking to golden record: ${error.message}`, {
        variant: 'error'
      })
    }
  })

  return { linkRecords, createNewGoldenRecord }
}

export default useRelink
