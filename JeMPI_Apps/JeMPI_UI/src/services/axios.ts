import axios from 'axios'
import { config } from '../config'
import { getCookie } from '../utils/misc'

const axiosInstance = axios.create({
  withCredentials: true,
  baseURL: config.apiUrl || 'http://localhost:50000/JeMPI',
  headers: { 'Content-Type': 'application/json' },
  responseType: 'json'
})

// Add a request interceptor to set CSRF and update loading state
axiosInstance.interceptors.request.use((request )=> {
  const { method } = request
  if (['post', 'patch', 'put', 'delete'].indexOf(method || '') !== -1) {
    const csrfToken = getCookie('XSRF-TOKEN')
    if (csrfToken) {
      request.headers['X-XSRF-TOKEN'] = csrfToken
    }
  }
  return request
})

// Add a response interceptor to handle server error and reset loading.
// axiosInstance.interceptors.response.use((response) => {
//   // ...
//   return response
// }, (error) => {
//   //..
//   return Promise.reject(error.response)
// })

export default axiosInstance
