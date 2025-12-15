## Repo defaults (TypeScript-first)

- Prefer TypeScript types end-to-end; do not use `any` (use `unknown` + type guards instead).
- Keep ESLint rules enabled; fix root causes rather than disabling rules.
- Vue: avoid side effects inside `computed` (`vue/no-side-effects-in-computed-properties`).

## Frontend conventions

- Error handling: `catch (err: unknown)` and convert via `frontend/src/utils/httpError.ts` (`getHttpErrorMessage`, `getHttpStatus`).
- Narrow unknown data with `frontend/src/utils/typeGuards.ts` (`isRecord`, `isString`, `isNumber`) before indexing.

## Local validation (frontend)

- Use Node 20 (repo expects `^20.19.0`): `source ~/.nvm/nvm.sh && nvm use 20`
- Lint: `cd frontend && npm run -s lint -- --no-fix --max-warnings=0`
- Type-check: `cd frontend && npm run -s type-check`
