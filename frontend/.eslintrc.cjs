// Enforces /docs/17_Coding_Standards.md §3 — no-explicit-any as an error (not a
// warning), jsx-a11y enabled as the automated first line of defense for
// NFR-A11Y-01/02, react-hooks rules-of-hooks enforced.
module.exports = {
  root: true,
  env: { browser: true, es2022: true },
  extends: [
    'eslint:recommended',
    'plugin:@typescript-eslint/recommended',
    'plugin:react-hooks/recommended',
    'plugin:jsx-a11y/recommended',
  ],
  parser: '@typescript-eslint/parser',
  parserOptions: { ecmaVersion: 'latest', sourceType: 'module' },
  plugins: ['react-hooks', 'jsx-a11y'],
  rules: {
    '@typescript-eslint/no-explicit-any': 'error',
    'no-console': 'warn',
  },
};
