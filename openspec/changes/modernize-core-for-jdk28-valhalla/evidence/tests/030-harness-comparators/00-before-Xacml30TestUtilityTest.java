package org.xacml4j.v20;

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

import static org.junit.jupiter.api.Assertions.assertThrows;

import javax.xml.bind.JAXBElement;

import org.junit.jupiter.api.Test;
import org.oasis.xacml.v30.jaxb.AttributeAssignmentType;
import org.oasis.xacml.v30.jaxb.AdviceType;
import org.oasis.xacml.v30.jaxb.AssociatedAdviceType;
import org.oasis.xacml.v30.jaxb.DecisionType;
import org.oasis.xacml.v30.jaxb.IdReferenceType;
import org.oasis.xacml.v30.jaxb.ObjectFactory;
import org.oasis.xacml.v30.jaxb.ObligationType;
import org.oasis.xacml.v30.jaxb.ObligationsType;
import org.oasis.xacml.v30.jaxb.PolicyIdentifierListType;
import org.oasis.xacml.v30.jaxb.ResponseType;
import org.oasis.xacml.v30.jaxb.ResultType;
import org.oasis.xacml.v30.jaxb.StatusCodeType;
import org.oasis.xacml.v30.jaxb.StatusType;
import org.xacml4j.v30.Decision;
import org.xacml4j.v30.ResponseContext;
import org.xacml4j.v30.Result;
import org.xacml4j.v30.xml.Xacml30ResponseContextMarshaller;

public class Xacml30TestUtilityTest
{
	@Test
	public void testAbsentPolicyIdentifierLists()
	{
		Xacml30TestUtility.assertPolicyIdentifierList(null, null);
	}

	@Test
	public void testEmptyPolicyIdentifierListMatchesAbsent()
	{
		Xacml30TestUtility.assertPolicyIdentifierList(null, new PolicyIdentifierListType());
		Xacml30TestUtility.assertPolicyIdentifierList(new PolicyIdentifierListType(), null);
	}

	@Test
	public void testSamePolicyIdentifiersWithVersions()
	{
		Xacml30TestUtility.assertPolicyIdentifierList(identifiers("policy", "1.0", "0.9", "1.1"),
				identifiers("policy", "1.0", "0.9", "1.1"));
	}

	@Test
	public void testMissingPolicyIdentifierRejected()
	{
		assertThrows(AssertionError.class, () ->
			Xacml30TestUtility.assertPolicyIdentifierList(identifiers("policy", "1.0", null, null), null));
	}

	@Test
	public void testUnexpectedPolicyIdentifierRejected()
	{
		assertThrows(AssertionError.class, () ->
			Xacml30TestUtility.assertPolicyIdentifierList(null, identifiers("policy", "1.0", null, null)));
	}

	@Test
	public void testDifferingPolicyIdRejected()
	{
		assertThrows(AssertionError.class, () ->
			Xacml30TestUtility.assertPolicyIdentifierList(identifiers("policy-a", "1.0", null, null),
					identifiers("policy-b", "1.0", null, null)));
	}

	@Test
	public void testDifferingVersionRejected()
	{
		assertThrows(AssertionError.class, () ->
			Xacml30TestUtility.assertPolicyIdentifierList(identifiers("policy", "1.0", null, null),
					identifiers("policy", "2.0", null, null)));
	}

	@Test
	public void testDifferingEarliestVersionRejected()
	{
		assertThrows(AssertionError.class, () ->
			Xacml30TestUtility.assertPolicyIdentifierList(identifiers("policy", "1.0", "0.9", null),
					identifiers("policy", "1.0", "1.0", null)));
	}

	@Test
	public void testDifferingLatestVersionRejected()
	{
		assertThrows(AssertionError.class, () ->
			Xacml30TestUtility.assertPolicyIdentifierList(identifiers("policy", "1.0", null, "1.1"),
					identifiers("policy", "1.0", null, "1.2")));
	}

	@Test
	public void testEmptyObligationsMatchAbsent()
	{
		Xacml30TestUtility.assertObligations(null, new ObligationsType());
		Xacml30TestUtility.assertObligations(new ObligationsType(), null);
	}

	@Test
	public void testMissingObligationRejected()
	{
		assertThrows(AssertionError.class, () ->
			Xacml30TestUtility.assertObligations(obligations("obligation", "value"), null));
	}

	@Test
	public void testUnexpectedObligationRejected()
	{
		assertThrows(AssertionError.class, () ->
			Xacml30TestUtility.assertObligations(null, obligations("obligation", "value")));
	}

	@Test
	public void testDifferentObligationAssignmentRejected()
	{
		assertThrows(AssertionError.class, () ->
			Xacml30TestUtility.assertObligations(obligations("obligation", "value-a"),
					obligations("obligation", "value-b")));
	}

