# Truth compatibility lookup

The local tree and red test gate establish the dependency defect; the docs
lookup was used only to choose a compatible retained Truth version.

Native Context7 `resolve-library-id` for Google Truth returned unrelated
libraries, so no unrelated library was selected or queried. Native Tavily
search did not return useful Truth documentation. The fallback read the
official Google Truth GitHub release notes and tagged source through the
web tool. No private source or project name was sent to these services.

- [Truth 1.1.5 POM](https://github.com/google/truth/blob/v1.1.5/core/pom.xml)
  still declares JUnit; the root managed exclusion is therefore necessary.
- [Truth 1.1.5 Platform.java](https://github.com/google/truth/blob/v1.1.5/core/src/main/java/com/google/common/truth/Platform.java)
  handles missing JUnit comparison-failure classes by returning
  AssertionErrorWithFacts. This is confirmed on the actual JDK 21 Surefire
  classpath by the positive and negative Truth/Truth8 probes in
  `11-truth-oracle.txt`.
- [Truth 1.4.2 release notes](https://github.com/google/truth/releases/tag/v1.4.2)
  describe overload ambiguity when Truth and Truth8 are both used. The
  compatibility pin **1.1.5** keeps the current Truth8 API and avoids a
  source migration in the frozen test files. This is a compatibility choice,
  not a claim that 1.1.5 is the newest release or the earliest workable one.
- Native Context7 resolved `/mockito/mockito` and queried the inline
  mock-maker/Objenesis topic. It returned official default-inline and
  startup-agent documentation, but did not answer whether Objenesis could
  be excluded. The dependency tree and actual focused/full test runs,
  rather than that incomplete response, establish the observations here.

The existing JUnit and Mockito versions and the Mockito startup agent were
kept. The retained assertion APIs are tested unchanged; other Truth API
uses and alternative Mockito instantiator modes are outside these probes.
