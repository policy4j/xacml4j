# Run 050: donor pre-audit for task 6.4, SeqOf and value streams (Agy)

Date: 2026-10-02. Lane: `policy4j_code_review_agy` (NTM persona), Antigravity;
pane shows Gemini 3.8 Flash (High). Session `policy4j--g1`, pane 3, 59 tool
calls, 29 files. Read-only: donor `b9f114a3` and worktree callers. Raw report:
[agy-report.txt](agy-report.txt) (7 Stream implementors, 7 donor defects,
541 worktree bag references, 20 test rows).

Reported findings: SeqOf, ValueStream, ValueStreamOfType, Value, BagOf and
Attribute extend or implement `java.util.stream.Stream` (single-use pipelines
posing as values; design D9); `SeqOf.asSingle()` uses `findFirst()` (also run
046); BagOf and Value implement SeqOf with conflicting equality; lazy streams
leak PIP exceptions and logging context; donor `Streams` discards its
concatenation.

## Conductor spot check (2026-10-02)

Confirmed: `b9f114a3:xacml-core/src/main/java/org/xacml4j/util/Streams.java`
builds `stream` by concatenating Iterator, Iterable and Stream parameters and
then returns `extractType(type, Arrays.stream(params))`, discarding the
concatenation. (No file at that path in the worktree.) Other rows
unverified.

Use: 6.4 follows D9 (Iterable plus explicit `stream()`, the existing
`AttributeStream` pattern); the 20 test rows (iterate twice, multiplicity,
error timing) are the minimum.
