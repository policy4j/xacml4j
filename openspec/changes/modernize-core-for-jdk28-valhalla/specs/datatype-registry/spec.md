## ADDED Requirements

### Requirement: Every XACML data type is registered
The data type registry SHALL resolve, by canonical identifier, every data
type the library supported at `a8a90d46`, including at least:
`string`, `boolean`, `integer`, `double`, `time`, `date`, `dateTime`,
`anyURI`, `hexBinary`, `base64Binary`, `dayTimeDuration`,
`yearMonthDuration`, `x500Name`, `rfc822Name`, `ipAddress`, `dnsName`,
`xpathExpression`, and `entity`.

#### Scenario: Round-trip every registered type
- **WHEN** each registered canonical identifier is looked up and a value is parsed from and written back to its XACML string form
- **THEN** lookup succeeds and the written string equals the canonical lexical form of the input

#### Scenario: Legacy function sets still register
- **WHEN** the default function provider loads
- **THEN** `anyURI-equal`, `rfc822Name-match`, `ipAddress-*`, and `entity`-typed functions are registered

### Requirement: Type identifiers and aliases are unique
Each canonical data type identifier and each alias SHALL map to exactly one
data type. Registering a duplicate identifier or alias SHALL fail at
startup. In particular, exactly one type SHALL own
`http://www.w3.org/2001/XMLSchema#integer`.

#### Scenario: Duplicate identifier rejected
- **WHEN** two data types declare the same canonical identifier or alias
- **THEN** registry construction fails with an error naming both types

#### Scenario: Integer has one owner
- **WHEN** the registry resolves `http://www.w3.org/2001/XMLSchema#integer`
- **THEN** it returns the XACML integer type, and no unsigned or 32-bit type claims that identifier

### Requirement: Identifier equality is canonical
Identifier values (category, attribute, data type, status code, issuer)
SHALL compare equal only when their canonical forms are equal, codepoint by
codepoint (XACML 3.0 §7.20). Equality SHALL be reflexive, symmetric, and
transitive, and equal identifiers SHALL have equal hash codes. Aliases
SHALL be resolved to the canonical form by the registry and SHALL NOT take
part in equality.

#### Scenario: Alias resolution does not break transitivity
- **WHEN** identifier A shares an alias with B, and B shares a different alias with C
- **THEN** A equals C only if their canonical forms are equal, and equals/hashCode stay consistent

#### Scenario: Identifiers as map keys
- **WHEN** an identifier is used as a key in a hash map and looked up with an equal identifier built from the same canonical string
- **THEN** the lookup returns the stored entry

### Requirement: Integer values are arbitrary-precision-safe or fail
The XACML integer type SHALL either represent the full `xs:integer` value
range or SHALL make parsing and arithmetic of out-of-range values evaluate
to Indeterminate with status `processing-error`; it SHALL NOT wrap
silently. Integer parsing SHALL accept an optional leading sign and SHALL
reject any other non-digit character.

#### Scenario: Overflow is not silent
- **WHEN** `integer-add` is applied to `9223372036854775807` and `1`
- **THEN** the result is either `9223372036854775808` or Indeterminate, never a negative number

#### Scenario: Lexical validation
- **WHEN** the strings `"+1"`, `"-1"`, and `"12+3"` are parsed as integers
- **THEN** `+1` and `-1` parse, and `12+3` is a syntax error
