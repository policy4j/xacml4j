# 002: gate builds with both Maven binaries (task 1.3)

Date: 2026-10-26. JDK: Oracle GraalVM 21.0.6+8.1. Sources: `5fbbd6bc` plus the
two POM edits of this change (and the unrelated, uncommitted group 1 edits of
`optimize-pdp-evaluation` in `xacml-pdp`). No `-Dlicense.skipUpdateLicense`
flag was passed, so the new POM default was in effect.

| Build | Result | Tests | Failures | Errors | Skipped | Conformance |
| --- | --- | --- | --- | --- | --- | --- |
| Maven 3.9.16, online (first resolution of the pinned plugins) | SUCCESS, 36.6 s | 8,013 | 0 | 0 | 18 | 937, 12 skipped |
| Maven 3.9.16, offline | SUCCESS, 34.0 s | 8,013 | 0 | 0 | 18 | 937, 12 skipped |
| Maven 3.10.0, offline | SUCCESS, 42.9 s | 8,013 | 0 | 0 | 18 | 937, 12 skipped |

- The counts equal those of `optimize-pdp-evaluation` run `tests/003` (before
  this change): 6,501 + 253 + 24 + 199 + 99 + 937.
- `rtk git status` after the three builds: only the files of the changes
  (two POMs, the group 1 files); no license header or fixture changed.
- Maven 3.10.0 had resolved the plugins online first (an earlier run whose
  log `rtk mvn` condensed; its exit code was 0). Maven 3.9.16 offline then
  failed for `enforcer-api` 3.6.3: Maven 3.10 records downloads as
  `central-<hash>` in `_remote.repositories`, which the Maven 3.9 offline check
  does not accept. One online 3.9.16 build fixed it (first row).
- Effective plugin versions after the change are the same for both binaries
  (`tests/001-characterization/effective-plugins-after-*.txt`).

Raw lines: `gate-*-summary.txt` (Surefire totals, reactor summary).
