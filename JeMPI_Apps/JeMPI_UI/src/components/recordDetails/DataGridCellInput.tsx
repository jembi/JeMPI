import { InputAdornment, TextField, Tooltip } from '@mui/material'
import { GridEditInputCellProps, useGridApiContext } from '@mui/x-data-grid'
import ErrorIcon from '@mui/icons-material/Error'

const DataGridCellInput = (props: GridEditInputCellProps) => {
  const { id, value, field, error, message } = props
  const apiRef = useGridApiContext()

  const handleValueChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    const newValue = event.target.value
    apiRef.current.setEditCellValue({ id, field, value: newValue })
  }

  return (
    <TextField
      value={value}
      onChange={handleValueChange}
      InputProps={{
        endAdornment: error && (
          <Tooltip title={`Invalid ${field}: ${message}`}>
            <InputAdornment position="end">
              <ErrorIcon color="error" />
            </InputAdornment>
          </Tooltip>
        )
      }}
    />
  )
}

export default DataGridCellInput
