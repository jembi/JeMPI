/// <reference types="cypress" />

describe('Browse Records', () => {
  beforeEach(() => {
    cy.visit(
      '/browse-records?isFetchingInteractions=false&parameters=%5B%5D&limit=25&offset=0&sortAsc=0&sortBy="auxDateCreated"'
    )
  })

  const openRecordDetails = () => {
    cy.get('.MuiDataGrid-row').first().dblclick()
  }

  describe('Page Header', () => {
    it('should display the page header', () => {
      cy.get('#page-header').contains('Browse Patients')
    })
  })

  describe('Filters', () => {
    beforeEach(() => {
      cy.get('#panel1a-header').click()
      cy.get('#panel1a-content').should('be.visible')
    })

    it('should expand and collapse the filter accordion', () => {
      cy.get('#panel1a-header').click()
      cy.get('#panel1a-content').should('not.be.visible')

      cy.get('#panel1a-header').click()
      cy.get('#panel1a-content').should('be.visible')
    })

    it('should display the start date filter', () => {
      cy.get(
        '#panel1a-content > div > div > div.MuiStack-root.css-uhybnf-MuiStack-root > div > div:nth-child(1) > div'
      ).should('be.visible')
    })

    it('should display the end date filter', () => {
      cy.get(
        '#panel1a-content > div > div > div.MuiStack-root.css-uhybnf-MuiStack-root > div > div:nth-child(2) > div'
      ).should('be.visible')
    })

    it('should toggle get interactions switch', () => {
      cy.get('#interactions-switch').click()
      cy.get(
        '#interactions-switch > span.MuiSwitch-root.MuiSwitch-sizeMedium.css-julti5-MuiSwitch-root > span.MuiButtonBase-root.MuiSwitch-switchBase.MuiSwitch-colorPrimary.Mui-checked.PrivateSwitchBase-root.MuiSwitch-switchBase.MuiSwitch-colorPrimary.Mui-checked.Mui-checked.css-byenzh-MuiButtonBase-root-MuiSwitch-switchBase > input'
      ).should('be.checked')
    })
  })

  describe('Search Results', () => {
    it('should display search results in the data grid', () => {
      cy.get('.MuiDataGrid-root').should('be.visible')
      cy.get('.MuiDataGrid-row').should('have.length.at.least', 1)
    })

    it('should navigate to the record details page on row double-click', () => {
      cy.get('.MuiDataGrid-row').first().dblclick()
      cy.url().should('include', '/record-details/')
    })
  })
  describe('Record Details Page', () => {
    beforeEach(() => {
      cy.visit('/browse-records')
      openRecordDetails()
    })

    it('It navigates to the record details page when a row is double-clicked', () => {
      cy.url().should('match', /\/record-details\/\w+/)
    })

    it('It displays the records table', () => {
      cy.get('.MuiDataGrid-root').should('be.visible')
    })

    it('It displays the audit trail table', () => {
      cy.get('.MuiDataGrid-root').eq(1).should('be.visible')
    })
    it('It enables the edit button when a record is selected', () => {
      cy.get('.MuiDataGrid-row').first().click()
      cy.get('button').contains('Edit').should('be.enabled')
    })

    it('It enables the save button when changes are made', () => {
      cy.get('.MuiDataGrid-row').first().click()
      cy.get('button').contains('Edit').click()
      cy.get('.MuiDataGrid-cell').first().dblclick()
      cy.get('button').contains('Save').should('be.enabled')
    })

    it('It discards changes when the cancel button is clicked', () => {
      cy.get('.MuiDataGrid-row').first().click()
      cy.get('button').contains('Edit').click()
      cy.get('.MuiDataGrid-cell').first().dblclick()
      cy.get('button').contains('Cancel').click()
      cy.get('.MuiDataGrid-cell').first().should('not.contain', 'New value')
    })

    it('It enables relink button on row double click', () => {
      openRecordDetails()
      cy.get('.MuiDataGrid-row').eq(1).dblclick()
      cy.get('button').contains('Relink')
      cy.get('button').contains('Relink').click()
      cy.contains('PATIENT LINKED TO GOLDEN RECORD').should('be.visible')
      cy.pause()
    })
  })

  describe('API Calls', () => {
    beforeEach(() => {
      cy.intercept('POST', '**/search', { fixture: 'searchResponse.json' }).as(
        'searchQuery'
      )
      cy.visit('/browse-records')
    })

    it('It displays search results in the data grid', () => {
      cy.get('.MuiDataGrid-root').should('be.visible')
      cy.get('.MuiDataGrid-row').should('have.length.at.least', 1)
    })
  })
})
