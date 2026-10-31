## ADDED Requirements

### Requirement: One bag per request attribute and decision
Within one individual decision, every evaluation of an attribute designator whose
attribute the request holds SHALL give a bag equal to the one the first evaluation
found (XACML 3.0 §7.3.5: every later test of an attribute uses the same bag that
was first tested); on one thread, later evaluations SHALL NOT search the request
again. Concurrent resolutions of one key in one decision (a policy information
point calling back from another thread) SHALL all return the bag stored first and
SHALL NOT block each other. Bags supplied by a policy
information point SHALL keep their current handling, and the resolved attributes
that a response can include SHALL NOT change. A failed resolution SHALL NOT be
remembered.

#### Scenario: Repeated designator
- **GIVEN** a request with a resource-id attribute and one individual decision that evaluates a designator for it twice
- **WHEN** both evaluations run
- **THEN** both return the same bag instance and the request callback is asked for the resource entity once (`DefaultEvaluationContextHandlerTest`)

#### Scenario: Attribute from a policy information point
- **GIVEN** a designator that the request does not hold and a policy information point that supplies it
- **WHEN** it is evaluated twice in one decision
- **THEN** the information point is asked once, and `getResolvedDesignators()` lists it as before (`DefaultEvaluationContextHandlerTest`)

#### Scenario: Concurrent first resolutions
- **GIVEN** two threads that resolve one request designator of one decision at the same moment (a barrier releases both)
- **WHEN** both resolutions complete
- **THEN** both return the same bag and neither blocks (`DefaultEvaluationContextHandlerTest`)

#### Scenario: Information point calling back
- **GIVEN** an information point that, while supplying one attribute, resolves a request attribute through the context it receives
- **WHEN** the outer designator is evaluated
- **THEN** both resolve, and a later evaluation of the request attribute gives the same bag (`DefaultEvaluationContextHandlerTest`)

#### Scenario: Separate decisions
- **GIVEN** two individual decisions of one multiple-decision request
- **WHEN** each evaluates the same designator
- **THEN** each decision resolves it in its own context (`DefaultEvaluationContextHandlerTest`)

#### Scenario: Decisions unchanged
- **GIVEN** the OASIS 2.0/3.0 and RSA 2008 suites
- **WHEN** they run after the change
- **THEN** the counts and results are unchanged (937 run, 12 skipped) (reactor gate)
