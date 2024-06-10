import React, { useEffect, useState } from 'react';
import { AddOutlined } from '@mui/icons-material';
import {
  Button,
  FormControl,
  InputLabel,
  Select,
  IconButton,
  Box,
  MenuItem,
  SelectChangeEvent,
} from '@mui/material';
import SourceView, { RowData } from '../deterministic/SourceView';
import { Field, Rule } from 'types/Configuration';
import { Operator, options } from '../deterministic/DeterministicContent';
import { transformFieldName } from 'utils/helpers';

interface BlockingContentProps {
  demographicData: Field[];
  hasUndefinedRule: boolean;
  linkingRules: {
    validate?: { deterministic?: Rule[] };
    matchNotification?: {
      probabilistic?: Rule[];
    };
  };
}

const transformRulesToRowData = (rules: { probabilistic: Rule[] }): RowData[] => {
  return rules.probabilistic.map((rule: any, index: any) => ({
    id: index,
    ruleNumber: index + 1,
    ruleText: rule.text,
  }));
};

const BlockingContent = ({ demographicData = [], hasUndefinedRule, linkingRules }: BlockingContentProps) => {
  const [viewType, setViewType] = useState(0);

  const probabilisticRows = linkingRules.matchNotification?.probabilistic ?? [];
  const [comparators, setComparators] = useState<number[]>([]);
  const [fields, setFields] = useState<string[]>([]);
  const [operators, setOperators] = useState<Operator[]>([]);
  const [rules, setRules] = useState<Rule[]>([]);
  const [initialState, setInitialState] = useState({
    comparators: [] as number[],
    fields: [] as string[],
    operators: [] as Operator[],
  });

  useEffect(() => {
    const savedComparators = localStorage.getItem('comparators');
    const savedFields = localStorage.getItem('fields');
    const savedOperators = localStorage.getItem('operators');
    const savedRules = localStorage.getItem('rules');

    if (savedComparators || savedFields || savedOperators || savedRules) {
      try {
        const parsedRules = savedRules ? JSON.parse(savedRules) : [];
        setRules(parsedRules);
        const parsedComparators = savedComparators ? JSON.parse(savedComparators) : [];
        const parsedFields = savedFields ? JSON.parse(savedFields) : [];
        const parsedOperators = savedOperators ? JSON.parse(savedOperators) : [];

        setComparators(parsedComparators);
        setFields(parsedFields);
        setOperators(parsedOperators);
        setInitialState({
          comparators: parsedComparators,
          fields: parsedFields,
          operators: parsedOperators,
        });
      } catch (e) {
        console.error('Error parsing saved data from localStorage:', e);
      }
    }
  }, []);

  useEffect(() => {
    localStorage.setItem('comparators', JSON.stringify(comparators));
  }, [comparators]);

  useEffect(() => {
    localStorage.setItem('fields', JSON.stringify(fields));
  }, [fields]);

  useEffect(() => {
    localStorage.setItem('operators', JSON.stringify(operators));
  }, [operators]);

  useEffect(() => {
    localStorage.setItem('rules', JSON.stringify(rules));
  }, [rules]);

  const handleComparatorChange = (index: number, event: SelectChangeEvent<number>) => {
    const newComparators = [...comparators];
    newComparators[index] = event.target.value as number;
    setComparators(newComparators);
  };

  const handleFieldChange = (index: number, event: SelectChangeEvent<string>) => {
    const newFields = [...fields];
    newFields[index] = event.target.value as string;
    setFields(newFields);
  };

  const handleOperatorChange = (index: number, event: SelectChangeEvent<Operator>) => {
    const newOperators = [...operators];
    newOperators[index] = event.target.value as Operator;
    setOperators(newOperators);
  };

  const handleAddRule = () => {
    const vars = fields.filter((field, index) => field !== '' && fields.indexOf(field) === index);
    const text = vars
      .map((field, index) => {
        const operator = index > 0 ? ` ${operators[index - 1].toLowerCase()} ` : '';
        const comparator = comparators[index];
        const comparatorFunction = comparator === 0 ? `eq(${field})` : `match(${field}, ${comparator})`;
        return `${operator}${comparatorFunction}`;
      })
      .join('');
  
    const rule = {
      function: comparators[0],
      vars,
      text,
    };
    setRules([...rules, rule]);
    setInitialState({
      comparators: [...comparators],
      fields: [...fields],
      operators: [...operators],
    });
  };
  

  const handleRowEdit = (row: RowData) => {
    const regex = /(?:eq|match)\(([^),]+)(?:,\s*(\d+))?\)/g;
    const operatorsRegex = /\s+(and|or)\s+/g;
    const matchedFields: string[] = [];
    const matchedComparators: number[] = [];
    const matchedOperators: string[] = [];
    let match;

    while ((match = regex.exec(row.ruleText)) !== null) {
        matchedFields.push(match[1]);
        matchedComparators.push(match[2] ? parseInt(match[2], 10) : 0);
    }

    let operatorMatch;
    while ((operatorMatch = operatorsRegex.exec(row.ruleText)) !== null) {
        matchedOperators.push(operatorMatch[1]);
    }

    setFields(matchedFields);
    setComparators(matchedComparators);

    const operatorLength = Math.max(matchedFields.length - 1, 0);
    const filledOperators = matchedOperators.length > 0 ? matchedOperators.slice(0, operatorLength) : [];
    setOperators(filledOperators.map(op => op === 'or' ? Operator.OR : Operator.AND));
    setViewType(1);
};


  const handleAddRow = () => {
    setComparators([...comparators, 0]);
    setFields([...fields, '']);
    setOperators([...operators, Operator.AND]);
  };

  const handleClose = () => {
    setViewType(0);
  };

  const isAddRuleDisabled = () => {
    if (fields.length === 0 || fields.some(field => field.length === 0) || operators.some((operator, index) => index < fields.length - 1 && !operator)) {
      return true;
    }

    return JSON.stringify(initialState) === JSON.stringify({ comparators, fields, operators });
  };

  const handleAddUndefinedRule = () => {
    handleAddRow();
    setViewType(1);
  };

  return (
    <Box>
        <Box sx={{ mr: 'auto', display: 'flex', flexDirection: 'row', gap: 2 ,  marginBottom: '40px'}}>
          <Button variant="outlined"  disabled={viewType === 1} size="medium" onClick={() => setViewType(0)}>
            Source View
          </Button>
          <Button variant="outlined" disabled={viewType === 0} size="medium" onClick={() => setViewType(1)}>
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
              alignItems: 'center',
            }}
          >
            <SourceView
              data={transformRulesToRowData({ probabilistic: probabilisticRows })}
              onEditRow={handleRowEdit}
              onAddUndefinedRule={handleAddUndefinedRule}
              hasUndefinedRule={hasUndefinedRule}
            />
          </Box>
        ) : (
          <>
            {fields.map((field, index) => (
              <Box
                key={index}
                sx={{
                  display: 'flex',
                  flexDirection: 'row',
                  width: '100%',
                  justifyContent: 'center',
                  gap: 2,
                  marginTop: '20px',
                  marginBottom: '20px',
                }}
              >
                <FormControl fullWidth>
                  <InputLabel id={`select-operator-label-${index}`}>Select Operator</InputLabel>
                  <Select
                    labelId={`select-operator-label-${index}`}
                    id="select-operator"
                    value={index === 0 ? '' : operators[index - 1] || ''}
                    label="Select Operator"
                    onChange={event => handleOperatorChange(index - 1, event)}
                    disabled={index === 0} 
                  >
                    {Object.values(Operator).map(op => (
                      <MenuItem key={op} value={op}>
                        {op}
                      </MenuItem>
                    ))}
                  </Select>
                </FormControl>
                <FormControl fullWidth>
                  <InputLabel id={`select-field-label-${index}`}>Select Field</InputLabel>
                  <Select
                    labelId={`select-field-label-${index}`}
                    id="select-field"
                    value={field}
                    label="Select Field"
                    onChange={event => handleFieldChange(index, event)}
                  >
                    {Array.isArray(demographicData) &&
                      demographicData.map(demographicField => (
                        <MenuItem
                          key={demographicField.fieldName}
                          value={demographicField.fieldName}
                          style={{
                            backgroundColor:
                              demographicField.fieldName === field
                                ? 'lightblue'
                                : 'inherit'
                          }}
                        >
                          {transformFieldName(demographicField.fieldName)}
                        </MenuItem>
                      ))}
                  </Select>
                </FormControl>
                <FormControl fullWidth>
                  <InputLabel id={`select-comparator-label-${index}`}>Select Comparator</InputLabel>
                  <Select
                    labelId={`select-comparator-label-${index}`}
                    id="select-comparator"
                    value={comparators[index]}
                    label="Select Comparator"
                    onChange={event => handleComparatorChange(index, event)}
                  >
                    {options.map(option => (
                      <MenuItem key={option.value} value={option.value}>
                        {option.label}
                      </MenuItem>
                    ))}
                  </Select>
                </FormControl>
              </Box>
            ))}
            <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', marginBottom:'40px', marginTop:'40px'}}>
              <IconButton aria-label="add" color="primary" onClick={handleAddRow}>
                <AddOutlined />
              </IconButton>
            </Box>
            <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center' }}>
              <Button variant="contained" color="primary" onClick={handleAddRule} disabled={isAddRuleDisabled()}>
                Add Rule
              </Button>
              <Button variant="contained" color="secondary" onClick={handleClose} sx={{ marginLeft: '20px' }}>
                Close
              </Button>
            </Box>
          </>
        )}
      </Box>
  );
};

export default BlockingContent;
