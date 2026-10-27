# 006: cycle A tests added after plan review rv005 (task 4.4)

Date: 2026-10-27. Oracle GraalVM 25.0.4, Maven 3.9.16 offline.

- `DefaultEvaluationContextHandlerTest.concurrentFirstResolutionsReturnTheBagStoredFirst`: a barrier holds two threads inside the request lookup of one key; both return the same bag, and a later resolve returns it too (no lock, no deadlock).
- `DefaultEvaluationContextHandlerTest.informationPointCallingBackResolvesTheRequestAttributeOnce`: a PIP resolves a request attribute through its context while supplying another; the request is searched once and later resolves return the same bag.
- `RequestAttributeMemoPdpTest.eachIndividualDecisionResolvesItsOwnResource`: a request with two resource categories through `defaultRequestHandlers()` gives two results; each individual decision resolves its own resource-id (doc-1, doc-2), one bag per decision.

Result: DefaultEvaluationContextHandlerTest 38 tests, RequestAttributeMemoPdpTest 1 test, 0 failures.
