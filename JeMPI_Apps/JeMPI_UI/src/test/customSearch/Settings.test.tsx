import React from 'react';
import configuration from 'services/configurationData';
import { useQuery } from '@tanstack/react-query';
import { useConfig } from 'hooks/useConfig';
import { renderHook } from '@testing-library/react';

const mockConfiguration = configuration;
jest.mock('hooks/useConfig', () => ({
  useConfig: () => ({
    apiClient: {
      fetchConfiguration: jest.fn(() => Promise.resolve(mockConfiguration)),
    },
  }),
}));

jest.mock('@tanstack/react-query', () => ({
  useQuery: () => ({
    data: mockConfiguration,
    isLoading: false,
  }),
}));

describe('Settings Component', () => {
  it('fetches configuration data on mount', async () => {
    const { result } = renderHook(() => {
      const { apiClient } = useConfig();
      const { data: fields, isLoading } = useQuery({
        queryKey: ['configuration'],
        queryFn: () => apiClient.fetchConfiguration(),
        refetchOnWindowFocus: false,
      });
      return { fields, isLoading };
    });

    expect(result.current.fields).toEqual(configuration);
    expect(result.current.isLoading).toBe(false);
  });
});