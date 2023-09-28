import {
  Dialog,
  Button,
  DialogActions,
  DialogContent,
  DialogTitle,
  Typography
} from '@mui/material'
import { AnyRecord } from 'types/PatientRecord'
import DataGrid from './CustomDataGrid'

const LinkRecordsDialog: React.FC<{
  isOpen: boolean
  data: AnyRecord[]
  onClose: () => void
  onConfirm: () => void
}> = ({ isOpen, data, onClose, onConfirm }) => {
  return (
    <>
      <Dialog fullWidth maxWidth={'lg'} open={isOpen}>
        <DialogTitle>Link these records?</DialogTitle>
        <DialogContent>
          <Typography>This action will link the following records: </Typography>
          <DataGrid rows={data} />
          <DialogActions>
            <Button onClick={onClose}>Cancel</Button>
            <Button onClick={onConfirm}>Confirm</Button>
          </DialogActions>
        </DialogContent>
      </Dialog>
    </>
  )
}

export default LinkRecordsDialog
