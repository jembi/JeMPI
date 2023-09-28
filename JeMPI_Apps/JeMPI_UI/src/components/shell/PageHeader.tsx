import HomeIcon from '@mui/icons-material/Home'
import {
  Breadcrumbs,
  Grid,
  IconButton,
  Link,
  Stack,
  Typography
} from '@mui/material'
import { useNavigate } from '@tanstack/react-location'

interface PageHeaderProps {
  breadcrumbs?: Array<{ icon?: JSX.Element; title?: string; link?: string }>
  buttons?: JSX.Element[]
  title: string
  description?: string | JSX.Element
  color?: string
}

const PageHeader: React.FC<PageHeaderProps> = ({
  breadcrumbs,
  buttons,
  title,
  description,
  color = 'rgba(0, 0, 0, 0.87)'
}) => {
  const Navigate = useNavigate()
  return (
    <Grid container justifyContent="space-between">
      <Grid item container direction="column" xs="auto">
        {breadcrumbs && breadcrumbs.length > 0 && (
          <Grid item>
            <Breadcrumbs>
              <IconButton href="/">
                <HomeIcon />
              </IconButton>
              {breadcrumbs?.map(({ icon, title, link }, idx) => {
                return (
                  <Link
                    underline="hover"
                    color="inherit"
                    onClick={() => Navigate({ to: link })}
                    key={idx}
                  >
                    <Stack direction={'row'} spacing={1}>
                      {icon}
                      <Typography sx={{ fontSize: '16px' }}>{title}</Typography>
                    </Stack>
                  </Link>
                )
              })}
            </Breadcrumbs>
          </Grid>
        )}
        <Grid item>
          <Typography
            variant="h5"
            sx={{
              fontSize: '34px',
              fontWeight: 400,
              color: color
            }}
          >
            {title}
          </Typography>
          {typeof description === 'string' ? (
            <Typography
              sx={{
                fontSize: '16px',
                fontWeight: 400,
                color: 'rgba(0, 0, 0, 0.6)'
              }}
            >
              {description}
            </Typography>
          ) : (
            description
          )}
        </Grid>
      </Grid>
      <Grid item container spacing={1} xs="auto">
        {buttons?.map((button, idx) => (
          <Grid key={idx} item>
            {button}
          </Grid>
        ))}
      </Grid>
    </Grid>
  )
}

export default PageHeader
