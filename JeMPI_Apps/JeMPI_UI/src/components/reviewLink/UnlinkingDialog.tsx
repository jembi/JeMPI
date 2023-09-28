import {
  Dialog,
  DialogTitle,
  DialogContent,
  Typography,
  DialogActions,
  Button
} from '@mui/material'
import React from 'react'

const UnlinkingDialog: React.FC<{
  isOpen: boolean
  onClose: () => void
  onConfirm: () => void
}> = ({ isOpen, onClose, onConfirm }) => {
  return (
    <>
      <Dialog fullWidth maxWidth={'sm'} open={isOpen}>
        <DialogTitle>Confirm unlinking?</DialogTitle>
        <DialogContent>
          <Typography>
            This action will unlink the Patient record and Golden record and
            create a new Golden record.
          </Typography>
          <DialogActions>
            <Button onClick={onClose}>Cancel</Button>
            <Button onClick={onConfirm}>Confirm</Button>
          </DialogActions>
        </DialogContent>
      </Dialog>
    </>
  )
}

export default UnlinkingDialog
