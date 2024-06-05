import { AddOutlined, Source } from '@mui/icons-material'
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
import { Field, Rule } from 'types/Configuration';
import SourceView, { RowData } from '../deterministic/SourceView';

interface BlockingProps {
  demographicData: Field[];
  rules : {
    matchNotification: {
      probabilistic: Rule[]
    }
  }
}

const transformRulesToRowData = (rules: { probabilistic: Rule[] }): RowData[] => {
  return rules.probabilistic.map((rule: any, index: any) => ({
    id: index,
    ruleNumber: index + 1,
    ruleText: rule.text,
  }));
};


const Blocking = ({demographicData= [], rules}: BlockingProps) => {
  const [viewType, setViewType] = React.useState(0);

  const probabilisticRows = transformRulesToRowData(rules.matchNotification);
const handleRowEdit = (row: RowData) => {
  console.log('row data', row)
  // const regex = /eq\(([^)]+)\)/g;
  // const matchedFields = [];
  // let match;
  // while ((match = regex.exec(row.ruleText)) !== null) {
  //   matchedFields.push(match[1]);
  // }

  // setComparators(new Array(matchedFields.length).fill(0));
  // setFields(matchedFields);
  // setOperators(new Array(matchedFields.length - 1).fill(Operator.AND));
  // setViewType(1);
};
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
              Source View
            </Button>
            <Button
              variant="outlined"
              size="medium"
              onClick={() => setViewType(1)}
            >
              Design View
            </Button>
            
          </Box>
          {viewType === 0 ? (
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
              <SourceView data={probabilisticRows} onEditRow={handleRowEdit}/>
              {/* <Typography variant='h5' >Match (phone number, 3)</Typography>
              Or
              <Typography variant='h5'>Match (National ID, 3)</Typography> Or
              <Typography variant='h5'>
                Int (Match (given name , 3)) + Int(Match (family name, 3)) +
                Int(Match (city, 3)) ≥ 3
              </Typography> */}
            </Box>
          ):(
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
          ) }
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
