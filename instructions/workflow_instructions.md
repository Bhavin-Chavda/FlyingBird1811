# Claude Workflow Instructions

Main folder:

```text
D:\Bhavin
```

Frontend:

```text
D:\Bhavin\FlyingBird-UI
```

Backend:

```text
D:\Bhavin\Backend-Java
```

## Step 1: Classify The Task

Classify the request as one of:

- Frontend only
- Backend only
- Full-stack
- Bug fix
- Refactor
- Documentation
- Setup/config
- Testing/debugging

Then read only the necessary instruction files.

## Step 2: Use Minimum Context

Before reading files, search for exact component name, route name, API endpoint, error message, function name, Java class name, package name, database entity name, or page name.

Open only relevant files.

Do not read the entire project unless the task requires architecture-level understanding.

## Step 3: Mandatory First Read

For every task, read:

```text
D:\Bhavin\project_details.md
```

Then read only the required instruction file:

Frontend task:

```text
D:\Bhavin\instructions\frontend_instructions.md
```

Backend task:

```text
D:\Bhavin\instructions\backend_instructions.md
```

Full-stack task:

```text
D:\Bhavin\instructions\frontend_instructions.md
D:\Bhavin\instructions\backend_instructions.md
```

## Step 4: Make A Small Plan

Before editing, provide:

```text
Understanding:
- Short summary.

Files I will inspect:
- file 1
- file 2

Likely changes:
- change 1
- change 2

Risk:
- risk 1
```

Keep it short.

## Step 5: Make Minimal Changes

Rules:

1. Fix the root cause, not symptoms.
2. Avoid unrelated cleanup.
3. Avoid large rewrites.
4. Preserve existing behavior.
5. Keep changes easy to review.
6. Do not introduce new dependencies unless necessary.
7. If adding dependency, explain why.
8. Do not change API contracts without checking both frontend and backend.
9. Do not change database schema unless required.

## Step 6: Verify

After changes, check imports, types, Java compilation risks, frontend build risks, backend build risks, API contract, affected routes, affected components, affected controllers/services/repositories, environment variables, and test commands.

If commands cannot be run, say exactly what should be run.

## Step 7: Update project_details.md

Update:

```text
D:\Bhavin\project_details.md
```

when project structure, frontend routes, backend APIs, database models/entities, commands, environment variables, important architecture decisions, or important known issues change.

Keep it compact.

## Step 8: Final Response

Use this format:

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

## Anti-Mistake Checklist

Before final answer, verify:

- Did I read `project_details.md` first?
- Did I avoid scanning unnecessary files?
- Did I follow existing patterns?
- Did I avoid unrelated refactoring?
- Did I preserve current behavior?
- Did I update `project_details.md` if needed?
- Did I provide exact test steps?
- Did I mention risks honestly?

If any answer is no, fix it before final response.
