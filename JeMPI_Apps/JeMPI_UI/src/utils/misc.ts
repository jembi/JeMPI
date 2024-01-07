export function range(startAt: number, size: number) {
  return Array.from(Array(size).keys()).map(i => i + startAt)
}

export function parseQuery(queryString: string) {
  const pairs = (
    queryString[0] === '?' || queryString[0] === '#'
      ? queryString.substring(1)
      : queryString
  ).split('&')
  return pairs.reduce((acc, curr) => {
    const [key, value] = curr.split('=')
    acc[decodeURIComponent(key)] = decodeURIComponent(value || '')
    return acc
  }, {} as { [key: string]: string })
}

export const getCookie = (name: string) => {
  return document.cookie.split('; ').reduce((r, v) => {
    const parts = v.split('=')
    return parts[0] === name ? decodeURIComponent(parts[1]) : r
  }, '')
}
