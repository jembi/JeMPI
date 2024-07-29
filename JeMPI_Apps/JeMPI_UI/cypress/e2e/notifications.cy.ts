describe('Notifications', () => {
  beforeEach(() => {
    cy.visit('/notifications');
  });

  describe('Page Header', () => {
    it('should display the page header', () => {
      cy.get('#page-header').should('contain.text', 'Notification Worklist');
    });
  });

  describe('Filters', () => {
    it('should display the start date filter', () => {
      cy.contains('Start Date').should('be.visible');
    });

    it('should display the end date filter', () => {
      cy.contains('End Date').should('be.visible');
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
      cy.contains('OPEN').should('be.visible');
    });

    it('should display the pagination', () => {
      cy.contains('Rows per page:').should('be.visible');
    });
  });


  describe('API Calls', () => {
    beforeEach(() => {
      cy.intercept("POST", "http://localhost:50000/JeMPI/notifications", { fixture: "notifications.json" }).as('getNotifications');
      cy.visit("/notifications");
    });
  
    it("It mocks API response", () => {
      cy.get('#notification-container').should('be.visible');
      cy.get('#notification-container .MuiDataGrid-main .MuiDataGrid-virtualScroller .MuiDataGrid-row').should('exist');
      cy.get('#notification-container .MuiDataGrid-main .MuiDataGrid-virtualScroller .MuiDataGrid-row').should('have.length',25);
    });
  });
  
});
 
  
