import {
  DialogActions,
  DialogContent,
  DialogTitle,
  Dialog as MuiDialog,
  Typography
} from '@mui/material'

interface DialogProps {
  buttons: JSX.Element[]
  title: string | JSX.Element
  subTitle?: string
  content: JSX.Element | string
  onOpen: boolean
  onClose: () => void
  fullWidth?: boolean
  maxWidth?: 'sm' | 'md' | 'lg'
}

const Dialog: React.FC<DialogProps> = ({
  buttons,
  title,
  subTitle,
  content,
  onOpen,
  onClose,
  fullWidth = false,
  maxWidth = 'sm'
}) => {
  return (
    <MuiDialog
      open={onOpen}
      onClose={onClose}
      fullWidth={fullWidth}
      maxWidth={maxWidth}
    >
      <DialogTitle>{title}</DialogTitle>

      <DialogContent>
        {subTitle && <Typography>{subTitle}</Typography>}
        {content}
      </DialogContent>
      <DialogActions>{buttons}</DialogActions>
    </MuiDialog>
  )
}

export default Dialog
