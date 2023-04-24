import {
  CalendarMonth as CalendarIcon,
  CircleOutlined as CircleOutlinedIcon,
  Person as PersonIcon,
  Star as StarIcon
} from '@mui/icons-material'
import { Chip, Stack } from '@mui/material'
import { FC } from 'react'

import { useAppConfig } from '../../hooks/useAppConfig'
import { FieldType } from '../../types/Fields'
import { AnyRecord } from '../../types/PatientRecord'

const getIconByFieldType = (type: FieldType) => {
  switch (type) {
    case 'Date':
      return CalendarIcon
    case 'String':
    default:
      return PersonIcon
  }
}

type SubHeadingProps = { data: AnyRecord; isGoldenRecord: boolean }

const SubHeading: FC<SubHeadingProps> = ({ data, isGoldenRecord }) => {
  const { getFieldsByGroup } = useAppConfig()
  const chips = getFieldsByGroup('sub_heading')
    .filter(({ fieldName }) => fieldName in data)
    .map(({ fieldName, fieldType, formatValue }) => {
      return {
        fieldName,
        label: formatValue(data[fieldName]),
        Icon: getIconByFieldType(fieldType)
      }
    })

  return (
    <Stack direction="row" spacing={1} sx={{ my: 3 }}>
      {chips.map(({ label, Icon, fieldName }) =>
        fieldName === 'uid' ? (
          <Chip
            key={fieldName}
            icon={isGoldenRecord ? <StarIcon /> : <CircleOutlinedIcon />}
            label={data.uid}
            color={isGoldenRecord ? 'primary' : 'default'}
          />
        ) : (
          <Chip key={fieldName} icon={<Icon />} label={label} />
        )
      )}
    </Stack>
  )
}

export default SubHeading
