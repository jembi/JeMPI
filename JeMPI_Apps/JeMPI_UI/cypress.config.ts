import { defineConfig } from "cypress";

export default defineConfig({
  projectId: '35adzy',
  e2e: {
    baseUrl:'http://localhost:3001/',
    defaultCommandTimeout: 10000, 
    setupNodeEvents(on, config) {
      // implement node event listeners here
    },
  },

  component: {
    devServer: {
      framework: "create-react-app",
      bundler: "webpack",
    },
  },
});
