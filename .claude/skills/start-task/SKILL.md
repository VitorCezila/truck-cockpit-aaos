---
name: start-task
description: Open a GitHub issue and create the matching branch before any change (feature, fix, maintenance, docs, tests, CI). Use at the start of every task, before editing code, or when asked to "start", "pick up" or "work on" something.
---

# Start a task

## When to use
- Before any change that will reach `main`. No exceptions: features, fixes, chores, docs, tests, CI.
- If an issue already exists for the work, skip step 2 and reuse its number.

## Step by step
1. **Sync.** `git switch main && git pull --ff-only`. Make sure the working tree is clean (`git status`); if not, stop and ask.
2. **Issue.** Create it with `gh issue create`:
   - Title: short and imperative, e.g. `Show speed and gear on the Dashboard`.
   - Body: **Context** (why), **Scope** (what is in / out), **Acceptance criteria** (checklist, including parked + driving snapshots when UI changes).
   - Label with the type (`feature`, `fix`, `chore`, `docs`, `test`, `ci`) if the labels exist.
   - Show the draft to the user before creating it.
3. **Branch.** `git switch -c <type>/<issue-number>-<short-slug>`, e.g. `feat/12-dashboard-speed`. Type matches Conventional Commits (`feat`, `fix`, `chore`, `docs`, `test`, `ci`, `refactor`, `perf`).
4. **Plan.** Pick the skill for the work (`new-screen`, `new-vehicle-property`, `new-use-case`, `measure-startup`) and follow it.
5. When done, run `merge-checklist`, which opens the PR with `Closes #<issue-number>`.

## Final checklist
- [ ] Issue exists and has acceptance criteria.
- [ ] Branch created from up-to-date `main` and named `<type>/<issue>-<slug>`.
- [ ] Nothing committed on `main`.
