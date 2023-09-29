import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { FieldArray, Form, Formik } from 'formik'
import FieldGroup from '../../components/customSearch/FieldGroup'
import { AppConfigProvider } from '../../hooks/useAppConfig'
import {
  CustomSearchQuery,
  SearchParameter,
  SimpleSearchQuery
} from '../../types/SimpleSearch'
import routes from './Routes'

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {}
  }
})

const initialSearchParameter: SearchParameter = {
  fieldName: '',
  value: '',
  distance: 0
}

const initialValues: CustomSearchQuery = {
  $or: [
    {
      parameters: [initialSearchParameter]
    }
  ],
  sortBy: 'uid',
  sortAsc: true,
  offset: 0,
  limit: 10
}

const SearchRowTest: React.FC = () => {
  return (
    <QueryClientProvider client={queryClient}>
      <AppConfigProvider>
        <Formik
          initialValues={initialValues}
          onSubmit={() => {
            /*noop*/
          }}
        >
          {({ values, handleChange, setFieldValue }) => (
            <Form>
              <FieldArray name="$or">
                {({ push, remove }) => (
                  <>
                    {values.$or.map(
                      (parameters: SimpleSearchQuery, index: number) => {
                        return (
                          <FieldGroup
                            values={parameters}
                            initialCustomSearchValues={initialSearchParameter}
                            handleChange={handleChange}
                            fieldGroupIndex={index}
                            removeFieldGroup={remove}
                            setFieldValue={setFieldValue}
                            push={push}
                            key={index}
                          />
                        )
                      }
                    )}
                  </>
                )}
              </FieldArray>
            </Form>
          )}
        </Formik>
      </AppConfigProvider>
    </QueryClientProvider>
  )
}

export default SearchRowTest
