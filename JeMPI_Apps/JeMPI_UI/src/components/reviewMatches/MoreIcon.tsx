import MoreVertIcon from '@mui/icons-material/MoreVert'
import {
  Divider,
  IconButton,
  Link,
  Menu,
  MenuItem,
  Typography
} from '@mui/material'
import { GridRenderCellParams } from '@mui/x-data-grid'
import { useState } from 'react'

interface MoreIconProps {
  params: GridRenderCellParams
  handleOpenLinkedRecordDialog?: (uid: string) => void
  handleOpenCreateNewGRDialog?: (uid: string) => void
  disabled?: boolean
}

const MoreIcon: React.FC<MoreIconProps> = ({
  params,
  handleOpenLinkedRecordDialog,
  handleOpenCreateNewGRDialog,
  disabled = true
}) => {
  const [anchorEl, setAnchorEl] = useState<null | HTMLElement>(null)

  const isOpen = Boolean(anchorEl)
  const open = (event: React.MouseEvent<HTMLButtonElement>) => {
    setAnchorEl(event.currentTarget)
  }
  const close = () => {
    setAnchorEl(null)
  }

  return (
    <>
      <IconButton
        aria-controls={isOpen ? 'basic-menu' : undefined}
        aria-haspopup="true"
        aria-expanded={isOpen ? 'true' : undefined}
        onClick={open}
        size="large"
        edge="end"
        disabled={disabled}
      >
        <MoreVertIcon />
      </IconButton>

      <Menu
        id="basic-menu"
        anchorEl={anchorEl}
        open={isOpen}
        onClose={close}
        MenuListProps={{
          'aria-labelledby': 'basic-button'
        }}
      >
        <MenuItem>
          <Link
            href={`/golden-record/${params.row.uid}`}
            style={{ textDecoration: 'none', color: 'black' }}
          >
            <Typography>View details</Typography>
          </Link>
        </MenuItem>
        <Divider sx={{ my: 0.5 }} />
        <MenuItem
          onClick={() =>
            handleOpenLinkedRecordDialog &&
            handleOpenLinkedRecordDialog(params.row.uid)
          }
        >
          <Typography>Link this record</Typography>
        </MenuItem>
        <Divider sx={{ my: 0.5 }} />
        <MenuItem>
          <Typography
            onClick={() =>
              handleOpenCreateNewGRDialog &&
              handleOpenCreateNewGRDialog(params.row.uid)
            }
          >
            New golden record
          </Typography>
        </MenuItem>
      </Menu>
    </>
  )
}

export default MoreIcon
