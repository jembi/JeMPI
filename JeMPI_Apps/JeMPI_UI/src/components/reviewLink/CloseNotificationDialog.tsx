import {
  Dialog,
  DialogTitle,
  DialogContent,
  Typography,
  DialogActions,
  Button
} from '@mui/material'
import React from 'react'

const CloseNotificationDialog: React.FC<{
  isOpen: boolean
  onClose: () => void
  onConfirm: () => void
}> = ({ isOpen, onClose, onConfirm }) => {
  return (
    <>
      <Dialog fullWidth maxWidth={'sm'} open={isOpen}>
        <DialogTitle>Close and continue?</DialogTitle>
        <DialogContent>
          <Typography>
            This action will accept the currently linked Golden Record and close
            the notification.
          </Typography>
          <Typography>
            No changes will be made to the golden record link.
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

export default CloseNotificationDialog
