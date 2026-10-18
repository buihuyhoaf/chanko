# Chanko Project Guidelines

## Git & Branching Strategy
- **Base / Development Branch**: `dev-chanko-1x`
- **Release / Stable Branch**: `main`
- **Feature Branch Workflow**:
  - Always branch off from `dev-chanko-1x` (e.g., `feature/<feature-name>`, `fix/<bug-name>`).
  - Create a Pull Request / Merge Request (PR/MR) targeting `dev-chanko-1x`.
  - Do not commit directly to `dev-chanko-1x` or `main` for new features.

## Architecture Baseline
- Read `docs/architecture/phase-0/README.md` and its source inventory before architecture work. Keep Approved, Proposed, Pending and Superseded decisions distinct; do not treat unread sources as verified.
- Phase 0 is documentation only. Phase 1 requires a separate instruction; preserve existing code, Gradle, database migrations and UI until then.

## Agent Skills
- Do not modify, edit, or delete agent skills (under `.agent/skills/` or `~/.gemini/config/skills/`).
- Agent skills are strictly protected and must remain untouched.
