import { Tooltip } from '@mui/material'
import { GridEditInputCellProps, useGridApiContext } from '@mui/x-data-grid'

const DataGridCellInput = (props: GridEditInputCellProps) => {
  const { id, value, field, error } = props
  const apiRef = useGridApiContext()

  const handleValueChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    const newValue = event.target.value
    apiRef.current.setEditCellValue({ id, field, value: newValue })
  }

  return (
    <Tooltip open={!!error} title={`Invalid ${field}`}>
      <input
        style={{
          width: '100%',
          height: '100%',
          outline: 'none',
          margin: '0',
          border: 'none',
          padding: '10px'
        }}
        value={value}
        onChange={handleValueChange}
      />
    </Tooltip>
  )
}

export default DataGridCellInput
