# rv026: task 2.7 review, legacy test stack removed (Agy)

Date: 2026-10-01. Lane: `policy4j_code_review_agy` (NTM persona), Antigravity,
Gemini 3.8 Flash (High) (report header names "gemini-2.5-pro"; recorded as
reported). Session `policy4j--g1`, pane 3. Read-only; javap on
mockito-core 5.24.0; 13 files opened.

Scope: root and xacml-core pom changes for task 2.7 and evidence run
`tests/017-legacy-stack-removed`.

Verdict: REQUEST_CHANGES (1 finding).

Finding 1, HIGH, `pom.xml:93-98, 105-110`: Objenesis was excluded from
mockito-core and mockito-junit-jupiter. Mockito 5.24.0
DefaultInstantiatorProvider builds an ObjenesisInstantiator (ObjenesisStd);
the inline mock maker falls back to it when a class mock's constructor
throws with dummy arguments, and the subclass mock maker always uses it. The
suite passes only because every mock(...) targets an interface and the three
Target spies use the inline spy path. A class mock such as mock(Target.class)
would fail with NoClassDefFoundError. The task meant the direct Objenesis
dependency that EasyMock needed. Fix: delete both exclusion blocks; keep the
direct objenesis dependency and property removed.
Resolution: the conductor deleted both exclusion blocks (Mockito keeps its
transitive org.objenesis:objenesis 3.3); verification runs with the next
full gate (packet to the implementer lane).

Decision 1 (Truth 0.45 -> 1.1.5): APPROVED. Truth 0.45 statically needs
org.junit.ComparisonFailure (junit 4.12); excluding junit broke 21 core test
classes. Truth 1.1.5 falls back to AssertionErrorWithFacts; truth-java8-extension
stays separate (no 1.4 overload ambiguity); equality/identity semantics and
failure behavior unchanged for the calls used here.

Gate checks (reviewer): no org.junit.Test/Assert, junit.framework or
org.easymock in any source; easymock/objenesis properties and management
removed; junit-bom 5.14.4 imported; Surefire runs Jupiter only; Mockito agent
argLine kept; 934 tests, 0 failures, 0 errors, 6 skips.
