describe('NotificationWorklist component', () => {
    beforeEach(() => {
      cy.visit('/notifications')
        // cy.populateData();
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
        const notifications = [
            {
              id: '123',
              type: 'THRESHOLD',
              created: new Date('05-03-2023'),
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
            },
            {
              id: '345',
              type: 'THRESHOLD',
              created: new Date('02-03-2023'),
              names: 'Jane Doe',
              patient_id: '0x7j',
              status: 'OPEN',
              old_golden_id: '0x9493',
              current_golden_id: '0x9833',
              score: 0.9,
              candidates: [
                {
                  golden_id: '0x35',
                  score: 0.4
                }
              ]
            },
            {
              id: '758',
              type: 'MARGIN',
              created: new Date('02-03-2023'),
              names: 'Jane Smith',
              patient_id: '0x1a',
              status: 'OPEN',
              old_golden_id: '0x9493',
              current_golden_id: '0x9833',
              score: 0.3,
              candidates: [
                {
                  golden_id: '0x45',
                  score: 0.5
                }
              ]
            },
            {
              id: '541',
              type: 'UPDATE',
              created: new Date('02-03-2023'),
              names: 'John Themba',
              patient_id: '0x9a',
              status: 'OPEN',
              old_golden_id: '0x9493',
              current_golden_id: '0x9833',
              score: 0.7,
              candidates: [
                {
                  golden_id: '0x55',
                  score: 0.7
                }
              ]
            }
          ]

          it('should refetch notifications on filter change', () => {
            // Intercept the POST request to /notifications and reply with mock data
            cy.intercept('POST', '/notifications', (req) => {
              cy.log('Intercepted Request:', req);
              req.reply({
                statusCode: 200,
                body: { records: notifications },
              });
            }).as('fetchNotifications');
          
            // Visit the notifications page
            cy.visit('/notifications');
          
            // Check if the filter button exists
            cy.get('#filter-button').should('exist');
          
            // Pause execution for debugging
       
          
            // Click the filter button
            cy.get('#filter-button').click({ force: true });
          
            // Wait for the fetchNotifications alias to be called
            cy.wait('@fetchNotifications', { timeout: 10000 }).then((interception) => {
              cy.log('Intercepted Response:', interception.response);
              if (interception.response) {
                cy.log('Intercepted Response:', interception.response);
                expect(interception.response.statusCode).to.eq(200);
                expect(interception.response.body.records).to.have.length(4); // or any other assertion you want to make
              } else {
                throw new Error('Response is undefined');
              }
            });
          });
          

            
          
        //   it('should refetch notifications on filter change', () => {
        //     cy.intercept('POST', '/notifications', {
        //       statusCode: 200,
        //       body: { records: notifications },
        //     }).as('fetchNotifications');
          
        //     // Use cy.request() to interact directly with the JSON endpoint
        //     // cy.request('GET', '/notifications', {
        //     //   statusCode: 200,
        //     //   body: { records: notifications },
        //     // });
          
        //     // Visit the page after the request if necessary
        //     cy.visit('/notifications');
          
        //     // Check if the filter button exists
        //     cy.get('#filter-button').should('exist');
          
        //     // Click the filter button
        //     cy.get('#filter-button').click({ force: true });
          
        //     // Wait for the fetchNotifications alias to be called
        //     cy.pause()
        //     cy.wait('@fetchNotifications', { timeout: 10000 }).its('response.statusCode').should('eq', 200);
        //   });
          
          
    //   it('should fetch notifications on mount', () => {
    //     cy.intercept('GET', '/notifications', {
    //       statusCode: 200,
    //       body: { records: notifications },
    //     }).as('fetchNotifications');
    //     cy.visit('/notifications');
    //     cy.wait('@fetchNotifications');
    //   });
  
    // it('should refetch notifications on filter change', () => {
    //     cy.intercept('POST', '/notifications', {
    //       statusCode: 200,
    //       body: { records: notifications },
    //     }).as('fetchNotifications');
    //     cy.get('#filter-button').click();
    //     cy.wait('@fetchNotifications', { timeout: 10000 });
    //   });
    });
  });