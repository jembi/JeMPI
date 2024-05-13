import CustomSearch from '../../components/customSearch/CustomSearch'
import SimpleSearch from '../../components/search/SimpleSearch'
import SearchResult from '../../components/searchResult/SearchResult'

const Routes = [
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
            element: <SearchResult title="Golden Records Only" />
          }
        ]
      }
    ]
  }
]

export default Routes
