# 001: group 1 before (pair with 002)

Date: 2026-10-27 05:34 UTC. Jar built from a scratch copy of the tree with
the three group 1 files taken from `HEAD` (`git show HEAD:<path>`):
`DefaultPolicyDecisionPoint.java` (sha256 `da27d147...`), `MDCSupport.java`
(`49eb9bad...`), `PolicyDecisionPointContextFactory.java` (`302e1e77...`);
the jar's `DefaultPolicyDecisionPointContextFactory` class references
`java.util.Random` (checked). Flags and machine as in `env.txt`
(GraalVM 21.0.6, C2). Results: `results-decide.json`; comparison in 002.
