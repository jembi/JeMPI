import { fireEvent, render, screen } from '@testing-library/react'
import '@testing-library/jest-dom/extend-expect'
import { BrowserRouter } from 'react-router-dom'
import mockData from 'services/mockData'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { ConfigProvider } from 'hooks/useConfig'
import ProbabilisticContent from 'pages/settings/probabilistic/ProbabilisticContent'

const queryClient = new QueryClient({
    defaultOptions: {
      queries: {}
    }
  })


const linkingRules: any = mockData.configuration.rules

describe('ProbabilisticContent Component', () => {
    it('renders correctly', () => {
      const { container } = render(
        <QueryClientProvider client={queryClient}>
          <BrowserRouter>
            <ConfigProvider>
              <ProbabilisticContent
                          linkingRules={linkingRules} currentTab={'link'}              />
            </ConfigProvider>
          </BrowserRouter>
        </QueryClientProvider>
      )
      expect(container).toMatchSnapshot()
    })

    it('handles input changes correctly', () => {
     
            render(
              <QueryClientProvider client={queryClient}>
                <BrowserRouter>
                  <ConfigProvider>
                    <ProbabilisticContent linkingRules={linkingRules} currentTab={'link'} />
                  </ConfigProvider>
                </BrowserRouter>
              </QueryClientProvider>
            );

        const linkThresholdInput = document.getElementById('link-threshold') as HTMLElement
        const minThresholdInput = document.getElementById('min-review-threshold') as HTMLElement
        const maxThresholdInput = document.getElementById('max-review-threshold') as HTMLElement
        const marginWindowSizeInput = document.getElementById('margin-window-size') as HTMLElement

        if(linkThresholdInput){
        fireEvent.change(linkThresholdInput, { target: { value: '0.5' } })
        expect(screen.getByDisplayValue('0.5')).toBeInTheDocument()
        }
        
        if(minThresholdInput){
            fireEvent.change(minThresholdInput, { target: { value: '0.2' } })
            expect(screen.getByDisplayValue('0.2')).toBeInTheDocument()
        }

        if(maxThresholdInput){
            fireEvent.change(maxThresholdInput, { target: { value: '0.8' } })
            expect(screen.getByDisplayValue('0.8')).toBeInTheDocument()
        } 

       if(marginWindowSizeInput){
        fireEvent.change(marginWindowSizeInput, { target: { value: '0.4' } })
        expect(screen.getByDisplayValue('0.4')).toBeInTheDocument()
       }
    
      })

      test('handles slider changes correctly', () => {
        render(
        <QueryClientProvider client={queryClient}>
        <BrowserRouter>
          <ConfigProvider>
            <ProbabilisticContent linkingRules={linkingRules} currentTab={'link'} />
          </ConfigProvider>
        </BrowserRouter>
        </QueryClientProvider>)
    
        const sliders = document.getElementById('slider-summary')
        if(sliders) {
        fireEvent.change(sliders, { target: { value: 0.3 } });
        expect(screen.getByDisplayValue('0.3')).toBeInTheDocument();
        }
     
      })
})