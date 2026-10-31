# Maintainer decisions on the browser plan

Task 5.2 (`policy4j-971.5.2`), 2026-10-10. The maintainer answered eight
questions about the browser plan in the conversation, after a written guide
with the options and a recommendation for each.
JDK, Maven, XACML conformance and JMH are not applicable.

## Result

PARTIAL. Six questions are decided. The terms question is not: the
maintainer chose to try the programmatic routes first and to decide the
consumer surfaces after reading those reports. This run is not an approval
to build the browser surface. Task 5.2 stays open.

| Question | Answer | State and effect |
| --- | --- | --- |
| Terms risk per provider, browser and desktop surfaces (D14) | "API routes first": one API research call each for ChatGPT and Gemini before anything is built; the consumer surfaces, including Claude, are decided afterwards | NOT DECIDED. Tasks 5.3 to 5.14, 4.1 and 4.2 keep waiting. The trial is task 5.15 |
| Driver (D10) | Scripted bridge on the Playwright library | DECIDED, for the case that the surface is built. D10 stands |
| The two cuts (D13) | Both accepted | DECIDED. `policy4j-6p67` and `policy4j-j5ft` stay follow-up work |
| Gemini (D11) | API only | DECIDED. Gemini leaves the browser surface. No Google session is put in a driven browser |
| Live browser budget | As proposed | DECIDED, for the case that the surface is built: per provider one characterization run, one acceptance run and one follow-up chat turn; Claude and ChatGPT only, so at most four research runs and two chat turns |
| API trial | Run the OpenAI call now; Gemini after its research server is wired to the client | DECIDED. One call per route with the approved public comparison question. Run `builds/022-api-route-trial-openai/` |
| Browser run toward the two-provider objective | Decide after the trial | NOT DECIDED |
| Acknowledgement on a computer without biometric authentication (D14) | The surface is unavailable there | DECIDED. Nothing weaker replaces the biometric step |

## Not asked

- The desktop surface under the terms question (continue as approved, add
  the same gate, or stop). The guide advised the same answer per provider on
  both surfaces. It waits with the terms question.
- Separate decision records for the driver boundary and the reservation
  rule (D15).
- Whether biometric authentication is the way to give the acknowledgement
  was not asked as its own question. The answer on computers without
  biometrics takes it as given. This is the conductor's reading.

## What follows for the plan

- The plan text still names three providers on the browser surface
  (proposal, D9, D11, D14, seven places in the spec, tasks 5.7 and 5.10).
  The Gemini decision overrides that text. The revision is made with the
  next plan version, once the terms question is answered, because that
  answer decides whether the surface is built at all.
- The fourth plan version has still not been read by a reviewer. Task 5.1
  stays open.

## Limits

- A selection in a dialog shows what the maintainer chose among the options
  shown. `receipt.json` has the options, so the choice can be read in
  context.
- The budget and the driver answers take effect only if the terms question
  is later answered with acceptance for a provider.

## Files

- `receipt.json`: every question, the options shown and the answer.
- `command.txt`: how the answers were obtained.
