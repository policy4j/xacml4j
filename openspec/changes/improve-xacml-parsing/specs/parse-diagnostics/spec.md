## ADDED Requirements

### Requirement: Positioned diagnostics
Every problem found while parsing an XACML 2.0 or 3.0 XML policy, policy set,
or request, or an XACML JSON request, SHALL be reported as a diagnostic with a
severity (error, warning, or note), a stable code, a message, the source name
(the system id, file name, or a caller-given name; `<input>` when none is
known), the 1-based line and column of the element, attribute, or JSON value
that causes it, and the element path (XML) or JSON path. When the input has
no source text (a caller-created DOM node or JAXB object), the line and column
SHALL be reported as unavailable, never as an invented value, and the element
path SHALL still be given. A diagnostic SHALL NOT carry a home or absolute
path that the caller did not pass as the source name.

#### Scenario: Unknown function in a policy
- **GIVEN** a XACML 3.0 policy whose `Apply` element on line 12, column 7 names an unknown `FunctionId`
- **WHEN** the policy is parsed
- **THEN** a `SyntaxException` is thrown whose diagnostics hold one error with the function code, line 12, column 7, the element path to that `Apply`, and the unknown id in the message (`PolicyDiagnosticsTest`)

#### Scenario: Malformed XML
- **GIVEN** a request whose line 4 has an unclosed tag
- **WHEN** the request is parsed
- **THEN** one error with the well-formedness code and the parser's line and column is reported, and the parse stops (`RequestDiagnosticsTest`)

#### Scenario: Invalid JSON value
- **GIVEN** a JSON request whose attribute value on line 9 is not a valid `xs:integer`
- **WHEN** the request is parsed
- **THEN** an error with the value code, line 9, its column, and the JSON path of the value is reported (`JsonRequestDiagnosticsTest`)

#### Scenario: Caller DOM input
- **GIVEN** a policy passed as a DOM `Node` whose `Apply` names an unknown `FunctionId`
- **WHEN** the policy is parsed
- **THEN** the diagnostic has the function code and the element path, and its line and column are unavailable (`PolicyDiagnosticsTest`)

#### Scenario: Unknown root element
- **GIVEN** an XML document whose root element is neither a policy nor a policy set
- **WHEN** it is parsed as a policy
- **THEN** a `SyntaxException` with a root-element diagnostic is thrown, not an `IllegalArgumentException` (`PolicyDiagnosticsTest`)

### Requirement: Several errors per parse
After a model-level error (a bad identifier, value, reference, or missing
attribute), the parser SHALL continue with the next sibling element and SHALL
report every error it finds, up to a maximum (default 100, configurable); it
SHALL then report that more errors were suppressed. A well-formedness or
security-limit error SHALL stop the parse. No model SHALL be returned when an
error was reported.

#### Scenario: Three bad rules
- **GIVEN** a policy with three rules, each with a different model error
- **WHEN** it is parsed
- **THEN** the exception holds three errors in document order (`PolicyDiagnosticsTest`)

#### Scenario: Error cap
- **GIVEN** a policy with 150 model errors and a maximum of 100
- **WHEN** it is parsed
- **THEN** 100 errors and one note that 50 more were suppressed are reported (`PolicyDiagnosticsTest`)

### Requirement: Compiler-like text format
The text formatter SHALL write each diagnostic as
`<source>:<line>:<column>: <severity>: <message> [<code>]` (the code at the end,
so the GCC-style patterns of editors and CI match; design D13), then the source
line and a caret under the column, then one `note:` or `help:` line per note;
a hint for an unknown function, data type, or combining-algorithm id SHALL
name the closest known id when its edit distance is at most 2 (or a third of
the id's last segment, whichever is larger).

#### Scenario: Golden output
- **GIVEN** each document of the broken-document catalog (`xacml-xml/src/test/resources/diagnostics/`)
- **WHEN** it is parsed and its diagnostics are formatted
- **THEN** the text equals the document's golden file (`DiagnosticGoldenTest`)

#### Scenario: Did you mean
- **GIVEN** a policy that uses `urn:oasis:names:tc:xacml:1.0:function:string-equa`
- **WHEN** its diagnostics are formatted
- **THEN** a `help:` line names `urn:oasis:names:tc:xacml:1.0:function:string-equal` (`DiagnosticFormatterTest`)

### Requirement: Machine-readable format
A second formatter SHALL write the diagnostics of one or more sources in a
machine-readable form (SARIF 2.1.0 unless research Q5 picks another), with
the same codes, positions, and messages as the text form.

#### Scenario: Same content as the text form
- **GIVEN** the diagnostics of a catalog document
- **WHEN** both formatters write them
- **THEN** every code, line, column, and message of the text form appears in the machine form (`DiagnosticFormatterTest`)

### Requirement: Compatible exception
`SyntaxException` SHALL keep its public constructors and SHALL expose its
diagnostics as an unmodifiable list (empty for an exception built without
any). Its message SHALL start with the first diagnostic in the text form's
first line. Callers that ignore the diagnostics SHALL see the same exception
types as before, except for the unknown-root case above.

#### Scenario: Old constructor
- **GIVEN** `new SyntaxException("message")`
- **WHEN** its diagnostics are read
- **THEN** the list is empty and the message is unchanged (`SyntaxExceptionTest`)

#### Scenario: Conformance unchanged
- **GIVEN** the OASIS 2.0/3.0 and RSA 2008 suites
- **WHEN** they run
- **THEN** the counts are unchanged (937 run, 12 skipped) (reactor gate)

### Requirement: No cost on valid input
Collecting positions SHALL NOT change the parse result of a valid document;
its time and allocation cost on valid documents SHALL be measured by the parse
benchmarks and reported in the group's JMH before/after pair.

#### Scenario: Same model
- **GIVEN** every policy of the RSA 2008 and OASIS 3.0 conformance sets
- **WHEN** each is parsed before and after the change
- **THEN** both models give the same decisions for the suites' requests (conformance gate)
