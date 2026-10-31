# 016: fixes of re-checks rv009 and rv010, red and green

Date: 2026-10-27. Oracle GraalVM 25.0.4, Maven 3.9.16 offline.

Red on the round-1 tree (after the rv006/rv007 fixes, before these): `validatorListIsNotChangedByTheCall`, `responseViewsKeptBeforeTheFirstWriteSeeLaterWrites` (`contains(null)`), `keptViewRemovesOnlyTheTestedResponse`. Green on the trees before D and before E, and on the working tree. Output: `output.txt`.
