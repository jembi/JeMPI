import {
  Box,
  Button,
  Card,
  CardContent,
  FormControl,
  InputLabel,
  Select,
  Typography
} from '@mui/material'
import React from 'react'
import { Configuration } from 'types/Configuration'

interface DeterministicProps {
  linkingRules: Configuration['rules']['link']
}

const Deterministic = ({ linkingRules }: DeterministicProps) => {
  const [viewType, setViewType] = React.useState(0)

  const deterministicRules = linkingRules.deterministic || {}

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
                alignItems: 'flex-start'
              }}
            >
              {Object.keys(deterministicRules).map((key, index) => (
                <Typography
                  key={index}
                  variant="h5"
                  sx={{ fontSize: '1.1rem' }}
                >
                  {`Rule ${index + 1}:  ${deterministicRules[key].text}`}
                </Typography>
              ))}
            </Box>
          )}
        </CardContent>
      </Card>
    </>
  )
}

export default Deterministic
