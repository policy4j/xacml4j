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
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilderFactory;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.oasis.xacml.v30.jaxb.AttributeType;
import org.oasis.xacml.v30.jaxb.AttributeValueType;
import org.oasis.xacml.v30.jaxb.AttributesType;
import org.oasis.xacml.v30.jaxb.ContentType;
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
	@ParameterizedTest
	@CsvSource({"double,27.50,27.5", "double,1E2,100.0", "integer,1,01", "integer,-0,0",
			"boolean,true,1", "boolean,false,0"})
	public void lexicalFormsCompareByDataType(String type, String expected, String actual)
	{
		Xacml30TestUtility.assertAttributeValues(List.of(typedValue(type, expected)), List.of(typedValue(type, actual)));
	}

	@ParameterizedTest
	@CsvSource({"double,27.5,27.6", "integer,1,2", "boolean,true,false", "boolean,false,1", "string,abc,ABC"})
	public void differentValuesOfTheSameTypeRemainRejected(String type, String expected, String actual)
	{
		assertThrows(AssertionError.class, () -> Xacml30TestUtility.assertAttributeValues(
				List.of(typedValue(type, expected)), List.of(typedValue(type, actual))));
	}

	@Test
	public void invalidBooleanLexicalFormsAreRejected()
	{
		assertThrows(AssertionError.class, () -> Xacml30TestUtility.assertAttributeValues(
				List.of(typedValue("boolean", "invalid")), List.of(typedValue("boolean", "invalid"))));
	}

	@Test
	public void stringsKeepWhitespaceAndTextChunkBoundariesDoNotMatter()
	{
		AttributeValueType chunks = typedValue("string", "ab");
		chunks.getContent().add("c");
		Xacml30TestUtility.assertAttributeValues(List.of(chunks), List.of(typedValue("string", "abc")));
		assertThrows(AssertionError.class, () -> Xacml30TestUtility.assertAttributeValues(
				List.of(typedValue("string", " abc")), List.of(typedValue("string", "abc"))));
	}

	@Test
	public void valueBagsAreUnorderedButKeepDuplicatesAndMetadata()
	{
		Xacml30TestUtility.assertAttributeValues(List.of(typedValue("integer", "1"), typedValue("integer", "2")),
				List.of(typedValue("integer", "2"), typedValue("integer", "01")));
		assertThrows(AssertionError.class, () -> Xacml30TestUtility.assertAttributeValues(
				List.of(typedValue("integer", "1"), typedValue("integer", "1")),
				List.of(typedValue("integer", "1"), typedValue("integer", "2"))));
		assertThrows(AssertionError.class, () -> Xacml30TestUtility.assertAttributeValues(
				List.of(typedValue("integer", "1")), List.of(typedValue("double", "1"))));
		AttributeValueType metadata = typedValue("integer", "1");
		metadata.getOtherAttributes().put(new QName("urn:test", "metadata"), "value");
		assertThrows(AssertionError.class, () -> Xacml30TestUtility.assertAttributeValues(
				List.of(metadata), List.of(typedValue("integer", "1"))));
	}

	@Test
	public void absentContentIsSafeAndPresenceIsComparedSymmetrically()
	{
		AttributesType absent = included("category", "abc");
		AttributesType present = included("category", "abc");
		present.setContent(new ContentType());
		Xacml30TestUtility.assertResults(withAttributes(absent), withAttributes(included("category", "abc")));
		assertThrows(AssertionError.class, () -> Xacml30TestUtility.assertAttributesType(List.of(absent), List.of(present)));
		assertThrows(AssertionError.class, () -> Xacml30TestUtility.assertAttributesType(List.of(present), List.of(absent)));
	}

	@Test
	public void includedCategoriesAttributesAndValuesAreMultisets()
	{
		AttributesType first = included("first-category", "abc");
		AttributesType second = included("second-category", "def");
		Xacml30TestUtility.assertResults(withAttributes(first, second), withAttributes(second, first));
		AttributesType repeatedFirst = included("first-category", "changed");
		assertThrows(AssertionError.class, () -> Xacml30TestUtility.assertAttributesType(
				List.of(first, first), List.of(first, repeatedFirst)));
		AttributesType sameCategoryOtherValue = included("first-category", "other");
		Xacml30TestUtility.assertAttributesType(List.of(first, sameCategoryOtherValue), List.of(sameCategoryOtherValue, first));
		first.getAttribute().add(included("ignored", "other").getAttribute().get(0));
		AttributesType permuted = included("first-category", "other");
		permuted.getAttribute().add(included("ignored", "abc").getAttribute().get(0));
		Xacml30TestUtility.assertAttributesType(List.of(first), List.of(permuted));
	}

	@ParameterizedTest
	@ValueSource(strings = {"category", "reference-id", "attribute-id", "issuer", "include-in-result",
			"value", "data-type", "attribute-count"})
	public void categoryMatchingKeepsAllAttributeFields(String field)
	{
		AttributesType expected = included("category", "abc");
		AttributesType actual = included("category", "abc");
		switch (field) {
		case "category" -> actual.setCategory("different");
		case "reference-id" -> actual.setId("different");
		case "attribute-id" -> actual.getAttribute().get(0).setAttributeId("different");
		case "issuer" -> actual.getAttribute().get(0).setIssuer("different");
		case "include-in-result" -> actual.getAttribute().get(0).setIncludeInResult(false);
		case "value" -> actual.getAttribute().get(0).getAttributeValue().get(0).getContent().set(0, "ABC");
		case "data-type" -> actual.getAttribute().get(0).getAttributeValue().get(0).setDataType("urn:test:type");
		case "attribute-count" -> actual.getAttribute().clear();
		default -> throw new AssertionError(field);
		}
		assertThrows(AssertionError.class, () -> Xacml30TestUtility.assertAttributesType(List.of(expected), List.of(actual)));
	}

	@Test
	public void contentComparesTypeAndDomStructure() throws Exception
	{
		var element = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument().createElement("record");
		element.setTextContent("abc");
		AttributesType expected = included("category", "abc");
		expected.setContent(new ContentType());
		expected.getContent().getContent().add(element);
		AttributesType actual = included("category", "abc");
		actual.setContent(new ContentType());
		actual.getContent().getContent().add(element.cloneNode(true));
		Xacml30TestUtility.assertAttributesType(List.of(expected), List.of(actual));
		actual.getContent().setType("application/json");
		assertThrows(AssertionError.class, () -> Xacml30TestUtility.assertAttributesType(List.of(expected), List.of(actual)));
		actual.getContent().setType("application/xml");
		actual.getContent().getContent().clear();
		assertThrows(AssertionError.class, () -> Xacml30TestUtility.assertAttributesType(List.of(expected), List.of(actual)));
	}

	@Test
	public void obligationAndAdviceAssignmentsAlsoUseTypedValues()
	{
		AttributeAssignmentType expected = assignment("attribute", "category", "issuer",
				"http://www.w3.org/2001/XMLSchema#double", "27.50");
		AttributeAssignmentType same = assignment("attribute", "category", "issuer",
				"http://www.w3.org/2001/XMLSchema#double", "27.5");
		AttributeAssignmentType different = assignment("attribute", "category", "issuer",
				"http://www.w3.org/2001/XMLSchema#double", "27.6");
		Xacml30TestUtility.assertAttributeAssignment(expected, same);
		Xacml30TestUtility.assertResults(result(advice(advice("advice", expected)), obligationsWithAssignments("obligation", expected)),
				result(advice(advice("advice", same)), obligationsWithAssignments("obligation", same)));
		assertThrows(AssertionError.class, () -> Xacml30TestUtility.assertAttributeAssignment(expected, different));
	}

	@ParameterizedTest
	@ValueSource(strings = {"direct", "obligation", "advice"})
	public void assignmentExtensionAttributesMustMatch(String path)
	{
		AttributeAssignmentType expected = assignment("attribute", "category", "issuer", "type", "value");
		AttributeAssignmentType actual = assignment("attribute", "category", "issuer", "type", "value");
		QName metadata = new QName("urn:test:metadata", "name");
		expected.getOtherAttributes().put(metadata, "first");
		actual.getOtherAttributes().put(metadata, "first");
		Runnable compare = switch (path) {
		case "direct" -> () -> Xacml30TestUtility.assertAttributeAssignment(expected, actual);
		case "obligation" -> () -> Xacml30TestUtility.assertResults(
				result(null, obligationsWithAssignments("obligation", expected)),
				result(null, obligationsWithAssignments("obligation", actual)));
		case "advice" -> () -> Xacml30TestUtility.assertResults(
				result(advice(advice("advice", expected))), result(advice(advice("advice", actual))));
		default -> throw new AssertionError(path);
		};
		compare.run();
		actual.getOtherAttributes().put(metadata, "changed");
		assertThrows(AssertionError.class, compare::run);
		actual.getOtherAttributes().clear();
		assertThrows(AssertionError.class, compare::run);
		actual.getOtherAttributes().put(new QName("urn:test:different", "name"), "first");
		assertThrows(AssertionError.class, compare::run);
	}

	private AttributeValueType typedValue(String type, String lexical)
	{
		AttributeValueType value = new AttributeValueType();
		value.setDataType("http://www.w3.org/2001/XMLSchema#" + type);
		value.getContent().add(lexical);
		return value;
	}

	private AttributesType included(String category, String lexical)
	{
		AttributeType attribute = new AttributeType();
		attribute.setAttributeId("attribute");
		attribute.setIssuer("issuer");
		attribute.setIncludeInResult(true);
		attribute.getAttributeValue().add(typedValue("string", lexical));
		AttributesType attributes = new AttributesType();
		attributes.setCategory(category);
		attributes.getAttribute().add(attribute);
		return attributes;
	}

	private ResultType withAttributes(AttributesType... attributes)
	{
		ResultType result = result(null, null);
		java.util.Collections.addAll(result.getAttributes(), attributes);
		return result;
	}

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
