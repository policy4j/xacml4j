## ADDED Requirements

### Requirement: Multiple-decision XPath expansion is bounded
The PDP SHALL limit the expansion of a request that carries
`urn:oasis:names:tc:xacml:3.0:profile:multiple:content-selector`
attributes. There SHALL be a limit on the number of node paths that one
selector selects in its category and a limit on the number of individual
requests that the request expands to. Both limits SHALL be configurable
and SHALL default to 100. A request that exceeds a limit SHALL get exactly
one result with Decision Indeterminate and status code
`urn:oasis:names:tc:xacml:1.0:status:processing-error`, and the PDP SHALL
NOT evaluate any individual request of it. The status message SHALL be a
fixed text that names no request data. The result SHALL carry the
attributes of the request that are marked `IncludeInResult`.

#### Scenario: One selector selects too many nodes
- **WHEN** a request has one `multiple:content-selector` whose XPath selects 101 nodes and the limits are the defaults
- **THEN** the response has one result with Decision Indeterminate and status `urn:oasis:names:tc:xacml:1.0:status:processing-error`, and no individual request is evaluated

#### Scenario: The product of the categories is too large
- **WHEN** a request has selectors in two categories that select 11 nodes each and the limits are the defaults
- **THEN** the response has one result with Decision Indeterminate and status `urn:oasis:names:tc:xacml:1.0:status:processing-error`, and no individual request is evaluated

#### Scenario: A request at the limit is expanded
- **WHEN** a request has selectors in two categories that select 10 nodes each and the limits are the defaults
- **THEN** 100 individual requests are evaluated and the response has 100 results

#### Scenario: Configured limits
- **WHEN** the handler chain is created with 200 node paths per category and 200 individual requests, and a selector selects 101 nodes
- **THEN** 101 individual requests are evaluated
