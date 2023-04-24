import { useState } from 'react'

interface CookieOptions {
  days?: number
  path?: string
  domain?: string
  SameSite?: 'None' | 'Lax' | 'Strict'
  Secure?: boolean
  HttpOnly?: boolean
}

const isBrowser = typeof window !== 'undefined'

export function stringifyOptions(options: CookieOptions) {
  return (Object.keys(options) as Array<keyof CookieOptions>).reduce(
    (acc, key) => {
      if (key === 'days') {
        return acc
      } else {
        if (options[key as keyof CookieOptions] === false) {
          return acc
        } else if (options[key] === true) {
          return `${acc}; ${key}`
        } else {
          return `${acc}; ${key}=${options[key]}`
        }
      }
    },
    ''
  )
}

export const setCookie = <T>(
  name: string,
  value: T,
  options: CookieOptions
) => {
  if (!isBrowser) return

  const optionsWithDefaults = {
    days: 7,
    path: '/',
    ...options
  }

  const expires = new Date(
    Date.now() + optionsWithDefaults.days * 864e5
  ).toUTCString()

  document.cookie =
    name +
    '=' +
    encodeURIComponent((value || '').toString()) +
    '; expires=' +
    expires +
    stringifyOptions(optionsWithDefaults)
}

export const getCookie = <T>(name: string, initialValue: T) => {
  return (
    (isBrowser &&
      document.cookie.split('; ').reduce((r, v) => {
        const parts = v.split('=')
        return parts[0] === name ? decodeURIComponent(parts[1]) : r
      }, '')) ||
    initialValue
  )
}

export const useCookie = <T>(
  key: string,
  initialValue: T,
  options: CookieOptions
) => {
  const [cookieItem, setItem] = useState(getCookie<T>(key, initialValue))
  const updateCookieItem = (value: T) => {
    setItem(value)
    setCookie<T>(key, value, options)
  }

  return {
    cookieItem,
    updateCookieItem
  }
}
