import {
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle
} from '@mui/material'
import { DataGrid } from '@mui/x-data-grid'
import Button from '../shared/Button'
import { UpdatedFields } from './PatientDetails'

const columns = [
  {
    field: 'fieldName',
    headerName: 'Field',
    flex: 1
  },
  {
    field: 'originalFieldValue',
    headerName: 'Original',
    flex: 1
  },
  {
    field: 'newFieldValue',
    headerName: 'New',
    flex: 1
  }
]

const ConfirmationModal: React.FC<{
  isVisible: boolean
  handleClose: () => void
  onConfirm: () => void
  updatedFields: UpdatedFields
}> = ({ isVisible, handleClose, updatedFields, onConfirm }) => {
  const rows = Object.keys(updatedFields).map(key => {
    return {
      id: key,
      fieldName: key,
      originalFieldValue: updatedFields[key].oldValue,
      newFieldValue: updatedFields[key].newValue
    }
  })

  return (
    <Dialog fullWidth maxWidth={'sm'} open={isVisible} onClose={handleClose}>
      <DialogContent
        sx={{
          p: 0,
          '& .new': {
            color: '#FF6F00'
          }
        }}
      >
        <DialogTitle>Confirm Golden Record Changes</DialogTitle>
        <DataGrid
          getRowId={({ id }) => id}
          columns={columns}
          rows={rows}
          autoHeight={true}
          hideFooter={true}
          getCellClassName={params =>
            params.field === 'newFieldValue' ? 'new' : ''
          }
        />
        <DialogActions sx={{ p: '20px' }}>
          <Button onClick={handleClose}>Cancel</Button>
          <Button onClick={onConfirm}>Confirm</Button>
        </DialogActions>
      </DialogContent>
    </Dialog>
  )
}

export default ConfirmationModal
