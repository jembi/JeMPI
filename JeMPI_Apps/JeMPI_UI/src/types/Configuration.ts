interface Field {
  fieldName: string
  fieldType: string
  csvCol?: number
  source?: string
  default?: string
  indexGoldenRecord?: string
  indexInteraction?: string
  linkMetaData?: {
    comparison: string
    comparisonLevels: number[]
    m: number
    u: number
  }
}

interface Rule {
  vars: string[]
  text: string
}

interface DeterministicRule {
  [key: string]: Rule
}

interface ProbabilisticRule {
  [key: string]: Rule
}

interface LinkRules {
  deterministic: DeterministicRule
  probabilistic: ProbabilisticRule
}

export interface Configuration {
  uniqueInteractionFields: Field[]
  uniqueGoldenRecordFields: Field[]
  additionalNodes: {
    nodeName: string
    fields: Field[]
  }[]
  demographicFields: Field[]
  rules: {
    link: {
      deterministic: DeterministicRule
      probabilistic: ProbabilisticRule
    }
  }
}