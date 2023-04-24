import { ToggleButton, ToggleButtonGroup, Typography } from '@mui/material'
import { useRef, useState } from 'react'
import { ToggleButtonOptions } from '../../types/SimpleSearch'

export const triggerInputChange = (node: HTMLInputElement, value = '') => {
  // only process the change on elements we know have a value setter in their constructor
  // eslint-disable-next-line @typescript-eslint/ban-ts-comment
  // @ts-ignore
  const setValue = Object.getOwnPropertyDescriptor(node.__proto__, 'value').set
  const event = new Event('input', { bubbles: true })
  // eslint-disable-next-line @typescript-eslint/ban-ts-comment
  // @ts-ignore
  setValue.call(node, value)
  node.dispatchEvent(event)
}

interface ToggleButtonsProps {
  selectedButton: string
  name?: string
  onChange?: (e: React.ChangeEvent<HTMLInputElement>) => void
  options: ToggleButtonOptions[]
  disabled?: boolean
}

const ToggleButtons: React.FC<ToggleButtonsProps> = ({
  selectedButton,
  name,
  onChange,
  options,
  disabled = false
}) => {
  const [selected, setSelected] = useState(selectedButton)
  const ref = useRef<HTMLInputElement>(null)

  const handleToggle = (
    _e: React.MouseEvent<HTMLElement, MouseEvent>,
    value: string
  ) => {
    if (value && ref.current) {
      setSelected(value)
      triggerInputChange(ref.current, value)
    }
  }

  return (
    <>
      <ToggleButtonGroup
        value={selected}
        onChange={handleToggle}
        exclusive
        size={'small'}
        disabled={disabled}
        id={name}
      >
        {options.map(option => {
          return (
            <ToggleButton
              value={`${option.value}`}
              aria-label="left aligned"
              key={option.value}
            >
              <Typography id={name} variant="button">
                {option.label}
              </Typography>
            </ToggleButton>
          )
        })}
      </ToggleButtonGroup>
      <input
        ref={ref}
        name={name}
        onChange={onChange}
        style={{ display: 'none' }}
      />
    </>
  )
}

export default ToggleButtons
