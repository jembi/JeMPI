import {
  Button as MuiButton,
  ButtonProps as MuiButtonProps,
  CircularProgress
} from '@mui/material'

interface ButtonProps extends MuiButtonProps {
  variant?: 'contained' | 'text' | 'outlined' | 'header' | undefined
  isLoading?: boolean
  isFullWidth?: boolean
}

const Button: React.FC<ButtonProps> = ({
  variant = 'text',
  isLoading = false,
  isFullWidth = false,
  className,
  children,
  ...rest
}) => {
  const classes = className ? [className] : []
  if (isFullWidth) {
    classes.push('full-width')
  }
  return (
    <MuiButton variant={variant} className={classes.join(' ')} {...rest}>
      {isLoading && <CircularProgress />}
      {children}
    </MuiButton>
  )
}

export default Button
