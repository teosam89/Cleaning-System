module.exports = {
  preset: "@vue/cli-plugin-unit-jest",
  testEnvironment: "jsdom",
  transform: {
    "^.+\\.vue$": "@vue/vue3-jest",
    "^.+\\js$": "babel-jest",
  },
  testMatch: ["**/tests/unit/**/*.spec.js", "**/tests/unit/**/*.test.js"],
  collectCoverageFrom: [
    "src/**/*.{js,vue}",
    "!src/main.js",
    "!src/router/index.js",
    "!**/node_modules/**",
  ],
  moduleFileExtensions: ["js", "json", "vue"],
  moduleNameMapping: {
    "^@/(.*)$": "<rootDir>/src/$1",
  },
  setupFilesAfterEnv: ["<rootDir>/tests/setup.js"],
};
