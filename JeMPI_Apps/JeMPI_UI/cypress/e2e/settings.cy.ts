/// <reference types="cypress" />

describe('Settings Component', () => {
    beforeEach(() => {
      cy.visit('/settings')
    })
  
    it('should render all tabs', () => {
      cy.get('button[role="tab"]').should('have.length', 7)
      cy.get('button[role="tab"]').eq(0).should('contain', 'Common')
      cy.get('button[role="tab"]').eq(1).should('contain', 'Unique to Golden Record')
      cy.get('button[role="tab"]').eq(2).should('contain', 'Unique to Interaction')
      cy.get('button[role="tab"]').eq(3).should('contain', 'Golden Records Lists')
      cy.get('button[role="tab"]').eq(4).should('contain', 'Deterministic')
      cy.get('button[role="tab"]').eq(5).should('contain', 'Blocking')
      cy.get('button[role="tab"]').eq(6).should('contain', 'Probabilistic')
    })
  
    it('should switch between tabs', () => {
      cy.get('button[role="tab"]').eq(1).click()
      cy.get('[role="tabpanel"]').eq(1).should('contain', 'Setup properties that are unique to the golden record')
      
      cy.get('button[role="tab"]').eq(2).click()
      cy.get('[role="tabpanel"]').eq(2).should('contain', 'Setup properties that are unique to the interaction')
  
      cy.get('button[role="tab"]').eq(3).click()
      cy.get('[role="tabpanel"]').eq(3).should('contain', 'Setup properties for Golden record lists')
    })
  
    it('should save configuration', () => {
      cy.intercept('POST', '/http://localhost:50000/JeMPI/configuration', { response: 'ok' }).as('saveConfig')
  
      cy.get('button').contains('Save').click()
      
      cy.get('.SnackbarContent-root').should('contain', 'Successfully saved configuration')
    })

    it('should allow editing and saving a row in Common Settings', () => {

        cy.get('button[role="tab"]').eq(0).click()
    

        cy.get('[data-id="1"]').find('#edit-button').click()
        

        cy.get('[data-id="1"]').find('[data-field="fieldName"] input').clear().type('newFieldName')
    

        cy.get('[data-id="1"]').find('#save-button').click()

        cy.get('[data-id="1"]').should('contain', 'New Field Name')
      })

      it('should retain original value if editing is cancelled', () => {
    
        cy.get('button[role="tab"]').eq(0).click()
    
 
        cy.get('[data-id="1"]').find('#edit-button').click()
    

        cy.get('[data-id="1"]').find('[data-field="fieldName"]').invoke('text').as('originalFieldName')
    
   
        cy.get('[data-id="1"]').find('[data-field="fieldName"] input').clear().type('newFieldName')
  
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
          cy.get('[data-id="1"]').find('[data-field="fieldName"] input').clear().type('newFieldName')
          cy.get('[data-id="1"]').find('#save-button').click()
          cy.get('[data-id="1"]').should('contain', 'NewFieldName')
        })
        
        it('should retain original value if editing is cancelled', () => {
          cy.get('[data-id="1"]').find('#edit-button').click()
          cy.get('[data-id="1"]').find('[data-field="fieldName"]').invoke('text').as('originalFieldName')
          cy.get('[data-id="1"]').find('[data-field="fieldName"] input').clear().type('newFieldName')
          cy.get('[data-id="1"]').find('#cancel-button').click()
          cy.get('@originalFieldName').then(originalFieldName => {
            cy.get('[data-id="1"]').should('contain', originalFieldName)
          })
        })
     

    })
    describe('UniqueToInteraction Component End-to-End Tests', () => {
        beforeEach(() => {
        cy.get('button[role="tab"]').eq(2).click()
        })
    
        it('should allow editing and saving a row', () => {
        // Edit the first row
        cy.get('[data-id="1"]').find('#edit-button').click()
        
        // Change fieldName
        cy.get('[data-id="1"]').find('[data-field="fieldName"] input').clear().type('newFieldName')
    
        // Save the row
        cy.get('[data-id="1"]').find('#save-button').click()
        
        // Verify the row is saved with new data
        cy.get('[data-id="1"]').should('contain', 'NewFieldName')
        })
    
        it('should retain original value if editing is cancelled', () => {
        // Edit the first row
        cy.get('[data-id="1"]').find('#edit-button').click()
    
        // Capture the original fieldName value
        cy.get('[data-id="1"]').find('[data-field="fieldName"]').invoke('text').as('originalFieldName')
    
        // Change fieldName
        cy.get('[data-id="1"]').find('[data-field="fieldName"] input').clear().type('newFieldName')
    
        // Cancel the row editing
        cy.get('[data-id="1"]').find('#cancel-button').click()
    
        // Verify the row retains the original data
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
        // Click on the Edit button of the first row
        cy.get('.MuiDataGrid-row').first().find('#edit-button').click();

        // Edit the fieldName input
        cy.get('.MuiDataGrid-row').first().find('[data-field="fieldName"] input').clear().type('newFieldName');

        // Save the row
        cy.get('.MuiDataGrid-row').first().find('#save-button').click();

        // Verify the row is saved with new data
        cy.get('.MuiDataGrid-row').first().should('contain', 'NewFieldName');
    });

    it('should retain original value if editing is cancelled', () => {
        // Click on the Edit button of the first row
        cy.get('.MuiDataGrid-row').first().find('#edit-button').click();

        // Capture the original fieldName value
        cy.get('.MuiDataGrid-row').first().find('[data-field="fieldName"]').invoke('text').as('originalFieldName');

        // Edit the fieldName input
        cy.get('.MuiDataGrid-row').first().find('[data-field="fieldName"] input').clear().type('newFieldName');

        // Cancel the row editing
        cy.get('.MuiDataGrid-row').first().find('#cancel-button').click();

        // Verify the row retains the original data
        cy.get('@originalFieldName').then(originalFieldName => {
            cy.get('.MuiDataGrid-row').first().should('contain', originalFieldName);
        });
    });

})

