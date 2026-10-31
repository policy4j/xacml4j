# Task 2.8: document the Jupiter and Mockito stack

Result: **documentation checks PASS**. The text is ready for the post-2.7
state. One sentence is explicitly marked for conductor confirmation after
task 2.7 removes the old dependencies.

## Changes and reasons

- `AGENTS.md`: replaced only the Build and Test test-stack paragraph.
  It names Jupiter (`junit-bom` 5.14.4), Mockito 5.24.0, Google Truth and
  Hamcrest, and the startup agent supplied by the dependency plugin's
  `properties` goal through Surefire's `argLine`.
- `docs/ai/TESTING.md`: updated Current state, Writing tests, and Test
  migration parity. Removed the obsolete no-Jupiter and pre-task-2.1
  guidance, the old baseline prerequisite, and the removed file/skip APIs.
  Kept `@TempDir` and the requirement for a skip reason and issue.
- Added the interaction rules from the migration: use real policy model
  and value objects; mock interfaces and collaborators; verify exact
  counts, including stubbed calls; retain `InOrder` and
  `verifyNoMoreInteractions` for order or strictness; use
  `verifyNoInteractions` for untouched collaborators; assert all captor
  values; avoid test-side calls counted by later verification; put
  verifications after `assertThrows`.
- Prefer `MockitoExtension` with strict stubs, fresh mocks in
  `@BeforeEach`, and a reason comment for any `lenient()` stub.
  Explicit total counts remain necessary alongside ordered checks:
  the core audit's initial cache mutation survived strict-stub handling
  until total counts were added.
- Documented the empty disabled-class reporting difference: core
  762 to 760, PDP 74 to 73. Declared test methods and real skips must
  remain unchanged. These are migration accounting facts supplied in
  the task packet, not counts from a new test run in this docs lane.

Version and agent sources: [run 009](../../tests/009-dual-engine/summary.md)
and the current root `pom.xml:31-32,151-155,312-327`.
Mocking rules: [rv014](../../reviews/code/rv014-g2-pdp-migration-agy/summary.md),
[rv015](../../reviews/code/rv015-g2-pdp-migration-grok/summary.md), design D4,
and the [core oracle audit](../../tests/010-core-junit5-mockito/review-artifacts.md).

## Sentence pending task 2.7

The sole dependency-removal sentence is marked in `docs/ai/TESTING.md:18`:

> **After task 2.7 (conductor confirmation pending):** JUnit 4, Vintage,
> EasyMock, and Objenesis are removed.

The conductor must confirm dependency removal before clearing this marker.
This lane did not run the task-2.7 dependency gate or change any POM.

## Verification and scope

- `rtk rg` finds no removed API names or obsolete task-2.1 guidance in
  either document (exit 1, no matches). The legacy stack names remain
  only in the marked removal sentence and the historical Vintage count
  note. `@TempDir` and all requested interaction rules are present.
- A local link/reference check passes all **11** targets, including the
  existing AGENTS anchor and the new run-009 link. No external URLs need
  checking in these documents. Coverage: **SUPPORTED** for the literal
  Markdown links and document paths found by the parser.
- `rtk git diff --check` passes. Task-start SHA-256 checks confirm that
  the AGENTS prefix and suffix outside the allowed paragraph are
  unchanged. Its existing JDK/release edits were already present and
  were preserved.
- Commands, Test layers, What to test by change type, and Recording results
  in the testing guide are byte-identical to task start.
- All **138** files under `xacml-core/src/test` and the root POM retain
  their task-start hashes. `CLAUDE.md` remains a symlink to `AGENTS.md`.
  Shared dirty work was preserved.

Receipts: [checks.json](checks.json) and [final-checks.txt](final-checks.txt);
exact checks: [command.txt](command.txt).
No Maven run was needed for this documentation-only change. No code,
fixtures, license headers, task checkboxes, issue state, or commits were
changed by this lane. The conductor owns evidence indexing and tracking.

Residual risk: dependency removal remains conditional on task 2.7.
