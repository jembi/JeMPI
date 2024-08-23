export interface Source {
  csvCol?: number;
  interactionField?: string;
}

export interface LinkMetaData {
  comparison: string;
  comparisonLevels: number[];
  m: number;
  u: number;
}

export type FieldType = 'String' | 'DateTime' | 'Bool' | 'Number';

export interface Field {
  id?: string;
  fieldName: string;
  fieldType: FieldType;
  source?: Source;
  default?: string;
  indexGoldenRecord?: string;
  indexInteraction?: string;
  linkMetaData?: LinkMetaData;
  isDisabled?: boolean; 
}



export interface CustomNode {
  id?: string;
  nodeName: string;
  fields: Field[];
}

export interface Rule {
  id?: string;
  vars: string[];
  text: string;
  linkThreshold?: number; 
  marginWindowSize?: number; 
  reviewThresholdRange?: { low: number, high: number }; 
}

export interface LinkingRules {
  deterministic: Rule[];
  probabilistic?: Rule[];
}

export interface Rules {
  link: LinkingRules;
  validate: LinkingRules;
  matchNotification: LinkingRules;
}

export interface Configuration {
  auxInteractionFields: Field[];
  auxGoldenRecordFields: Field[];
  additionalNodes: CustomNode[];
  demographicFields: Field[];
  rules: Rules;
}