	@Test
	public void testObligationAssignmentsWithSameAttributeIdAreUnordered()
	{
		ObligationsType expected = obligationsWithAssignments("obligation",
				assignment("same-attribute", "category", "issuer", "type", "first"),
				assignment("same-attribute", "category", "issuer", "type", "second"));
		ObligationsType permuted = obligationsWithAssignments("obligation",
				assignment("same-attribute", "category", "issuer", "type", "second"),
				assignment("same-attribute", "category", "issuer", "type", "first"));
		ObligationsType changed = obligationsWithAssignments("obligation",
				assignment("same-attribute", "category", "issuer", "type", "first"),
				assignment("same-attribute", "category", "issuer", "type", "changed"));

		Xacml30TestUtility.assertObligations(expected, permuted);
		assertThrows(AssertionError.class, () -> Xacml30TestUtility.assertObligations(expected, changed));
	}

	@Test
	public void testDuplicateObligationIdsAreComparedAsMultiset()
	{
		ObligationsType expected = obligationsWithAssignments("duplicate-id",
				assignment("attribute", "category", "issuer", "type", "first"));
		expected.getObligation().add(obligation("duplicate-id",
				assignment("attribute", "category", "issuer", "type", "second")));
		ObligationsType permuted = obligationsWithAssignments("duplicate-id",
				assignment("attribute", "category", "issuer", "type", "second"));
		permuted.getObligation().add(obligation("duplicate-id",
				assignment("attribute", "category", "issuer", "type", "first")));
		ObligationsType changedEarlierItem = obligationsWithAssignments("duplicate-id",
				assignment("attribute", "category", "issuer", "type", "changed"));
		changedEarlierItem.getObligation().add(obligation("duplicate-id",
				assignment("attribute", "category", "issuer", "type", "second")));

		Xacml30TestUtility.assertResults(result(null, expected), result(null, permuted));
		assertThrows(AssertionError.class,
				() -> Xacml30TestUtility.assertResults(result(null, expected), result(null, changedEarlierItem)));
	}

	@Test
	public void testMissingAdviceRejected()
	{
		assertAdviceDifferenceRejected(advice("advice", assignment("attribute", "category", "issuer", "type", "value")), null);
	}

	@Test
	public void testExtraAdviceRejected()
	{
		assertAdviceDifferenceRejected(null, advice("advice", assignment("attribute", "category", "issuer", "type", "value")));
	}

	@Test
	public void testDifferentAdviceIdRejected()
	{
		assertAdviceDifferenceRejected(advice("advice-a", assignment("attribute", "category", "issuer", "type", "value")),
				advice("advice-b", assignment("attribute", "category", "issuer", "type", "value")));
	}

	@Test
	public void testDifferentAdviceAssignmentFieldsRejected()
	{
		AdviceType expected = advice("advice", assignment("attribute", "category", "issuer", "type", "value"));
		assertAdviceDifferenceRejected(expected,
				advice("advice", assignment("different-attribute", "category", "issuer", "type", "value")));
		assertAdviceDifferenceRejected(expected,
				advice("advice", assignment("attribute", "different-category", "issuer", "type", "value")));
		assertAdviceDifferenceRejected(expected,
				advice("advice", assignment("attribute", "category", "different-issuer", "type", "value")));
		assertAdviceDifferenceRejected(expected,
				advice("advice", assignment("attribute", "category", "issuer", "different-type", "value")));
		assertAdviceDifferenceRejected(expected,
				advice("advice", assignment("attribute", "category", "issuer", "type", "different-value")));
	}

	@Test
	public void testAdviceAssignmentsWithSameAttributeIdAreUnordered()
	{
		AdviceType expected = advice("advice",
				assignment("same-attribute", "category", "issuer", "type", "first"),
				assignment("same-attribute", "category", "issuer", "type", "second"));
		AdviceType permuted = advice("advice",
				assignment("same-attribute", "category", "issuer", "type", "second"),
				assignment("same-attribute", "category", "issuer", "type", "first"));
		AdviceType changed = advice("advice",
				assignment("same-attribute", "category", "issuer", "type", "first"),
				assignment("same-attribute", "category", "issuer", "type", "changed"));

		Xacml30TestUtility.assertAssociatedAdvice(advice(expected), advice(permuted));
		assertThrows(AssertionError.class,
				() -> Xacml30TestUtility.assertAssociatedAdvice(advice(expected), advice(changed)));
	}

	@Test
	public void testDuplicateAdviceIdsAreComparedAsMultiset()
	{
		AdviceType first = advice("duplicate-id", assignment("attribute", "category", "issuer", "type", "first"));
		AdviceType second = advice("duplicate-id", assignment("attribute", "category", "issuer", "type", "second"));
		AdviceType changed = advice("duplicate-id", assignment("attribute", "category", "issuer", "type", "changed"));

		Xacml30TestUtility.assertResults(result(advice(first, second), null), result(advice(second, first), null));
		assertThrows(AssertionError.class,
				() -> Xacml30TestUtility.assertResults(result(advice(first, second), null),
						result(advice(changed, second), null)));
	}

