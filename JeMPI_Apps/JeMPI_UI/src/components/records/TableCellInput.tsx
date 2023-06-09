import { TextField } from '@mui/material'
import { FC } from 'react'

const TableCellInput: FC<{
  onChange: (param: string) => (value: string) => void
}> = ({ onChange }) => {
  const handleValueChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    const onValueChange = onChange('value')
    onValueChange(event.target.value)
  }

  return (
    <TextField
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
