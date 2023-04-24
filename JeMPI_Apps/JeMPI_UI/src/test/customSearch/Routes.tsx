import { Route } from '@tanstack/react-location'
import CustomSearch from '../../components/customSearch/CustomSearch'
import SimpleSearch from '../../components/search/SimpleSearch'
import SearchResult from '../../components/searchResult/SearchResult'

const Routes: Route[] = [
  {
    path: '',
    children: [
      {
        path: 'search',
        children: [
          {
            path: 'simple',
            element: <SimpleSearch />
          },
          {
            path: 'custom',
            element: <CustomSearch />
          }
        ]
      },
      {
        path: 'search-results',
        children: [
          {
            path: 'golden',
            element: (
              <SearchResult isGoldenRecord={true} title="Golden Records Only" />
            )
          },
          {
            path: 'patient',
            element: (
              <SearchResult
                isGoldenRecord={false}
                title="Patient Records Only"
              />
            )
          }
        ]
      }
    ]
  }
]

export default Routes
