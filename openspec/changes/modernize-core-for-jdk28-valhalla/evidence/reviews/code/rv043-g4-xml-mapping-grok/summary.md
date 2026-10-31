# rv043: bugs 4.19 and 3.7 (IIIE301, IIIE303) review (Grok, adversarial)

Date: 2026-10-02. Lane: adversarial code reviewer, Grok Build grok-4.7
(xhigh), role `.ntm/roles/policy4j_code_review.md`, session cleared first.
Session `policy4j--g1`, pane 0. Read-only (lane and base sources, the
conformance log, the OASIS 2.0 Multiple Resource Profile PDF). Maven not run.

Scope: same as rv040.

Verdict: REQUEST_CHANGES (3 MEDIUM).

1. MEDIUM, `RequestContext.java:74-79` via
   `Xacml30RequestContextFromJaxbToObjectModelMapper.java:465` and
   `BaseJAXBUnmarshaller.java:65-70, 97-98`: a duplicate xml:id throws
   Guava's IllegalArgumentException (ImmutableMap) instead of SyntaxException
   with syntax-error. Fix: throw SyntaxException when the id is already
   indexed; test two Attributes with xml:id="dup".
2. MEDIUM, `RequestContext.java:383` (same in the base): equals compares
   `entities.equals(entities)` (itself), while hashCode (`:360-368`) includes
   entities and Category (`:199-200`) includes the reference id. Now that the
   mapper stores xml:id, two 3.0 requests that differ only by xml:id are
   equal with different hash codes. Fix: `entities.equals(r.entities)`; test
   unequal ids and the hashCode contract. (No HashMap/HashSet is keyed by
   RequestContext today; NoCachePolicyDecisionCache ignores the key.)
3. MEDIUM, `Xacml20RequestContextUnmarshaller.java:204-205`: only the 1.0
   scope URN sets IncludeInResult; a single Resource using
   urn:oasis:names:tc:xacml:2.0:resource:scope omits Result/@ResourceId,
   contrary to the 2.0 Multiple Resource Profile (1 Feb 2005) 4.1. Fix: match
   either URN; test with resource-id urn:root. (Also rv040 LOW 1.)

Confirmed: IIIC001 passes; IIIE301 and IIIE303 produce the expected
decisions and fail only at the known harness NPE (bug 3.11); the draft URN is
request-input only.
Resolution: repair packet to the implementer lane (xmlbugs), with rv040 LOW 2.
