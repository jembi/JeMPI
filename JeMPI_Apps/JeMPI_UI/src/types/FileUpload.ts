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
  reporting: boolean
  computing: number
  leftMargin: number
  rightMargin: number
  threshold: number
  windowSize: number
}
