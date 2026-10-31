# 003: revised characterization on the tree after the change (review rv001-plugins-code, B2)

Date: 2026-10-27. JDK: Oracle GraalVM 21.0.6+8.1. Maven 3.9.16. Scratch copies
outside the repository; `characterize-v2.sh` (in run 001) forces each plugin
version and `-Dlicense.skipUpdateLicense=false`, and stops on a failed command.

| Check | Result |
| --- | --- |
| forced `update-file-header`, license 1.7 | 1,809 files of the copy differ from the tree |
| forced `update-file-header`, license 2.7.1 | 1,704 files differ |
| JAXB 0.15.1 and 0.15.3 generated sources | both versions confirmed in their POMs and generated; 0 changed lines other than the generator comment and timestamp |

The counts are higher than in run 001 (1,741 with 1.7) because the tree now
holds more files (the new `xacml-jmh` module and evidence). Either version
rewrites files when the header update is forced, which is why the POM makes
the update opt-in (design D5).
