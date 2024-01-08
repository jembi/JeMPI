import { AddOutlined } from '@mui/icons-material'
import {
  Card,
  CardContent,
  Button,
  FormControl,
  InputLabel,
  Select,
  CardActions,
  IconButton,
  Typography
} from '@mui/material'
import { Box } from '@mui/system'
import React from 'react'

const Blocking = () => {
  const [viewType, setViewType] = React.useState(0)
  return (
    <>
      <Card sx={{ minWidth: 275 }}>
        <CardContent
          sx={{
            width: '100%',
            display: 'flex',
            flexDirection: 'column',
            gap: 6
          }}
        >
          <Box
            sx={{ mr: 'auto', display: 'flex', flexDirection: 'row', gap: 2 }}
          >
            <Button
              variant="outlined"
              size="medium"
              onClick={() => setViewType(0)}
            >
              Design View
            </Button>
            <Button
              variant="outlined"
              size="medium"
              onClick={() => setViewType(1)}
            >
              Source View
            </Button>
          </Box>
          {viewType === 0 ? (
            <Box
              sx={{
                display: 'flex',
                flexDirection: 'row',
                width: '100%',
                justifyContent: 'center',
                gap: 2
              }}
            >
              <FormControl fullWidth>
                <InputLabel id="demo-simple-select-label">
                  Select Comparator Function
                </InputLabel>
                <Select
                  labelId="demo-simple-select-label"
                  id="demo-simple-select"
                  label="Select Comparator Function"
                ></Select>
              </FormControl>
              <FormControl fullWidth>
                <InputLabel id="demo-simple-select-label">
                  Select Field
                </InputLabel>
                <Select
                  labelId="demo-simple-select-label"
                  id="demo-simple-select"
                  label="Select Field"
                ></Select>
              </FormControl>
              <FormControl fullWidth>
                <InputLabel id="demo-simple-select-label">
                  Select Operator
                </InputLabel>
                <Select
                  labelId="demo-simple-select-label"
                  id="demo-simple-select"
                  label="Select Operator"
                ></Select>
              </FormControl>
            </Box>
          ) : (
            <Box
              sx={{
                width: '100%',
                display: 'flex',
                flexDirection: 'column',
                gap: 2,
                justifyContent: 'center',
                alignItems: 'center'
              }}
            >
              <Typography variant='h5' >Match (phone number, 3)</Typography>
              Or
              <Typography variant='h5'>Match (National ID, 3)</Typography> Or
              <Typography variant='h5'>
                Int (Match (given name , 3)) + Int(Match (family name, 3)) +
                Int(Match (city, 3)) ≥ 3
              </Typography>
            </Box>
          )}
        </CardContent>
        <CardActions>
          <IconButton aria-label="delete" size="small">
            <AddOutlined fontSize="small" />
          </IconButton>
        </CardActions>
      </Card>
    </>
  )
}

export default Blocking
