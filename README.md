# xacml4j: XACML 3.0 and 2.0 policy engine for Java

[![Java 25](https://img.shields.io/badge/Java-25-blue)](https://openjdk.org/projects/jdk/25/)
[![XACML 3.0 and 2.0](https://img.shields.io/badge/OASIS%20XACML-3.0%20%7C%202.0-green)](https://docs.oasis-open.org/xacml/3.0/xacml-3.0-core-spec-os-en.html)
[![License: LGPL v3](https://img.shields.io/badge/License-LGPL%20v3-blue.svg)](LICENSE)

xacml4j is an open-source Java implementation of the OASIS eXtensible Access Control Markup Language
(XACML) 3.0 and 2.0. It provides a complete policy model, a fast policy decision point (PDP), a
policy information point (PIP) SPI for attributes from your own systems, and XML and JSON
marshalling. Use it to add attribute-based access control (ABAC) and fine-grained authorization to a
Java application or service.

- **Standards:** XACML 3.0 core and XACML 2.0 policies, requests, and responses; all standard data
  types, functions, and rule and policy combining algorithms; the Multiple Decision Profile.
- **Fast:** about 2 µs and under 1 KB allocated per decision on the RSA 2008 interoperability policies
  (JMH, JDK 25), with an optional target index for policy sets with many children.
- **Extensible:** attribute resolvers, function providers, combining algorithms, policy repositories,
  regular-expression engines, and decision observers are SPIs.
- **Tested:** the OASIS XACML 2.0 and 3.0 conformance suites and the RSA 2008 interoperability suite
  run in every build.

See [ABOUT.md](ABOUT.md) for the project's scope, standards coverage, and history.

## Contents

- [Requirements](#requirements)
- [Modules](#modules)
- [Getting started](#getting-started)
- [Examples](#examples)
- [Configuration](#configuration)
- [Testing your policies](#testing-your-policies)
- [Performance](#performance)
- [Building from source](#building-from-source)
- [Contributing](#contributing)
- [License](#license)

## Requirements

- JDK 25. The build compiles with `--release 25` and rejects an older JDK.
- Apache Maven 3.9 or later.

## Modules

All Java packages are under `org.xacml4j`. The Maven group id is `org.xacml4j`; the version on the
main development line is `2.0.0-SNAPSHOT`.

| Artifact | Purpose |
| --- | --- |
| `xacml-core` | Policy model, data types, functions, combining algorithms, request and response model, marshalling SPI |
| `xacml-pdp` | The PDP, its builder, the PIP SPI (attribute resolvers, caching), and policy repositories |
| `xacml-xml` | XACML 3.0 and 2.0 XML marshalling of policies, requests, and responses |
| `xacml-xml-jaxb` | JAXB classes generated from the OASIS schemas (used by `xacml-xml`) |
| `xacml-json` | XACML 3.0 JSON marshalling of requests and responses |
| `xacml-test` | Test support for policy authors (`XacmlPolicyTestSupport`) |
| `xacml-conformance-tests` | The OASIS and RSA 2008 conformance suites (tests only) |
| `xacml-jmh` | JMH benchmarks of PDP decisions and policy stores |

## Getting started

### 1. Add the dependencies

Build and install the project (see [Building from source](#building-from-source)), then depend on
the PDP and the marshalling you need:

```xml
<dependency>
  <groupId>org.xacml4j</groupId>
  <artifactId>xacml-pdp</artifactId>
  <version>2.0.0-SNAPSHOT</version>
</dependency>
<dependency>
  <groupId>org.xacml4j</groupId>
  <artifactId>xacml-xml</artifactId>
  <version>2.0.0-SNAPSHOT</version>
</dependency>
<!-- Optional: JSON requests and responses -->
<dependency>
  <groupId>org.xacml4j</groupId>
  <artifactId>xacml-json</artifactId>
  <version>2.0.0-SNAPSHOT</version>
</dependency>
```

`xacml-xml` registers its XACML 3.0 and 2.0 marshalling through `ServiceLoader`, so having it on the
class path is enough. `xacml-json` provides marshaller classes that you create directly.

### 2. Write a policy

This XACML 3.0 policy permits the action `read` to subjects whose role is `manager`, and denies
everything else (`policy.xml`):

```xml
<Policy xmlns="urn:oasis:names:tc:xacml:3.0:core:schema:wd-17"
        PolicyId="urn:example:policy:documents" Version="1.0"
        RuleCombiningAlgId="urn:oasis:names:tc:xacml:3.0:rule-combining-algorithm:deny-unless-permit">
  <Target/>
  <Rule RuleId="urn:example:rule:managers-read" Effect="Permit">
    <Target>
      <AnyOf>
        <AllOf>
          <Match MatchId="urn:oasis:names:tc:xacml:1.0:function:string-equal">
            <AttributeValue DataType="http://www.w3.org/2001/XMLSchema#string">read</AttributeValue>
            <AttributeDesignator Category="urn:oasis:names:tc:xacml:3.0:attribute-category:action"
                AttributeId="urn:oasis:names:tc:xacml:1.0:action:action-id"
                DataType="http://www.w3.org/2001/XMLSchema#string" MustBePresent="false"/>
          </Match>
        </AllOf>
      </AnyOf>
    </Target>
    <Condition>
      <Apply FunctionId="urn:oasis:names:tc:xacml:1.0:function:string-is-in">
        <AttributeValue DataType="http://www.w3.org/2001/XMLSchema#string">manager</AttributeValue>
        <AttributeDesignator Category="urn:oasis:names:tc:xacml:1.0:subject-category:access-subject"
            AttributeId="urn:example:attribute:role"
            DataType="http://www.w3.org/2001/XMLSchema#string" MustBePresent="false"/>
      </Apply>
    </Condition>
  </Rule>
</Policy>
```

### 3. Build a PDP and ask for a decision

```java
import java.io.ByteArrayInputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import org.xacml4j.v30.CompositeDecisionRule;
import org.xacml4j.v30.PolicyDecisionPoint;
import org.xacml4j.v30.RequestContext;
import org.xacml4j.v30.ResponseContext;
import org.xacml4j.v30.Result;
import org.xacml4j.v30.marshal.MarshallingProvider;
import org.xacml4j.v30.marshal.MediaType;
import org.xacml4j.v30.pdp.PolicyDecisionPointBuilder;
import org.xacml4j.v30.spi.pip.PolicyInformationPoint;
import org.xacml4j.v30.spi.repository.PolicyRepository;

// A repository holds policies. Its import tool parses them with the repository's functions.
PolicyRepository repository = PolicyRepository.build("documents").build();
byte[] policyXml = Files.readAllBytes(Path.of("policy.xml"));
CompositeDecisionRule policy = repository.newImportTool().importPolicy(
        MediaType.Type.XACML30_XML, () -> new ByteArrayInputStream(policyXml));

// A PIP resolves attributes that a request does not carry.
PolicyInformationPoint pip = PolicyInformationPoint.builder("documents-pip")
        .withDefaultRegistry()
        .noCacheProvider()
        .build();

try (PolicyDecisionPoint pdp = PolicyDecisionPointBuilder.builder("documents-pdp")
        .policyRepository(repository)
        .pip(pip)
        .rootPolicy(policy)
        .defaultRequestHandlers()          // the Multiple Decision Profile
        .build()) {

    MarshallingProvider xml = MarshallingProvider.getProvider(MediaType.Type.XACML30_XML).orElseThrow();
    RequestContext request = xml.newRequestUnmarshaller().orElseThrow()
            .unmarshal(Files.newInputStream(Path.of("request.xml")));

    ResponseContext response = pdp.decide(request);
    for (Result result : response.getResults()) {
        System.out.println(result.getDecision());   // PERMIT
    }
}
```

`request.xml`, an XACML 3.0 request from a manager who wants to read:

```xml
<Request xmlns="urn:oasis:names:tc:xacml:3.0:core:schema:wd-17"
         ReturnPolicyIdList="false" CombinedDecision="false">
  <Attributes Category="urn:oasis:names:tc:xacml:1.0:subject-category:access-subject">
    <Attribute AttributeId="urn:example:attribute:role" IncludeInResult="false">
      <AttributeValue DataType="http://www.w3.org/2001/XMLSchema#string">manager</AttributeValue>
    </Attribute>
  </Attributes>
  <Attributes Category="urn:oasis:names:tc:xacml:3.0:attribute-category:action">
    <Attribute AttributeId="urn:oasis:names:tc:xacml:1.0:action:action-id" IncludeInResult="false">
      <AttributeValue DataType="http://www.w3.org/2001/XMLSchema#string">read</AttributeValue>
    </Attribute>
  </Attributes>
</Request>
```

One `PolicyDecisionPoint` serves concurrent decisions: build it once and share it. Close it when
you no longer need it; closing also detaches it from its repository.

The same example, with the JSON and PIP examples below, is a runnable program at
[docs/examples/GettingStarted.java](docs/examples/GettingStarted.java).

## Examples

### Build a request in code

```java
import org.xacml4j.v30.Attribute;
import org.xacml4j.v30.Category;
import org.xacml4j.v30.CategoryId;
import org.xacml4j.v30.Decision;
import org.xacml4j.v30.RequestContext;
import org.xacml4j.v30.types.Entity;
import org.xacml4j.v30.types.XacmlTypes;

RequestContext request = RequestContext.builder()
        .attributes(
                Category.builder(CategoryId.SUBJECT_ACCESS)
                        .entity(Entity.builder()
                                .attribute(Attribute.builder("urn:example:attribute:role")
                                        .value(XacmlTypes.STRING.ofAny("manager"))
                                        .build())
                                .build())
                        .build(),
                Category.builder(CategoryId.ACTION)
                        .entity(Entity.builder()
                                .attribute(Attribute.builder("urn:oasis:names:tc:xacml:1.0:action:action-id")
                                        .value(XacmlTypes.STRING.ofAny("read"))
                                        .build())
                                .build())
                        .build())
        .build();

Decision decision = pdp.decide(request).getResults().iterator().next().getDecision();
```

### Read and write JSON

`xacml-json` reads and writes requests and responses in the XACML 3.0 JSON format, with short
category identifiers and data types:

```java
import java.io.StringReader;
import java.io.StringWriter;

import org.xacml4j.v30.marshal.json.JsonRequestContextUnmarshaller;
import org.xacml4j.v30.marshal.json.JsonResponseContextMarshaller;

RequestContext request = new JsonRequestContextUnmarshaller().unmarshal(new StringReader(requestJson));

StringWriter out = new StringWriter();
new JsonResponseContextMarshaller().marshal(pdp.decide(request), out);
```

The getting-started request as JSON:

```json
{"ReturnPolicyIdList":false,"CombinedDecision":false,"Category":[
  {"CategoryId":"access-subject","Attribute":[
    {"AttributeId":"urn:example:attribute:role","DataType":"string","Value":"manager"}]},
  {"CategoryId":"action","Attribute":[
    {"AttributeId":"urn:oasis:names:tc:xacml:1.0:action:action-id","DataType":"string","Value":"read"}]}]}
```

and its response:

```json
{"Result":[{"Decision":"Permit","Status":{"StatusCode":{"Value":"ok"}}}]}
```

`JsonRequestContextMarshaller` and `JsonResponseContextUnmarshaller` do the reverse.

### Supply attributes from your own systems

An attribute resolver supplies attributes that a request does not carry, for example a user's role
from a directory. The PDP calls it only when a policy needs the attribute, and can cache its answer:

```java
import java.util.Map;

import org.xacml4j.v30.spi.pip.AttributeResolverDescriptor;
import org.xacml4j.v30.spi.pip.ResolverRegistry;

ResolverRegistry registry = ResolverRegistry.builder()
        .withDefaultResolvers()
        .withAttributeResolver(AttributeResolverDescriptor
                .builder("roles", "Role directory", CategoryId.SUBJECT_ACCESS)
                .cache(300)                                   // seconds
                .attribute("urn:example:attribute:role", XacmlTypes.STRING)
                .build(context -> Map.of("urn:example:attribute:role",
                        XacmlTypes.STRING.bagOf(XacmlTypes.STRING.ofAny(lookUpRole(context))))))
        .build();

PolicyInformationPoint pip = PolicyInformationPoint.builder("roles-pip")
        .registry(registry)
        .noCacheProvider()                                    // or cacheProvider(...)
        .build();
```

A resolver can depend on other attributes of the request (`contextRef(...)`), resolve several
attributes in one call, or resolve attributes in parallel (`buildParallel()`). Overlapping lookups of
the same key within a decision share one call.

### Choose a regular-expression engine

The `*-regexp-match` functions run on a pluggable engine. The default is the JDK engine with the
`legacy` profile, which keeps the behavior of earlier versions. The `xf` profile follows the XPath
2.0 regular-expression syntax (XQuery 1.0 and XPath 2.0 Functions and Operators, §7.6) that XACML
3.0 references:

```java
PolicyRepository repository = PolicyRepository.build("documents")
        .withRegexEngine("jdk", "xf")
        .build();
```

The system properties `org.xacml4j.regex.engine` and `org.xacml4j.regex.profile` select the default
engine and profile. A pattern literal is compiled once, at its first evaluation, and its call site
keeps it.

## Configuration

`PolicyDecisionPointBuilder` options:

| Option | Effect |
| --- | --- |
| `policyRepository(r)` | The repository that resolves policy references (required) |
| `pip(p)` | The policy information point (required) |
| `rootPolicy(p)` | The policy or policy set that every request is evaluated against (required) |
| `defaultRequestHandlers()` | The Multiple Decision Profile: repeated attributes, request references, and XPath content selectors |
| `targetIndex()` | Index the targets of large policy sets and policies, so a decision checks only the children that can apply |
| `decisionCache(c)` | Cache whole decisions |
| `decisionAuditor(a)` | Receive every decision for auditing |
| `observer(o)` | Observe decision events (policy evaluation, attribute requests) |
| `metrics(registry)` | Publish Dropwizard metrics |
| `xpathProvider(p)` | Replace the XPath provider used by attribute selectors |

## Testing your policies

`xacml-test` helps you test policies against requests and expected responses in JUnit:

```xml
<dependency>
  <groupId>org.xacml4j</groupId>
  <artifactId>xacml-test</artifactId>
  <version>2.0.0-SNAPSHOT</version>
  <scope>test</scope>
</dependency>
```

Extend `org.xacml4j.v30.XacmlPolicyTestSupport` to load policies and to compare a decision with an
expected XACML response.

## Performance

The `xacml-jmh` module benchmarks decisions over several corpora: the RSA 2008 interoperability
policies (`rsa2008`), synthetic policy sets of 100 to 1,000 children (`synthetic:N`), rule-heavy
policies (`rules:N`), and PIP-heavy decisions (`pip:N`).

```sh
mvn -Dlicense.skipUpdateLicense=true -pl xacml-jmh -am -DskipTests package
java -jar xacml-jmh/target/benchmarks.jar PdpDecideBenchmark \
    -p store=in-memory -p corpus=rsa2008 -p request=all \
    -jvmArgs -XX:-UseJVMCICompiler -prof gc
```

Indicative results (JDK 25 with C2, Apple M2 Pro, in-memory store):

| Workload | Time per decision | Allocated per decision |
| --- | ---: | ---: |
| RSA 2008 interoperability policies (`rsa2008`, 17 requests) | about 2 µs | about 0.7 KB |
| 1,000 policies, target index on (`synthetic:1000`) | about 0.5 µs | about 0.3 KB |

[docs/ai/BENCHMARKS.md](docs/ai/BENCHMARKS.md) describes the corpora, parameters, and how to run a
controlled comparison.

## Building from source

```sh
git clone https://github.com/xacml4j/xacml4j.git
cd xacml4j
mvn -Dlicense.skipUpdateLicense=true clean install
```

`mvn verify` runs the unit tests and the OASIS and RSA 2008 conformance suites, about 10,000
tests. To run one module or one test class:

```sh
mvn -Dlicense.skipUpdateLicense=true -pl xacml-pdp -am test
mvn -Dlicense.skipUpdateLicense=true -pl xacml-pdp -am \
    -Dtest=DefaultPolicyDecisionPointTest -Dsurefire.failIfNoSpecifiedTests=false test
```

## Contributing

Issues and pull requests are welcome. Before you change code:

- Read [AGENTS.md](AGENTS.md): build rules, code conventions, and the project's tools.
- Plan new capabilities and behavior changes as an OpenSpec change under `openspec/changes/`
  ([docs/ai/OPENSPEC.md](docs/ai/OPENSPEC.md)).
- Follow the test rules in [docs/ai/TESTING.md](docs/ai/TESTING.md): tests pin outcomes, not
  mechanisms.
- Keep the LGPL license block of each Java file.

## License

xacml4j is licensed under the [GNU Lesser General Public License, version 3](LICENSE).

XACML is a standard of [OASIS](https://www.oasis-open.org/committees/xacml/).
