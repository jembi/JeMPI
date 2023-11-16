import { ExpandLess, ExpandMore } from '@mui/icons-material'
import { Box, Fab } from '@mui/material'
import useElementSizeById from 'hooks/useEleSize'
import { useEffect, useState } from 'react'

const scrollToBottom = () => {
  window.scrollTo({
    top: document.documentElement.scrollHeight,
    behavior: 'smooth'
  })
}
const scrollToTop = () => {
  window.scrollTo({
    top: 0,
    behavior: 'smooth'
  })
}

const ScrollBackButtons = () => {
  const { size } = useElementSizeById('root')
  const [scrollPosition, setScrollPosition] = useState(0)

  useEffect(() => {
    window.addEventListener('scroll', () => setScrollPosition(window.scrollY))
    return () =>
      window.removeEventListener('scroll', () =>
        setScrollPosition(window.scrollY)
      )
  }, [])

  return (
    <>
      {size.height > window.innerHeight && (
        <Box
          sx={{
            display: 'flex',
            flexDirection: 'column',
            gap: 1,
            position: 'fixed',
            bottom: 16,
            right: 16
          }}
        >
          {scrollPosition > 0 && (
            <Fab size="small" onClick={scrollToTop} color="secondary">
              <ExpandLess />
            </Fab>
          )}
          {scrollPosition === 0 && (
            <Fab size="small" onClick={scrollToBottom} color="primary">
              <ExpandMore />
            </Fab>
          )}
        </Box>
      )}
    </>
  )
}

export default ScrollBackButtons
