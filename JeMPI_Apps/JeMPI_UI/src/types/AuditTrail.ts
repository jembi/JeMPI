export default interface AuditTrailRecord {
  process: string
  actionTaken: string
  links: string[]
  when: string
  changedBy: string
  comment: string
}
