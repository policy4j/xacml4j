/*
 * #%L
 * Xacml4J Policy Unit Test Support
 * %%
 * Copyright (C) 2009 - 2014 Xacml4J.org
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

import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.List;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

import org.oasis.xacml.v30.jaxb.AttributeAssignmentType;
import org.oasis.xacml.v30.jaxb.ResponseType;
import org.xacml4j.v20.Xacml30TestUtility;
import org.xacml4j.v30.ResponseContext;
import org.xacml4j.v30.PolicyDecisionPoint;
import org.xacml4j.v30.xml.Xacml30ResponseContextMarshaller;

/** Evidence-only probe: reproduce the failed cases and inspect assignment metadata. */
public class AssignmentMetadataProbe {
    public static void main(String[] args) throws Exception {
        Class<?> manifest = Class.forName("org.xacml4j.v20.ConformanceManifest");
        Method read = manifest.getDeclaredMethod("read", Path.class);
        read.setAccessible(true);
        List<?> cases = (List<?>) read.invoke(null, Path.of(args[0]));
        Class<?> runner = Class.forName("org.xacml4j.v20.ConformanceCaseRunner");
        QName xpathCategory = new QName("XPathCategory");
        for (String id : List.of("IIIA030", "IIIA330")) {
            Object testCase = cases.stream().filter(c -> c.toString().equals("oasis30/" + id))
                    .findFirst().orElseThrow();
            Method buildPdp = runner.getDeclaredMethod("buildPdp", testCase.getClass());
            buildPdp.setAccessible(true);
            PolicyDecisionPoint pdp = (PolicyDecisionPoint) buildPdp.invoke(null, testCase);
            ResponseContext response = pdp.decide(Xacml30TestUtility.getRequest(
                    "xacml3.0-ct-v.0.4/" + id + "Request.xml"));
            @SuppressWarnings("unchecked")
            ResponseType actual = ((JAXBElement<ResponseType>) new Xacml30ResponseContextMarshaller()
                    .marshal(response)).getValue();
            ResponseType expected = Xacml30TestUtility.getResponse(
                    "xacml3.0-ct-v.0.4/" + id + "Response.xml");
            List<AttributeAssignmentType> ea = assignments(expected, id);
            List<AttributeAssignmentType> aa = assignments(actual, id);
            AttributeAssignmentType e = ea.get(1);
            AttributeAssignmentType a = aa.stream().filter(v -> v.getAttributeId().equals(e.getAttributeId()))
                    .findFirst().orElseThrow();
            System.out.println("METADATA oasis30/" + id + " attribute=" + e.getAttributeId()
                    + " expected=" + e.getOtherAttributes() + " actual=" + a.getOtherAttributes());
            if (!"urn:oasis:names:tc:xacml:3.0:attribute-category:resource".equals(
                    e.getOtherAttributes().get(xpathCategory)) || !a.getOtherAttributes().isEmpty()) {
                throw new AssertionError("Expected XPathCategory loss was not reproduced: " + id);
            }
        }
    }

    private static List<AttributeAssignmentType> assignments(ResponseType response, String id) {
        var result = response.getResult().get(0);
        return id.equals("IIIA030") ? result.getObligations().getObligation().get(0).getAttributeAssignment()
                : result.getAssociatedAdvice().getAdvice().get(0).getAttributeAssignment();
    }
}
