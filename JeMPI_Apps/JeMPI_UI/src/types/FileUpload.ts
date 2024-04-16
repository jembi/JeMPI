export interface FileObj {
  file: File
  progress: number
  status: UploadStatus
}

export enum UploadStatus {
  Pending = 'Pending',
  Loading = 'Loading',
  Complete = 'Complete',
  Failed = 'Failed'
}

export type importQueriesType = {
  reportingRequired: boolean
  uploadWorkflow: number
  minThreshold: number
  maxThreshold: number
  linkThreshold: number
  marginWindowSize: number
}
