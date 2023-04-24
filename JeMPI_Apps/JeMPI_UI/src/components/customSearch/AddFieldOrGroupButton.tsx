import AddIcon from '@mui/icons-material/Add'
import { Button } from '@mui/material'
import { SearchParameter } from '../../types/SimpleSearch'

interface AddFieldOrGroupButtonProps {
  onClick: (obj: any) => void
  initialCustomSearchValues:
    | SearchParameter
    | {
        parameters: Array<SearchParameter>
      }
  label: string
}
const AddFieldOrGroupButton: React.FC<AddFieldOrGroupButtonProps> = ({
  onClick,
  initialCustomSearchValues,
  label
}) => {
  return (
    <Button
      variant="text"
      startIcon={<AddIcon />}
      onClick={() => {
        onClick(initialCustomSearchValues)
      }}
    >
      {label}
    </Button>
  )
}

export default AddFieldOrGroupButton
