# Run 060: toolchain survey for task group 8, JDK 28 build (Agy)

Date: 2026-10-02. Lane: `policy4j_code_review_agy` (NTM persona), Antigravity;
pane shows Gemini 3.8 Flash (High) (self-label unreliable). Session
`policy4j--g1`, pane 3, 18 tool calls. Read-only (jenv queries, poms, CI
workflow, design/tasks). The first attempt stopped without a report and was
discarded; this is the resend. Raw report: [agy-report.txt](agy-report.txt).

Result:
- JDK 28 is installed through jenv and its build id matches the pinned
  `28-ea-jdk-fd9698357bb0`.
- Change list for 8.2-8.6: drop `oss-parent:7`; replace
  `maven-jaxb2-plugin:0.15.1` with `org.jvnet.jaxb:jaxb-maven-plugin` (2.x);
  maven-compiler-plugin 3.13+ with release 28 and `--enable-preview`;
  Surefire 3.5+ with `--enable-preview`, the Mockito `-javaagent`, and
  `-Dnet.bytebuddy.experimental=true` if needed; license-maven-plugin and
  auto-service upgrades; JDK 28 CI job with build-identity check; AGENTS.md.
- Top risks: ByteBuddy/Mockito on class file 72; XJC on JDK 28 with the
  replacement JAXB plugin; Surefire/ASM scanning class file 72; CI
  provisioning of the custom JDK 28 build; the license plugin rewriting
  headers during process-sources.

## Conductor notes

The specific plugin versions in the report (for example jaxb-maven-plugin
2.0.9, Surefire 3.5.2, license-maven-plugin 2.4.0, auto-service 1.1.1) are
UNVERIFIED: task 8.1 must confirm each against current documentation
(Context7) and a real JDK 28 build. The report's one web citation is
unrelated to the question and is ignored.
