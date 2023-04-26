import DeleteIcon from '@mui/icons-material/Delete'
import { Button, Grid, SelectChangeEvent, Typography } from '@mui/material'
import { FieldArray } from 'formik'
import { SearchParameter, SimpleSearchQuery } from '../../types/SimpleSearch'
import SearchRow from '../search/SearchRow'
import AddFieldOrGroupButton from './AddFieldOrGroupButton'
interface FieldGroupProps {
  values: SimpleSearchQuery
  handleChange: (e: SelectChangeEvent<string>) => void
  initialCustomSearchValues: SearchParameter
  fieldGroupIndex: number
  removeFieldGroup: <T>(index: number) => T | undefined
  setFieldValue: (
    field: string,
    value: string | Date | number,
    shouldValidate?: boolean | undefined
  ) => void
  push: (obj: { parameters: Array<SearchParameter> }) => void
}

const FieldGroup: React.FC<FieldGroupProps> = ({
  values,
  handleChange,
  initialCustomSearchValues,
  fieldGroupIndex,
  removeFieldGroup,
  setFieldValue
}) => {
  return (
    <Grid item container direction="column" width="100%" alignItems={'center'}>
      {fieldGroupIndex > 0 ? (
        <Grid
          item
          container
          direction={'row'}
          justifyContent={'center'}
          sx={{ mb: 2 }}
        >
          <Typography sx={{ fontWeight: 800 }}>Or</Typography>
        </Grid>
      ) : null}

      <Grid
        item
        container
        direction="column"
        width="fit-content"
        alignItems={'center'}
        sx={{
          borderRadius: '4px',
          boxShadow: '0px 0px 0px 1px #E0E0E0',
          display: 'flex',
          mb: 3
        }}
      >
        {fieldGroupIndex > 0 ? (
          <Grid
            item
            container
            direction={'row'}
            justifyContent={'flex-end'}
            sx={{ pr: 1, mt: 1 }}
          >
            <Button
              variant="text"
              color={'error'}
              startIcon={<DeleteIcon />}
              onClick={() => {
                removeFieldGroup(fieldGroupIndex)
              }}
            >
              Remove group
            </Button>
          </Grid>
        ) : null}

        <FieldArray name={`$or[${fieldGroupIndex}].parameters`}>
          {({ push, remove }) => (
            <>
              {values.parameters.map((p: SearchParameter, index: number) => {
                const parameter = values.parameters[index]
                return (
                  <SearchRow
                    parameter={parameter}
                    index={index}
                    onChange={handleChange}
                    key={index}
                    setFieldValue={setFieldValue}
                    fieldGroupIndex={fieldGroupIndex}
                    remove={remove}
                    enableDelete={index > 0}
                    isCustomRow={true}
                  />
                )
              })}

              <Grid
                item
                container
                direction={'row'}
                justifyContent={'center'}
                width="100%"
              >
                <AddFieldOrGroupButton
                  onClick={push}
                  initialCustomSearchValues={initialCustomSearchValues}
                  label="Add Search Rule"
                />
              </Grid>
            </>
          )}
        </FieldArray>
      </Grid>
    </Grid>
  )
}

export default FieldGroup
