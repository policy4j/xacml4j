<!-- Lane BR4 of the authorization-model brainstorm swarm, Agy Gemini 4 Argon, read-only; scope: the type system (ValueBox module and design, Cedar, CEL) and functions on types; report taken from the Agy conversation database, paths made repository-relative. -->

# BR4 Brainstorm Research Report: Expressive Type System, ValueBox, and Typed Functions for Multi-Frontend `xacml4j` (XACML 2.0/3.0/4.0, ALFA, Cedar)

## Research Tool Inventory & Coverage Labels

| Tool / Lane | Status | Scope & Notes |
| --- | --- | --- |
| Local native file reader (`read_file`) | `SUPPORTED` | Read `xacml4j` type/function source (`xacml-core`), `artagon-valuebox` Java module (`artagon-uri/artagon-valuebox/src/main/java/org/artagon/box/**`, 124 files), `artagon-uri/openspec/changes/restore-valuebox-module-build/*`, and `artagon-uri/docs/wiki/02-type-system/*` (chapters 00–08). |
| `context7` MCP (`resolve-library-id`, `query-docs`) | `SUPPORTED` | Resolved `/cedar-policy/cedar-docs` (97 snippets) and `/google/cel-spec` (267 snippets); queried Cedar schema, validation, entities, and extension types. |
| `duckduckgo` MCP (`search`, `fetch_content`) | `SUPPORTED` | Fetched primary specification texts directly: Google CEL `langdef.md`, OASIS XACML v3.0 Related and Nested Entities Profile v1.0 CS03, OASIS ALFA for XACML v1.0 WD01, and OpenJDK JEP 401. |
| `tavily` MCP (`tavily_search`, `tavily_extract`) | `PARTIAL` | First `tavily_search` call succeeded and returned official `docs.cedarpolicy.com` and `cedar-policy/rfcs` citations; second concurrent `tavily_search` call returned `HTTP 429 Too Many Requests`. Per research rules, recorded the 429 once without retrying and switched to `duckduckgo` `fetch_content` and `context7`. |
| `arxiv` / `deep-research-mcp` | `SUPPORTED` (not needed) | Primary language specifications and local `artagon-valuebox` source were directly available via `read_file`, `context7`, and `duckduckgo`. |
| `gemini-search` / `google-devknowledge` | `UNAVAILABLE` | Disabled by user rule since 2026-10-11; not called. |
| `github` MCP | `UNAVAILABLE` (by rule) | Disallowed by packet instructions ("do not use any GitHub connector"); not called. |

---

## 1. Facts with Sources

### 1.1 Repository Facts: `xacml4j` Current Type System & Function SPI

