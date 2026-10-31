# rv012: task 1.6 review (Agy)

Date: 2026-10-01. Lane: `policy4j_code_review_agy` (NTM persona), Antigravity,
Gemini 3.8 Flash (High). Session `policy4j--g1`, pane 3. Read-only.

Scope: `pom.xml` and `xacml-core/pom.xml` compiler change to
`<release>21</release>` and evidence run `tests/008-baseline-jdk11-jdk21`.

Verdict: APPROVE.

Checked (reviewer): release 21 in both compiler configurations, source and
target removed, no preview flag; the JDK 21 and post-change JDK 11 runs are
clean builds (targets deleted, every source set compiled at release 21);
JDK 11 fails as expected; bytecode major version 65 (verify_bytecode.py);
summary counts equal the Surefire log lines; all 96 JDK 21 errors are
EasyMock class-proxy errors in 11 xacml-core test classes; Java versions
recorded.

Findings:
- LOW, documentation drift: AGENTS.md (CLAUDE.md) "Build and Test" said
  xacml-core adds `--enable-preview`; the poms no longer do. Fixed by the
  conductor in the worktree AGENTS.md (also notes the stale-class pitfall).
- LOW, tracking: index run 008 in evidence/README.md. Done.

Conductor note: the first run 008 attempt reused stale JDK 11 class files
(the compiler plugin did not treat the source/target to release change as
stale); the conductor caught it from the "Nothing to compile" lines and the
lane redid the runs with `clean`. The recorded results are from the clean
builds.
