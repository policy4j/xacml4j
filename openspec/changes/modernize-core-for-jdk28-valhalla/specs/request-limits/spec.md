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

### Requirement: Regular-expression matching is bounded
The `*-regexp-match` functions SHALL limit the length of the pattern, the
length of the input, and the time of one match. The limits SHALL be
configurable and SHALL default to 10,000 characters, 100,000 characters,
and 1,000 milliseconds. The time limit SHALL include the compilation of
the pattern. A match that reads the input after the time limit SHALL be
stopped, and a result that is produced after the time limit SHALL NOT be
returned. A pattern whose nested counted repetitions (`{n}`, `{n,}`,
`{n,m}`) have a product of minimum counts above a configurable bound
(default 1,000,000) SHALL be rejected before it is compiled; quoted text,
escapes, and character classes of the pattern SHALL be read as the
regular-expression engine reads them, so that they cannot hide a
repetition. A match that exceeds a limit SHALL make the function
Indeterminate with status code
`urn:oasis:names:tc:xacml:1.0:status:processing-error`. The status message
SHALL be a fixed text that contains neither the pattern nor the input. A
cache of compiled patterns SHALL hold a bounded number of patterns and
SHALL evict the least recently used one. A configured limit that is not a
positive number SHALL be rejected when the functions are loaded. This
requirement does not bound the processor time of a pattern whose match
works without reading the input in another way than through counted
repetitions; only the late result of such a match is discarded.

#### Scenario: A pattern that backtracks for a long time
- **WHEN** `string-regexp-match` evaluates a pattern whose match on the given input does not finish within the time limit
- **THEN** the function is Indeterminate with status `urn:oasis:names:tc:xacml:1.0:status:processing-error` after about the time limit

#### Scenario: Input above the length limit
- **WHEN** `string-regexp-match` is called with an input of 100,001 characters and the limits are the defaults
- **THEN** the function is Indeterminate with status `urn:oasis:names:tc:xacml:1.0:status:processing-error`

#### Scenario: Input at the length limit
- **WHEN** `string-regexp-match` is called with an input of 100,000 characters and a pattern that matches it
- **THEN** the function returns true

#### Scenario: Nested counted repetitions of an empty group
- **WHEN** `string-regexp-match` is called with the pattern `(?:(?:){2147483647}){2147483647}` and an empty input
- **THEN** the function is Indeterminate with status `urn:oasis:names:tc:xacml:1.0:status:processing-error` without matching the pattern

#### Scenario: A repetition behind an empty quoted section
- **WHEN** `string-regexp-match` is called with the pattern `(?:(?:){1000})\Q\E{2147483647}` and an empty input
- **THEN** the function is Indeterminate with status `urn:oasis:names:tc:xacml:1.0:status:processing-error` without matching the pattern

#### Scenario: Counted repetitions next to each other
- **WHEN** `string-regexp-match` is called with the pattern `(?:){1000}{1001}` and an empty input
- **THEN** the function returns true

#### Scenario: Braces inside a character class
- **WHEN** `string-regexp-match` is called with the pattern `[]a{1000001}]` and the input `a`
- **THEN** the function returns true

#### Scenario: A result that comes after the time limit
- **WHEN** a match that does not read the input finishes after the time limit
- **THEN** the function is Indeterminate with status `urn:oasis:names:tc:xacml:1.0:status:processing-error`

#### Scenario: A configured limit that is not a positive number
- **WHEN** a limit is configured as `0`, as a negative number, or as text that is not a number
- **THEN** reading the limits fails with an error that names the property

#### Scenario: Pattern cache at its size limit
- **WHEN** the pattern cache holds its maximum number of patterns and another pattern is compiled
- **THEN** the least recently used pattern is removed from the cache
