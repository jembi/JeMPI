import { useState, useEffect, FC } from 'react'

type CountUpProps = {
  end: number
  duration: number
}

const CountUp: FC<CountUpProps> = ({ end, duration }) => {
  const [count, setCount] = useState<number>(0)
  useEffect(() => {
    let startTimestamp: number | null = null
    const step = (timestamp: number) => {
      if (startTimestamp === null) startTimestamp = timestamp
      const timeElapsed = timestamp - startTimestamp
      const progress = Math.min(timeElapsed / duration, 1)
      setCount(Math.floor(progress * end))
      if (progress < 1) {
        requestAnimationFrame(step)
      }
    }

    requestAnimationFrame(step)
  }, [end, duration])

  return <>{count.toLocaleString()}</>
}

export default CountUp
