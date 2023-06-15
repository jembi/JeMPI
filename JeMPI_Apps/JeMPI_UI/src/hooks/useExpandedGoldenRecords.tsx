import { useQuery } from '@tanstack/react-query'
import { AxiosError } from 'axios'
import { useEffect } from 'react'
import ApiClient from 'services/ApiClient'

const useExpandedGoldenRecords = (offset: number, length: number) => {
  console.log(offset, length)

  useEffect(() => {
    goldenIdsQuery.refetch()
  }, [offset, length])
  const goldenIdsQuery = useQuery<Array<string>, AxiosError>({
    queryKey: ['golden-records-ids'],
    queryFn: async () => await ApiClient.getGoldenIds(offset, length),
    refetchOnWindowFocus: false
  })

  const expandeGoldenRecordsQuery = useQuery<any, AxiosError>({
    queryKey: ['expanded-golden-records'],
    queryFn: async () =>
      await ApiClient.getExpandedGoldenRecords(goldenIdsQuery?.data),
    enabled: !!goldenIdsQuery.data,
    refetchOnWindowFocus: false
  })

  return { expandeGoldenRecordsQuery, goldenIdsQuery }
}

export default useExpandedGoldenRecords
