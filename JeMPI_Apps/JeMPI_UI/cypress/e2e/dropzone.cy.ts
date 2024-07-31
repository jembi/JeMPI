/// <reference types="cypress" />

describe('It renders dropzone correctly', () => {
  beforeEach(() => {
    cy.visit('/import')
  })
  it('should open the file dialog when clicking on the SVG icon', () => {
    cy.get('#upload-file-icon').click()
  })

  it('displays the threshold section with sliders and text fields', () => {
    cy.contains('Threshold').should('be.visible')
    cy.get('.MuiSlider-root').should('have.length', 2)
    cy.get('input[name="minThreshold"]').should('be.visible')
    cy.get('input[name="linkThreshold"]').should('be.visible')
    cy.get('input[name="maxThreshold"]').should('be.visible')
    cy.get('input[name="marginWindowSize"]').should('be.visible')
  })

  it('toggles between report options', () => {
    cy.contains('Reports').should('be.visible')

    cy.get('input[value="false"]').should('be.checked')
    cy.get('input[value="true"]').should('not.be.checked')

    cy.get('input[value="true"]').check()
    cy.get('input[value="true"]').should('be.checked')
    cy.get('input[value="false"]').should('not.be.checked')
  })

  it('toggles between upload workflow options', () => {
    cy.contains('Send to the linker and use the current M & U values').should(
      'be.visible'
    )
    cy.contains('Send to EM task to compute new M & U values').should(
      'be.visible'
    )

    cy.get('input[name="uploadWorkflow"][value="0"]').should('be.checked')
    cy.get('input[name="uploadWorkflow"][value="1"]').should('not.be.checked')

    cy.get('input[name="uploadWorkflow"][value="1"]').check()
    cy.get('input[name="uploadWorkflow"][value="1"]').should('be.checked')
    cy.get('input[name="uploadWorkflow"][value="0"]').should('not.be.checked')
  })

  it('interacts with sliders', () => {
    // Drag the slider to a different value
    cy.get('.MuiSlider-root').eq(0).invoke('val', 0.5).trigger('change')
    cy.get('.MuiSlider-root').eq(0).should('have.value', '0.5') // Adjust selector based on slider

    cy.get('.MuiSlider-root').eq(1).invoke('val', 0.7).trigger('change')
    cy.get('.MuiSlider-root').eq(1).should('have.value', '0.7') // Adjust selector based on slider
  })

  it('uploads a file and verifies upload status', () => {
    const fileName = 'basic1000.csv'
    cy.fixture(fileName).then(fileContent => {
      cy.get('input[type="file"]').attachFile({
        fileContent,
        fileName,
        mimeType: 'text/csv'
      })
    })
    cy.contains(fileName).should('be.visible')

    cy.get('button').contains('Submit').click()

    cy.contains('file imported').should('be.visible')
  })

  it('can cancel file upload', () => {
    const fileName = 'basic1000.csv'
    cy.fixture(fileName).then(fileContent => {
      cy.get('input[type="file"]').attachFile({
        fileContent,
        fileName,
        mimeType: 'text/csv'
      })
    })

    cy.get('button').contains('Cancel').click()

    cy.get('input[type="file"]').should('not.have.value')
  })
})
