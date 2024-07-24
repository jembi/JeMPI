describe('NotificationWorklist component', () => {
  beforeEach(() => {
    cy.visit('/notifications');
    cy.populateData();
  });

  describe('Page Header', () => {
    it('should display the page header', () => {
      cy.get('#page-header').should('contain.text', 'Notification Worklist');
    });
  });

  describe('Filters', () => {
    it('should display the start date filter', () => {
      cy.get('#start-date-filter').should('be.visible');
    });

    it('should display the end date filter', () => {
      cy.get('#end-date-filter').should('be.visible');
    });

    it('should display the states dropdown', () => {
      cy.get('#single-chip').click();
      cy.contains('ALL').click();
    });

    it('should have the correct default selected value', () => {
      cy.get('#single-chip').click();
      cy.contains('OPEN').click({ force: true });
    });

    it('should allow selecting an option', () => {
      cy.get('#single-chip').click();
      cy.contains('ALL').click();
    });

    it('should not allow multiple selections', () => {
      cy.get('#single-chip').click();
      cy.contains('ALL').click();
      cy.get('#single-chip').click();
      cy.contains('CLOSED').click();
      cy.get('#single-chip').find('span.MuiChip-label').should('contain.text', 'CLOSED');
    });
  });

  describe('Data Grid', () => {
    it('should display the data grid', () => {
      cy.get('.MuiDataGrid-root').should('be.visible');
    });

    it('should display the pagination', () => {
      cy.get('.MuiPagination-root').should('be.visible');
    });
  });

  describe('API Calls', () => {
    it('should fetch notifications on mount', () => {
      cy.populateData();
      cy.request('/notifications')
        .its('body')
        .should((notifications) => {
          const expectedNotifications = [
            {
              id: '123',
              type: 'THRESHOLD',
              created: new Date('2023-05-03T00:00:00.000Z').toISOString(), 
              names: 'Bob Smith',
              patient_id: '0x5a',
              status: 'CLOSED',
              old_golden_id: '0x9493',
              current_golden_id: '0x9833',
              score: 0.5,
              candidates: [
                {
                  golden_id: '0x45',
                  score: 0.4
                }
              ]
            }
          ];

          expect(notifications).to.deep.equal(expectedNotifications);
        });
    });

    it('should refetch notifications on filter change', () => {
      cy.populateData();
      cy.visit('/notifications');
      cy.get('#filter-button').click();
      cy.wait('@fetchNotifications', { timeout: 10000 });
    });
  });
});
