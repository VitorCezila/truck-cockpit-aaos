---
name: merge-checklist
description: Run the project's quality gates before merging (build, unit tests, Roborazzi verify, lint, LeakCanary, reviewer agents) and write a Conventional Commit. Use when a feature is done, when asked to commit, open a PR, or "check everything".
---

# Merge checklist

## When to use
- Finishing a feature or fix.
- Before committing, pushing or opening a PR.

## Step by step
1. **Scope check.** `git status` and `git diff`. Nothing from Phase 2, no stray files, no secrets, no `local.properties`.
2. **Build and tests:**
   ```bash
   ./gradlew assembleDebug
   ./gradlew test
   ./gradlew verifyRoborazziDebug
   ./gradlew lint
   ```
   Report each failure with its output; do not skip a gate.
3. **Snapshots.** If `verifyRoborazziDebug` fails:
   - Look at the diff images in `build/outputs/roborazzi/`.
   - Only if the UI change was intended, run `./gradlew recordRoborazziDebug` and commit the new images together with the code that changed them.
   - If it was not intended, fix the code.
4. **Leaks.** If the change touches lifecycle, Car API callbacks, or ViewModels, run the debug build on the AAOS emulator, open/close the screens and switch parked ↔ driving. Any LeakCanary report blocks the merge.
5. **Review.** Delegate to `architecture-reviewer`, and to `automotive-reviewer` when screens, manifest or `:core:data` changed. Fix or explicitly justify every finding.
6. **Docs.** Update `.claude/CLAUDE.md` (properties table, commands, modules) and README if behaviour, commands or startup numbers changed.
7. **Commit** with Conventional Commits:
   - `feat(dashboard): show range in driving mode`
   - `fix(data): report speed as unavailable when permission is denied`
   - `test(trip): add driving-mode snapshots`
   - Types: `feat`, `fix`, `test`, `chore`, `docs`, `perf`, `refactor`. Scope = module or screen.
   - Never commit on `main`. If you are on `main`, stop and run `start-task` first.
8. **PR.** `git push -u origin <branch>`, then `gh pr create --base main` with:
   - Title in Conventional Commits format (same as the main commit).
   - Body: summary, how it was tested (gates + emulator checks), screenshots/snapshot diffs for UI changes, and `Closes #<issue-number>`.
   - Show the draft to the user before creating it. Merge only after CI is green and the user approves.

## Final checklist
- [ ] `assembleDebug`, `test`, `verifyRoborazziDebug`, `lint` all pass.
- [ ] Snapshot changes are intended and committed with the code.
- [ ] No LeakCanary reports.
- [ ] Reviewer findings handled.
- [ ] Docs updated; commit message follows Conventional Commits.
- [ ] Work is on a `<type>/<issue>-<slug>` branch; PR to `main` has `Closes #<issue>`.
