/// <reference types="cypress" />

describe('Settings Page', () => {
  beforeEach(() => {
    cy.visit('/settings')
  })

  it('should render all tabs', () => {
    cy.get('button[role="tab"]').should('have.length', 7)
    cy.get('button[role="tab"]').eq(0).should('contain', 'Common')
    cy.get('button[role="tab"]')
      .eq(1)
      .should('contain', 'Unique to Golden Record')
    cy.get('button[role="tab"]')
      .eq(2)
      .should('contain', 'Unique to Interaction')
    cy.get('button[role="tab"]').eq(3).should('contain', 'Golden Records Lists')
    cy.get('button[role="tab"]').eq(4).should('contain', 'Deterministic')
    cy.get('button[role="tab"]').eq(5).should('contain', 'Blocking')
    cy.get('button[role="tab"]').eq(6).should('contain', 'Probabilistic')
  })

  it('should switch between tabs', () => {
    cy.get('button[role="tab"]').eq(1).click()
    cy.get('[role="tabpanel"]')
      .eq(1)
      .should(
        'contain',
        'Setup properties that are unique to the golden record'
      )

    cy.get('button[role="tab"]').eq(2).click()
    cy.get('[role="tabpanel"]')
      .eq(2)
      .should('contain', 'Setup properties that are unique to the interaction')

    cy.get('button[role="tab"]').eq(3).click()
    cy.get('[role="tabpanel"]')
      .eq(3)
      .should('contain', 'Setup properties for Golden record lists')
  })

  it('should save configuration', () => {
    cy.intercept('POST', '/http://localhost:50000/JeMPI/configuration', {
      response: 'ok'
    }).as('saveConfig')

    cy.get('button').contains('Save').click()

    cy.get('.SnackbarContent-root').should(
      'contain',
      'Successfully saved configuration'
    )
  })

  it('should allow editing and saving a row in Common Settings', () => {
    cy.get('button[role="tab"]').eq(0).click()

    cy.get('[data-id="1"]').find('#edit-button').click()

    cy.get('[data-id="1"]')
      .find('[data-field="fieldName"] input')
      .clear()
      .type('new_field_name')

    cy.get('[data-id="1"]').find('#save-button').click()

    cy.get('[data-id="1"]').should('contain', 'New Field Name')
  })

  it('should retain original value if editing is cancelled', () => {
    cy.get('button[role="tab"]').eq(0).click()

    cy.get('[data-id="1"]').find('#edit-button').click()

    cy.get('[data-id="1"]')
      .find('[data-field="fieldName"]')
      .invoke('text')
      .as('originalFieldName')

    cy.get('[data-id="1"]')
      .find('[data-field="fieldName"] input')
      .clear()
      .type('new_field_name')

    cy.get('[data-id="1"]').find('#cancel-button').click()

    cy.get('@originalFieldName').then(originalFieldName => {
      cy.get('[data-id="1"]').should('contain', originalFieldName)
    })
  })
  describe('Unique to Golden Record End-to-End-Tests', () => {
    beforeEach(() => {
      cy.get('button[role="tab"]').eq(1).click()
    })

    it('should allow editing and saving a row', () => {
      cy.get('[data-id="1"]').find('#edit-button').click()
      cy.get('[data-id="1"]')
        .find('[data-field="fieldName"] input')
        .clear()
        .type('new_field_name')
      cy.get('[data-id="1"]').find('#save-button').click()
      cy.get('[data-id="1"]').should('contain', 'New Field Name')
    })

    it('should retain original value if editing is cancelled', () => {
      cy.get('[data-id="1"]').find('#edit-button').click()
      cy.get('[data-id="1"]')
        .find('[data-field="fieldName"]')
        .invoke('text')
        .as('originalFieldName')
      cy.get('[data-id="1"]')
        .find('[data-field="fieldName"] input')
        .clear()
        .type('new_field_name')
      cy.get('[data-id="1"]').find('#cancel-button').click()
      cy.get('@originalFieldName').then(originalFieldName => {
        cy.get('[data-id="1"]').should('contain', originalFieldName)
      })
    })
  })

  describe('UniqueToInteraction Tab End-to-End Tests', () => {
    beforeEach(() => {
      cy.get('button[role="tab"]').eq(2).click()
    })

    it('should allow editing and saving a row', () => {
      cy.get('[data-id="1"]').find('#edit-button').click()
      cy.get('[data-id="1"]')
        .find('[data-field="fieldName"] input')
        .clear()
        .type('new_field_name')
      cy.get('[data-id="1"]').find('#save-button').click()
      cy.get('[data-id="1"]').should('contain', 'New Field Name')
      cy.pause()
    })

    it('should retain original value if editing is cancelled', () => {
      cy.get('[data-id="1"]').find('#edit-button').click()
      cy.get('[data-id="1"]')
        .find('[data-field="fieldName"]')
        .invoke('text')
        .as('originalFieldName')
      cy.get('[data-id="1"]')
        .find('[data-field="fieldName"] input')
        .clear()
        .type('new_field_name')
      cy.get('[data-id="1"]').find('#cancel-button').click()
      cy.get('@originalFieldName').then(originalFieldName => {
        cy.get('[data-id="1"]').should('contain', originalFieldName)
      })
    })
  })

  describe('Golden Record Lists End-to-End Tests', () => {
    beforeEach(() => {
      cy.get('button[role="tab"]').eq(3).click()
    })

    it('should allow editing and saving a row', () => {
      cy.get('.MuiDataGrid-row').first().find('#edit-button').click()
      cy.wait(1000)
      cy.get('.MuiDataGrid-row')
        .first()
        .find('[data-field="fieldName"] input')
        .clear()
        .type('facility')
      cy.wait(1000)
      cy.get('.MuiDataGrid-row').first().find('#save-button').click()
      cy.wait(1000) 
      cy.get('.MuiDataGrid-row').should('contain', 'Facility')
    })
    it('should retain original value if editing is cancelled', () => {
      cy.get('.MuiDataGrid-row').first().find('#edit-button').click()
      cy.get('.MuiDataGrid-row')
        .first()
        .find('[data-field="fieldName"]')
        .invoke('text')
        .as('originalFieldName')
      cy.get('.MuiDataGrid-row')
        .first()
        .find('[data-field="fieldName"] input')
        .clear()
        .type('new_field_name')
      cy.get('.MuiDataGrid-row').first().find('#cancel-button').click()
      cy.get('@originalFieldName').then(originalFieldName => {
        cy.get('.MuiDataGrid-row').first().should('contain', originalFieldName)
      })
      cy.pause()
    })
 
  })

  describe('Deterministic End-to-End-Tests', () => {
    beforeEach(() => {
      cy.get('button[role="tab"]').eq(4).click()
    })
    it('should render the Deterministic tab and check initial tab', () => {
      cy.get('div.MuiCardContent-root').should('exist')
      cy.get('button[role="tab"]')
        .contains('Linking')
        .should('have.attr', 'aria-selected', 'true')
    })

    it('should switch tabs and display appropriate content', () => {
      cy.get('button[role="tab"]').contains('Validate').click()
      cy.get('button[role="tab"]')
        .contains('Validate')
        .should('have.attr', 'aria-selected', 'true')

      cy.get('button[role="tab"]').contains('Matching').click()
      cy.get('button[role="tab"]')
        .contains('Matching')
        .should('have.attr', 'aria-selected', 'true')
    })

    it('should render Deterministic Content and show the initial tab', () => {
      cy.get('div.MuiCardContent-root').should('exist')
      cy.get('button[role="tab"]')
        .contains('Linking')
        .should('have.attr', 'aria-selected', 'true')
    })

    it('should switch to Design View and add a new rule', () => {
      cy.get('[data-testid="edit-button-1"]').click()
      cy.get('#design-view-button').should('exist')
      cy.get('button[id="add-row-button"]').click()
    })

    it('should open select demographic field and select an option', () => {
      cy.get('button[id="edit-button-1"]').click()
      cy.document().then($doc => {
        const clickEvent = $doc.createEvent('MouseEvents')
        clickEvent.initEvent('mousedown', true, true)
        $doc
          .querySelector(`[data-testid="select-field-data-testid"]`)
          ?.dispatchEvent(clickEvent)
      })
      cy.get(`#select-field-phone_number`).should('be.visible', 'Phone Number')
      cy.get('#add-rule-button').click({ force: true })
    })

    it('should open select comparator field, select an option and add a rule', () => {
      cy.get('button[id="edit-button-1"]').click()
      cy.document().then($doc => {
        const clickEvent = $doc.createEvent('MouseEvents')
        clickEvent.initEvent('mousedown', true, true)
        $doc
          .querySelector(`[data-testid="select-comparator-data-testid"]`)
          ?.dispatchEvent(clickEvent)
      })
      cy.get(`#select-comparator-function-Exact`).should('be.visible', 'Exact')
      cy.get('#add-rule-button').click({ force: true })
    })

    it('should switch to Source View and verify source view functionality', () => {
      cy.get('#source-view-button').should('exist')
      cy.get('button[id="edit-button-1"]').click()
    })

    it('should handle rule deletion correctly', () => {
      cy.get('[data-testid="edit-button-1"]').click()
      cy.get('button[id="add-row-button"]').click()
      cy.get('button[id="delete-button"]').first().click()
    })
  })

  describe('Blocking End-to-End-Tests', () => {
    beforeEach(() => {
      cy.get('button[role="tab"]').eq(5).click()
    })

    it('should switch to Source View and add or edit a rule', () => {
      cy.get('#source-view-undefined-rule').then($addButton => {
        if ($addButton.length > 0) {
          cy.get('#source-view-undefined-rule').click()
        } else {
          cy.get('[data-testid="edit-button-1"]').click()

          cy.get('#add-rule-button').click()
        }
      })
    })
    it('should delete row when delete button is clicked', () => {
      cy.get('#source-view-undefined-rule').then($addButton => {
        if ($addButton.length > 0) {
          cy.get('#source-view-undefined-rule').click()
          cy.get('#delete-button').first().click()
        } else {
          cy.get('[data-testid="edit-button-1"]').click()
          cy.get('#delete-button').first().click()
        }
      })
    })
    it('should render source view and design view buttons', () => {
      cy.get('#source-view-button').should('be.visible')
      cy.get('#design-view-button').should('be.visible')
    })
    it('should open select comparator field, select an option and add a rule', () => {
      cy.get('#source-view-undefined-rule').then($addButton => {
        if ($addButton.length > 0) {
          cy.get('#source-view-undefined-rule').click()
          cy.document().then($doc => {
            const clickEvent = $doc.createEvent('MouseEvents')
            clickEvent.initEvent('mousedown', true, true)
            $doc
              .querySelector(`[data-testid="select-comparator-data-testid"]`)
              ?.dispatchEvent(clickEvent)
          })
          cy.get(`#select-comparator`).should('be.visible', 'Exact')
        } else {
          cy.get('[data-testid="edit-button-1"]').click()
          cy.document().then($doc => {
            const clickEvent = $doc.createEvent('MouseEvents')
            clickEvent.initEvent('mousedown', true, true)
            $doc
              .querySelector(`[data-testid="select-comparator-data-testid"]`)
              ?.dispatchEvent(clickEvent)
          })
          cy.get(`#select-comparator-function-Exact`).should(
            'be.visible',
            'Exact'
          )
        }
      })
    })

    it('should open select demographic field and select an option', () => {
      cy.get('#source-view-undefined-rule').then($addButton => {
        if ($addButton.length > 0) {
          cy.get('#source-view-undefined-rule').click()
          cy.document().then($doc => {
            const clickEvent = $doc.createEvent('MouseEvents')
            clickEvent.initEvent('mousedown', true, true)
            $doc
              .querySelector(`[data-testid="select-comparator-data-testid"]`)
              ?.dispatchEvent(clickEvent)
          })
          cy.get(`#select-field`).should('be.visible', 'Phone Number')
        } else {
          cy.get('[data-testid="edit-button-1"]').click()
          cy.document().then($doc => {
            const clickEvent = $doc.createEvent('MouseEvents')
            clickEvent.initEvent('mousedown', true, true)
            $doc
              .querySelector(`[data-testid="select-field-data-testid"]`)
              ?.dispatchEvent(clickEvent)
          })
          cy.get(`#select-field-phone_number`).should(
            'be.visible',
            'Phone Number'
          )
        }
      })
    })

    it('should close switch to source view the when close button is clicked', () => {
      cy.get('#source-view-undefined-rule').then($addButton => {
        if ($addButton.length > 0) {
          cy.get('#source-view-undefined-rule').click()
          cy.get('#close-button').click()
          cy.get('#source-view-button').should('be.visible')
        } else {
          cy.get('[data-testid="edit-button-1"]').click()
          cy.get('#close-button').click()
          cy.get('#source-view-button').should('be.visible')
        }
      })
    })

    it('should add new row when add button is clicked', () => {
      cy.get('#source-view-undefined-rule').then($addButton => {
        if ($addButton.length > 0) {
          cy.get('#source-view-undefined-rule').click()
          cy.get('#add-row-button').click()
          cy.get('#select-field').should('be.visible', 'Select Field')
        } else {
          cy.get('[data-testid="edit-button-1"]').click()
          cy.get('#add-row-button').click()
          cy.get('#select-field').should('be.visible', 'Select Field')
        }
      })
    })
  })

  describe('Probabilistic End-to-End-Tests', () => {
    beforeEach(() => {
      cy.get('button[role="tab"]').eq(6).click()
    })

    it('should render the slider', () => {
      cy.get('#slider-summary').should('exist')
    })

    it('should render the link threshold field', () => {
      cy.get('#link-threshold').should('exist')
    })

    it('should render the min review threshold field', () => {
      cy.get('#min-review-threshold').should('exist')
    })

    it('should render the max review threshold field', () => {
      cy.get('#max-review-threshold').should('exist')
    })

    it('should render the margin window size field', () => {
      cy.get('#margin-window-size').should('exist')
    })

    it('should render the save button', () => {
      cy.get('button[type="submit"]').should('exist')
    })

    it('should save the configuration when the save button is clicked', () => {
      cy.get('button[type="submit"]')
        .click()
        .then(() => {
          cy.contains('Successfully saved probabilistic rule').should(
            'be.visible'
          )
        })
      it('should update link threshold slider value when input field is changed', () => {
        cy.get('#link-threshold').clear().type('0.6')
        cy.get('button[type="submit"]')
          .click()
          .then(() => {
            cy.get('#link-threshold').should('have.attr', 'value', '0.6')
          })
      })

      it('should update margin window size slider value when input field is changed', () => {
        cy.get('#margin-window-size').clear().type('0.1')
        cy.get('button[type="submit"]')
          .click()
          .then(() => {
            cy.get('#margin-window-size').should('have.attr', 'value', '0.1')
          })
      })

      it('should update max review threshold value when input field is changed', () => {
        cy.get('#max-review-threshold')
          .clear({ force: true })
          .then(() => {
            cy.wait(3000)
            cy.get('#max-review-threshold')
              .invoke('val', '')
              .type(`${0.7}{enter}`)
          })
      })
      it('should update min review threshold value when input field is changed', () => {
        cy.get('#min-review-threshold').clear().type('0.3')
        cy.get('button[type="submit"]')
          .click()
          .then(() => {
            cy.get('#min-review-threshold').should('have.attr', 'value', '0.3')
          })
      })
    })
  })
})
