# Frontend Development Instructions

Frontend folder:

```text
D:\Bhavin\FlyingBird-UI
```

Framework:

```text
React
```

## Main Goals

Frontend code should be clean, responsive, reusable, accessible, fast, easy to maintain, visually consistent, and aligned with backend API contracts.

Do not create random new design patterns if existing ones already exist.

## Before Editing Frontend

First check:

1. Existing component structure
2. Existing styling system
3. Existing routing system
4. Existing API service pattern
5. Existing state management pattern
6. Existing form handling and validation pattern
7. Existing auth handling pattern
8. Existing environment variable pattern

Do not guess. Match the project.

## React Rules

1. Use functional components.
2. Keep components small.
3. Move repeated UI into reusable components.
4. Avoid deeply nested JSX.
5. Avoid unnecessary state.
6. Use derived values instead of duplicate state.
7. Use memoization only when there is a real performance reason.
8. Keep side effects inside proper hooks.
9. Clean up subscriptions, timers, and listeners.
10. Do not mutate state directly.
11. Do not create duplicate components if a reusable component already exists.

## Component Structure

Preferred structure:

```tsx
type ComponentProps = {
  // props here
};

export function ComponentName(props: ComponentProps) {
  // hooks
  // derived values
  // handlers
  // render
}
```

Keep in this order:

1. imports
2. types
3. constants
4. component
5. local helper functions
6. export

Follow the existing export style if the project already uses another pattern.

## API Integration Rules

API calls should not be scattered across components.

Before adding a new API call, check whether a service already exists in:

```text
src/services
src/api
src/lib
src/utils
```

Every API integration should handle loading state, success state, error state, and empty state where applicable.

Do not hardcode backend URLs. Use existing environment variable patterns.

Common env names may include:

```text
VITE_API_URL
REACT_APP_API_URL
NEXT_PUBLIC_API_URL
```

Use the actual existing project pattern.

## Backend Contract Rule

Before changing a frontend API call:

1. Check the backend endpoint.
2. Confirm request body.
3. Confirm response shape.
4. Confirm error shape.
5. Confirm auth requirement.
6. Update `D:\Bhavin\project_details.md` if the contract changes.

No blind API guessing.

## Forms

Forms should include:

- clear labels
- validation
- disabled submit while loading
- useful error messages
- success feedback where needed
- no silent failures

## UI And UX Rules

Every user-facing screen should consider:

- mobile layout
- desktop layout
- loading state
- error state
- empty state
- long text handling
- keyboard accessibility
- clear call-to-action

Avoid basic rough UI unless the task is purely functional.

## Styling Rules

Use the existing styling system.

Do not mix styling systems unless the project already does.

Follow existing conventions for spacing, colors, typography, cards, buttons, forms, modals, tables, dashboards, and animations.

## TypeScript Rules

If TypeScript is used:

1. Avoid `any`.
2. Create shared types for API responses.
3. Type props clearly.
4. Type event handlers where useful.
5. Keep types near the feature unless reused globally.

If JavaScript is used, avoid introducing TypeScript unless explicitly asked.

## Performance Rules

Avoid unnecessary re-renders, duplicate API calls, unbounded list rendering, and expensive operations inside render.

Use pagination, lazy loading, or memoization only when useful.

## Frontend Testing

When frontend changes are made, suggest or run the actual project commands.

Common commands to verify:

```bash
cd D:\Bhavin\FlyingBird-UI
npm run lint
npm run build
npm run test
```

If commands are different, update `D:\Bhavin\project_details.md`.

## Frontend Final Response Checklist

Before final response, verify:

- Did I read `project_details.md` first?
- Did I inspect only relevant frontend files?
- Did I follow existing React patterns?
- Did I avoid unrelated refactoring?
- Did I preserve existing behavior?
- Did I confirm backend contract if API changed?
- Did I update `project_details.md` if needed?
- Did I provide exact test steps?
