/// <reference types="cypress" />
describe('Import Component', () => {
  beforeEach(() => {
    cy.visit('/import')
  })

  it('should display the UploadFileIcon', () => {
    cy.get(
      '#root > div.MuiBox-root.css-k008qs > main > div.MuiContainer-root.css-ur2jdm-MuiContainer-root > div.MuiStack-root.css-1lqoyt0-MuiStack-root > div > div > div > div.MuiGrid-root.MuiGrid-container.MuiGrid-item.MuiGrid-direction-xs-column.MuiGrid-grid-xs-12.MuiGrid-grid-lg-6.css-vlgb4c-MuiGrid-root > div.MuiGrid-root.MuiGrid-item.MuiGrid-grid-xs-8.css-wp27ow-MuiGrid-root > div > div > svg'
    )
      .should('exist')
      .should('have.attr', 'data-testid', 'UploadFileIcon')
  })

  it('Uploads CSV', () => {
    cy.fixture('basic1000.csv').then(fileContent => {
      cy.get('input[type="file"]').attachFile({
        fileContent: fileContent.toString(),
        fileName: 'CSV',
        mimeType: 'text/csv'
      })
      cy.get('#submit-csv').click()
    })
  })

  it('should open the file dialog when clicking on the SVG icon', () => {
    cy.get('#upload-file-icon').click()
  })

  it('should render the PageHeader component with the correct title and breadcrumbs', () => {
    cy.get('#page-header').contains('Import')
  })

  it('should render the DropZone component', () => {
    cy.get('input[type="file"]').should('exist')
  })

  it('displays the machine learning configuration section', () => {
    cy.contains('Machine Learning Configuration').should('be.visible')
  })
})
