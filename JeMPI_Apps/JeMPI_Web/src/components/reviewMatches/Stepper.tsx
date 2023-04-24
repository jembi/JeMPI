import { Step, StepLabel, Stepper as MuiStepper } from '@mui/material'
import { REVIEW_LINK_STEPS } from 'utils/constants'
const Stepper = () => {
  return (
    <MuiStepper sx={{ mb: 5 }} activeStep={1}>
      {REVIEW_LINK_STEPS.map(label => (
        <Step key={label}>
          <StepLabel>{label}</StepLabel>
        </Step>
      ))}
    </MuiStepper>
  )
}

export default Stepper
