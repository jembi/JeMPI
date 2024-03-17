import { GridColDef } from '@mui/x-data-grid'
import { CustomSearchQuery, SearchParameter } from 'types/SimpleSearch'
import { formatDate } from './formatters'

export enum ACTIONS {
  newnewUserCreated,
  grUpdate,
  grAndPatientRecordLinked,
  grAndPatientRecordLinkApproved
}

export const ACTION_TYPE: { [key: string]: string } = {
  [ACTIONS.newnewUserCreated]: 'New user created',
  [ACTIONS.grUpdate]: 'GR updated',
  [ACTIONS.grAndPatientRecordLinked]: 'GR and Patient record linked',
  [ACTIONS.grAndPatientRecordLinkApproved]:
    'GR and Patient record link approved'
}

export const THRESHOLD_SPECIFIC_REASON = {
  ABOVE: "ABOVE THRESHOLD",
  BELOW: "BELOW THRESHOLD",
}

export const RESOLUTION_TYPES = {
  RELINKED: "RELINKED",
  RELINKED_NEW: "RELINKED_NEW",
  APPROVED: "APPROVED"
}

export const PAGINATION_LIMIT = 10

export const INITIAL_SEARCH_PARAMETER: SearchParameter = {
  fieldName: '',
  value: '',
  distance: 0
}

export const INITIAL_VALUES: CustomSearchQuery = {
  $or: [
    {
      parameters: [INITIAL_SEARCH_PARAMETER]
    }
  ],
  sortBy: 'uid',
  sortAsc: true,
  offset: 0,
  limit: PAGINATION_LIMIT
}

export const REVIEW_LINK_STEPS = [
  'Review Linked Record',
  'Refine Search (Optional)',
  'Close'
]

export const AUDIT_TRAIL_COLUMNS: GridColDef[] = [
  {
    field: 'created_at',
    headerName: 'CreatedAt',
    valueFormatter: ({ value }) => formatDate(value),
    sortable: false,
    disableColumnMenu: true,
    headerClassName: 'super-app-theme--header'
  },
  {
    field: 'inserted_at',
    headerName: 'InsertedAt',
    valueFormatter: ({ value }) => formatDate(value),
    sortable: false,
    disableColumnMenu: true,
    headerClassName: 'super-app-theme--header'
  },
  {
    field: 'interaction_id',
    headerName: 'InteractionID',
    sortable: false,
    disableColumnMenu: true,
    headerClassName: 'super-app-theme--header'
  },
  {
    field: 'golden_id',
    headerName: 'GoldenID',
    sortable: false,
    disableColumnMenu: true,
    headerClassName: 'super-app-theme--header'
  },
  {
    field: 'entry',
    headerName: 'Event',
    sortable: false,
    disableColumnMenu: true,
    headerClassName: 'super-app-theme--header',
    flex: 1
  },
  {
    field: 'linking_rule',
    headerName: 'Matching Type',
    sortable: false,
    disableColumnMenu: true,
    headerClassName: 'super-app-theme--header',
    flex: 1
  }
]
