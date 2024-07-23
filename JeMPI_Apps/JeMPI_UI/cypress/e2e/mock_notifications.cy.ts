describe('it mocks fetch notifications',()=>{
it.only('checks api call', () => {
    cy.populateData()
    cy.wait('@fetchNotifications') // wait for the intercept to finish
    cy.get('[data-cy=item-list]').should('contain', 'Bob Smith');
    })
      
})