import { TextField } from '@mui/material'
import { FC } from 'react'

const TableCellInput: FC<{
  value: string | Date | undefined
  onChange: (value: string | Date) => void
}> = ({ value, onChange }) => {
  const handleValueChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    onChange(event.target.value)
  }

  return (
    <TextField
      value={value}
      variant="standard"
      sx={{
        width: '100%',
        height: '100%',
        outline: 'none',
        margin: '0',
        border: 'none'
      }}
      onChange={handleValueChange}
    />
  )
}

export default TableCellInput