describe('Deterministic End-to-End-Tests', () => {
    beforeEach(() => {
    cy.get('button[role="tab"]').eq(4).click()
    })
    it('should render the Deterministic component and check initial tab', () => {
        cy.get('div.MuiCardContent-root').should('exist');
        cy.get('button[role="tab"]').contains('Linking').should('have.attr', 'aria-selected', 'true');
    });

    it('should switch tabs and display appropriate content', () => {
    
        cy.get('button[role="tab"]').contains('Validate').click();
        cy.get('button[role="tab"]').contains('Validate').should('have.attr', 'aria-selected', 'true');
   
        cy.get('button[role="tab"]').contains('Matching').click();
        cy.get('button[role="tab"]').contains('Matching').should('have.attr', 'aria-selected', 'true');
    });

    it('should render Deterministic Content and show the initial tab', () => {
        cy.get('div.MuiCardContent-root').should('exist');
        cy.get('button[role="tab"]').contains('Linking').should('have.attr', 'aria-selected', 'true');
      });
  
      it('should switch to Design View and add a new rule', () => {
        cy.get('[data-testid="edit-button-1"]').click();
        
        // Verify Design View is visible
        cy.get('#design-view-button').should('exist'); // Replace with actual selector if needed
  
        // Add a new row
        cy.get('button[id="add-row-button"]').click();
  
       

        cy.get('#select-field').click().click()
        .get('#select-field-given_name').should('not.be.disabled').click()
      
        cy.get('#select-comparator-function').click()
        .get('#select-comparator-function-Exact').should('not.be.disabled').click()

        cy.pause()
        // Add a rule
        cy.get('button[id="add-rule-button"]').click({force: true});
  
        // Verify rule is added (you might need to update this based on actual behavior)
        cy.get('div.MuiCardContent-root').contains('Rule added').should('exist'); // Update with actual confirmation
      })
   
      it('should switch to Source View and verify source view functionality', () => {
        cy.get('button[id="source-view-button"]').click();
  
        // Verify Source View is visible
        cy.get('div[data-testid="source-view"]').should('exist'); // Replace with actual selector if needed
  
        // Verify edit button functionality
        cy.get('button[id="edit-button-1"]').click(); // Assuming there's an edit button for the first rule
        // Add assertions for edit behavior if needed
  
        // Verify undefined rule button functionality
        cy.get('button[id="add-undefined-rule"]').should('exist').click(); // Ensure the button appears and functions
        // Add additional checks based on functionality
      });
  
      it('should handle rule deletion correctly', () => {
        // Ensure there are rows to delete
        cy.get('button[id="design-view-button"]').click();
        cy.get('button[id="add-row-button"]').click(); // Add a row if necessary
  
        // Switch to Source View
        cy.get('button[id="source-view-button"]').click();
  
        // Verify the delete button functionality
        cy.get('button[id="delete-button"]').first().click(); // Click the delete button on the first row
  
        // Verify the row is removed
        cy.get('div[data-testid="source-view"]')
          .should('not.contain', 'Rule Number'); // Replace with actual text that should be absent
      });
 

  })
  
  
  describe('Blocking End-to-End-Tests', () => {
    beforeEach(() => {
    cy.get('button[role="tab"]').eq(5).click()
    })

    it('should render operator select fields', () => {
      cy.get('#source-view-undefined-rule').click();
      cy.get('#select-field').parent().should('be.visible').click()
      cy.get('#select-field')
        .as('options')
        .find('option')
        .should('have.length', 0)
      cy.get('@options')
        .contains('option', 'family_name') // select it
        .click()
      cy.get('#select-field').should('have.value', 'family_name');
      
      cy.get('#select-field')
      .find('input')
      .invoke('val')
      .should('eq', 'Given Name') 
    });
    cy.get('#insurers-select') // increase don't reduce timeout
    .parent()
    .should('be.visible')
    .click()
    it('should render demographic fields select', () => {
      cy.get('#source-view-undefined-rule').click();
      cy.get('#select-field').click().click()
      cy.get('#select-field').should('have.length', 7); 
    });
  
      it('should switch to Source View and add or edit a rule', () => {
        cy.get('#source-view-undefined-rule').then(($addButton) => {
          if ($addButton.length > 0) {
            cy.get('#source-view-undefined-rule').click();
          
          } else {
            // If the button doesn't exist, click the edit button for the first rule
            cy.get('[data-testid="edit-button-1"]').click();
            cy.get('#add-rule-button').click();
          }
        })
    })
})


  describe('Probabilistic End-to-End-Tests', () => {
    beforeEach(() => {
    cy.get('button[role="tab"]').eq(6).click()
    })
    
    it('should render the slider', () => {
      cy.get('#slider-summary').should('exist');
    });

    it('should render the link threshold field', () => {
      cy.get('#link-threshold').should('exist');
    });
  
    it('should render the min review threshold field', () => {
      cy.get('#min-review-threshold').should('exist');
    });
  
    it('should render the max review threshold field', () => {
      cy.get('#max-review-threshold').should('exist');
    });
  
    it('should render the margin window size field', () => {
      cy.get('#margin-window-size').should('exist');
    });
  
    it('should render the save button', () => {
      cy.get('button[type="submit"]').should('exist');
    });

    it('should save the configuration when the save button is clicked', () => {
      cy.get('button[type="submit"]').click().then(() => {
        cy.contains('Successfully saved probabilistic rule').should('be.visible');
      });
    it('should update link threshold slider value when input field is changed', () => {
      cy.get('#link-threshold').clear().type('0.6');
      cy.get('button[type="submit"]').click().then(() => {
        cy.get('#link-threshold').should('have.attr', 'value', '0.6');
      });

    })

    it('should update margin window size slider value when input field is changed', () => {
      cy.get('#margin-window-size').clear().type('0.1');
      cy.get('button[type="submit"]').click().then(() => {
        cy.get('#margin-window-size').should('have.attr', 'value', '0.1');
      });
    });

    it('should update max review threshold value when input field is changed', () => {
      cy.get('#max-review-threshold').clear({ force: true }).then(() => {
        cy.wait(3000)
        cy.get('#max-review-threshold').invoke('val', '').type(`${0.7}{enter}`)
      })
    });
    it('should update min review threshold value when input field is changed', () => {
      cy.get('#min-review-threshold').clear().type('0.3');
      cy.get('button[type="submit"]').click().then(() => {
      cy.get('#min-review-threshold').should('have.attr', 'value', '0.3');
      });
    });
  
  })

})
})
