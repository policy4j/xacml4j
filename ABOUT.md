# About xacml4j

xacml4j is an open-source Java library that implements the OASIS eXtensible Access Control Markup
Language (XACML) 3.0 and 2.0: the policy language, the request and response context, and a policy
decision point (PDP) that evaluates them. It is a library, not a server: you embed the PDP in your
application or service, and you decide how requests reach it.

**Topics:** XACML, XACML 3.0, attribute-based access control (ABAC), authorization, access control,
policy engine, policy decision point (PDP), policy information point (PIP), fine-grained
authorization, externalized authorization, Java.

## Why XACML

XACML separates authorization policy from application code. A policy states who can do what to
which resource under which conditions, in terms of attributes of the subject, the resource, the
action, and the environment. An application (the policy enforcement point) asks the PDP for a
decision and enforces it. Policies can then change, be reviewed, and be tested without changing
the application.

## Standards coverage

| Area | Coverage |
| --- | --- |
| XACML 3.0 core | Policies and policy sets, targets, rules, conditions, variables, obligations and advice, policy references, extended Indeterminate values, `ReturnPolicyIdList`, `IncludeInResult` |
| Data types | The XACML 3.0 types: string, boolean, integer, double, date, time, dateTime, the two durations, anyURI, hexBinary, base64Binary, rfc822Name, x500Name, ipAddress, dnsName, and xpathExpression; plus `entity` and `jsonPathExpression` |
| Functions | The standard function set: equality, arithmetic, string, conversion, comparison, date and time arithmetic, bag and set functions, higher-order functions, regular-expression match functions, special match functions, XPath functions |
| Combining algorithms | Deny-overrides, permit-overrides and their ordered variants, deny-unless-permit, permit-unless-deny, first-applicable, only-one-applicable, and the legacy XACML 2.0 algorithms |
| Multiple Decision Profile | Repeated attribute categories, request references, and XPath content selectors |
| XACML 2.0 | Policies, requests, and responses in XML |
| Formats | XACML 3.0 and 2.0 XML; XACML 3.0 JSON requests and responses |
| Attribute selectors | XPath over XML content, and JSONPath over JSON content |

Regular-expression functions run on a pluggable engine. The `xf` profile follows the regular
expressions of XQuery 1.0 and XPath 2.0 Functions and Operators, §7.6, which XACML 3.0 references;
the `legacy` profile keeps the behavior of earlier xacml4j versions.

The build runs the OASIS XACML 2.0 and 3.0 conformance suites and the RSA 2008 interoperability
suite.

## Architecture

```text
  application (PEP)
        │ RequestContext
        ▼
  ┌───────────────────────────────┐        ┌─────────────────────────────┐
  │ PolicyDecisionPoint           │        │ PolicyRepository            │
  │  request handlers (MDP)       │◀──────▶│  policies, policy sets,     │
  │  root policy evaluation       │        │  references, listeners      │
  │  combining algorithms         │        └─────────────────────────────┘
  │  functions                    │        ┌─────────────────────────────┐
  │  target index (optional)      │◀──────▶│ PolicyInformationPoint      │
  └───────────────────────────────┘        │  attribute resolvers,       │
        │ ResponseContext                  │  caching, parallel lookups  │
        ▼                                  └─────────────────────────────┘
  application enforces the decision
```

- **Model (`xacml-core`):** an immutable object model of policies, expressions, values, requests,
  and responses, with builders. Marshalling turns XML or JSON into this model and back.
- **PDP (`xacml-pdp`):** evaluates a request against a root policy or policy set. It resolves policy
  references through the repository and attributes through the PIP. Each decision has its own
  evaluation context. One PDP serves concurrent decisions.
- **PIP:** attribute resolvers supply attributes that a request does not carry. A resolver is called
  only when a policy needs its attribute; its results can be cached, resolved in parallel, and
  shared by overlapping lookups in one decision.

## Extension points

| Extension | How |
| --- | --- |
| Functions | A class annotated `@XacmlFunctionProvider`, with static methods annotated `@XacmlFuncSpec`, `@XacmlFuncReturnType`, and `@XacmlFuncParam`; registered through `ServiceLoader` |
| Combining algorithms | Methods annotated `@XacmlRuleDecisionCombiningAlgorithm` or `@XacmlPolicyDecisionCombiningAlgorithm` |
| Attribute resolvers | `AttributeResolverDescriptor.builder(...)`, registered in a `ResolverRegistry` |
| Policy repositories | `PolicyRepository`; the in-memory repository is built in |
| Regular-expression engines | The `RegexEngine` SPI, selected per function provider or repository |
| Marshalling | `MarshallingProvider`, discovered through `ServiceLoader` |
| Decisions | A decision cache, a decision auditor, and a decision observer on `PolicyDecisionPointBuilder` |

## Performance

The PDP is tuned with JMH benchmarks and allocation profiles. Indicative results on the RSA 2008
interoperability policies (JDK 25 with C2, Apple M2 Pro):

| | Time per decision | Allocated per decision |
| --- | ---: | ---: |
| First measured baseline | about 13.7 µs | about 41.5 KB |
| Current development line | about 2 µs | about 0.7 KB |

A policy set with 1,000 children decides in about 0.5 µs with the target index on. The benchmark
ledger and the method are in
[openspec/changes/optimize-pdp-evaluation](openspec/changes/optimize-pdp-evaluation) and
[docs/ai/BENCHMARKS.md](docs/ai/BENCHMARKS.md).

## Status

xacml4j 2.0 is under active development on JDK 25; the version is `2.0.0-SNAPSHOT`. Work is planned
and tracked as OpenSpec changes under [openspec/changes](openspec/changes):

| Change | Scope |
| --- | --- |
| `optimize-pdp-evaluation` | Decision time and allocation, with a controlled JMH pair for every batch |
| `add-regex-engine-spi` | The regular-expression engine SPI; an RE2/J engine with linear-time matching is planned |
| `improve-xacml-parsing` | Parsing security limits and diagnostics with positions |
| `adopt-conformance-test-vectors` | More conformance test vectors |
| `modernize-core-for-jdk28-valhalla` | Preparing the value types for JDK value classes |
| `explore-policy-evaluation-strategies`, `add-authorization-model-and-policy-languages` | Research and design |

## History

xacml4j started in 2009 as an implementation of XACML 2.0 and 3.0 for Java, under the Xacml4J.org
organization. The 1.x line was released up to 1.5.0 (tags `xacml-1.3.0` to `xacml-1.5.0`). The 2.0
line moves to JDK 25, JUnit 5, Logback, and a refactored core, and adds the performance work above.

Maintainers: Giedrius Trumpickas and Valdas Sevelis.

## Resources

- [OASIS XACML 3.0 core specification (Plus Errata 01)](https://docs.oasis-open.org/xacml/3.0/errata01/os/xacml-3.0-core-spec-errata01-os-complete.html)
- [OASIS XACML 3.0 Multiple Decision Profile](https://docs.oasis-open.org/xacml/3.0/multiple/v1.0/xacml-3.0-multiple-v1.0.html)
- [OASIS XACML 2.0 core specification](https://docs.oasis-open.org/xacml/2.0/access_control-xacml-2.0-core-spec-os.pdf)
- [OASIS XACML Technical Committee](https://www.oasis-open.org/committees/xacml/)
- [README.md](README.md): getting started, examples, and building from source
- [AGENTS.md](AGENTS.md): contributor guide

## License

The GNU Lesser General Public License, version 3. See [LICENSE](LICENSE).
