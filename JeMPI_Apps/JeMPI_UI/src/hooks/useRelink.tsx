import { useMutation } from '@tanstack/react-query'
import { AxiosError } from 'axios'
import { useSnackbar } from 'notistack'
import ApiClient from 'services/ApiClient'

const useRelink = () => {
  const { enqueueSnackbar } = useSnackbar()

  const createNewGoldenRecord = useMutation({
    mutationFn: ApiClient.newGoldenRecord,
    onError: (error: AxiosError) => {
      enqueueSnackbar(`Error creating new golden record: ${error.message}`, {
        variant: 'error'
      })
    }
  })

  const linkRecords = useMutation({
    mutationFn: ApiClient.linkRecord,
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
