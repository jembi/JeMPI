import { createTheme } from '@mui/material'
declare module '@mui/material/Button' {
  interface ButtonPropsVariantOverrides {
    header: true
  }
}

declare module '@mui/material/styles' {
  interface TypographyVariants {
    dgSubTitle: React.CSSProperties
  }

  // allow configuration using `createTheme`
  interface TypographyVariantsOptions {
    dgSubTitle?: React.CSSProperties
  }
}

// Update the Typography's variant prop options
declare module '@mui/material/Typography' {
  interface TypographyPropsVariantOverrides {
    dgSubTitle: true
  }
}

const baseTheme = createTheme({
  palette: {
    background: {
      default: '#FAFAFA'
    }
  },
  components: {
    MuiAppBar: {
      styleOverrides: {
        colorPrimary: {
          background: 'white'
        }
      }
    },
    MuiButton: {
      styleOverrides: {
        root: ({ ownerState, theme }) => ({
          ...(ownerState.className === 'Appbar' && {
            color: theme.palette.text.primary
          })
        })
      },
      variants: [
        {
          props: { variant: 'header' },
          style: ({ theme }) => ({
            maxWidth: 'sm',
            color: 'white',
            backgroundColor: theme.palette.primary.main,
            '&:hover': { backgroundColor: theme.palette.primary.dark }
          })
        },
        {
          props: { variant: 'contained' },
          style: () => ({
            backgroundColor: '#274263',
            color: 'white',
            '&:hover': { backgroundColor: '#375982' },
            maxWidth: 'sm'
          })
        },
        {
          props: { variant: 'outlined' },
          style: ({ theme }) => ({
            borderColor: theme.palette.primary.main,
            color: theme.palette.primary.main,
            maxWidth: 'sm'
          })
        }
      ]
    },
    MuiCard: {
      styleOverrides: {
        root: () => ({
          marginTop: '33px',
          background: '#FFFFFF',
          boxShadow: '0px 0px 0px 1px #E0E0E0',
          borderRadius: '4px',
          display: 'flex',
          flexDirection: 'column',
          alignItems: 'center',
          justifyContent: 'center'
        })
      }
    },
    MuiPaper: {
      styleOverrides: {
        root: {
          '& .super-app-theme--header': {
            backgroundColor: '#274263',
            color: 'white'
          },
          '& .super-app-theme--linkHeader': {
            backgroundColor: '#274263',
            color: 'white',
            borderRadius: '0px'
          },
          '& .super-app-theme--searchable': {
            backgroundColor: '#c5e1a5',
            '&.MuiDataGrid-row:hover': {
              backgroundColor: '#a2cf6e'
            },
            '&.MuiDataGrid-row.Mui-selected': {
              backgroundColor: '#e2be1d',
              '&.MuiDataGrid-row:hover': { backgroundColor: '#fff08d' }
            }
          },
          '& .super-app-theme--SelectedPatient': {
            backgroundColor: '#c5e1a5',
            '&.MuiDataGrid-row:hover': {
              backgroundColor: '#a2cf6e'
            },
            '&.MuiDataGrid-row.Mui-selected': {
              backgroundColor: '#a2cf6e',
              '&:hover': { backgroundColor: '#c5e1a5' }
            }
          },
          '& .super-app-theme--Current': {
            backgroundColor: '#f5df68',
            '&.MuiDataGrid-row:hover': {
              backgroundColor: '#fff08d'
            },
            '&.MuiDataGrid-row.Mui-selected': {
              backgroundColor: '#e2be1d',
              '&:hover': { backgroundColor: '#fff08d' }
            }
          }
        }
      }
    },
    MuiToggleButton: {
      styleOverrides: {
        root: ({ theme }) => ({
          width: '130px',
          height: '42px',
          borderColor: theme.palette.primary.main,
          color: theme.palette.primary.main,
          '&.Mui-selected, &.Mui-selected:hover': {
            color: 'white',
            backgroundColor: theme.palette.primary.main
          }
        })
      }
    }
  },
  typography: {
    fontFamily: 'Roboto',
    h5: {
      color: 'rgba(0, 0, 0, 0.6)'
    },
    dgSubTitle: {
      fontFamily: 'Roboto',
      fontSize: '12px',
      fontWeight: 400,
      lineHeight: '32px',
      letterSpacing: '1px',
      textAlign: 'left'
    }
  }
})

export default baseTheme
