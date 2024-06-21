import { Rule } from 'types/Configuration';
import * as Yup from 'yup';

export const marks = [
    { value: 0, label: '0' },
    { value: 0.1, label: '' },
    { value: 0.2, label: '' },
    { value: 0.3, label: '' },
    { value: 0.4, label: '' },
    { value: 0.5, label: '0.5' },
    { value: 0.6, label: '' },
    { value: 0.7, label: '' },
    { value: 0.8, label: '' },
    { value: 0.9, label: '' },
    { value: 1, label: '1' },
  ]

  export const defaultValues = {
    minReviewThreshold: '0.55',
    linkThreshold: '0.65',
    maxReviewThreshold: '0.75',
    marginWindowSize: '0.1'
  };
  
  export const validationSchema = Yup.object({
    linkThreshold: Yup.number()
      .min(0, 'Must be between 0 and 1')
      .max(1, 'Must be between 0 and 1')
      .required('Required'),
    minThreshold: Yup.number()
      .min(0, 'Must be between 0 and 1')
      .max(1, 'Must be between 0 and 1')
      .required('Required'),
    maxThreshold: Yup.number()
      .min(0, 'Must be between 0 and 1')
      .max(1, 'Must be between 0 and 1')
      .required('Required'),
    doNotLinkWindowStart: Yup.number()
      .min(0, 'Must be between 0 and 1')
      .max(1, 'Must be between 0 and 1')
      .required('Required'),
    doNotLinkWindowEnd: Yup.number()
      .min(0, 'Must be between 0 and 1')
      .max(1, 'Must be between 0 and 1')
      .required('Required'),
    marginWindowSize: Yup.number()
      .min(0, 'Must be between 0 and 1')
      .max(1, 'Must be between 0 and 1')
      .required('Required')
  })

  export const initializeValues = (rule: Rule) => ({
    minReviewThreshold: rule.reviewThresholdRange?.low.toString() || defaultValues.minReviewThreshold,
    linkThreshold: rule.linkThreshold?.toString() || defaultValues.linkThreshold,
    maxReviewThreshold: rule.reviewThresholdRange?.high.toString() || defaultValues.maxReviewThreshold,
    marginWindowSize: rule.marginWindowSize?.toString() || defaultValues.marginWindowSize
  });
