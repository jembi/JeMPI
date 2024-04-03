export const getTestEnvConfig = () => {
    return {
        isDev: process.env.NODE_ENV !== 'production',
        apiUrl: process.env.REACT_APP_JEMPI_BASE_API_HOST
          ? `${process.env.REACT_APP_JEMPI_BASE_API_HOST}:${process.env.REACT_APP_JEMPI_BASE_API_PORT}`
          : `http://localhost:50000/JeMPI`,
        shouldMockBackend: process.env.REACT_APP_MOCK_BACKEND === 'true',
        KeyCloakUrl: process.env.KC_FRONTEND_URL || 'http://localhost:8080',
        KeyCloakRealm: process.env.KC_REALM_NAME || 'jempi-dev',
        KeyCloakClientId: process.env.KC_JEMPI_CLIENT_ID || 'jempi-oauth',
        useSso: process.env.REACT_APP_ENABLE_SSO === 'true',
        maxUploadCsvSize: +(
          process.env.REACT_APP_MAX_UPLOAD_CSV_SIZE_IN_MEGABYTES || 128
        ),
        showBrandLogo: process.env.REACT_APP_SHOW_BRAND_LOGO === 'true',
        refetchInterval: +(
        process.env.REACT_APP_REFETCH_INTERVAL || 3000
        ),
        cacheTime: +(
          process.env.REACT_APP_CACHE_TIME || 3000
        ),
        staleTime: +(
          process.env.REACT_APP_CACHE_TIME || 3000
        )
      } 
}