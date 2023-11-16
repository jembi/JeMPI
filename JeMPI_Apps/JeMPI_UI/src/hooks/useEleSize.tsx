import { useEffect, useRef, useState } from 'react'

type ElementSize = {
  width: number
  height: number
}

function useElementSizeById(id: string): {
  targetRef: React.MutableRefObject<null | HTMLElement>
  size: ElementSize
} {
  const [size, setSize] = useState<ElementSize>({ width: 0, height: 0 })
  const targetRef = useRef<null | HTMLElement>(null)

  useEffect(() => {
    const target = document.getElementById(id)
    if (target) {
      targetRef.current = target
      const resizeObserver = new ResizeObserver(entries => {
        for (const entry of entries) {
          const { width, height } = entry.contentRect
          setSize({ width, height })
        }
      })
      resizeObserver.observe(target)
      return () => {
        resizeObserver.unobserve(target)
        resizeObserver.disconnect()
      }
    }
  }, [id])

  return { targetRef, size }
}

export default useElementSizeById
