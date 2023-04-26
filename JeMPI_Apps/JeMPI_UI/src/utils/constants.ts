import { CustomSearchQuery, SearchParameter } from 'types/SimpleSearch'

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
  'Read Instructions',
  'Review Linked Records',
  'Refine Search (Optional)',
  'Accept'
]
