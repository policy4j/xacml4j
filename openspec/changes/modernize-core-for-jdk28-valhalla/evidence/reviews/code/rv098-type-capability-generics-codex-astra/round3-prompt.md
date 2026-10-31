Round 3 for lane R4 (same rules: read-only, no builds, no file writes).

The conductor rewrote two passages of design.md D13 for your round-2 findings R4-3 and R4-4 (`rtk git diff -- openspec/changes/modernize-core-for-jdk28-valhalla/design.md`, section "D13"):
- The first paragraph now says that `<T extends Value<T>>` accepts only self-valued types (`RFC822Name` is a `SelfValue<RFC822Name>`) and rejects payload-wrapping values (`BooleanVal`), and that `Value<?>` accepts both.
- The compatibility bullet now says that erasures do not change, that some source callers need a change because `ValueType` was a raw `Function<Object, Value>` and generic arguments are invariant, with your example and its replacement.

Check that both passages are now true for the code and complete. Finish with one block that starts with `FINAL REVIEW (R4 round 3, <model>)` and ends with the line `END FINAL REVIEW R4-3`: verdict APPROVE or REQUEST_CHANGES, one line per finding, any new finding.
