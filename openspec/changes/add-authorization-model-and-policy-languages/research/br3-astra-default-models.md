<!-- Lane BR3 of the authorization-model brainstorm swarm, Codex GPT-6 Astra, read-only; scope: default XACML 2.0, 3.0, profile, and 4.0 (ACAL) models; report from the NTM pane, paths made repository-relative. -->

FINAL REPORT (xacml4j JMH benchmark engineer, GPT-6)

Recommend **versioned standards catalogs plus separate application authorization models**. Keep XACML 2.0, XACML 3.0, and each profile independently selectable. Add XACML 4.0 as an explicitly experimental catalog pinned to its published draft.

The central distinction is between **mandatory implementation support**, **required request content**, and **attributes supplied during resolution**. These cannot safely share one `required` flag.

Research date: **27 October 2026**. This is an unscheduled design recommendation. No files, configuration, repository state, or tracking records were changed. No builds, tests, benchmarks, or profilers ran.

1. **Facts and sources**

Research classification: standards discovery and cross-source synthesis, grounded in repository source.

| Tool | Coverage |
|---|---|
| Tavily search | **PARTIAL** — returned useful discovery results, then HTTP 429. Further research used DuckDuckGo and web fetch. |
| DuckDuckGo search | **PARTIAL** — some searches worked; others returned an ambiguous no-results/bot-detection response. Those responses are not evidence of absence. |
| DuckDuckGo fetch | **SUPPORTED** for the cited HTML documents and public raw license files; **PARTIAL** for binary Word documents and blocked archive pages. |
| Web search/open | **SUPPORTED** for official pages and the XACML 2.0 PDF; **PARTIAL** for the historical errata download and LDAP proposal archive. Each failed retrieval received one fallback attempt. |
| GitHub connector | **UNAVAILABLE by instruction**. Not called; no sign-in attempted. |
| Local `rtk` reads/searches | **SUPPORTED** for the cited source and documentation. Runtime behavior remains unverified. |

The historical XACML 2.0 errata Word document failed through web fetch; DuckDuckGo returned binary content rather than usable document text. Consequently, the 2.0 observations below distinguish the published 2005 standard from unresolved errata reconciliation.

**Core vocabulary and conformance**

For compactness, the following notation expands to exact identifiers:

- `u1:` = `urn:oasis:names:tc:xacml:1.0:`
- `u2:` = `urn:oasis:names:tc:xacml:2.0:`
- `u3:` = `urn:oasis:names:tc:xacml:3.0:`
- `xs:` = `http://www.w3.org/2001/XMLSchema#`

These abbreviations are report notation, not proposed aliases.

