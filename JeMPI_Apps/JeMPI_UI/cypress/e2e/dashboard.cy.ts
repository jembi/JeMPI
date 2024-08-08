describe('Dashboard Component', () => {
  beforeEach(() => {
    cy.visit('/')
  })

  it('should display the dashboard tabs', () => {
    cy.get('[aria-label="dashboard tabs"]').should('be.visible')
  })

  it('should navigate through tabs', () => {
    // Check initial tab (Confusion Matrix)
    cy.get('[id="dashboard-tab-0"]').click()
    cy.get('#dashboard-tabpanel-0').should('be.visible')

    // Switch to M & U Values tab
    cy.get('[id="dashboard-tab-1"]').click()
    cy.get('#dashboard-tabpanel-1').should('be.visible')
    cy.get('#dashboard-tabpanel-0').should('not.be.visible')

    // Switch to Import Process Status tab
    cy.get('[id="dashboard-tab-2"]').click()
    cy.get('#dashboard-tabpanel-2').should('be.visible')
    cy.get('#dashboard-tabpanel-1').should('not.be.visible')
  })

  it('should display widgets in Confusion Matrix tab', () => {
    cy.get('[id="dashboard-tab-0"]').click()
    cy.get('#dashboard-tabpanel-0').within(() => {
      cy.get('fieldset legend').contains('Records').should('be.visible')
      cy.get('fieldset legend').contains('Notifications').should('be.visible')
      cy.get('legend').contains('Confusion Matrix').should('be.visible')
    })
  })

  it('should display M & U widget in M & U Values tab', () => {
    cy.get('[id="dashboard-tab-1"]').click()
    cy.get('#dashboard-tabpanel-1').within(() => {
      cy.get('legend').contains('M & U Values').should('be.visible')
    })
  })

  it('should render correctly with no data', () => {
    // Mock the useDashboardData hook to return no data
    cy.intercept('GET', '/api/dashboard-data', {
      body: {
        data: {
          dashboardData: {}
        }
      }
    })

    // Visit the page and click on the dashboard tab
    cy.visit('/')
    cy.get('[id="dashboard-tab-0"]').click()

    // Verify that the page renders correctly with no data
    cy.get('#dashboard-tabpanel-0').within(() => {
      cy.get('fieldset legend').contains('Records').should('be.visible')
      cy.get('fieldset legend').contains('Notifications').should('be.visible')
      cy.get('legend').contains('Confusion Matrix').should('be.visible')
    })
  })

  it('should display loading state correctly', () => {
    // Mock the useDashboardData hook to simulate loading state
    cy.intercept('GET', '/api/dashboard-data', req => {
      req.on('response', res => {
        res.setDelay(1000)
      })
    })
    cy.visit('/')
    cy.get('[id="dashboard-tab-0"]').click()
  })

  it('should handle API errors', () => {
    // Mock the useDashboardData hook to return an error
    cy.intercept('GET', '/api/dashboard-data', {
      statusCode: 500,
      body: { error: 'Internal Server Error' }
    })
    cy.visit('/')
    cy.get('[id="dashboard-tab-0"]').click()
  })
})
