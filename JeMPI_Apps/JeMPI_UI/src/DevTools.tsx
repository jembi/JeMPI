import { config } from 'config'
import { lazy } from 'react'

export const ReactQueryDevtools = !config.isDev
  ? () => <></>
  : lazy(() =>
      import('@tanstack/react-query-devtools').then(res => ({
        default: res.ReactQueryDevtools
      }))
    )
