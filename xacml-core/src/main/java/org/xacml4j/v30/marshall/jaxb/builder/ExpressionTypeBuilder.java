package org.xacml4j.v30.marshall.jaxb.builder;

import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.JAXBElement;

import org.oasis.xacml.v30.jaxb.ApplyType;
import org.oasis.xacml.v30.jaxb.AttributeDesignatorType;
import org.oasis.xacml.v30.jaxb.ExpressionType;
import org.oasis.xacml.v30.jaxb.VariableReferenceType;
import org.xacml4j.v30.pdp.Apply;
import org.xacml4j.v30.pdp.AttributeDesignator;
import org.xacml4j.v30.pdp.VariableReference;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

public interface ExpressionTypeBuilder
{
 	ExpressionType build();
 	JAXBElement<?> buildJaxbElement();


 	public class ApplyTypeBuilder extends JAXBObjectBuilder
 		implements ExpressionTypeBuilder
 	{
 		private String functionId;
 		private List<JAXBElement<?>> params = new LinkedList<JAXBElement<?>>();

 		private ApplyTypeBuilder(){
 		}

 		public ApplyTypeBuilder from(Apply apply){
 			this.functionId = apply.getFunctionId();
 			return this;
 		}

 		public ApplyTypeBuilder functionId(String functionId){
 			Preconditions.checkArgument(!Strings.isNullOrEmpty(functionId));
 			this.functionId = functionId;
 			return this;
 		}

 		public static ApplyTypeBuilder newBuilder(){
 			return new ApplyTypeBuilder();
 		}

 		public ApplyTypeBuilder expression(ExpressionTypeBuilder exp)
 		{
 			JAXBElement<?> p = exp.buildJaxbElement();
 			Preconditions.checkState(p != null && p.getValue() != null);
 			this.params.add(p);
 			return this;
 		}

		@Override
		public ApplyType build() {
			ApplyType exp = new ApplyType();
			exp.setFunctionId(functionId);
			exp.getExpression().addAll(params);
			return exp;
		}

		public JAXBElement<?> buildJaxbElement(){
			return factory.createApply(build());
		}
 	}
	public class VariableReferenceTypeBuilder extends JAXBObjectBuilder
			implements ExpressionTypeBuilder
 	{
 		private String variableId;

 		private VariableReferenceTypeBuilder(){
 		}

 		public VariableReferenceTypeBuilder variableId(String variableId){
 			Preconditions.checkArgument(!Strings.isNullOrEmpty(variableId));
 			this.variableId = variableId;
 			return this;
 		}

 		public static VariableReferenceTypeBuilder newBuilder(){
 			return new VariableReferenceTypeBuilder();
 		}

 		public static VariableReferenceTypeBuilder newBuilder(VariableReference v){
 			return new VariableReferenceTypeBuilder()
 			.variableId(v.getVariableId());
 		}

		@Override
		public VariableReferenceType build() {
			VariableReferenceType exp = new VariableReferenceType();
			Preconditions.checkState(variableId != null, "Variable reference identifier can't be null");
			exp.setVariableId(variableId);
			return exp;
		}

		public JAXBElement<?> buildJaxbElement(){
			return factory.createVariableReference(build());
		}
 	}

	public class AttributeDesignatorTypeBuilder extends JAXBObjectBuilder
		implements ExpressionTypeBuilder
	{
		private String attributeId;
		private String category;
		private String issuer;
		private String dataType;
		private boolean mustBePresent = false;

		private AttributeDesignatorTypeBuilder(){

		}

		public AttributeDesignatorTypeBuilder attributeId(String attributeId){
			Preconditions.checkArgument(!Strings.isNullOrEmpty(attributeId));
			this.attributeId = attributeId;
			return this;
		}

		public AttributeDesignatorTypeBuilder category(String category){
			Preconditions.checkArgument(!Strings.isNullOrEmpty(category));
			this.category = category;
			return this;
		}

		public AttributeDesignatorTypeBuilder dataType(String dataType){
			Preconditions.checkArgument(!Strings.isNullOrEmpty(dataType));
			this.dataType = dataType;
			return this;
		}

		public AttributeDesignatorTypeBuilder issuer(String issuer){
			Preconditions.checkArgument(!Strings.isNullOrEmpty(issuer));
			this.issuer = issuer;
			return this;
		}

		public AttributeDesignatorTypeBuilder mustBePresent(boolean mustBePresent){
			this.mustBePresent = mustBePresent;
			return this;
		}

		public static AttributeDesignatorTypeBuilder newBuilder(){
			return new AttributeDesignatorTypeBuilder();
		}

		public static AttributeDesignatorTypeBuilder newBuilder(AttributeDesignator d){
			return new AttributeDesignatorTypeBuilder()
			.attributeId(d.getAttributeId())
			.category(d.getCategory().toString())
			.dataType(d.getDataType().getDataTypeId())
			.issuer(d.getIssuer())
			.mustBePresent(d.isMustBePresent());
		}

		@Override
		public AttributeDesignatorType build() {
			AttributeDesignatorType exp = new AttributeDesignatorType();
			Preconditions.checkState(attributeId != null, "Attribute identifier can't be null");
			exp.setAttributeId(attributeId);
			Preconditions.checkState(category != null, "Attribute category can't be null");
			exp.setCategory(category);
			exp.setIssuer(issuer);
			Preconditions.checkState(dataType != null, "Attribute dataType can't be null");
			exp.setDataType(dataType);
			exp.setMustBePresent(mustBePresent);
			return exp;
		}

		public JAXBElement<?> buildJaxbElement(){
			return factory.createAttributeDesignator(build());
		}
	}
}
