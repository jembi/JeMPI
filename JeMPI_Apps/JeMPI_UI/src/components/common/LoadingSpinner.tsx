import { Backdrop, CircularProgress } from '@mui/material'

const LoadingSpinner = ({ id }: {id?:string}) => {
  return (
    <Backdrop
      sx={{ color: '#fff', zIndex: theme => theme.zIndex.drawer + 1 }}
      open={true}
      id={id || 'loading-spinner'}
    >
      <CircularProgress color="inherit" />
    </Backdrop>
  )
}

export default LoadingSpinner