The editions inspected were [XACML 2.0, OASIS Standard, 1 February 2005](https://docs.oasis-open.org/xacml/2.0/access_control-xacml-2.0-core-spec-os.pdf), and [XACML 3.0 plus Errata 01, OASIS Standard incorporating Approved Errata, 12 July 2017](https://docs.oasis-open.org/xacml/3.0/errata01/os/xacml-3.0-core-spec-errata01-os-complete.html). Section 10 supplies conformance classifications; Appendices A and B supply definitions.

| Area | Identifiers and classification | Modeling consequence |
|---|---|---|
| Subject categories | `u1:subject-category:access-subject` is **M** in both editions. `codebase`, `intermediary-subject`, `recipient-subject`, `requesting-machine` are **O**. | Category support is separate from a required subject identifier. |
| Resource/action/environment categories | XACML 2.0 uses dedicated structural elements. XACML 3.0 defines `u3:attribute-category:{resource,action,environment}`; Appendix B.2 requires these for translating corresponding 2.0 categories and recommends them for new policies. | Preserve the 2.0 structural mapping; do not manufacture 2.0 category URIs. |
| Subject attributes | Section 10.2.6 marks `u1:subject:{subject-id,subject-id-qualifier,key-info,authentication-time,authentication-method,request-time,session-start-time,authn-locality:ip-address,authn-locality:dns-name}` **O**. | Do not require every request to identify a subject by `subject-id`. Several datatype choices remain domain-dependent. |
| Resource attributes | `u1:resource:resource-id` is **M**; `resource-location` and `simple-file-name` are **O**. `u2:resource:target-namespace` is defined as `xs:anyURI`; it appears in the 3.0 conformance table. | “M” does not mean inject an identifier into every request irrespective of policy and context. |
| Action attributes | `u1:action:action-id` is **O**. Appendix B also defines `action-namespace`. `u1:action:implied-action` is an **attribute value**, not another attribute. | Keep attributes and enumerated values in different catalog sections. |
| Environment attributes | `u1:environment:current-time`, `current-date`, `current-dateTime` are **M**, with types `xs:time`, `xs:date`, `xs:dateTime`. | They may be absent from the incoming request; the context handler supplies missing values. |
| Status codes | `u1:status:{ok,missing-attribute,syntax-error,processing-error}`. Section 10.2.4 requires all four when status-code support is implemented. | Status vocabulary belongs outside entity attributes. |

Sources: core §10.2.4–10.2.7 and Appendix B in the [2.0 standard](https://docs.oasis-open.org/xacml/2.0/access_control-xacml-2.0-core-spec-os.pdf) and [3.0 standard with errata](https://docs.oasis-open.org/xacml/3.0/errata01/os/xacml-3.0-core-spec-errata01-os-complete.html).

A catalog must preserve edition-specific discrepancies rather than resolve them by spelling similarity:

- **Authentication locality:** 2.0 Appendix B uses the `u1:` identifiers with `xs:string`. The 3.0 appendix defines `u3:` identifiers with `u2:data-type:ipAddress` and `dnsName`, while its conformance table still lists the `u1:` identifiers.
- **Authentication method:** the 2.0 appendix and conformance table contain different identifier spellings.
- **Status support:** 3.0 §10.2.1 marks `StatusCode` mandatory, while §10.2.4 describes its implementation support as optional.
- **Multiplicity:** 3.0 Appendix B.4 limits the listed subject attributes per subject, while §7.3.2–7.3.3 separately defines bags, duplicate values, and equivalent attribute representations. Attribute occurrence and value cardinality need separate fields.

These are catalog reconciliation issues in the inspected publications, not proposed changes to xacml4j semantics. [2.0 source](https://docs.oasis-open.org/xacml/2.0/access_control-xacml-2.0-core-spec-os.pdf), [3.0 source](https://docs.oasis-open.org/xacml/3.0/errata01/os/xacml-3.0-core-spec-errata01-os-complete.html).

| Edition | Datatypes | Functions and combining algorithms |
|---|---|---|
| 2.0 | §10.2.7 explicitly marks 14 types M: string, boolean, integer, double, time, date, dateTime, anyURI, hexBinary, base64Binary, two durations, rfc822Name, x500Name. Duration identifiers use the older XQuery working-draft namespace. Appendix B additionally defines `u2:data-type:ipAddress` and `dnsName`; their omission from that table needs reconciliation with errata. | §10.2.8 marks the listed ordinary functions M and the three XPath node functions O. Preserve the edition’s signatures and algorithm identifiers. |
| 3.0 | The same families use the current duration identifiers; `ipAddress` and `dnsName` are explicitly M. `u3:data-type:xpathExpression` is O. | §10.2.8 has four optional functions: `u3:function:xpath-node-count`, `xpath-node-equal`, `xpath-node-match`, and `access-permitted`. Other listed functions are M. §10.2.9 separately retains required legacy identifiers despite recommending newer alternatives. |

Sources: [2.0 §10 and Appendix A](https://docs.oasis-open.org/xacml/2.0/access_control-xacml-2.0-core-spec-os.pdf); [3.0 §10.2.3, §10.2.7–10.2.9 and Appendix A](https://docs.oasis-open.org/xacml/3.0/errata01/os/xacml-3.0-core-spec-errata01-os-complete.html).

For function metadata, represent the complete normative signature table, including families such as:

| Family | Signature shape |
|---|---|
| Equality | `(T, T) → boolean` |
| Bag construction | `(T...) → Bag<T>`, including zero arguments where specified |
| Bag size | `Bag<T> → integer` |
| One-and-only | `Bag<T> → T`, with a cardinality-dependent error |
| Membership | `(T, Bag<T>) → boolean` |
| Set operations | Typed bag inputs; result type depends on the particular operation |
| Higher-order functions | Function-typed parameters, type variables, variadic positions and argument-dependent return types |
| Context-sensitive functions | Additional execution requirements, such as the optional `access-permitted` function |

These are signature shapes, not permission to generate every conceivable `T × operation` combination. The official function lists determine which combinations exist.

**Profile inventory**

Here, **M/O applies within the selected profile and conformance role**. Supporting XACML 3.0 core does not require implementing every profile. CS means Committee Specification; CSD means Committee Specification Draft.

| Official document, version and status | Default-model content and conformance boundary |
|---|---|
| [Multiple Decision Profile v1.0, CS02, 18 May 2014](https://docs.oasis-open.org/xacml/3.0/multiple/v1.0/cs02/xacml-3.0-multiple-v1.0-cs02.html) | Five independently selectable conformance modes: scope, XPath, repeated categories, references, combined decision. `u2:resource:scope` is a string with `Immediate`, `Children`, `Descendants`; supported subsets are allowed. `u3:profile:multiple:content-selector` is distinct from hierarchical `u3:content-selector`. Requires expansion/aggregation behavior, not merely attribute declarations. |
| [Hierarchical Resource Profile v1.0, CS02, 18 May 2014](https://docs.oasis-open.org/xacml/3.0/hierarchical/v1.0/cs02/xacml-3.0-hierarchical-v1.0-cs02.html) | Selectable XML-node, URI-node and ancestor-attribute modes. Defines `u2:resource:{resource-parent,resource-ancestor,resource-ancestor-or-self,document-id}` and uses `u3:content-selector`. Ancestor attributes are optional capabilities; obligations differ by selected mode. Preserve multiple-parent semantics. |
| [Core and Hierarchical RBAC Profile v1.0, CS02, 23 October 2014](https://docs.oasis-open.org/xacml/3.0/rbac/v1.0/cs02/xacml-3.0-rbac-v1.0-cs02.html) | Domains must agree on role attributes and value semantics. `u2:subject:role` is recommended, with `xs:anyURI` recommended; it is not a universal mandatory role field. `u2:actions:hasPrivilegesOfRole` is an action value. Conformance also constrains Role and Permission PolicySet structure. |
| [XACML SAML Profile v2.0, CS02, 19 August 2014](https://docs.oasis-open.org/xacml/xacml-saml-profile/v2.0/cs02/xacml-saml-profile-v2.0-cs02.html) | Supports several XACML versions over SAML 2.0. §9 defines separately claimed attribute, SOAP, authorization-query, WS-Trust, policy, advice and token capabilities. This is a mapping/protocol overlay, not a universal subject schema. `AdditionalAttributes` support differs between XACML 3.0 and earlier versions. |
| [Privacy Policy Profile v1.0, CS02, 25 January 2015](https://docs.oasis-open.org/xacml/3.0/privacy/v1.0/cs02/xacml-3.0-privacy-v1.0-cs02.html) | Defines `u2:resource:purpose` and `u2:action:purpose`, both strings. Conformance is using the attributes as defined. Purpose vocabularies remain domain-defined. The matching-purpose rule is an example, not a required global policy. |
| [Export Compliance–US v1.0, OASIS Standard, 19 January 2015](https://docs.oasis-open.org/xacml/3.0/ec-us/v1.0/os/xacml-3.0-ec-us-v1.0-os.html) | §5 marks all 12 listed attribute identifiers M. Defines strings, dates, a boolean and restricted value vocabularies; detailed bundle below. These encode the profile’s vocabulary, not a maintained determination of current export law. |
| [Intellectual Property Control v1.0, OASIS Standard, 19 January 2015](https://docs.oasis-open.org/xacml/3.0/ipc/v1.0/os/xacml-3.0-ipc-v1.0-os-en.html) | §5.1 lists 18 M attributes and two O attributes, plus two M obligation identifiers. Includes boolean classifications, URI vocabularies, strings and dateTimes. |
| [DLP/NAC v1.0, CS01, 16 February 2015](https://docs.oasis-open.org/xacml/xacml-3.0-dlp-nac/v1.0/xacml-3.0-dlp-nac-v1.0.html) | §5 requires four network datatypes, six functions, six listed categories, 14 attributes and two listed obligation identifiers. Adds recipient-machine category, network value/pattern types and domain vocabulary. Publication inconsistencies need explicit adjudication. |
| [JSON Profile v1.1, OASIS Standard, 20 June 2019](https://docs.oasis-open.org/xacml/xacml-json-http/v1.1/os/xacml-json-http-v1.1-os.html) | Defines JSON requests/responses, datatype encodings and HTTP behavior. It does not define a JSON policy language or a domain entity schema. Record encoding rules separately from domain constraints. |
| [REST Profile v1.1, OASIS Standard, 20 June 2019](https://docs.oasis-open.org/xacml/xacml-rest/v1.1/os/xacml-rest-v1.1-os.html) | HTTP/PDP interaction requirements; the home entry point is optional. HTTP status codes are transport metadata, not XACML attribute or decision identifiers. |
| [Additional Combining Algorithms v1.0, CS01, 18 August 2014](https://docs.oasis-open.org/xacml/xacml-3.0-combalgs/v1.0/xacml-3.0-combalgs-v1.0.html) | Requires `u3:policy-combining-algorithm:on-permit-apply-second`. This belongs in an algorithm registry, not a function-signature or attribute table. |
| [Related and Nested Entities v1.0, CS03, 30 January 2024](https://docs.oasis-open.org/xacml/xacml-3.0-related-entities/v1.0/cs03/xacml-3.0-related-entities-v1.0-cs03.html) | **The Entities profile exists.** Entity datatype, quantified expressions and functions are M, except `attribute-selector`, which is O. Defines nested attribute containers and navigation; it does not prescribe application entities such as User or Invoice. |
| [Time Extensions v1.0, CS01, 13 February 2020](https://docs.oasis-open.org/xacml/xacml-3.0-time-extensions/v1.0/cs01/xacml-3.0-time-extensions-v1.0-cs01.html) | Adds `u3:data-type:dayOfWeek`, `u3:entity:time-zone`, recurring-time/date arithmetic and conversion functions. §9 separates Evaluation, Composition, Request and Fetch conformance. Time-zone is recommended as one dayTimeDuration value with stated restrictions. |
| [Dynamic Attribute Authority v1.0, CS01, 25 January 2022](https://docs.oasis-open.org/xacml/xacml-3.0-dyn-attr/v1.0/cs01/xacml-3.0-dyn-attr-v1.0-cs01.html) | Extended data flow and inclusion/exclusion obligations are M. Defines obligation assignment metadata, including category, attribute-id, datatype and issuer. These are not mandatory attributes on every ordinary request. |
| [Separation of Duties v1.0, CS01, 30 January 2024](https://docs.oasis-open.org/xacml/xacml-3.0-duties/v1.0/cs01/xacml-3.0-duties-v1.0-cs01.html) | Adds entity-valued action history, constraint/transaction identifiers, optional time limit, identifier generation and history-maintenance obligations. Stateful PEP behavior is required; a catalog alone cannot implement conformance. |
| [XML Digital Signature Profile v1.0, CS02, 18 May 2014](https://docs.oasis-open.org/xacml/3.0/dsig/v1.0/cs02/xacml-3.0-dsig-v1.0-cs02.html) | Separate signing/verification requirements for producers and consumers. Adds representation/security behavior rather than domain attributes. |
| [Administration and Delegation v1.0, CSD04/PRD02, 13 November 2014](https://docs.oasis-open.org/xacml/3.0/administration/v1.0/csprd02/xacml-3.0-administration-v1.0-csprd02.html) | Latest document inspected is a **draft**, not an OASIS Standard. Delegation reduction creates reserved categories and `u3:delegation:decision`. Reserved delegate/delegation-info/delegated categories must not arrive in a PEP request. |
| [MAP Authorization v1.0, OASIS Standard, 19 January 2015](https://docs.oasis-open.org/xacml/xacml-map-authz/v1.0/os/xacml-map-authz-v1.0-os.html) | §4 requires the listed IF-MAP attribute/obligation vocabulary. Some identifiers contain parameterized components such as `RELATIONSHIP`, `IDENTIFIER-TYPE` and `ATTR`; a finite enumeration alone cannot express this profile. |
| [XSPA Profile of XACML 2.0 for Healthcare v1.0, OASIS Standard, 1 November 2009](https://docs.oasis-open.org/xacml/xspa/v1.0/xacml-xspa-1.0-os.html) | Additional healthcare profile, explicitly based on **2.0**. Its conformance table distinguishes mandatory identity, organization, permission, role, purpose, resource and confidentiality vocabulary from optional patient-masking and other fields. Do not label it a 3.0 profile. |

The official catalog also lists the older 2.0 RBAC, hierarchy, multiple-resource, privacy, SAML and signature editions. Those require separate version records, not aliases to their 3.0 successors. It lists an LDAP policy-distribution proposal among inactive work, which should not be presented as an approved default profile. [Official TC publication inventory, inspected 27 October 2026](https://www.oasis-open.org/committees/tc_home.php?wg_abbrev=acal).

**Concrete profile attribute bundles**

These expand the profiles with substantial domain vocabulary. Unless a document specifies otherwise, issuer constraints and cardinality should remain **unspecified**, not silently become “one value, no issuer.”

| Bundle | Exact identifiers/types after prefix expansion |
|---|---|
| EC-US | Prefix `u3:ec-us:`. Resource strings: `resource:{jurisdiction,eccn,usml,authority-to-export,work-effort}`; resource dates: `resource:{effective-date,expiration-date}`. Subject strings: `subject:{nationality,current-nationality,organization,location}`; boolean: `subject:us-person`. All are M in §5. Nationality explicitly permits multiple citizenship values; date fields are **date**, not dateTime. [§2, §5](https://docs.oasis-open.org/xacml/3.0/ec-us/v1.0/os/xacml-3.0-ec-us-v1.0-os.html) |
| IPC | Prefix `u3:ipc:`. M resource booleans: `resource:{copyright,patent,proprietary,public-domain,trademark}`; strings: `{ip-owner,ip-licensee,agreement-id,work-effort}`; anyURI: `{agreement-type,authorized-end-use}`; dateTime: `{effective-date,expiration-date}`. O: `resource:valid-agreement-exists` boolean and `resource:number-of-valid-agreements` integer. M subject strings: `subject:{subject-id,organization,agreement-id}`; anyURI: `{subject-to-organization-relationship,business-context}`. `obligation:{encrypt,marking}` are separate obligation definitions. [§2, §5](https://docs.oasis-open.org/xacml/3.0/ipc/v1.0/os/xacml-3.0-ipc-v1.0-os-en.html) |
| DLP/NAC | Reuses `u1:resource:{resource-id,resource-location}`, `u1:subject:{subject-id,authentication-time,authentication-method,request-time}`, and `u1:action:action-id`. Adds `u3:subject:{subject-security-domain,removable-media,authn-locality:ip-address,authn-locality:dns-name}`, `u3:codebase:authorized-application`, and `u3:dlp-nac:action:{action-protocol,action-method}`. Network locality uses the profile’s `ipAddress-value`/`dnsName-value`, not core network-pattern types. Resource-location permits several datatypes, including GeoXACML geometry. [§2, §5](https://docs.oasis-open.org/xacml/xacml-3.0-dlp-nac/v1.0/xacml-3.0-dlp-nac-v1.0.html) |
| Entities | `u3:data-type:entity`; functions `u3:function:{attribute-designator,attribute-selector,entity-one-and-only,entity-bag-size,entity-bag}`. `attribute-designator` accepts a category URI or entity, attribute/type identifiers, optional MustBePresent and issuer arguments; its return is a typed bag. [§4–6, §9](https://docs.oasis-open.org/xacml/xacml-3.0-related-entities/v1.0/cs03/xacml-3.0-related-entities-v1.0-cs03.html) |
| Separation of Duties | `u3:sod:attribute:history` contains entity values. Each history record requires resource-id, `constraint-id` and `transaction-id`; action-id and subject-id are recommended. `u3:sod:attribute:time-limit` is optional, exactly one dateTime when present. Constraint/transaction types are policy-selected subject to the profile’s equality requirements. [§3–7](https://docs.oasis-open.org/xacml/xacml-3.0-duties/v1.0/cs01/xacml-3.0-duties-v1.0-cs01.html) |
| Dynamic Attribute Authority | Prefix `u3:daa:`. Assignment fields `attribute:{category,attribute-id,data-type,function-id}` use anyURI; `attribute:issuer` uses string; `attribute:value` depends on the operation. Obligations: `include`, `include-values`, `exclude`, `exclude-values`, `exclude-matching-values`, `exclude-all-values`. Operation-specific assignment cardinality and forbidden Category/Issuer fields must be represented. [§3](https://docs.oasis-open.org/xacml/xacml-3.0-dyn-attr/v1.0/cs01/xacml-3.0-dyn-attr-v1.0-cs01.html) |
| MAP Authorization | Prefix `u3:if-map:content:`. Subject: `role`, `task:RELATIONSHIP:IDENTIFIER-TYPE`; resource: `metadata-type`, `identifier-type`, `is-map-client-identifier`, `is-self-identifier`, `on-link`, `metadata-attribute:ATTR`, `identifier-attribute:IDENTIFIER-TYPE:ATTR`; action: `request-type`, `purge-own-metadata`, `publish-request-subtype`; environment: `dry-run`; obligations: `caching`, `maximum-policy-lag`. These require identifier templates and datatype/value rules from §2. [§2, §4](https://docs.oasis-open.org/xacml/xacml-map-authz/v1.0/os/xacml-map-authz-v1.0-os.html) |
| XSPA | Mandatory rows include core subject-id/resource-id, `u2:subject:role`, and `urn:oasis:names:tc:xspa:1.0:` fields for subject organization, organization-id, HL7 permission, purposeofuse; resource HL7 type, organizational permissions/role, patient confidentiality and dissent; environment locality. Optional families include NPI, functional role, allowed organizations, operating hours, patient opt-in and masked clinical-section dissent. Preserve its healthcare vocabulary bindings rather than treating every field as an unconstrained string. [Conformance table and attribute definitions](https://docs.oasis-open.org/xacml/xspa/v1.0/xacml-xspa-1.0-os.html) |

Two additional publication hazards matter for a machine-readable catalog:

- DLP/NAC §2.8.1 declares action-id as boolean but lists URI-shaped action values. It also defines a logging obligation outside the two obligation rows in §5.3. Mark these as unresolved classification/type issues; do not silently “correct” them. [DLP/NAC CS01](https://docs.oasis-open.org/xacml/xacml-3.0-dlp-nac/v1.0/xacml-3.0-dlp-nac-v1.0.html).
- The hierarchical profile contains inconsistent namespace spellings in some definitions and capability references. A reviewed record should retain the source spelling, proposed interpretation and disposition. [Hierarchical CS02, §3 and §5–7](https://docs.oasis-open.org/xacml/3.0/hierarchical/v1.0/cs02/xacml-3.0-hierarchical-v1.0-cs02.html).

**XACML 4.0 is published as a draft**

It would be incorrect to report that nothing has been published.

| Publication | Status inspected |
|---|---|
| [Attribute-Centric Authorization Language — ACAL v1.0](https://docs.oasis-open.org/xacml/acal/acal/core/v1.0/csd01/acal-core-v1.0-csd01.html) | **CSD01, 18 February 2026**. Abstract authorization model and processing semantics. |
| [XACML v4.0 — XML Representation of ACAL v1.0](https://docs.oasis-open.org/xacml/acal/xacml/core/v4.0/csd01/acal-core-xml-v4.0-csd01.html) | **CSD01, 18 February 2026**. XML mapping, XSD, Schematron and identifier artifacts. |
| [JACAL v1.0 — JSON Representation of ACAL](https://docs.oasis-open.org/xacml/acal/jacal/core/v1.0/csd01/acal-core-json-v1.0-csd01.html) | **CSD01, 18 February 2026**. JSON representation including policies; uses JSON Schema 2020-12. |

The inspected [4.0 publication directory](https://docs.oasis-open.org/xacml/acal/xacml/core/v4.0/) contained CSD01. That establishes the published stage inspected, not a guarantee against later working changes.

Material changes include:

- Representation-independent semantics with XML and JSON mappings.
- Policy and PolicySet merged; rule and policy combining algorithms unified.
- Boolean-expression targets; removal of separate Match/AnyOf/AllOf target syntax and rule targets.
- Obligations/advice represented through a shared notice structure.
- Entities and quantified expressions incorporated.
- Short identifiers, reusable bundles and shared definitions.
- New ACAL identifier namespaces; selected legacy aliases are optional, and changed definitions are not equivalent aliases.
- Argument-order changes for several matching/string functions.
- XPath separated into a profile; the release tree also contains a JSONPath profile directory.

These are described in the [ACAL CSD01 change appendix and §11](https://docs.oasis-open.org/xacml/acal/acal/core/v1.0/csd01/acal-core-v1.0-csd01.html), with representation details in [XACML 4.0](https://docs.oasis-open.org/xacml/acal/xacml/core/v4.0/csd01/acal-core-xml-v4.0-csd01.html) and [JACAL](https://docs.oasis-open.org/xacml/acal/jacal/core/v1.0/csd01/acal-core-json-v1.0-csd01.html). The [ACAL profile directory](https://docs.oasis-open.org/xacml/acal/acal/profiles/) confirms XPath/JSONPath publication artifacts; their complete conformance tables were not audited in this lane.

**Repository facts**

| Source | Observed fact and implication |
|---|---|
| [CategoryId.java:41](xacml-core/src/main/java/org/xacml4j/v30/CategoryId.java:41), [custom-ID fallback:136](xacml-core/src/main/java/org/xacml4j/v30/CategoryId.java:136) | CategoryId is an extensible identifier class, **not an enum**. Unknown nonempty identifiers can produce CategoryId instances. A default catalog must preserve extension identifiers. |
| [AttributeDesignatorKey.java:99](xacml-core/src/main/java/org/xacml4j/v30/AttributeDesignatorKey.java:99) | Equality includes category, datatype, attribute ID and issuer. A model keyed only by attribute ID loses information. |
| [WellKnownAttributeIds.java:38](xacml-core/src/main/java/org/xacml4j/v30/WellKnownAttributeIds.java:38) | `MDP_SCOPE` contains `u3:profile:multiple:scope`, a capability identifier. The profile’s attribute is `u2:resource:scope`. Exporting existing constants directly as normative metadata would propagate this mismatch. |
| [WellKnownAttributeIds.java:68](xacml-core/src/main/java/org/xacml4j/v30/WellKnownAttributeIds.java:68) | `AUTHN_METHOD` uses the abbreviated string `authentication-time`. Full IDs and short-name aliases need independent checks. |
| [XacmlTypes.java:42](xacml-core/src/main/java/org/xacml4j/v30/types/XacmlTypes.java:42) | This is an enum implementing ValueType. It mixes core types, legacy aliases, XPath, JSONPath and entity support. Membership alone does not establish an edition’s normative requirements or complete profile support. |
| [ValueType.java:39](xacml-core/src/main/java/org/xacml4j/v30/types/ValueType.java:39) | Exposes full identifier, short identifier and aliases. These can populate an implementation-capability view. |
| [FunctionSpec.java:45](xacml-core/src/main/java/org/xacml4j/v30/policy/FunctionSpec.java:45) | Exposes parameter metadata, variadic/lazy behavior, validation and argument-dependent return-type resolution. A signature model needs more than Java parameter classes. |
| [FunctionProvider.java:73](xacml-core/src/main/java/org/xacml4j/v30/policy/function/FunctionProvider.java:73), [FunctionProviderBuilder.java:117](xacml-core/src/main/java/org/xacml4j/v30/policy/function/FunctionProviderBuilder.java:117) | Providers expose their functions; discovery and annotated classes/instances are supported. Export the configured provider set, including custom functions. |
| [Entity.java:45](xacml-core/src/main/java/org/xacml4j/v30/types/Entity.java:45) | Entity already represents related attributes/content. This is distinct from a declared application entity type with identity and relationships. |
| [AttributeResolverDescriptor.java:122](xacml-pdp/src/main/java/org/xacml4j/v30/spi/pip/AttributeResolverDescriptor.java:122) | Resolver matching uses category, ID, datatype and optional issuer filtering. An omitted requested issuer acts as a wildcard. PIP model validation should preserve that behavior. |

The ValueBox documents are useful design material, but explicitly distinguish proposed APIs from source. The inspected [entity design:5](artagon-uri/docs/wiki/02-type-system/05-entities-and-identity.md:5) and [refinement design:5](artagon-uri/docs/wiki/02-type-system/06-restrictions-and-refinement.md:5) contain a source check dated **23 September 2026**, not a fresh build result. Identity-bearing references, structural records and refinements are reasonable ideas to borrow; the documents do not establish a ready-to-integrate library.

**Reuse and licensing**

| Candidate | Reuse assessment for LGPL v3 |
|---|---|
| OASIS specifications and schemas | Their own OASIS notices apply. The document grant permits copying and implementation-assisting derivatives with retained notices, while restricting alteration of the specification itself. Keep vendored standards artifacts unchanged and separately attributed; do not relabel them LGPL. The inspected [4.0 XSD](https://docs.oasis-open.org/xacml/acal/xacml/core/v4.0/csd01/acal-core-xml-v4.0-schema.xsd) explicitly points to the specification’s Annex A. See [OASIS IPR policy §14](https://www.oasis-open.org/policies-guidelines/ipr/) and [ACAL Annex A](https://docs.oasis-open.org/xacml/acal/acal/core/v1.0/csd01/acal-core-v1.0-csd01.html). |
| Original xacml4j model format and implementation | Can follow the project’s LGPL v3 licensing. Preserve provenance for imported definitions and notices for copied material. A catalog should be identified as an implementation aid, not an altered OASIS specification. |
| ANTLR 4 | BSD-3-Clause terms permit use alongside LGPL v3 with required notices and conditions. This licenses the tool/runtime, **not an independently sourced ALFA or Cedar grammar**. [ANTLR license, inspected 27 October 2026](https://raw.githubusercontent.com/antlr/antlr4/master/LICENSE.txt). |
| JSON Schema artifacts | The repository license file contains BSD-3-Clause terms and an AFL-3.0 text for appropriately designated work. Check the selected artifact’s designation before vendoring. Merely expressing our own model in the 2020-12 dialect does not require copying a validator implementation. [License](https://raw.githubusercontent.com/json-schema-org/json-schema-spec/main/LICENSE), [2020-12 core specification](https://json-schema.org/draft/2020-12/json-schema-core). |
| Artagon ValueBox | The inspected [ValueBox.java:3](artagon-uri/artagon-valuebox/src/main/java/org/artagon/box/ValueBox.java:3) carries LGPL-3.0-or-later terms. That supports compatible reuse of that file with obligations preserved; it is not a blanket license audit of the module, dependencies or wiki. |
| ALFA/Cedar grammars | **Not audited in BR3.** Do not infer permission from language availability, product documentation, or ANTLR’s license. A concrete grammar/library choice needs its own provenance and license record. |

2. **Design options and trade-offs — proposals, not existing behavior**

| Question | Do not do it | Recommended option | Larger alternative |
|---|---|---|---|
| Defaults by version/profile | Keep constants and documentation only. Lowest maintenance; weak discovery and validation. | Immutable, separately versioned catalogs with profile dependencies, conformance roles and unresolved issues. | One merged “complete XACML model.” Easier browsing, but obscures conflicting editions, optional features and domain-specific requirements. |
| XACML 4.0 | Defer all work until a final standard. Avoids draft churn. | Publish an experimental metadata pack pinned to CSD01; no claim that the current PDP executes it. | Rebase the runtime on ACAL now. Broad semantic/API migration involving targets, policies, algorithms, notices and identifiers. |
| Machine-readable domain model | Keep application schemas outside xacml4j. No new dependency, but repeated application work. | A versioned JSON model, JSON Schema for document shape, and Java semantic validation. Separate standards, domain constraints and runtime capabilities. | Make Cedar schema, an ALFA dialect or ValueBox types canonical. Better integration with one ecosystem, but risks losing XACML-specific semantics or adding substantial coupling. |
| Existing registries | Leave them unrelated to model metadata. Simple, but drift remains invisible. | Export runtime capabilities from registries and compare them with reviewed catalogs. | Generate all public identifier/type APIs from catalogs. Reduces duplication but risks source/binary compatibility and custom-provider behavior. |

A concrete future layout could be:

```text
xacml-model/
  org.xacml4j.model.catalog
  org.xacml4j.model.schema
  org.xacml4j.model.validation
  org.xacml4j.model.adapter.xacml

models/
  xacml-2.0-os/catalog.json
  xacml-3.0-errata01/catalog.json
  profiles/<profile>/<edition>/catalog.json
  experimental/acal-1.0-csd01/catalog.json
  experimental/xacml-4.0-csd01/representation.json
  experimental/jacal-1.0-csd01/representation.json
```

Suggested types:

- `StandardCatalog`, `ProfileCatalog`, `ConformanceRequirement`, `SourceReference`.
- `AuthorizationModel`, `EntityTypeDefinition`, `AttributeDefinition`, `RelationshipDefinition`.
- `TypeExpression`: primitive URI, bag, record, entity reference, union, refinement and type variable.
- `FunctionSignature`, `CombiningAlgorithmDefinition`, `ObligationDefinition`.
- `EngineCapabilities`, `ResolverContract`, `ModelDiagnostic`.

Keep three independent layers:

| Layer | Meaning |
|---|---|
| Standards catalog | What a particular published edition/profile defines and requires. |
| Application authorization model | Entities, attributes, allowed values, relationships and application constraints. |
| Engine capabilities | What this configured xacml4j instance can parse, evaluate, resolve and enforce. |

An illustrative attribute entry:

```json
{
  "id": "urn:oasis:names:tc:xacml:1.0:environment:current-dateTime",
  "category": "urn:oasis:names:tc:xacml:3.0:attribute-category:environment",
  "valueType": "http://www.w3.org/2001/XMLSchema#dateTime",
  "implementationSupport": "mandatory",
  "incomingRequestPresence": "optional",
  "resolutionRule": "context-handler-supplies-if-absent",
  "issuerConstraint": "unspecified",
  "valueCardinality": {
    "sourceRule": "preserve-normative-rule-without-inferred-bound"
  },
  "source": {
    "edition": "xacml-3.0-errata01",
    "sections": ["7.3.6", "10.2.5", "B.7"]
  }
}
```

The schema should also carry `sourceUrl`, publication status/date, artifact digest, requirement actor, conditional requirements, deprecated identifiers, explicit equivalence mappings, and unresolved conflicts. Overlay conflicts should produce diagnostics rather than “last file wins.”

**Validation proposal**

| Input | Validation |
|---|---|
| Policy | Resolve identifiers against selected catalogs and configured providers; check signatures, type variables, variadic arguments, return types, selected profile constructs and domain attribute declarations. Retain existing `FunctionSpec` validation as an execution-relevant authority. |
| Incoming request | Validate representation and supplied values. Do not demand context-handler/PIP-supplied attributes prematurely. Allow extension attributes unless the application explicitly selects a closed model. |
| Resolved request context | Check domain constraints and resolver promises at the appropriate resolution boundary. Preserve XACML missing-attribute, bag and `MustBePresent` behavior. |
| PIP configuration | Compare advertised category/ID/type/issuer tuples and resolver dependencies with the domain model. Report missing or conflicting providers. Static configuration cannot guarantee that an external PIP will return a value. |
| ALFA/Cedar front end | Map into the domain model through an explicit adapter; diagnose unsupported or lossy constructs. Do not treat similar-looking types or policies as semantically interchangeable. |

XACML 3.0 §7.3.4–7.3.6 governs matching, retrieval and environment defaults. A global preflight “all referenced attributes must exist” check would alter these rules. [Normative source](https://docs.oasis-open.org/xacml/3.0/errata01/os/xacml-3.0-core-spec-errata01-os-complete.html).

For generation/consumption:

- Export `CategoryId`, `WellKnownAttributeIds` and `XacmlTypes` as **implementation observations**.
- Compare full identifiers with reviewed standard entries; retain custom and legacy identifiers.
- Enumerate the configured `FunctionProvider.getProvidedFunctions()`.
- Supply declarative descriptors for signatures that cannot be fully expressed by existing metadata.
- Let models bind to existing `ValueType` instances and providers. Do not require replacement of `Value`, `Entity` or `BagOfValues`.
- Use JSON initially. ANTLR 4 becomes relevant only when a separately authorized language frontend needs a grammar.

3. **Recommendation and why**

Adopt the catalog/domain/capability separation as the future enhancement’s starting point.

First, define reviewed 2.0 and 3.0 catalogs and a diagnostic-only comparison with existing identifiers and registries. Add profile catalogs independently, including their optional conformance modes and PEP/PIP requirements. Keep unresolved publication discrepancies visible.

Then add application models for entities, typed attributes and resolver contracts. Draw on ValueBox’s record, identity and refinement ideas without making its proposed runtime a prerequisite.

Keep ACAL/XACML 4.0/JACAL metadata experimental and version-pinned. The published changes are substantial enough that transparent aliasing or a generic “version=4” switch would be misleading.

This approach supports later ALFA and Cedar adapters while preserving the current XACML execution model and extension points.

4. **Risks and BREAKING changes**

| Risk | Boundary |
|---|---|
| Turning metadata into mandatory rejection | **BREAKING** if enabled by default: formerly valid requests, custom identifiers or policies could be rejected before normal evaluation. Start with diagnostics and opt-in domain validation. |
| Treating `MustBePresent` as global schema requiredness | **BREAKING semantic change**. Missing values affect the evaluated expression and enclosing policy logic; they are not universally invalid incoming requests. |
| Flattening issuer/category/datatype identity | Can select the wrong attribute or resolver. Preserve the existing tuple and absent-issuer matching behavior. |
| Replacing bags with sets/lists | **BREAKING** where duplicate values, cardinality or unordered semantics matter. |
| Applying one profile’s datatype globally | DLP/NAC, core locality attributes and domain-specific IDs demonstrate why an ID alone is insufficient. |
| Replacing public identifier classes or the `XacmlTypes` enum | Potential **BREAKING source, binary and serialization changes**. An adapter layer avoids making that necessary. |
| Assuming catalog inclusion proves profile support | Entities, dynamic authorities, delegation and SoD require behavior beyond datatype registration. Capability claims need separate evidence. |
| Automatic 3.0→4.0 translation | **BREAKING** without explicit migration: constructs disappear, algorithms change and some function arguments reverse. |
| Treating refinement metadata as arbitrary executable code | Introduces validation and execution complexity. Prefer bounded declarative constraints; separately govern custom predicates. |
| Sharing mutable models during evaluation | Could make interpretation change mid-decision. Proposed models should be immutable snapshots, with explicit version selection. |

Future verification should cover unchanged decisions with validation disabled; custom functions/types/categories; issuer matching; absent versus empty attributes; duplicate bag values; PIP-supplied values; profile-specific cardinality; and rejection of lossy edition conversions. These are proposed checks, **not executed evidence**.

5. **Open questions for the maintainer**

1. Does “default authorization model” mean standards vocabulary, an application entity schema, or both? The recommendation keeps both available but separate.
2. Should the first release offer discovery/linting only, or opt-in request and PIP contract enforcement?
3. Should experimental ACAL/4.0 catalogs ship before execution support, clearly marked unsupported by the current PDP?
4. Which profile conformance roles matter first: PDP, PAP, PEP, PIP, or transport? This determines whether a catalog entry is descriptive or actionable.
5. How should standards discrepancies be adjudicated: published errata only, documented maintainer interpretations, or selectable compatibility interpretations?
6. Should ALFA and Cedar be authoring frontends, import/export formats, separate evaluation semantics, or some combination? That decision must precede claims of semantic interoperability.
7. Should ValueBox inform the model vocabulary only, or eventually become a shared library? The latter needs a separate build, API and license audit.

Residual limits: the 2.0 errata text was not successfully decoded; the ACAL XPath/JSONPath profile tables and ALFA/Cedar grammar licenses were not fully audited. This report establishes catalog structure, publication inventory and concrete semantic boundaries; it is not a completed machine-readable catalog or a conformance certification.
