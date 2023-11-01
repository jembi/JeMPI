import { ExpandLess, ExpandMore } from '@mui/icons-material'
import { Box, Fab } from '@mui/material'
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
  const [scrollPosition, setScrollPosition] = useState(0)
  const [isScrollable, setIsScrollable] = useState(false)
  const handleScroll = () => {
    setScrollPosition(window.scrollY)
  }

  useEffect(() => {
    window.addEventListener('scroll', handleScroll)
    return () => window.removeEventListener('scroll', handleScroll)
  }, [])
  useEffect(() => {
    setIsScrollable(
      document.documentElement.scrollHeight >
        document.documentElement.clientHeight
    )
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [document.documentElement.scrollHeight])

  return (
    <>
      {isScrollable && (
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
