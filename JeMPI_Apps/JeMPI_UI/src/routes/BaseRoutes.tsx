import { Login } from '@mui/icons-material'
import { Route } from '@tanstack/react-location'
import Records from 'components/browseRecords/BrowseRecords'
import NotFound from 'components/error/NotFound'
import Import from 'components/import/Import'
import RecordDetails from 'components/recordDetails/RecordDetails'
import NotificationWorklist from 'components/notificationWorklist/NotificationWorklist'
import ReviewLink from 'components/reviewLink/ReviewLink'

const baseRoutes: Route[] = [
  { path: 'login', element: <Login /> },
  {
    path: '',
    children: [
      {
        path: '/',
        element: <NotFound />
      },
      {
        path: 'browse-records',
        children: [
          { path: '/', element: <Records /> },
          {
            path: 'record-details',
            children: [
              {
                path: ':uid',
                children: [
                  {
                    path: '/',
                    element: <RecordDetails />,
                    loader: async ({ params }) => ({
                      uid: params.uid
                    })
                  },
                  {
                    path: 'relink',
                    element: <ReviewLink />
                  }
                ]
              }
            ]
          }
        ]
      },
      {
        path: 'notifications',
        children: [
          {
            path: '/',
            element: <NotificationWorklist />
          },
          {
            path: 'match-details',
            element: <ReviewLink />
          }
        ]
      },
      { path: 'import', element: <Import /> }
    ]
  },
  { element: <NotFound /> }
]

export default baseRoutes
