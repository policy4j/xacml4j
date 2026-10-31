# Discovery synthesis (task 1.1)

Six read-only lanes ran on 2026-10-27 (lane table in `README.md`). Each
finding below is adopted in design D13 or left open; nothing here is
measured. Time and allocation figures in the reports are hypotheses for the
JMH pairs of task 2.2 and group 9.

## Adopted

| Area | Finding | Lanes | Design |
| --- | --- | --- | --- |
| Diagnostic model | Spans instead of points; typed `note` and `help` children; XML path or JSON Pointer; `Serializable`; families `VAL`, `RSP`, `DIA` | DQ1 | D13 (D1) |
| Positions | Capture and binding are separate choices; SAX and JDK StAX give event-end positions; Woodstox gives event-start positions; positions carry a kind (`start`, `event`, `unavailable`); three capture candidates are measured before one is chosen | DQ1, DQ4, DQ6 | D3, D13 (D3) |
| Recovery | JAXB validation events go to the collector; an invalid child makes its parent invalid instead of absent; the variable stack is restored after a failure; diagnostics are ordered by document ordinal | DQ1, DQ4 | D13 (D4) |
| Text format | `<source>:<line>:<col>: error: <message> [<CODE>]`, code last; SARIF 2.1.0 by a small built-in writer | DQ1 | D13 (D5) |
| XML security | Factories from `newDefaultInstance()`, configured once per JVM; JDK limit errors mapped to `LIM4nn`; system-id-only sources rejected; fail fast on `com.sun.xml.bind.disableXmlSecurity`; limits sized from the corpora | DQ2, DQ6 | D13 (D6) |
| XSD validation | Off by default; `Schema` compiled lazily once per JVM; a `ValidatorHandler` per thread | DQ5 | D13 (D7) |
| JSON | Provider identities and selection at the factory layer; the three SPI defects fixed first; the XACML JSON Profile is the first bake-off gate; strict RFC 8259 mode | DQ2, DQ3 | D13 (D8) |
| Parse cycles | Factory and parser reuse first, then streaming JSON, mapper collections, and the direct StAX parser (3.0 request first); Woodstox set option by option | DQ4, DQ5, DQ6 | D13 (D9) |

## Disagreements

- Order of the direct StAX parser: DQ6 proposes it early for positions; DQ4
  shows that JAXB over Woodstox gives the same start positions without a
  mapper rewrite. The design follows DQ4: the direct parser is a late D9
  cycle, decided by its JMH pair.
- Gain estimates: DQ5 expects about half of the parse time and allocation
  from cycle 1; DQ4 gives no estimate. Neither is evidence until the pair
  runs.

## Open for the maintainer

- None that blocks group 2. The serialization question of the PDP change
  (`optimize-pdp-evaluation` design Open Question 1) is separate.
- Native-image compatibility of the RI-generated classes and of the parser
  providers is a gate of the checker CLI (group 10), not tested here.
