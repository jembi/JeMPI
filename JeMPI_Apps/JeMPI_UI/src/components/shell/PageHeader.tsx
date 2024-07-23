import HomeIcon from '@mui/icons-material/Home'
import {
  Breadcrumbs,
  Grid,
  IconButton,
  Link,
  Stack,
  Typography
} from '@mui/material'
import { useNavigate } from 'react-router-dom'

interface PageHeaderProps {
  id?: string;
  breadcrumbs?: Array<{ icon?: JSX.Element; title?: string; link?: string }>
  buttons?: JSX.Element[]
  title: string
  color?: string
}

const PageHeader: React.FC<PageHeaderProps> = ({
  breadcrumbs,
  buttons,
  title,
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
                    onClick={() => Navigate(link || '')}
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
            id='page-header'
            sx={{
              fontSize: '34px',
              fontWeight: 400,
              color: color
            }}
          >
            {title}
          </Typography>
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
