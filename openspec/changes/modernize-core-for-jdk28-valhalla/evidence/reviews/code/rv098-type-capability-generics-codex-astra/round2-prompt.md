Round 2 for lane R4 (same rules as round 1: read-only, no builds, no file writes).

The conductor changed files after your review:
1. R4-1 (MEDIUM): `xacml-core/src/main/java/org/xacml4j/v30/types/TypeToString.java` `forType` uses the lambda of the committed code again (`.map(t->Types.capabilities.get(t))`). Check with `rtk git diff -- xacml-core/src/main/java/org/xacml4j/v30/types/TypeToString.java` that no other line changed, and that no other file of the 5.6 diff uses a method reference whose receiver is evaluated early in the same way.
2. Your note that task 5.6 should name the `Value<?>` signature changes: tasks.md 5.6 now names `TypeToString`, `TypeToGSon`, and `TypeToXacml30`, and design.md has a new D13. Read D13 (`rtk git diff -- openspec/changes/modernize-core-for-jdk28-valhalla/design.md`): is every technical claim in it true for the code (erasures, source compatibility, the bound `<T extends Value<T>>` that no value can meet)?
3. R4-2 (LOW, RFC822Name.java formatting) is left for the maintainer: it is their edit. The landing procedure carries it over unchanged.
4. The contract test you proposed (`GenericTypeApiContractTest`) is not written in this landing; it goes on bead policy4j-the.5.6 as remaining work.

The full reactor gate ran again after the change: 8,009 tests, 0 failures, 18 skipped; conformance 937 with 12 skipped.

Finish with one block that starts with `FINAL REVIEW (R4 round 2, <model>)` and ends with the line `END FINAL REVIEW R4-2
