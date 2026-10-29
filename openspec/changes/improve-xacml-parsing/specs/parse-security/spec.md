## ADDED Requirements

### Requirement: Hardened XML factories on every path
Every XML parser, transformer, and schema validator that xacml4j creates
(SAX for JAXB, DOM, StAX, `Transformer`, `SchemaFactory`, `Validator`) SHALL
come from one owner that enables secure processing and disables external
DTDs, external general and parameter entities, external schemas and
stylesheets, and XInclude. A document with a DOCTYPE declaration SHALL be
rejected with a security diagnostic. A factory that cannot be configured SHALL
fail the parse; it SHALL NOT fall back to an unconfigured factory.

#### Scenario: External entity
- **GIVEN** each document of the attack corpus that declares an external entity through `file:` or `http:` (with a local test server that records requests)
- **WHEN** it is parsed as a policy and as a request, through every entry point (byte array, stream, `StreamSource`, `InputSource`, DOM via `DOMUtil`)
- **THEN** the parse fails with the DOCTYPE diagnostic, no file is read, and the server records no request (`XmlAttackCorpusTest`)

#### Scenario: Entity expansion
- **GIVEN** a "billion laughs" document and a quadratic-blowup document
- **WHEN** they are parsed
- **THEN** each fails with the DOCTYPE diagnostic within one second and without heap growth over 64 MB (`XmlAttackCorpusTest`)

#### Scenario: Factory setup fails
- **GIVEN** a factory property that the JAXP implementation rejects (simulated)
- **WHEN** a parser is requested
- **THEN** the request fails with an exception; no parser is returned (`SecureXmlFactoriesTest`)

#### Scenario: Transformer
- **GIVEN** a `DOMUtil` serialization of a node
- **WHEN** the transformer is built
- **THEN** secure processing is on and external DTD and stylesheet access is empty (`SecureXmlFactoriesTest`)

### Requirement: Parse limits
Every XML and JSON parse SHALL enforce limits with defaults set in the design
(document bytes, element or nesting depth, element count, attributes per
element, attribute value and text length, name length, and for JSON string,
number, and member-name length). Each limit SHALL be configurable per parse
through the parse options. Exceeding a limit SHALL stop the parse with a limit
diagnostic that names the limit and its value; it SHALL NOT produce a partial
model.

#### Scenario: Deep nesting
- **GIVEN** a policy whose `Apply` elements nest one deeper than the depth limit
- **WHEN** it is parsed
- **THEN** a limit error naming the depth limit is reported at the first element past it (`ParseLimitsTest`)

#### Scenario: Limits on a caller's reader
- **GIVEN** a caller-supplied `XMLStreamReader`, `XMLEventReader`, `SAXSource` with its own `XMLReader`, and a DOM node, each holding a document over the element-count limit
- **WHEN** each is parsed
- **THEN** each fails with the element-count limit error (`ParseLimitsTest`)

#### Scenario: Raised limit
- **GIVEN** the same deep policy and parse options with a higher depth limit
- **WHEN** it is parsed
- **THEN** it parses (`ParseLimitsTest`)

#### Scenario: JSON limits and duplicates
- **GIVEN** JSON requests over the nesting, string-length, and number-length limits, and one with a duplicate member name in one object
- **WHEN** each is parsed
- **THEN** each fails with the matching limit or duplicate-member diagnostic and its JSON position (`JsonParseLimitsTest`)

### Requirement: Caller-supplied parsers
A caller that passes its own `XMLReader`, StAX reader, or DOM node keeps its
own DTD and entity settings, and the parse SHALL still enforce the limits. A
strict option SHALL reject caller-supplied parsers with a security diagnostic.

#### Scenario: Strict option
- **GIVEN** parse options with the strict option and a `SAXSource` with its own `XMLReader`
- **WHEN** it is parsed
- **THEN** the parse fails with the caller-parser diagnostic (`ParseLimitsTest`)

### Requirement: Optional XSD validation
XSD validation SHALL be off by default. When parse options enable it, the
document SHALL be validated against the bundled OASIS XACML 2.0 or 3.0 schema
of its namespace, compiled once per JVM and resolved only from the class path
(no network or file access for imports), and every schema error SHALL become
an error diagnostic with its line and column. A document of an unknown
namespace SHALL fail with a schema diagnostic when validation is on.

#### Scenario: Off by default
- **GIVEN** a policy that is well-formed, maps to the model, and violates the schema (an element out of order that the mapper accepts)
- **WHEN** it is parsed with default options
- **THEN** it parses as today (`XsdValidationTest`)

#### Scenario: On
- **GIVEN** the same policy and options with XSD validation enabled
- **WHEN** it is parsed
- **THEN** the parse fails with a schema error at the element's line and column (`XsdValidationTest`)

#### Scenario: No external schema access
- **GIVEN** validation enabled and the network and file system denied to the schema resolver (a resolver that records every lookup)
- **WHEN** a policy is validated
- **THEN** every lookup is served from the class path (`XsdValidationTest`)

#### Scenario: Conformance fixtures
- **GIVEN** the conformance policies and requests (read-only)
- **WHEN** each is validated with the option on
- **THEN** the result equals the characterization of research Q6 (`XsdConformanceCharacterizationTest`)
