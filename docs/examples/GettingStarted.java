package example;

/*
 * #%L
 * Xacml4J Examples
 * %%
 * Copyright (C) 2009 - 2026 Xacml4J.org
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 *
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.xacml4j.v30.Attribute;
import org.xacml4j.v30.Category;
import org.xacml4j.v30.CategoryId;
import org.xacml4j.v30.CompositeDecisionRule;
import org.xacml4j.v30.Decision;
import org.xacml4j.v30.PolicyDecisionPoint;
import org.xacml4j.v30.RequestContext;
import org.xacml4j.v30.ResponseContext;
import org.xacml4j.v30.Result;
import org.xacml4j.v30.marshal.MarshallingProvider;
import org.xacml4j.v30.marshal.MediaType;
import org.xacml4j.v30.marshal.json.JsonRequestContextMarshaller;
import org.xacml4j.v30.marshal.json.JsonRequestContextUnmarshaller;
import org.xacml4j.v30.marshal.json.JsonResponseContextMarshaller;
import org.xacml4j.v30.pdp.PolicyDecisionPointBuilder;
import org.xacml4j.v30.spi.pip.AttributeResolverDescriptor;
import org.xacml4j.v30.spi.pip.PolicyInformationPoint;
import org.xacml4j.v30.spi.pip.ResolverRegistry;
import org.xacml4j.v30.spi.repository.PolicyRepository;
import org.xacml4j.v30.types.Entity;
import org.xacml4j.v30.types.XacmlTypes;

/**
 * The examples of the project README as one runnable program.
 *
 * <p>Run it with {@code xacml-pdp}, {@code xacml-xml}, and {@code xacml-json} and their dependencies
 * on the class path, for example the JMH uber jar plus the JSON module and Gson:
 *
 * <pre>
 * mvn -Dlicense.skipUpdateLicense=true -DskipTests package
 * CP=xacml-jmh/target/benchmarks.jar:xacml-json/target/xacml-json-2.0.0-SNAPSHOT.jar:$GSON_JAR
 * javac -d target/example -cp "$CP" docs/examples/GettingStarted.java
 * java -cp "target/example:$CP" example.GettingStarted
 * </pre>
 *
 * <p>Expected output:
 *
 * <pre>
 * XML request: PERMIT
 * Built request: PERMIT
 * {"Result":[{"Decision":"Permit","Status":{"StatusCode":{"Value":"ok"}}}]}
 * {"ReturnPolicyIdList":false,"CombinedDecision":false,"Category":[...]}
 * JSON request: PERMIT
 * Role from the PIP: PERMIT
 * </pre>
 */
public class GettingStarted {

    /** Permits the action "read" to subjects whose role is "manager"; denies everything else. */
    static final String POLICY = """
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
        """;

    /** A manager asks to read. */
    static final String REQUEST = """
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
        """;

    static final String ROLE = "urn:example:attribute:role";
    static final String ACTION_ID = "urn:oasis:names:tc:xacml:1.0:action:action-id";

    public static void main(String[] args) throws Exception {
        // A repository holds policies; its import tool parses them with the repository's functions.
        PolicyRepository repository = PolicyRepository.build("documents").build();
        CompositeDecisionRule policy = repository.newImportTool()
                .importPolicy(MediaType.Type.XACML30_XML, () -> utf8(POLICY));

        // A PIP resolves attributes that a request does not carry; this one has no extra resolvers.
        PolicyInformationPoint pip = PolicyInformationPoint.builder("documents-pip")
                .withDefaultRegistry()
                .noCacheProvider()
                .build();

        try (PolicyDecisionPoint pdp = PolicyDecisionPointBuilder.builder("documents-pdp")
                .policyRepository(repository)
                .pip(pip)
                .rootPolicy(policy)
                .defaultRequestHandlers()
                .build()) {

            // An XACML 3.0 XML request
            MarshallingProvider xml = MarshallingProvider.getProvider(MediaType.Type.XACML30_XML).orElseThrow();
            RequestContext request = xml.newRequestUnmarshaller().orElseThrow().unmarshal(utf8(REQUEST));
            ResponseContext response = pdp.decide(request);
            for (Result result : response.getResults()) {
                System.out.println("XML request: " + result.getDecision());
            }

            // The same request, built in code
            RequestContext built = RequestContext.builder()
                    .attributes(category(CategoryId.SUBJECT_ACCESS, ROLE, "manager"),
                            category(CategoryId.ACTION, ACTION_ID, "read"))
                    .build();
            System.out.println("Built request: " + decision(pdp, built));

            // The response as JSON
            StringWriter responseJson = new StringWriter();
            new JsonResponseContextMarshaller().marshal(response, responseJson);
            System.out.println(responseJson);

            // The built request as JSON, read back and decided again
            StringWriter requestJson = new StringWriter();
            new JsonRequestContextMarshaller().marshal(built, requestJson);
            System.out.println(requestJson);
            RequestContext fromJson = new JsonRequestContextUnmarshaller()
                    .unmarshal(new StringReader(requestJson.toString()));
            System.out.println("JSON request: " + decision(pdp, fromJson));
        }

        // A PIP whose resolver supplies the subject's role when the request does not carry it
        ResolverRegistry registry = ResolverRegistry.builder()
                .withDefaultResolvers()
                .withAttributeResolver(AttributeResolverDescriptor
                        .builder("roles", "Role directory", CategoryId.SUBJECT_ACCESS)
                        .cache(300)
                        .attribute(ROLE, XacmlTypes.STRING)
                        .build(context -> Map.of(ROLE, XacmlTypes.STRING.bagOf(XacmlTypes.STRING.ofAny("manager")))))
                .build();
        PolicyInformationPoint rolePip = PolicyInformationPoint.builder("roles-pip")
                .registry(registry)
                .noCacheProvider()
                .build();
        try (PolicyDecisionPoint pdp = PolicyDecisionPointBuilder.builder("roles-pdp")
                .policyRepository(repository)
                .pip(rolePip)
                .rootPolicy(policy)
                .defaultRequestHandlers()
                .build()) {
            RequestContext withoutRole = RequestContext.builder()
                    .attributes(category(CategoryId.ACTION, ACTION_ID, "read"))
                    .build();
            System.out.println("Role from the PIP: " + decision(pdp, withoutRole));
        }

        // A repository whose policies use the strict XPath 2.0 regular-expression profile
        PolicyRepository strict = PolicyRepository.build("strict").withRegexEngine("jdk", "xf").build();
        strict.getId();
    }

    static Category category(CategoryId id, String attributeId, String value) {
        return Category.builder(id)
                .entity(Entity.builder()
                        .attribute(Attribute.builder(attributeId)
                                .value(XacmlTypes.STRING.ofAny(value))
                                .build())
                        .build())
                .build();
    }

    static Decision decision(PolicyDecisionPoint pdp, RequestContext request) {
        return pdp.decide(request).getResults().iterator().next().getDecision();
    }

    static InputStream utf8(String text) {
        return new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8));
    }
}
