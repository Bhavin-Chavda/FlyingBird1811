# CLAUDE.md

## Project Location

Main project folder:

```text
D:\Bhavin
```

Frontend React application:

```text
D:\Bhavin\FlyingBird-UI
```

Backend Java Spring Boot application:

```text
D:\Bhavin\Backend-Java
```

## Purpose

You are working inside a full-stack project with:

* Frontend: React
* Backend: Java Spring Boot

Your goal is to make accurate, minimal, production-safe changes with the lowest useful context usage.

Do not scan the entire codebase unless explicitly required. First read `project_details.md`, then read only the files relevant to the task.

## Mandatory Startup Rule

At the start of every task, first read:

1. `D:\Bhavin\project_details.md`
2. `D:\Bhavin\instructions\workflow_instructions.md`

Then decide whether the task touches frontend, backend, or both.

For frontend tasks, read:

```text
D:\Bhavin\instructions\frontend_instructions.md
```

For backend tasks, read:

```text
D:\Bhavin\instructions\backend_instructions.md
```

For full-stack tasks, read both frontend and backend instruction files.

Do not inspect application code before reading the required project memory and workflow files.

## First Files To Read

For every new task, read these files first:

1. `D:\Bhavin\project_details.md`
2. `D:\Bhavin\instructions\workflow_instructions.md`
3. `D:\Bhavin\instructions\frontend_instructions.md` only if the task touches frontend
4. `D:\Bhavin\instructions\backend_instructions.md` only if the task touches backend

After reading these, inspect only the specific files needed for the task.

## Core Development Rules

1. Never rewrite large files unless necessary.
2. Never change architecture without explaining why.
3. Never rename files, folders, routes, APIs, components, packages, classes, database tables, or fields unless asked.
4. Never remove existing behavior unless the task clearly requires it.
5. Prefer small, focused changes.
6. Keep code consistent with the existing style.
7. Before editing, explain what files you will inspect, what you expect to change, and what risk exists.
8. After editing, summarize changed files, what changed, how to test, and any risks or follow-ups.

## Token-Saving Rules

Use this priority order:

1. Read `D:\Bhavin\project_details.md`.
2. Search for exact feature, route, component, API, class, function, error, or table names.
3. Open only the most relevant files.
4. Avoid loading generated folders, build folders, lock files, or dependency folders unless needed.
5. Do not read the full project tree repeatedly.
6. If `project_details.md` contains enough information, use it instead of rescanning.
7. Keep responses short unless deep explanation is requested.

Never inspect these unless required:

* `node_modules`
* `.next`
* `dist`
* `build`
* `target`
* `.git`
* `.idea`
* `.vscode`
* coverage folders
* generated files
* package lock files unless dependency changes are needed

## Project Memory Rule

`D:\Bhavin\project_details.md` is the source of truth for:

* project structure
* architecture
* frontend routes
* backend APIs
* database models
* environment variables
* commands
* known decisions
* known issues
* integration rules

Update `project_details.md` whenever routes, APIs, database entities, environment variables, commands, dependencies, structure, architecture, or important gotchas change.

Do not add tiny implementation details. Keep it compact and useful.

## Frontend Rules

Frontend folder:

```text
D:\Bhavin\FlyingBird-UI
```

For frontend work, follow:

```text
D:\Bhavin\instructions\frontend_instructions.md
```

Focus on:

* React component quality
* clean state management
* responsive UI
* accessibility
* reusable components
* API integration
* loading states
* error states
* empty states

## Backend Rules

Backend folder:

```text
D:\Bhavin\Backend-Java
```

For backend work, follow:

```text
D:\Bhavin\instructions\backend_instructions.md
```

Focus on:

* Spring Boot API design
* validation
* authentication and authorization
* database safety
* DTO usage
* service layer discipline
* exception handling
* logging
* security
* performance

## Full-Stack Rules

When a task touches both frontend and backend:

1. Confirm the frontend expected API contract.
2. Confirm the backend actual API contract.
3. Do not change one side without checking the other.
4. Keep response shapes consistent.
5. Update `project_details.md` if any route, endpoint, DTO, environment variable, or integration rule changes.

## File Reading Discipline

Before opening files, search using exact names when possible:

* component name
* page name
* route path
* API endpoint
* controller name
* service name
* repository name
* entity name
* DTO name
* error message
* function name
* environment variable name

Open only the files required to solve the task.

Do not use broad project scanning unless the task requires architectural understanding.

## Change Discipline

When making code changes:

1. Fix the root cause, not only the symptom.
2. Keep changes small and reviewable.
3. Avoid unrelated refactoring.
4. Avoid introducing new dependencies unless necessary.
5. Preserve existing behavior unless the task clearly requires changing it.
6. Do not change API contracts without checking both frontend and backend.
7. Do not change database schema unless required.
8. Do not edit generated files.
9. Do not expose secrets or private configuration values.
10. Update `project_details.md` only when meaningful project facts changed.

## Verification Rule

After changes, verify or provide exact commands to verify:

Frontend:

```cmd
cd /d D:\Bhavin\FlyingBird-UI
npm run build
npm run lint
npm run test
```

Backend Maven:

```cmd
cd /d D:\Bhavin\Backend-Java
mvn clean install
mvn test
```

Backend Gradle:

```cmd
cd /d D:\Bhavin\Backend-Java
gradlew build
gradlew test
```

Use only the commands that actually exist in the project. If actual commands differ, update `project_details.md`.

## Required Response Format Before Editing

```text
Understanding:
- Short summary of the task.

Files I will inspect:
- Relevant files only.

Likely changes:
- Small list of expected changes.

Risk:
- Any risk or assumption.
```

## Required Response Format After Editing

```text
Done.

Files changed:
- file 1
- file 2

What changed:
- change 1
- change 2

How to test:
1. command or step
2. command or step

Notes:
- risk or assumption
```

## Optional Claude Code Imports

Keep these commented by default to save tokens.

Uncomment only if Claude keeps ignoring instruction files or repeatedly forgets to read `project_details.md`.

```text
<!--
@project_details.md
@instructions/workflow_instructions.md
@instructions/frontend_instructions.md
@instructions/backend_instructions.md
-->
```
