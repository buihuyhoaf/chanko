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

## Build & Testing Execution
- **No Device / Emulator Installation**: Do not install or run the app on physical devices or emulators/virtual machines (e.g., no `adb install`, `./gradlew installDebug`, or device launches). Cấm cài đặt trên máy thật hoặc máy ảo.
- **Build & Verification Only**: Only perform build checks, unit tests, and verify build/runtime compilation results via terminal output/logs (e.g., `./gradlew assembleDebug`, `./gradlew test`). Chỉ build và verify kết quả build runtime.

## User verification and UI preferences (2026-10-18)
- Never install, create, configure, start, or use virtual machines, Android emulators, or AVDs.
- Only build and verify runtime on a real physical device. When unavailable or installation is rejected, report runtime unverified; never substitute an emulator or retry a refused installation.
- Do not add a separate debug/review launcher or app icon. Installed APKs must expose only the Chanko launcher.
- All Initial Setup UI text must be Vietnamese.

## Coding Standards & Conventions
- Tuân thủ nghiêm ngặt các quy tắc kiến trúc, quy chuẩn Compose, đặt tên và xử lý tài nguyên được mô tả trong [CODING_STANDARDS.md](file:///Users/buihoa/Ho%CC%A3c%20ta%CC%A3%CC%82p/Android/chanko/CODING_STANDARDS.md).

