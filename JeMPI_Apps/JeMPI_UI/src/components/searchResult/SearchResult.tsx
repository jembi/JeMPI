import { MoreHorizOutlined } from '@mui/icons-material'
import SearchIcon from '@mui/icons-material/Search'
import { Container, Link } from '@mui/material'
import Divider from '@mui/material/Divider'
import {
  DataGrid,
  GridColDef,
  GridRenderCellParams,
  GridSortModel
} from '@mui/x-data-grid'
import { useQuery } from '@tanstack/react-query'
import { AxiosError } from 'axios'
import React, { useCallback, useMemo, useState } from 'react'
import { useAppConfig } from '../../hooks/useAppConfig'
import {
  ApiSearchResult,
  CustomSearchQuery,
  SearchQuery
} from '../../types/SimpleSearch'
import PageHeader from '../shell/PageHeader'
import { useLocation } from 'react-router-dom'
import { useConfig } from 'hooks/useConfig'

type SearchResultProps = {
  title: string
}

const SearchResult: React.FC<SearchResultProps> = () => {
  const {
    state: { payload: searchPayload }
  } = useLocation()
  const [payload, setPayLoad] = useState<SearchQuery | CustomSearchQuery>(
    searchPayload || ({} as CustomSearchQuery)
  )
  const { apiClient } = useConfig()
  const { availableFields } = useAppConfig()

  const columns: GridColDef[] = useMemo(
    () =>
      availableFields.map(({ fieldName, fieldLabel }) => {
        if (fieldName === 'uid') {
          return {
            field: fieldName,
            headerName: 'Golden ID',
            flex: 2,
            align: 'center',
            headerAlign: 'center',
            renderCell: (params: GridRenderCellParams) => {
              return (
                <Link
                  href={`/golden-record/${params.row.uid}`}
                  key={params.row.uid}
                >
                  {params.row.uid}
                </Link>
              )
            },
            filterable: false
          }
        }
        return {
          field: fieldName,
          headerName: fieldLabel,
          minWidth: 150,
          flex: 2,
          align: 'center',
          headerAlign: 'center',
          filterable: false
        }
      }),
    [availableFields]
  )

  const { data: searchResults, isLoading } = useQuery<
    ApiSearchResult,
    AxiosError
  >({
    queryKey: ['golden-record', payload],
    queryFn: () => {
      return apiClient.searchQuery(payload)
    },
    refetchOnWindowFocus: false
  })

  const handleRequestToSort = useCallback(
    (model: GridSortModel) => {
      if (model.length > 0) {
        const [column] = model
        setPayLoad({
          ...payload,
          sortAsc: column.sort === 'asc',
          sortBy: column.field
        })
      }
    },
    [payload, setPayLoad]
  )

  const handlePaginateModel = useCallback(
    ({ page, pageSize }: { page: number; pageSize: number }) => {
      setPayLoad({
        ...payload,
        offset: page * payload.limit,
        limit: pageSize
      })
    },
    [setPayLoad, payload]
  )

  return (
    <Container maxWidth={false}>
      <PageHeader
        title="Search Results"
        breadcrumbs={[
          {
            icon: <MoreHorizOutlined />
          },
          {
            icon: <SearchIcon />,
            title: 'Search'
          }
        ]}
      />
      <Divider />

      <DataGrid
        columns={columns}
        rows={searchResults ? searchResults.records.data : []}
        sx={{ mt: 4 }}
        autoHeight={true}
        getRowId={row => row.uid}
        onSortModelChange={handleRequestToSort}
        pagination
        pageSizeOptions={[payload.limit / 2, payload.limit, payload.limit * 2]}
        onPaginationModelChange={handlePaginateModel}
        rowCount={searchResults?.records.pagination.total || 0}
        paginationMode="server"
        loading={isLoading}
      />
    </Container>
  )
}

export default SearchResult
