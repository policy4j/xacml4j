# Task 2.1 dependency and test-engine decisions

Checked 2026-10-01. Context7: **UNAVAILABLE**. The native
`list_mcp_resource_templates(server="context7")` check failed during MCP
initialization with HTTP 500: upstream backends required DNS resolution,
which failed. No resolve-library-id/query-docs calls could be made because
those tools were not exposed. No MCP configuration was changed and no shell
Context7 transport was used. Under AGENTS.md's unavailable-lane fallback,
native DuckDuckGo MCP search/fetch retrieved the primary sources below.
The specifically requested Context7 lookup remains an evidence gap; the
version and configuration claims below were verified from official sources.

| Item | Choice and reason | Primary source |
| --- | --- | --- |
| JUnit | `junit.version=5.14.4`, imported `org.junit:junit-bom`. Latest stable 5.x in the published metadata; aligns Jupiter, Vintage and Platform versions, including Mockito's transitive API dependency. JUnit 6.1.3 is newer overall; the packet requests JUnit 5. | [JUnit 5.14.4 release notes](https://docs.junit.org/5.14.4/release-notes/) (released 2026-04-26); [JUnit BOM metadata](https://repo.maven.apache.org/maven2/org/junit/junit-bom/maven-metadata.xml) (last updated 20260807124330). |
| Mockito | `mockito.version=5.24.0` for mockito-core and mockito-junit-jupiter, test scope. Current 5.x release; inline mocking is the default and supports final classes on a modern JDK. MockitoExtension gives the temporary probe an actual extension lifecycle. | [Mockito 5.24.0 release](https://github.com/mockito/mockito/releases/tag/v5.24.0) (2026-09-23); [published core metadata](https://repo.maven.apache.org/maven2/org/mockito/mockito-core/maven-metadata.xml) (last updated 20260923140810); [Mockito 5.24.0 API, sections 0.2/0.3](https://javadoc.io/static/org.mockito/mockito-core/5.24.0/org.mockito/org/mockito/Mockito.html). |
| Transitive Jupiter API | Mockito's published extension POM requests Jupiter API 5.13.4 at runtime. The JUnit BOM aligns it with 5.14.4 and both engines instead of relying on declaration-order mediation. | [Published mockito-junit-jupiter 5.24.0 POM](https://repo.maven.apache.org/maven2/org/mockito/mockito-junit-jupiter/5.24.0/mockito-junit-jupiter-5.24.0.pom). |
| Surefire | Keep existing 3.0.0. Its versioned documentation describes mixed Jupiter/Vintage execution, automatic JUnit Platform provider selection and per-engine filters. Upgrade only if the local proof exposes incompatibility. | [Surefire 3.0.0 JUnit Platform support](https://maven.apache.org/surefire-archives/surefire-3.0.0/maven-surefire-plugin/examples/junit-platform.html); [Surefire 3.0.0 test goal](https://maven.apache.org/surefire-archives/surefire-3.0.0/maven-surefire-plugin/test-mojo.html). |
| JDK 21 instrumentation | Use Mockito's documented startup-agent recipe: dependency-plugin `properties`, then Surefire argLine `@{argLine} -javaagent:${org.mockito:mockito-core:jar}`. Initialize the argLine property to empty so late replacement has a value. Resolve the actual artifact location rather than embedding a machine path. Do not add an EnableDynamicAgentLoading override. | [Mockito section 0.3](https://javadoc.io/static/org.mockito/mockito-core/5.24.0/org.mockito/org/mockito/Mockito.html); [dependency:properties](https://maven.apache.org/plugins/maven-dependency-plugin/properties-mojo.html). |
| Dependency plugin | Pin documented 3.11.0. The properties goal resolves direct/transitive artifact paths and binds to initialize; required solely for the documented Mockito agent path. | [Dependency plugin properties goal](https://maven.apache.org/plugins/maven-dependency-plugin/properties-mojo.html). |

The inherited Vintage engine, JUnit 4, EasyMock 5.1.0 and Objenesis 3.3 stay
present. No module POM contains a Surefire override; core's unversioned
Vintage dependency inherits root management. xacml-test retains its approved
JUnit 4 compile-scope dependency until the support-library migration.
Structural inventory: 01-module-overrides.json; local execution proof is
recorded separately and takes precedence over documentation.

The throwaway test is expressly required by the packet. Its only temporary
source path is xacml-json/src/test/java/org/xacml4j/v30/marshal/json/
DualEngineTransitionProbeTest.java. It is copied into evidence as text and
deleted before the final clean gate. It does not mock a policy/value type.
