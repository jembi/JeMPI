import { lazy } from 'react'

export const ReactQueryDevtools =
  process.env.NODE_ENV !== 'development'
    ? () => <></>
    : lazy(() =>
        import('@tanstack/react-query-devtools').then(res => ({
          default: res.ReactQueryDevtools
        }))
      )
