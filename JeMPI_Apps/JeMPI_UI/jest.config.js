const { pathsToModuleNameMapper } = require('ts-jest')
const { compilerOptions } = require('./tsconfig')

module.exports = {
    preset: 'ts-jest',
    testEnvironment: 'node',
    coveragePathIgnorePatterns: [
      "/tests/",
      "/node_modules/",
      "tsconfig.json",
    ],
    collectCoverageFrom: [
      "**/src/**"
    ],
    transform: {
      '^.+\\.(ts|tsx)?$': ['ts-jest', { isolatedModules:true }]
    },
    testMatch: ["**/?(*.)+(spec|test).ts"],
    modulePaths: [compilerOptions.baseUrl],
    moduleNameMapper: { ...pathsToModuleNameMapper(compilerOptions.paths),
        ".+\\.(css|styl|less|sass|scss|ttf|woff|woff2)$": "identity-obj-proxy"
    },
  };