1. **Dual `ValueExpTypeInfo` hierarchy (`ValueType` vs. `BagOfValuesType`)**:
   - [`ValueExpTypeInfo`](xacml-core/src/main/java/org/xacml4j/v30/ValueExpTypeInfo.java#L31-L52) is the root type descriptor (`getDataTypeId()`, `isBag()`, `getJavaType()`).
   - [`ValueType<T extends Value>`](xacml-core/src/main/java/org/xacml4j/v30/types/ValueType.java#L29-L64) represents scalar/singular types and creates [`BagOfValuesType<T>`](xacml-core/src/main/java/org/xacml4j/v30/BagOfValuesType.java#L32-L106) via `bagType()` and [`BagOfValues<T>`](xacml-core/src/main/java/org/xacml4j/v30/BagOfValues.java#L41-L265) via `bagOf(...)` / `emptyBag()`. There is no `Set`, `List`, `Map`, `Option`, `Pair`, or `Range` type descriptor.
2. **Closed enum of 18 built-in types + dynamic string-keyed types**:
   - [`XacmlTypes`](xacml-core/src/main/java/org/xacml4j/v30/types/XacmlTypes.java#L30-L315) is a closed Java `enum` implementing `ValueType` for 17 OASIS XACML 2.0/3.0 types (`ANYURI`, `BASE64BINARY`, `BOOLEAN`, `DATE`, `DATETIME`, `DAYTIMEDURATION`, `DNSNAME`, `DOUBLE`, `HEXBINARY`, `INTEGER`, `IPADDRESS`, `RFC822NAME`, `STRING`, `TIME`, `X500NAME`, `XPATH`, `YEARMONTHDURATION`) plus the custom `ENTITY` type (`urn:org:xacml4j:v30:types:entity`, [`XacmlTypes.java:63`](xacml-core/src/main/java/org/xacml4j/v30/types/XacmlTypes.java#L63)).
   - Custom runtime types use [`DynamicType`](xacml-core/src/main/java/org/xacml4j/v30/types/DynamicType.java#L28-L120), interned in a static `ConcurrentMap<String, ValueType<?>>` (`TYPES`).
3. **Boxed scalar wrappers & identity**:
   - [`Value<T>`](xacml-core/src/main/java/org/xacml4j/v30/types/Value.java#L30-L105) is an abstract class holding `private final ValueType<?> type` (`Value.java:36`).
   - [`IntegerVal`](xacml-core/src/main/java/org/xacml4j/v30/types/IntegerVal.java#L31-L157) wraps a boxed `java.lang.Long` (`private final Long value`, line 36), [`DoubleVal`](xacml-core/src/main/java/org/xacml4j/v30/types/DoubleVal.java#L32-L120) wraps a boxed `java.lang.Double`, and [`BooleanVal`](xacml-core/src/main/java/org/xacml4j/v30/types/BooleanVal.java#L31-L97) wraps `java.lang.Boolean` (`TRUE`/`FALSE` singletons). There are no sized integers (`int8`/`16`/`32`, `uint8`/`16`/`32`/`64`) or `BigDecimal`.
4. **Untyped schema-less `Entity`**:
   - [`Entity`](xacml-core/src/main/java/org/xacml4j/v30/types/Entity.java#L41-L261) wraps an optional DOM `Node` (`content`) and a `Map<String, AttributeExp>` keyed only by `attributeId` (`Entity.java:46-47`). It has no `EntityId`, no `EntityType` / schema, no entity hierarchy (`in`), and its URI (`urn:org:xacml4j:v30:types:entity`) predates the official OASIS XACML v3.0 Related and Nested Entities Profile v1.0 URI (`urn:oasis:names:tc:xacml:3.0:data-type:entity`).
5. **Functions, typing, and reflective invocation**:
   - [`FunctionSpec`](xacml-core/src/main/java/org/xacml4j/v30/policy/FunctionSpec.java#L38-L162) binds one globally unique URI (`getId()`, e.g., `urn:oasis:names:tc:xacml:1.0:function:integer-add`) to a fixed list of [` FunctionParamSpec `](xacml-core/src/main/java/org/xacml4j/v30/policy/FunctionParamSpec.java)s (analyzed by [`FunctionSpecBuilder`](xacml-core/src/main/java/org/xacml4j/v30/policy/function/FunctionSpecBuilder.java#L45-L210) as a deterministic NFA for optional/vararg tails) and one static `ValueExpTypeInfo returnType`.
   - **No polymorphism or overloading**: Because each `FunctionSpec` has a single URI and a single fixed `ValueExpTypeInfo` per parameter/return slot, generic functions (`*-one-and-only`, `*-bag-size`, `*-is-in`, `*-bag`, `*- intersection`, `*-at-least-one-member-of`, `*-union`, `*-subset`, `*-set-equals`) must be duplicated per type in [`BagFunctions`](xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/BagFunctions.java#L45-L210) and [`SetFunctions`](xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/SetFunctions.java#L45-L210).
   - **Untyped higher-order functions**: [`HigherOrderFunctions`](xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/HigherOrderFunctions.java#L47-L230) (`any-of`, `all-of`, `any-of-any`, `map`) take the target function as `@FuncParamFuncReference FunctionSpec` and the arguments as `@FuncParamAnyBag ValueExpr...` or `BagOfValues<?>`, deferring compatibility checks to runtime evaluation.
   - **Reflective `Method.invoke` dispatch**: [`JavaMethodToFunctionSpecConverter`](xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/JavaMethodToFunctionSpecConverter.java#L50-L185) and [`DefaultFunctionInvocation`](xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/DefaultFunctionInvocation.java#L38-L82) invoke `java.lang.reflect.Method.invoke(null, params)` over an allocated `Object[]` on every call (`DefaultFunctionInvocation.java:53`), rather than `MethodHandle.invokeExact`.

---

### 1.2 Repository Facts: `artagon-valuebox` Java Module (Primary Input) & Wiki Design

1. **Module structure, provenance, and build state**:
   - `artagon-valuebox` (`artagon-uri/artagon-valuebox/src/main/java`, 124 Java files) carries `policy4j-core` / `Copyright (C) 2009 - 2025 Xacml4J.org` **LGPL v3** headers ([`ValueBox.java:5-22`](artagon-uri/artagon-valuebox/src/main/java/org/artagon/box/ValueBox.java#L5-L22)) and some original `Xacml4J Core Engine Implementation` headers ([`BinaryValue.java:6-23`](artagon-uri/artagon-valuebox/src/main/java/org/artagon/box/BinaryValue.java#L6-L23)).
   - As documented in [`restore-valuebox-module-build/proposal.md:1-38`](artagon-uri/openspec/changes/restore-valuebox-module-build/proposal.md#L1-L38) and [`restore-valuebox-module-build/design.md:1-60`](artagon-uri/openspec/changes/restore-valuebox-module-build/design.md#L1-L60), the module is split into:
     - **Box model (85 files, ~7,600 LOC)**: `org.artagon.box` (54 files), `org.artagon.box.schema` (21 files), `org.artagon.box.spi` (4 files), `org.artagon.box.iso8601` (1 file), `org.artagon.box.truffle` (5 files). Currently has 143 `javac` errors due to drift against `artagon-core` (`Opt`, `Result`, `Try`, `Verify`, `PairRefNull`) and intra-package typos (e.g., `entityBox.s` in [`EntitySchema.java:67`](artagon-uri/artagon-valuebox/src/main/java/org/artagon/box/schema/EntitySchema.java#L67)).
     - **Dead duplicate collection tree (39 files, ~8,400 LOC)**: `org.artagon.collection.*`, superseded by `artagon-core` and scheduled for deletion in `restore-valuebox-module-build` Decision D2 ([`design.md:45-60`](artagon-uri/openspec/changes/restore-valuebox-module-build/design.md#L45-L60)).
   - It uses Valhalla JEP 401 `value record` syntax on leaf carriers ([`StdIntTypes.java:53`](artagon-uri/artagon-valuebox/src/main/java/org/artagon/box/StdIntTypes.java#L53), [`BinaryValue.java:45`](artagon-uri/artagon-valuebox/src/main/java/org/artagon/box/BinaryValue.java#L45), [`OptionBoxNone.java:29`](artagon-uri/artagon-valuebox/src/main/java/org/artagon/box/OptionBoxNone.java#L29)), requiring a Valhalla EA JDK (`artagon.jdk=27` with `--enable-preview`, [`design.md:24-43`](artagon-uri/openspec/changes/restore-valuebox-module-build/design.md#L24-L43)).
2. **Three-tier parallel sealed hierarchies (`ValueExpr`, `Type`, `Kind`)**:
   - **Tier 1 (Values & Expressions)**:
     - [`Expr`](artagon-uri/artagon-valuebox/src/main/java/org/artagon/box/Expr.java#L28-L34) (`sealed interface Expr permits ValueExpr`, `ValueExpr eval(ExprContext)`).
     - [`ValueExpr`](artagon-uri/artagon-valuebox/src/main/java/org/artagon/box/ValueExpr.java#L31-L58) (`sealed interface ValueExpr extends Expr permits Sequence, ValueBox`, self-evaluating `default ValueExpr eval(ExprContext) { return this; }`).
     - [`ValueBox<V extends ValueBox<V>>`](artagon-uri/artagon-valuebox/src/main/java/org/artagon/box/ValueBox.java#L40-L85) (`sealed interface ValueBox<V> extends ValueExpr, OptExt.SomeExt<ValueBox<V>> permits ComparableBox, CompositeBox, EnumBox, IPNetworkBox, PrimitiveBox, URIBox`). Because `ValueBox` implements `OptExt.SomeExt`, every present `ValueBox` **is** its own `Some` option wrapper without allocating an `Optional` box ([`01-architecture-and-hierarchy.md:62-84`](artagon-uri/docs/wiki/02-type-system/01-architecture-and-hierarchy.md#L62-L84)).
   - **Tier 2 (Type Descriptors)**:
     - [`ValueExpr.Type`](artagon-uri/artagon-valuebox/src/main/java/org/artagon/box/ValueExpr.java#L60-L92) (`sealed interface Type permits Sequence.Type, ValueBox.Type`).
     - [`ValueBox.Type<V>`](artagon-uri/artagon-valuebox/src/main/java/org/artagon/box/ValueBox.java#L95-L260) provides `kind()`, `isOfType(Object)`, `cast(ValueBox<?>)`, `tryCast(ValueBox<?>)`, `bagType()`, `setType()`, `emptySet()`, and `emptyBag()`. (Note: [`ValueBox.java:186`](artagon-uri/artagon-valuebox/src/main/java/org/artagon/box/ValueBox.java#L186) has an inverted guard `if (isOfType(value)) throw ...` documented in [`00-valuebox-module-as-built.md:221`](artagon-uri/docs/wiki/02-type-system/00-valuebox-module-as-built.md#L221)).
     - [`TypeId`](artagon-uri/artagon-valuebox/src/main/java/org/artagon/box/TypeId.java#L37-L118) (`sealed public interface TypeId permits JDKRecordTypeId, ExtensionTypeId`) computes a 64-bit `typeIdAsLong()` fingerprint using Guava `Hashing.murmur3_128()` (`TypeId.java:46-52`).
   - **Tier 3 (Exhaustive `Kind` tags)**:
     - [`ValueExpr.Type.Kind`](artagon-uri/artagon-valuebox/src/main/java/org/artagon/box/ValueExpr.java#L83-L91) (`sealed interface Kind permits Sequence.Type.Kind, ValueBox.Type.Kind`) and [`ValueBox.Type.Kind`](artagon-uri/artagon-valuebox/src/main/java/org/artagon/box/ValueBox.java#L268-L289) allow exhaustive `switch` over all type categories (`StdIntTypes`, `ISO8601Box.Kind`, `StringBox.Type.Kind`, `BinaryValue.BinaryTypeId`, `CompositeBox.Type.Kind`, `EnumBox.Type.Kind`, `IPNetworkBox.Type.Kind`, `URIBox.Type.Kind`, `Sequence.Type.Kind`).
3. **Box families in `org.artagon.box`**:
   - **Primitives & Sized Integers**: [`PrimitiveBox`](artagon-uri/artagon-valuebox/src/main/java/org/artagon/box/PrimitiveBox.java#L30-L72) permits `ISO8601Box`, `NumericBox`, `StringBox`. [`IntegerBox`](artagon-uri/artagon-valuebox/src/main/java/org/artagon/box/IntegerBox.java#L29-L77) and [`StdIntTypes`](artagon-uri/artagon-valuebox/src/main/java/org/artagon/box/StdIntTypes.java#L27-L59) define 8 sized integer types (`INT8`, `INT16`, `INT32`, `INT64`, `UINT8`, `UINT16`, `UINT32`, `UINT64`) with primitive accessors (`int value()`, `long value()` in [`SInt32Box`](artagon-uri/artagon-valuebox/src/main/java/org/artagon/box/SInt32Box.java#L25-L37), [`UInt64Box`](artagon-uri/artagon-valuebox/src/main/java/org/artagon/box/UInt64Box.java#L25-L37)). Floating-point, `DecimalBox`, and `BooleanBox` are not yet in `src/main/java` and are specified in [`01-architecture-and-hierarchy.md:102-128`](artagon-uri/docs/wiki/02-type-system/01-architecture-and-hierarchy.md#L102-L128).
   - **Strings & Binary**: [`StringBox`](artagon-uri/artagon-valuebox/src/main/java/org/artagon/box/StringBox.java#L53-L368) models charset-aware strings (`UTF_8`, `UTF_16`, `US_ASCII`, `ISO_8859_1`, etc.) with `ByteBuffer`/`CharBuffer` encoding and `StringBoxExt` for regex/glob/format subtypes. [`BinaryValue`](artagon-uri/artagon-valuebox/src/main/java/org/artagon/box/BinaryValue.java#L45-L182) is a `value record` with `Base64Binary` and `HexBinary` kinds.
   - **Temporal (`ISO8601Box`)**: [`ISO8601Box`](artagon-uri/artagon-valuebox/src/main/java/org/artagon/box/ISO8601Box.java#L36-L144) defines 11 `java.time`-aligned kinds (`LocalDate`, `LocalTime`, `OffsetDateTime`, `Duration`, `Period`, `Instant`, `Year`, `YearMonth`, `MonthDay`, `Month`, `DayOfWeek`) via `ISO8601DateTime` and `ISO8601Duration`.
   - **Domain Types (`EnumBox`, `IPNetworkBox`, `URIBox`)**: [`EnumBox`](artagon-uri/artagon-valuebox/src/main/java/org/artagon/box/EnumBox.java#L32-L111) wraps either a JDK `Enum<T>` (`EnumBoxTypeOfJdkEnum`) or arbitrary enumerated values (`EnumBoxTypeOfAny`). [`IPNetworkBox`](artagon-uri/artagon-valuebox/src/main/java/org/artagon/box/IPNetworkBox.java#L32-L79) defines 13 network kinds (`HostName`, `IPv4Addr`, `IPv6Addr`, `IPv4Subnet`, `IPv6Subnet`, `IPv4AddrTrie`, `IPv6AddrTrie`, `IPV4Endpoint`, `IPV6Endpoint`, `Port`, `EUI`, `EUI48`, `EUI64`). [`URIBox`](artagon-uri/artagon-valuebox/src/main/java/org/artagon/box/URIBox.java#L35-L141) distinguishes `URI`, `URL`, and `URN` (plus `DIDTypeId` and `GeoTypeKind`).
   - **Algebraic & Composite Types (`CompositeBox`, `StructBox`, `EntityBox`)**:
     - [`CompositeBox`](artagon-uri/artagon-valuebox/src/main/java/org/artagon/box/CompositeBox.java#L62-L327) (`permits EntityBox, Product2Box, StructBox, TypesSumBox`) provides indexed component access (`arity()`, `valueAtOrNull(int)`, `type().typeAt(int)`).
     - [`Product2Box`](artagon-uri/artagon-valuebox/src/main/java/org/artagon/box/Product2Box.java#L38-L116) permits [`PairBox<L, R>`](artagon-uri/artagon-valuebox/src/main/java/org/artagon/box/PairBox.java#L53-L132) and [`RangeBox<T>`](artagon-uri/artagon-valuebox/src/main/java/org/artagon/box/RangeBox.java#L29-L70).
     - [`TypesSumBox`](artagon-uri/artagon-valuebox/src/main/java/org/artagon/box/TypesSumBox.java#L33-L45) permits [`OptionBox<V>`](artagon-uri/artagon-valuebox/src/main/java/org/artagon/box/OptionBox.java#L35-L89) and `value record` [`OptionBoxNone<V>`](artagon-uri/artagon-valuebox/src/main/java/org/artagon/box/OptionBoxNone.java#L29-L42).
     - [`StructBox`](artagon-uri/artagon-valuebox/src/main/java/org/artagon/box/StructBox.java#L41-L156) represents schema-governed records with lookup by name (`byName(String)`) or validated [`PropertyRef`](artagon-uri/artagon-valuebox/src/main/java/org/artagon/box/PropertyRef.java#L38-L90) (`byRef(PropertyRef)`). Notably, `StructBox` does **not** extend `EntityBox` (`StructBox` has no identity), whereas [`StructBox.Type`](artagon-uri/artagon-valuebox/src/main/java/org/artagon/box/StructBox.java#L117-L155) `permits EntityBox.Type` because an entity schema extends a struct schema with an [`EntityIdSchema`](artagon-uri/artagon-valuebox/src/main/java/org/artagon/box/schema/EntityIdSchema.java#L34-L70).
     - [`EntityBox`](artagon-uri/artagon-valuebox/src/main/java/org/artagon/box/EntityBox.java#L33-L115) has [`EntityId`](artagon-uri/artagon-valuebox/src/main/java/org/artagon/box/EntityId.java#L25-L28), [`EntityRef`](artagon-uri/artagon-valuebox/src/main/java/org/artagon/box/EntityRef.java#L28-L45) (`evaluate(ExprContext)` via [`ExprContext.resolve(EntityRef)`](artagon-uri/artagon-valuebox/src/main/java/org/artagon/box/ExprContext.java#L33-L48)), and [`EntitySchema`](artagon-uri/artagon-valuebox/src/main/java/org/artagon/box/schema/EntitySchema.java#L32-L70).
   - **Sequences (`Sequence`, `BagBox`, `SetBox`, `MapBox`)**:
     - [`Sequence<V, SeqT>`](artagon-uri/artagon-valuebox/src/main/java/org/artagon/box/Sequence.java#L32-L191) (`permits MapBox, BagBox, SetBox`) defines `Sequence.Type.Kind { Bag, Map, BiMap, Multimap, Set, List, Stream }`. [`MapBox<K, V>`](artagon-uri/artagon-valuebox/src/main/java/org/artagon/box/MapBox.java#L34-L111) is modeled as a `Sequence<PairBox<K, V>, MapBox<K, V>>`.
   - **Schemas, Refinements, and SPI**:
     - [`StructSchema`](artagon-uri/artagon-valuebox/src/main/java/org/artagon/box/schema/StructSchema.java#L38-L207) (`permits SchemaFFIStructLayout, SchemaOfRecordClass, SchemaOfTruffleShape, StructSchema.Ext`) supports multiple physical layouts (JDK `Record`, Panama `StructLayout`, GraalVM Truffle `Shape`, or dynamic map/array).
     - [`PropertyDef<V>`](artagon-uri/artagon-valuebox/src/main/java/org/artagon/box/schema/PropertyDef.java#L36-L154) and [`PropertyDefs`](artagon-uri/artagon-valuebox/src/main/java/org/artagon/box/schema/PropertyDefs.java#L47-L491) attach composable `Predicate<V>` refinement constraints (`required`, `unique`, `identity`, range/regex/cardinality predicates) directly to schema properties.
     - [`org.artagon.box.spi`](artagon-uri/artagon-valuebox/src/main/java/org/artagon/box/spi/SequenceBoxProvider.java#L34-L67) defines pluggable factories (`ValueBoxTypeProvider`, `SequenceBoxProvider`, `EntityRecordTypeProvider`, `NetworkTypeProvider`, and `ModelSchemaSPI`).
   - **Wiki Intended Design (Chapters 02–07)**:
     - [`03-operators-and-methodhandles.md`](artagon-uri/docs/wiki/02-type-system/03-operators-and-methodhandles.md#L1-L180) specifies `UnaryOp` / `BinaryOp` tables resolved by `(OpCode, Kind, Kind)` into `MethodHandle` call sites (`ConstantCallSite` / `MutableCallSite` PIC) with zero-boxing primitive specializations and three-valued Kleene logic (`TRUE`, `FALSE`, `INDETERMINATE`).
     - [`05-entities-and-identity.md`](artagon-uri/docs/wiki/02-type-system/05-entities-and-identity.md#L1-L160) adds `RelationshipDef` (directed edges with multiplicity and hierarchy `isHierarchy`, e.g., `memberOf` / `parentFolder`) and `EntityGraph` (`ancestors(EntityRef, String)`, `isRelatedTransitive(EntityRef, EntityRef, String)`), directly supporting Cedar's `in` hierarchy operator.

---

### 1.3 External Facts: AWS Cedar, Google CEL, OASIS Profiles, and OpenJDK JEP 401

1. **AWS Cedar Type System & Validator**:
   - **Sources**:
     - Cedar Policy Language Reference — *Syntax: Data types* (`https://docs.cedarpolicy.com/policies/syntax-datatypes.html`, current product docs for Cedar v4.x, doc history updated through 2025-10-15 at `https://docs.cedarpolicy.com/other/doc-history.html`).
     - Cedar Schema Reference — *Human-readable schema format* (`https://docs.cedarpolicy.com/schema/human-readable-schema.html`, Cedar v4.x product docs) and Cedar RFC 0024 (`https://github.com/cedar-policy/rfcs/blob/main/text/0024-schema-syntax.md`, status: Implemented).
     - Cedar Validation Reference (`https://docs.cedarpolicy.com/policies/validation.html`, queried via `context7` `/cedar-policy/cedar-docs`).
     - **License**: `cedar` (Rust) and `cedar-java` (JNI/FFI wrapper) and `cedar-spec` are licensed under **Apache-2.0** (permissive; compatible with LGPL v3).
   - **Facts**:
     - **Core Types**: `Bool`, `Long` (signed 64-bit integer only), `String`, `Set<T>` (homogeneous during validation), `Record` (`{ attr: Type, optionalAttr?: Type }`, structural closed/open record without identity), `Entity` (`Namespace::Type::"id"`, nominal type with identity, typed attributes, `in [ParentEntityTypes]` DAG hierarchy, and optional `tags: TagType` in Cedar v4).
     - **Extension Types**: `ipaddr` (IPv4/IPv6 address or CIDR prefix, `isIpv4()`, `isIpv6()`, `isLoopback()`, `isMulticast()`, `isInRange(ipaddr)`), `decimal` (fixed-point 4 decimal places, 64-bit signed, `lessThan`, `lessThanOrEqual`, `greaterThan`, `greaterThanOrEqual`), `datetime` (millisecond epoch offset, `offset`, `durationSince`, `toDate`, `toTime`), and `duration` (`toMilliseconds`, `toSeconds`, `toMinutes`, `toHours`, `toDays`), constructed via function syntax (`ip("...")`, `decimal("...")`) and invoked via method-call syntax.
     - **Schema & Type Checking**: A Cedar schema declares `namespace`, `type` aliases, `entity` declarations (`in [...]`, attributes, `tags`), and `action` declarations (`appliesTo { principal: [...], resource: [...], context: {...} }`). The validator performs **per-action-specialization flow-sensitive bidirectional type checking** (`Bool` subdivided into `True`, `False`, `Bool`) to catch unrecognized entity types/actions, missing/mistyped record/entity attributes, guarded optional access (`has` refines the record/entity type in the `&&` / `then` branch), `is` entity type narrowing, and impossible `in` hierarchy checks (`Entity::"id" in OtherEntity::"id"` when `Entity` cannot be a descendant of `OtherEntity`).

2. **Google CEL (Common Expression Language)**:
   - **Source**: Google `cel-spec` — *Language Definition* (`https://raw.githubusercontent.com/google/cel-spec/master/doc/langdef.md`, official specification, `master` branch fetched 2026-10-27). **License**: `google/cel-spec` and `dev.cel:cel` (`cel-java`) are licensed under **Apache-2.0** (compatible with LGPL v3).
   - **Facts**:
     - **Core Types**: `int` (64-bit signed), `uint` (64-bit unsigned), `double` (IEEE 754 64-bit float), `bool`, `string`, `bytes`, `list(A)`, `map(K, V)` (where `K` is `int`, `uint`, `bool`, or `string`), `null_type`, `google.protobuf.Timestamp`, `google.protobuf.Duration`, Protobuf `Message` / record types, `type(T)` (first-class runtime/compile-time type value), `dyn` (gradual typing top type disabling static checks), `error` (non-value error state for commutative logical `&&`/`||`), and `abstract_type(name, T1, T2, ...)` (opaque parameterized user types such as `vector(T)`).
     - **Function Overloads & Dispatch**: Every operator (`_+_`, `_==_`, `_in_`, `_[_]`) and function (`size`, `contains`, `startsWith`) is desugared into a function call with **multiple overload declarations**: an overload has a unique `overload_id` (e.g., `size_list`, `size_map`, `size_string`, `add_int64`, `add_duration_timestamp`), parameter types (which may contain **type variables** `A`, `K`, `V`), and a return type. Overloads are resolved statically by Hindley-Milner-style unification over type variables (`list(A) -> int`), or narrowed to a subset of overload candidates when `dyn` is involved and dispatched at runtime by actual value types.
     - **Comprehension Macros**: Higher-order operations (`all`, `exists`, `exists_one`, `map`, `filter`) are expanded at parse/check time into a single loop-accumulator AST node (`Comprehension`), avoiding runtime closure objects while statically typing the loop variable against the element/key type `A`.

3. **OASIS XACML v3.0 Related and Nested Entities Profile v1.0 & ALFA v1.0**:
   - **Sources**:
     - OASIS Committee Specification 03, *XACML v3.0 Related and Nested Entities Profile Version 1.0* (`https://docs.oasis-open.org/xacml/xacml-3.0-related-entities/v1.0/xacml-3.0-related-entities-v1.0.html`, 30 January 2024).
     - OASIS Working Draft 01, *ALFA for XACML Version 1.0* (`https://www.oasis-open.org/committees/download.php/55228/alfa-for-xacml-v1.0-wd01.doc`, 10 March 2015).
     - **License**: OASIS Specifications under **OASIS IPR Policy (RF on Limited Terms Mode)**; normative URIs and language grammar rules can be implemented in an LGPL v3 engine, while vendored OASIS XSDs remain read-only.
   - **Facts**:
     - The Related and Nested Entities Profile standardizes the `entity` data-type URI `urn:oasis:names:tc:xacml:3.0:data-type:entity`, nested `<AttributeDesignator>` / `<AttributeSelector>` functions taking `entity` values, standard bag functions (`entity-one-and-only`, `entity-bag-size`, `entity-bag`), and four quantified loop expressions (`<ForAny>`, `<ForAll>`, `<Map>`, `<Select>`) with bound `$VariableId` domain variables.
     - ALFA v1.0 declares `attribute` (with `id`, `type`, `category`), `category`, `function` signatures with type constructors (`anyAtomic`, `bag[anyAtomic]`, `anyAtomicOrBag`, `function`, and variadic `T*`), and `infix` operator bindings (`comm`, `inv`, `allowbags`) that automatically lift scalar comparisons (`==`, `<`, `>`) to XACML `any-of` / `any-of-any` over bags when `allowbags` is present.

4. **OpenJDK JEP 401 (Value Classes and Objects)**:
   - **Source**: OpenJDK JEP 401 (`https://openjdk.org/jeps/401`, status: Preview, updated 2026/08/07).
   - **Facts**:
     - `value class` / `value record` instances have no object identity (`==` compares state; `synchronized` and `WeakReference` throw `IdentityException`), all instance fields are implicitly `final`, and the superclass must be `Object` or an `abstract value class`.
     - **Scalarization vs. Heap Flattening**: Scalarization in JIT registers/stack frames applies to `value class`es of any field count when the compile-time type is concrete or a small sealed hierarchy. Heap array/field flattening on 64-bit JVMs packs **at most 64 bits including the 1-bit null channel** for nullable reference fields/arrays (`Value[]`), whereas fields of one `value class` embedded inside another `value class` can flatten beyond 64 bits.
     - **Serialization**: `value record`s serialize via their canonical constructor; non-record `value class`es implementing `Serializable` **must** define `writeReplace` and `readResolve` or `InvalidClassException` is thrown at runtime.

---

### 1.4 Side-by-Side Comparison Matrix

| Dimension | `xacml4j` (`org.xacml4j.v30`) | `artagon-valuebox` (`org.artagon.box` + wiki) | AWS Cedar (v4.x) | Google CEL |
| --- | --- | --- | --- | --- |
| **Root Value & Type Hierarchy** | `ValueExpr` -> `Value<T>` / `BagOfValues<T>`; `ValueExpTypeInfo` -> `ValueType<T>` / `BagOfValuesType<T>` | Sealed `Expr` -> `ValueExpr` (`ValueBox<V>`, `Sequence<V,S>`); parallel sealed `ValueExpr.Type` and `Kind` | `Value` (`Prim`, `Set`, `Record`, `EntityUID`, `Ext`); schema types in Validator | `Val` (`Int`, `Uint`, `Double`, `Bool`, `String`, `Bytes`, `List`, `Map`, `Type`, `Object`, `Null`, `Err`) |
| **Numerics** | `IntegerVal` (`Long`), `DoubleVal` (`Double`) | `StdIntTypes` (`INT8`..`INT64`, `UINT8`..`UINT64`); wiki adds `Float32`, `Float64`, `DecimalBox` | `Long` (64-bit signed), `decimal` extension (fixed 4-dp) | `int` (64-bit signed), `uint` (64-bit unsigned), `double` (IEEE 754) |
| **Temporal Types** | `DateVal`, `TimeVal`, `DateTimeVal`, `DayTimeDurationVal`, `YearMonthDurationVal` (XSD / Joda-like wrappers) | `ISO8601Box` (`ISO8601DateTime`, `ISO8601Duration`) with 11 `java.time` `Kind`s (`Instant`, `OffsetDateTime`, `LocalDate`, `Duration`, `Period`, etc.) | `datetime` and `duration` extension types | `google.protobuf.Timestamp`, `google.protobuf.Duration` |
| **Entities & Identity** | `Entity` (`urn:org:xacml4j:v30:types:entity`): untyped attribute map + optional XML `Node`, no ID or schema | `EntityBox`: `EntityId` + `EntityRef` (`ExprContext.resolve`) + `EntitySchema` (`EntityIdSchema` + `StructSchema`); wiki adds `RelationshipDef` & `EntityGraph` | First-class `Entity` (`Namespace::Type::"id"`), nominal schema with attributes, `in` parent DAG, and `tags` | No native entity identity; modeled as Protobuf messages or maps |
| **Records / Structs** | None (conflated with `Entity`) | `StructBox` + `StructSchema` (`SchemaOfRecordClass`, `SchemaFFIStructLayout`, `SchemaOfTruffleShape`, `Ext`) + validated `PropertyRef` | `Record` (`{ k: T, opt?: T }`), structural, closed/open, `has` presence test | Protobuf `Message` or `map(string, dyn)` with `has(e.f)` macro |
| **Collections** | `BagOfValues<T>` (multiset) only; sets emulated by `SetFunctions` over `BagOfValues` | `Sequence` (`BagBox<V>`, `SetBox<V>`, `MapBox<K,V>` as `Sequence<PairBox<K,V>>`; `Kind` also lists `List`, `BiMap`, `Multimap`, `Stream`) | `Set<T>` only (no bag/multiset, no list) | `list(A)` and `map(K, V)` (no multiset/bag, no set) |
| **Sums, Options, Tuples, Ranges** | None (missing attribute = empty `BagOfValues`) | `OptionBox<V>` (`ValueBox` is `Some`, `OptionBoxNone` is `None`), `PairBox<L,R>`, `RangeBox<T>`, `TypesSumBox` | Optional record/entity attributes checked via `has` | `null_type`, `dyn` union behavior, `optional(T)` library extension |
| **Refinement Types** | None | `PropertyDef<V> extends Predicate<V>` & `PropertyConstraint<V>` on schema properties; wiki Ch. 06 adds `RestrictedType<V>` | None (only nominal entity subtypes & `Action` enumeration) | None |
| **Function Typing & Dispatch** | Monomorphic URI -> 1 `FunctionSpec` -> reflective `Method.invoke(null, Object[])` | Wiki Ch. 03: `(OpCode, Kind, Kind)` -> `MethodHandle` (`invokeExact`) + `CallSite` caching | Built-in operators + extension functions/methods; no user functions | Overloads with type variables (`list(A) -> int`) resolved by unification |
| **Higher-Order Functions** | `any-of`, `all-of`, `any-of-any`, `map` via untyped `@FuncParamFuncReference`; no lambdas | Sequence/stream combinators; OASIS Related Entities `<ForAny>`, `<ForAll>`, `<Map>`, `<Select>` | `.all()`, `.any()` on tags/sets in Cedar v4; `in` array/set membership | Compile-time comprehension macros (`all`, `exists`, `exists_one`, `map`, `filter`) |

---

## 2. Design Options for `xacml4j` with Trade-Offs

### Question A: Core Type System & Value Representation (Bridging `xacml4j` and `artagon-valuebox`)

- **Option A0 — Do Not Do It (Keep `XacmlTypes` enum + `BagOfValues` as-is)**:
  - *Pros*: Zero migration cost; zero risk to existing serialized streams or custom `ValueType` implementations.
  - *Cons*: Cannot type-check ALFA or Cedar policies against entity/record schemas; cannot represent `Set<T>`, `Map<K,V>`, `Record`, `Option<T>`, sized integers, or `EntityRef`; forces every generic function to remain duplicated 18+ times.
- **Option A1 — Adopt `artagon-valuebox` Wholesale as a Separate Replacement Root (`org.artagon.box.*`)**:
  - *Pros*: Directly reuses the 54 `org.artagon.box` and 21 `org.artagon.box.schema` files.
  - *Cons*: **Severely `BREAKING`** for all `xacml4j` callers (`Value`, `ValueType`, `XacmlTypes`, `BagOfValues`); depends on `artagon-core` (`Opt`, `Result`, `Try`, `PairRefNull`) and Valhalla JDK 27 preview syntax (`value record`), which violates `xacml4j`'s JDK 25 `<release>25</release>` no-preview baseline (`AGENTS.md`); carries FFI/Truffle dependencies (`SchemaFFIStructLayout`, `SchemaOfTruffleShape`) that a policy engine does not need in core.
- **Option A2 (Recommended) — Evolutionary Sealed Type Algebra inside `org.xacml4j.v30` Porting `artagon-valuebox`'s Core Abstractions on Standard JDK 25**:
  - Evolve `ValueExpTypeInfo` (or introduce `TypeDescriptor` extending `ValueExpTypeInfo`) into a sealed hierarchy with a `TypeKind` tag hierarchy and a 64-bit `typeIdAsLong()` fingerprint (from [`TypeId.java:46-52`](artagon-uri/artagon-valuebox/src/main/java/org/xacml4j/v30/ValueExpTypeInfo.java)):
    - Keep `XacmlTypes` and `ValueType<T>` 100% source- and binary-compatible as the leaf descriptors for the 18 XACML types, and alias `urn:oasis:names:tc:xacml:3.0:data-type:entity` to `XacmlTypes.ENTITY`.
    - Port `artagon-valuebox`'s algebraic descriptors and carriers (`EntityType`/`EntityVal`, `StructType`/`StructVal`, `PropertyRef`, `EntityRef`, `SetType`/`SetOfValues`, `MapType`/`MapOfValues`, `OptionType`, `PairType`, `RangeType`, `RestrictedType`, `TypeVariable`, and sized integer/decimal/IP/temporal kinds) directly into `org.xacml4j.v30.types` and `org.xacml4j.v30.schema` using standard JDK 25 `record` / `final class` (ready for JEP 401 `value record` / `abstract value class` when Valhalla exits preview).
  - *Pros*: Zero `BREAKING` change to existing XACML 2.0/3.0 users; no external `artagon-core` or JDK 27 preview dependency; gives XACML 3.0/4.0, ALFA, and Cedar the exact algebraic type system designed in `artagon-valuebox`.
  - *Cons*: Requires carefully bridging `Entity` (legacy untyped attribute bag) and schema-typed `EntityVal` / `StructVal`.

### Question B: Authorization Domain Model (Entities, Structs/Records, Attributes, Hierarchy, Refinements)

- **Option B0 — Do Not Do It (Keep untyped `AttributeDesignatorKey` + `Entity`)**:
  - *Pros*: No schema required to evaluate a policy.
  - *Cons*: Impossible to validate attribute names/types at policy load time (a core feature of Axiomatics ALFA and AWS Cedar); cannot express Cedar `principal in Group::"admins"` or `resource has owner`.
- **Option B1 — Separate Frontend-Specific Schemas (An ALFA Symbol Table + a Separate Cedar Schema AST)**:
  - *Pros*: Each frontend mirrors its own spec verbatim.
  - *Cons*: Duplicate validators, cannot share PIPs/attribute resolvers or cross-compile/combine ALFA and Cedar policies in one PDP.
- **Option B2 (Recommended) — Unified `AuthzDomainModel` Based on `artagon-valuebox` `org.artagon.box.schema`**:
  - Port and complete [`StructSchema`](artagon-uri/artagon-valuebox/src/main/java/org/artagon/box/schema/StructSchema.java#L38-L207), [`EntitySchema`](artagon-uri/artagon-valuebox/src/main/java/org/artagon/box/schema/EntitySchema.java#L32-L70), [`EntityIdSchema`](artagon-uri/artagon-valuebox/src/main/java/org/artagon/box/schema/EntityIdSchema.java#L34-L70), [`PropertyDef`](artagon-uri/artagon-valuebox/src/main/java/org/artagon/box/schema/PropertyDef.java#L36-L154), and [`PropertyRef`](artagon-uri/artagon-valuebox/src/main/java/org/artagon/box/PropertyRef.java#L38-L90) into `org.xacml4j.v30.schema`, plus `ActionSchema` and `RelationshipDef` / `EntityGraph` (from wiki Ch. 05):
    - **XACML 2.0 / 3.0 / 4.0 Categories** (`access-subject`, `resource`, `action`, `environment`, plus all standard OASIS profile attributes: Core, Hierarchical Resource, Multiple Decision, RBAC, IPC, EC-US, SAML, Related & Nested Entities) are pre-packaged as built-in `EntitySchema` / `CategorySchema` modules (`Xacml20DefaultModel`, `Xacml30DefaultModel`, `Xacml40DefaultModel`).
    - **ALFA `category` and `attribute` declarations** compile directly into `CategorySchema` and `PropertyDef` entries (with optional vs. required and `bag[T]` vs. scalar `T` constraints).
    - **Cedar `entity` (`in [...]`, attributes, `tags`), `type` (records/sets), and `action` (`appliesTo`)** compile into `EntitySchema` (with `parentEntityTypes`), `StructSchema`, and `ActionSchema`.
  - *Pros*: Single source of truth for XACML, ALFA, and Cedar; supports both Java record binding (`SchemaOfRecordClass`) and dynamic map/array-backed entities; statically validates property access via `PropertyRef` (`O(1)` slot index lookup at evaluation time).
  - *Cons*: Needs a clean open-world vs. closed-world mode flag (`ValidationMode.STRICT_CLOSED` for Cedar vs. `ValidationMode.OPEN_EXTENSIBLE` for raw XACML XML).

### Question C: Function Type System, Generics, Overloads, Higher-Order Expressions, and Dispatch

- **Option C0 — Do Not Do It (Keep monomorphic `FunctionSpec` + `Method.invoke`)**:
  - *Pros*: No change to `FunctionProvider` SPI.
  - *Cons*: Cannot type-check polymorphic `bag[T]` / `Set<T>` functions without generating $N$ URI copies for every user-defined `EntityType` or `StructType`; keeps `Object[]` allocation and reflective `Method.invoke` overhead.
- **Option C1 — CEL-Style Arbitrary Ad-Hoc Overloading Everywhere**:
  - *Pros*: Flexible operator overloading.
  - *Cons*: Ambiguous overload resolution when combined with XACML's implicit `allowbags` bag-lifting in ALFA.
- **Option C2 (Recommended) — Parametric `PolymorphicFunctionSpec` + `MethodHandle` Dispatch + Typed Comprehension AST**:
  1. **Parametric Type Signatures (ALFA + CEL Unification)**: Extend `FunctionSpec` (additively) with `List<TypeVariable> typeParameters()` and `PolymorphicFunctionSpec` (e.g., `bag-one-and-only<T>(Bag<T>) -> T`, `set-contains<T>(Set<T>, T) -> Boolean`, `map-get<K,V>(Map<K,V>, K) -> Option<V>`). Standard XACML monomorphic URIs (`integer-one-and-only`) become pre-bound specializations of the generic template, while user-defined `EntityType` / `StructType` bags automatically instantiate `oneAndOnly(Bag<Entity<User>>) -> Entity<User>`.
  2. **Operator & Overload Table for ALFA/Cedar**: An `OperatorRegistry` maps `(OperatorId, LeftKind, RightKind)` to either a direct `FunctionSpec` or an ALFA `allowbags` lifted `any-of` invocation (per ALFA v1.0 §4.5 `infix ... allowbags`).
  3. **`MethodHandle` Invocation (Wiki Ch. 03)**: Replace `DefaultFunctionInvocation`'s `Method.invoke(null, params)` with `MethodHandles.Lookup.unreflect(method)` compiled into an exact/spreader `MethodHandle` (and specialized primitive fast paths `(long, long) -> boolean` / `(long[], long) -> boolean` connecting directly to Cycle C's primitive bag storage).
  4. **Typed Quantified / Comprehension Expressions**: Implement the OASIS Related & Nested Entities Profile v1.0 `<ForAny>`, `<ForAll>`, `<Map>`, `<Select>` AST nodes (`ForAnyExp`, `ForAllExp`, `MapExp`, `SelectExp`) with a typed loop variable (`VariableDecl(String varId, TypeDescriptor elementType)`). Cedar's `.all()` / `.any()` and CEL-style comprehensions compile to these exact same four nodes.

---

## 3. Concrete Recommendation and Why

### 3.1 Recommended Architecture & Package Layout

Keep `xacml-core` dependency-minimal and LGPL v3 clean, and organize the enhancement across four modules:

1. **`xacml-core` (`org.xacml4j.v30.types`, `org.xacml4j.v30.schema`, `org.xacml4j.v30.policy.function`)**:
   - **Port the `artagon-valuebox` type descriptor and schema model into `xacml-core` on JDK 25**:
     - `org.xacml4j.v30.types.TypeKind` (sealed interface + enums for `PrimitiveKind`, `NumericKind` [`INT8`..`INT64`, `UINT8`..`UINT64`, `FLOAT32`, `FLOAT64`, `DECIMAL`], `TemporalKind`, `NetworkKind`, `UriKind`, `CompositeKind` [`ENTITY`, `STRUCT`, `PAIR`, `RANGE`, `OPTION`, `UNION`], `SequenceKind` [`BAG`, `SET`, `LIST`, `MAP`]).
     - `org.xacml4j.v30.types.SequenceOfValues<V>` permitting `BagOfValues<V>` (existing), `SetOfValues<V>` (Cycle C deduplicated primitive/object storage), and `MapOfValues<K, V>`.
     - `org.xacml4j.v30.types.StructVal` and `org.xacml4j.v30.types.EntityVal` (implementing `Value` and backed by `StructSchema` / `EntitySchema` with indexed `ValueExpr[]` or primitive fields, plus `EntityId` and `EntityRef`). Keep legacy `org.xacml4j.v30.types.Entity` as the schema-less subtype/adapter and register both `urn:oasis:names:tc:xacml:3.0:data-type:entity` and `urn:org:xacml4j:v30:types:entity`.
     - `org.xacml4j.v30.schema`: `AuthzDomainModel`, `CategorySchema`, `EntitySchema`, `EntityIdSchema`, `StructSchema` (`RecordClassStructSchema<R extends Record>`, `IndexedStructSchema`), `ActionSchema`, `RelationshipDef`, `EntityGraph`, `PropertyDef<V>`, `PropertyRef` (carrying an `int slotIndex` in addition to `name`, `propertyType`, `recordType`), and `RestrictedType<V>`.
   - **Built-in Default Models (`org.xacml4j.v30.schema.std`)**:
     - `Xacml20DefaultModel`, `Xacml30DefaultModel`, and `Xacml40DefaultModel` (covering Core, Hierarchical Resource, Multiple Decision, RBAC, IPC, EC-US, SAML, and Related & Nested Entities v1.0 attributes, categories, functions, and combining algorithms).
   - **Static Policy Type-Checker (`org.xacml4j.v30.validation.PolicyTypeChecker`)**:
     - A two-phase pass run before evaluation (or at policy load/compile time):
       1. **Environment & Schema Binding**: Resolves every `AttributeDesignator`, `PropertyRef` (`entity.attr`), `EntityRef`, and `Action` against `AuthzDomainModel`.
       2. **Flow-Sensitive Bidirectional Type & Refinement Check (Cedar-style)**: Tracks path conditions from `has` (attribute presence) and `is` (entity type narrowing) through `and` / `or` / `Target` / `Condition`, verifies `in` against the `EntityGraph` allowed parent types, checks `RestrictedType` constraints on literals, and resolves generic/overloaded `FunctionSpec`s to monomorphic `MethodHandle`-backed `FunctionInvocation`s.
2. **`xacml-alfa` (New Maven Module, `org.xacml4j.v30.alfa`)**:
   - Parses `.alfa` files (OASIS ALFA v1.0 grammar + Axiomatics-style domain model extensions for `entity`, `record`, and XACML 3.0 Related Entities `<ForAny>`/`<ForAll>`/`<Map>`/`<Select>`) into `AuthzDomainModel` + `xacml-core` `PolicySet`/`Policy` AST, with bidirectional decompilation (`XacmlToAlfaPrinter`).
3. **`xacml-cedar` (New Maven Module, `org.xacml4j.v30.cedar`)**:
   - Parses `.cedarschema` (human-readable and JSON schema formats) into `AuthzDomainModel` (`EntitySchema` with `in` parents + `ActionSchema` with `appliesTo`) and `.cedar` policies (`permit`/`forbid`, `principal`, `action`, `resource`, `when`/`unless`) into `xacml-core` policies using a Cedar-compliant `PermitUnlessForbid` combining algorithm (`deny-overrides` with default-deny).

### 3.2 Grammar Tools, File Formats, and License Compatibility (for LGPL v3)

| Component | Recommended Tool / Format | License | Compatibility with `xacml4j` (LGPL v3) |
| --- | --- | --- | --- |
| `artagon-valuebox` (`org.artagon.box`, `.schema`, `.spi`) | Port/adapt directly into `xacml-core` | **LGPL v3** (`Copyright Xacml4J.org`) | **Native / Same Author & License** — 100% compatible. |
| Parser generator for `.alfa`, `.cedar`, `.cedarschema` | **ANTLR 4** (`org.antlr:antlr4-runtime`) | **BSD-3-Clause** | **Compatible** — permissive BSD-3 runtime can be linked by an LGPL v3 library. |
| Cedar schema & policy reference grammar / test corpus | Clean-room ANTLR4 grammar from `docs.cedarpolicy.com` & `cedar-policy/rfcs` (or adapt `cedar-spec`) | **Apache-2.0** | **Compatible** — Apache-2.0 is compatible with GPLv3 and LGPLv3 (FSF/OSI recognized). |
| CEL expression / overload design patterns | Clean-room implementation from `google/cel-spec` | **Apache-2.0** | **Compatible** with LGPL v3. |
| OASIS XACML 2.0/3.0/4.0, Related Entities v1.0, ALFA v1.0 | Implement normative URIs, model definitions, and grammar in `xacml4j` | **OASIS IPR Policy (RF on Limited Terms)** | **Compatible** — standard implementation allowed; keep vendored OASIS XSDs read-only. |
| IPv4/IPv6 CIDR & Trie (`IPNetworkBox`) | Built-in `java.net.InetAddress` + compact 64-bit/128-bit prefix math (or `com.github.seancfoley:ipaddress`) | JDK / **Apache-2.0** | **Compatible** with LGPL v3. |

### 3.3 What Valhalla Value Classes (JEP 401) Will Change

1. **Today on JDK 25 (`<release>25</release>`, no preview)**:
   - Implement leaf `Value` carriers (`IntegerVal`, `DoubleVal`, `BooleanVal`, `SInt32Val`, `UInt64Val`, `DecimalVal`, `DateTimeVal`, `DurationVal`, `EntityRef`, `PropertyRef`, `TypeId`) as `final` identity-free-ready classes or `record`s (no `synchronized`, no `WeakReference`, state-based `equals`/`hashCode`), and rely on Cycle C's primitive array storage (`long[]`, `double[]`, `boolean[]`) inside `BagOfValues` and `SetOfValues` for heap flattening and Java Vector API (`jdk.incubator.vector`) SIMD scans.
2. **When JEP 401 Exits Preview in a Future LTS JDK**:
   - Adding the `value` modifier (`public abstract value class Value<T>` and `public value record SInt32Val(int value)` / `EntityRef` / `PropertyRef`) lets the JIT **scalarize** all `Value` / `ValueBox` instances across `Expr.evaluate(EvaluationContext)` and `MethodHandle` returns in CPU registers with **zero heap allocation**.
   - **Crucial JEP 401 Caveats from the Spec (`https://openjdk.org/jeps/401`)**:
     - Because `Value<T>` currently holds `private final ValueType<?> type` (a 32/64-bit compressed oop) plus a 64-bit `long`/`double` payload, a subclass with both fields exceeds the **64-bit (including 1-bit null channel) heap reference flattening limit** on 64-bit JVMs. To achieve **heap array flattening** (not just stack scalarization), fixed-type carriers (`IntegerVal`, `DoubleVal`, `SInt32Val`) **must not store a `type` reference field** — their `getType()` method should return the constant `XacmlTypes.INTEGER` / `StdIntTypes.INT32.type()` via virtual dispatch, leaving only the single `long`/`int`/`double` field inside the `value class`!
     - Because `Value` implements `java.io.Serializable`, any non-record `value class` subclass of `Value` **must** implement `writeReplace()` and `readResolve()` (or be a `value record`), otherwise Java serialization throws `InvalidClassException` under JEP 401.

---

## 4. Risks and `BREAKING` Items

| Item | Severity | `BREAKING`? | Mitigation |
| --- | --- | --- | --- |
| Removing `protected final ValueType<?> type` field from `Value<T>` (so subclasses have only 1 primitive field for JEP 401 64-bit heap flattening) | Medium | **`BREAKING` (Protected / Serialization)** if changed on `Value<T>` directly | Keep `getType()` as an abstract/overridden method on `Value<T>` and provide `writeReplace`/`readResolve` or `serialPersistentFields` so the wire format of `IntegerVal`/`DoubleVal` stays identical (or introduce `ValueBox` alongside `Value`). |
| Changing `XacmlTypes.ENTITY` URI from `urn:org:xacml4j:v30:types:entity` to OASIS standard `urn:oasis:names:tc:xacml:3.0:data-type:entity` | High | **`BREAKING` (Wire / Policy URI)** if the old URI is removed | Register the official OASIS URI `urn:oasis:names:tc:xacml:3.0:data-type:entity` as the canonical ID and keep `urn:org:xacml4j:v30:types:entity` as a recognized alias in `XacmlTypes.fromId(...)` and XML/JSON marshallers. |
| Three-valued logic mismatch: XACML `Indeterminate` (with status code) vs. Cedar runtime error (skip policy, log diagnostic, except `Forbidden` dominates) vs. CEL commutative logical error absorption | High | Semantic risk across frontends | Keep the core expression evaluator 3-valued (`Result<EvaluationException, ValueExpr>` or `ValueExpr` + `EvaluationContext` error channel) and let the combining algorithm / policy wrapper enforce XACML vs. Cedar error semantics. |
| Empty Bag vs. Missing Attribute (`Option.None`): In XACML, a missing attribute evaluates to an empty `BagOfValues` (`MustBePresent=false`), whereas in Cedar accessing a missing optional attribute without `has` is a type/evaluation error | Medium | Semantic risk | Model Cedar entity/record attributes as `OptionType<T>` (checked by `has` in `PolicyTypeChecker`) and XACML multi-valued attributes as `BagOfValuesType<T>`. |
| `ValueBox.java:186` inverted type-assertion bug (`if (isOfType(value)) throw ...`) and `EntitySchema.java:67` typo (`entityBox.s`) in `artagon-valuebox` | Low | Bug if copied verbatim | Fix both defects and add unit tests when porting the classes into `xacml4j` (and during `restore-valuebox-module-build`). |

---

## 5. Open Questions for the Maintainer

1. **Relationship between `xacml4j` and `artagon-valuebox`**: Should the repaired `org.artagon.box` / `org.artagon.box.schema` design be ported directly into `xacml-core` under `org.xacml4j.v30.types` / `org.xacml4j.v30.schema` on JDK 25 (zero external dependencies, standard `record`s now and `value record`s when Valhalla ships), or do you plan to publish `artagon-core` + `artagon-valuebox` as Maven artifacts and have a future `xacml4j` major version depend on them directly?
2. **XACML 4.0 Scope**: Since OASIS XACML v4.0 is still evolving in the OASIS TC alongside the published v3.0 Related and Nested Entities Profile v1.0 (CS03, Jan 2024), which specific XACML 4.0 draft features beyond Related & Nested Entities (`entity`, `<ForAny>`, `<ForAll>`, `<Map>`, `<Select>`), JSON Profile, and dynamic attribute schemas should `Xacml40DefaultModel` include first?
3. **Cedar Execution Mode**: Should Cedar policies (`.cedar` + `.cedarschema`) compile into the existing `xacml-core` `PolicySet`/`Policy`/`Rule` AST (with a Cedar `PermitUnlessForbid` combining algorithm and `EntityGraph` PIP), so a single PDP can evaluate mixed ALFA, XACML XML/JSON, and Cedar policies over the same `AuthzDomainModel`?
4. **Java Serialization Compatibility for New `Value` Types**: Cycle C preserves Java `Serializable` golden streams for existing `BagOfValues` and `XacmlTypes`. For new composite/schema-governed values (`EntityVal`, `StructVal`, `SetOfValues`, `MapOfValues`), is Java `ObjectOutputStream` serialization required, or should marshalling rely exclusively on JSON/XML/binary schema codecs?
