# The API route is taken out of the tests

Task 5.2 (`policy4j-971.5.2`), 2026-10-10. Maintainer instruction in the
conversation, given while the trial of the programmatic routes was being
written up.
JDK, Maven, XACML conformance and JMH are not applicable.

## Instruction

> Remove API route from tests

## What was done

- Task 5.15 is removed from `tasks.md`, and its issue `policy4j-971.5.15`
  is closed as withdrawn. Task 5.2 no longer waits for it.
- The design, the proposal and the evidence index no longer name the trial
  as a step of this change.
- A further research run that the conductor had started on its own (the
  same question through the coding client's web tools) was stopped before
  it produced anything. Nothing of it is stored.
- No further call to a programmatic research route is made for this change.

## What stays

- Runs `builds/022-api-route-trial-openai/` to
  `builds/027-api-report-claim-check/` stay as the record of what was run
  and what it cost in attempts. They are not proof for any task or
  requirement of this change.
- `final/021-browser-decisions/` stays as given. Its first answer, "API
  routes first", is overtaken by this instruction: the terms question of
  D14 is open again and waits for nothing.
- The research servers that were added to the client at local scope for
  the trial, and the idle-timeout line in the client settings, are left as
  they are. They are client configuration, not part of this change.

## Conductor's reading, to be confirmed by the maintainer

- The answer "Gemini: API only" of run 021 put Gemini outside the browser
  surface. With the API route out of the tests, Gemini has no test in this
  change. Whether Gemini returns to the browser surface was not said.
- A second message, "Also google search should work also", arrived with
  this instruction. Its meaning is not settled in this run.

## Files

- `receipt.json`: the instruction and its date.
- `command.txt`: how it was received and applied.
