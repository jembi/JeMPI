import { GridRowModes, GridRowModesModel, GridRowsProp, GridToolbarContainer } from "@mui/x-data-grid"
import { randomId } from "utils/helpers"

interface EditToolbarProps {
    setRows: (newRows: (oldRows: GridRowsProp) => GridRowsProp) => void
    setRowModesModel: (
      newModel: (oldModel: GridRowModesModel) => GridRowModesModel
    ) => void
  }

export function EditToolbar(props: EditToolbarProps) {
    const { setRows, setRowModesModel } = props
  
    const handleClick = () => {
      const id = randomId()
      setRows(oldRows => [...oldRows, { id, name: '', age: '', isNew: true }])
      setRowModesModel(oldModel => ({
        ...oldModel,
        [id]: { mode: GridRowModes.Edit, fieldToFocus: 'name' }
      }))
    }
  
    return <GridToolbarContainer></GridToolbarContainer>
  }