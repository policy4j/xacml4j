# Specification and conductor decisions

The primary [XML Schema Part 2 section 3.2.2](https://www.w3.org/TR/xmlschema-2/#boolean)
defines four boolean lexical forms: `true`, `false`, `1`, `0`. Case variants and
other words are invalid. Serialization remains the canonical `true` or `false`.
The primary page was checked with native Tavily extract during this packet.

[Section 4.3.6](https://www.w3.org/TR/xmlschema-2/#rf-whiteSpace) defines whitespace
replacement/collapse using only space, tab, carriage return and line feed.
The boolean type fixes its whiteSpace facet to collapse. Leading/trailing XML
whitespace is accepted. None of the four valid tokens contains whitespace, so
keeping internal whitespace invalid has the same accepted lexical space as
full collapse. Form feed, vertical tab, nonbreaking space and Unicode em space
must not become trim characters; focused tests reject them.

The conductor supplied the IIIG300 adjudication: XACML 3.0 Plus Errata 01
section 5.48 includes all fully applicable policies. This fixture expects two
policy ids while the corrected engine produces three. The manifest keeps the
case skipped with `policy4j-the.3.9`. This adjudication was recorded as supplied;
IIIG300 was not executed in this packet.

The conductor required both IIA005 cases to stay skipped under
`policy4j-the.4.17`: request syntax errors now throw SyntaxException; mapping
them to an XACML response is still pending a decision. Those cases were not
executed. Their stale IllegalArgumentException reasons were replaced.
