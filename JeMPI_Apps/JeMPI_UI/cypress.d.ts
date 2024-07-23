declare namespace Cypress {
    interface Chainable {
      populateData(): Chainable<void>;
    }
  }