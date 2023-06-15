import { PatientRecord } from 'types/PatientRecord'
import { SearchParameter } from 'types/SimpleSearch'

export const levenshtein = (a: string, b: string) => {
  if (!a.length) return b.length
  if (!b.length) return a.length
  const arr = []
  for (let i = 0; i <= b.length; i++) {
    arr[i] = [i]
    for (let j = 1; j <= a.length; j++) {
      arr[i][j] =
        i === 0
          ? j
          : Math.min(
              arr[i - 1][j] + 1,
              arr[i][j - 1] + 1,
              arr[i - 1][j - 1] + (a[j - 1] === b[i - 1] ? 0 : 1)
            )
    }
  }
  return arr[b.length][a.length]
}

export const isTermsSimilar = (
  patientAttribute: string,
  searchParam: string,
  distance: number
) => {
  return levenshtein(patientAttribute, searchParam) <= distance
}

export const isPatientCorresponding = (
  patient: PatientRecord,
  searchQuery: Array<SearchParameter>
) => {
  let isCorresponding = true
  if (searchQuery.length === 0) {
    isCorresponding = false
  } else {
    searchQuery.forEach(param => {
      {
        if (
          !isTermsSimilar(
            patient[param.fieldName] as string,
            param.value as string,
            param.distance
          )
        ) {
          isCorresponding = false
        }
      }
    })
  }
  return isCorresponding
}
