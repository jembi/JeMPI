import { config } from 'config'
import { lazy } from 'react'

export const ReactLocationDevtools = !config.isDev
  ? () => <></>
  : lazy(() =>
      import('@tanstack/react-location-devtools').then(res => ({
        default: res.ReactLocationDevtools
      }))
    )

export const ReactQueryDevtools = !config.isDev
  ? () => <></>
  : lazy(() =>
      import('@tanstack/react-query-devtools').then(res => ({
        default: res.ReactQueryDevtools
      }))
    )