	@Test
	public void testAdviceOrderDoesNotMatter()
	{
		AdviceType adviceA = advice("advice-a", assignment("attribute-a", "category", "issuer", "type", "value-a"));
		AdviceType adviceB = advice("advice-b", assignment("attribute-b", "category", "issuer", "type", "value-b"));
		Xacml30TestUtility.assertResults(result(advice(adviceA, adviceB)), result(advice(adviceB, adviceA)));
	}

	@Test
	public void testEmptyAdviceMatchesAbsent()
	{
		Xacml30TestUtility.assertResults(result(null), result(new AssociatedAdviceType()));
		Xacml30TestUtility.assertResults(result(new AssociatedAdviceType()), result(null));
	}

	@Test
	public void testMarshalledEmptyPermitMatchesAbsentOptionalCollections() throws Exception
	{
		ResultType expected = new ResultType();
		expected.setDecision(DecisionType.PERMIT);
		StatusCodeType code = new StatusCodeType();
		code.setValue("urn:oasis:names:tc:xacml:1.0:status:ok");
		StatusType status = new StatusType();
		status.setStatusCode(code);
		expected.setStatus(status);
		ResponseContext source = ResponseContext.builder()
				.result(Result.ok(Decision.PERMIT).build()).build();
		ResponseType actual = (ResponseType) ((JAXBElement<?>)
				new Xacml30ResponseContextMarshaller().marshal(source)).getValue();
		Xacml30TestUtility.assertResults(expected, actual.getResult().get(0));
	}

	private PolicyIdentifierListType identifiers(String id, String version, String earliest, String latest)
	{
		IdReferenceType reference = new IdReferenceType();
		reference.setValue(id);
		reference.setVersion(version);
		reference.setEarliestVersion(earliest);
		reference.setLatestVersion(latest);
		PolicyIdentifierListType list = new PolicyIdentifierListType();
		list.getPolicyIdReferenceOrPolicySetIdReference().add(new ObjectFactory().createPolicyIdReference(reference));
		return list;
	}

	private ObligationsType obligations(String id, String value)
	{
		AttributeAssignmentType assignment = assignment("attribute-id", null, null,
				"http://www.w3.org/2001/XMLSchema#string", value);
		return obligationsWithAssignments(id, assignment);
	}

	private ObligationsType obligationsWithAssignments(String id, AttributeAssignmentType... assignments)
	{
		ObligationType obligation = obligation(id, assignments);
		ObligationsType list = new ObligationsType();
		list.getObligation().add(obligation);
		return list;
	}

	private ObligationType obligation(String id, AttributeAssignmentType... assignments)
	{
		ObligationType obligation = new ObligationType();
		obligation.setObligationId(id);
		for (AttributeAssignmentType assignment : assignments) {
			obligation.getAttributeAssignment().add(assignment);
		}
		return obligation;
	}

	private void assertAdviceDifferenceRejected(AdviceType expectedAdvice, AdviceType actualAdvice)
	{
		assertThrows(AssertionError.class, () ->
				Xacml30TestUtility.assertResults(
						result(expectedAdvice == null ? null : advice(expectedAdvice)),
						result(actualAdvice == null ? null : advice(actualAdvice))));
	}

	private AssociatedAdviceType advice(AdviceType... advice)
	{
		if (advice == null || advice.length == 0) {
			return null;
		}
		AssociatedAdviceType associatedAdvice = new AssociatedAdviceType();
		for (AdviceType item : advice) {
			associatedAdvice.getAdvice().add(item);
		}
		return associatedAdvice;
	}

	private AdviceType advice(String id, AttributeAssignmentType... assignments)
	{
		AdviceType advice = new AdviceType();
		advice.setAdviceId(id);
		for (AttributeAssignmentType assignment : assignments) {
			advice.getAttributeAssignment().add(assignment);
		}
		return advice;
	}

	private AttributeAssignmentType assignment(String id, String category, String issuer, String dataType, String value)
	{
		AttributeAssignmentType assignment = new AttributeAssignmentType();
		assignment.setAttributeId(id);
		assignment.setCategory(category);
		assignment.setIssuer(issuer);
		assignment.setDataType(dataType);
		assignment.getContent().add(value);
		return assignment;
	}

	private ResultType result(AssociatedAdviceType advice)
	{
		return result(advice, null);
	}

	private ResultType result(AssociatedAdviceType advice, ObligationsType obligations)
	{
		ResultType result = new ResultType();
		result.setDecision(DecisionType.PERMIT);
		result.setAssociatedAdvice(advice);
		result.setObligations(obligations);
		StatusCodeType code = new StatusCodeType();
		code.setValue("urn:oasis:names:tc:xacml:1.0:status:ok");
		StatusType status = new StatusType();
		status.setStatusCode(code);
		result.setStatus(status);
		return result;
	}
}
