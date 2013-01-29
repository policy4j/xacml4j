package org.xacml4j.v30.marshall.jaxb.builder;

import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.JAXBElement;

import org.oasis.xacml.v30.jaxb.ApplyType;
import org.oasis.xacml.v30.jaxb.AttributeDesignatorType;
import org.oasis.xacml.v30.jaxb.AttributeSelectorType;
import org.oasis.xacml.v30.jaxb.AttributeValueType;
import org.oasis.xacml.v30.jaxb.FunctionType;
import org.oasis.xacml.v30.jaxb.ObjectFactory;
import org.oasis.xacml.v30.jaxb.VariableReferenceType;
import org.xacml4j.v30.AttributeDesignatorKey;
import org.xacml4j.v30.AttributeExp;
import org.xacml4j.v30.AttributeSelectorKey;
import org.xacml4j.v30.Expression;
import org.xacml4j.v30.pdp.Apply;
import org.xacml4j.v30.pdp.AttributeDesignator;
import org.xacml4j.v30.pdp.AttributeSelector;
import org.xacml4j.v30.pdp.FunctionReference;
import org.xacml4j.v30.pdp.VariableReference;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;

public abstract class ExpressionTypeBuilder
{
	private final static ObjectFactory factory = new ObjectFactory();
	private static ImmutableMap<Class<? extends Expression>, Maker> builders;
	
	public abstract void from(Expression e);
 	public abstract JAXBElement<?> build();
 	
 	static
 	{
 		ImmutableMap.Builder<Class<? extends Expression>, Maker> b = ImmutableMap.builder();
 		b.put(Apply.class, new Maker(){
			@Override
			public ExpressionTypeBuilder make() {
				return new ApplyTypeBuilder();
			}
 		});
 		b.put(AttributeDesignator.class, new Maker(){
			@Override
			public ExpressionTypeBuilder make() {
				return new AttributeDesignatorTypeBuilder();
			}
 		});
 		b.put(AttributeSelector.class, new Maker(){
			@Override
			public ExpressionTypeBuilder make() {
				return new AttributeSelectorTypeBuilder();
			}
 		});
 		b.put(VariableReference.class, new Maker(){
			@Override
			public ExpressionTypeBuilder make() {
				return new VariableReferenceTypeBuilder();
			}
 		});
 		b.put(FunctionReference.class, new Maker(){
			@Override
			public ExpressionTypeBuilder make() {
				return new FunctionTypeBuilder();
			}
 		});
 		b.put(AttributeExp.class, new Maker(){
			@Override
			public ExpressionTypeBuilder make() {
				return new AttributeValueTypeBuilder();
			}
 		});
 		builders = b.build();
 	}
 	
 	public static ExpressionTypeBuilder getBuilder(Expression exp){
 		Maker m = builders.get(exp);
 		return (m != null)?m.make():null;
 	}

 	static class ApplyTypeBuilder extends ExpressionTypeBuilder
 	{
 		private String functionId;
 		private List<JAXBElement<?>> params = new LinkedList<JAXBElement<?>>();

 		private ApplyTypeBuilder(){
 		}

 		@Override
		public void from(Expression e)
 		{
 			Preconditions.checkArgument(e instanceof Apply);
 			Apply apply = (Apply)e;
 			this.functionId = apply.getFunctionId();
 			for(Expression exp : apply.getArguments()){
 				ExpressionTypeBuilder builder = getBuilder(exp);
 				builder.from(exp);
 				params.add(builder.build());
 			}
 		}

		@Override
		public JAXBElement<?> build() {
			ApplyType exp = new ApplyType();
			exp.setFunctionId(functionId);
			exp.getExpression().addAll(params);
			return factory.createApply(exp);
		}
 	}
	
 	static class VariableReferenceTypeBuilder extends ExpressionTypeBuilder
 	{
 		private String variableId;

 		private VariableReferenceTypeBuilder(){
 		}

 		@Override
		public void from(Expression e){
 			Preconditions.checkArgument(e instanceof VariableReference);
 			VariableReference ref = (VariableReference)e;
 			this.variableId = ref.getVariableId();
 		}

		@Override
		public JAXBElement<?> build() {
			VariableReferenceType exp = new VariableReferenceType();
			exp.setVariableId(variableId);
			return factory.createVariableReference(exp);
		}
 	}
 	
 	public static class FunctionTypeBuilder extends ExpressionTypeBuilder
 	{
 		private String functionId;

 		@Override
		public void from(Expression e){
 			Preconditions.checkArgument(e instanceof FunctionReference);
 			FunctionReference ref = (FunctionReference)e;
 			this.functionId = ref.getFunctionId();
 		}

		@Override
		public JAXBElement<?> build() {
			FunctionType exp = new FunctionType();
			exp.setFunctionId(functionId);
			return factory.createFunction(exp);
		}
 	}
 	
 	static class AttributeValueTypeBuilder extends ExpressionTypeBuilder
 	{
 		private String dataTypeId;
 		private String value;

 		@Override
		public void from(Expression e){
 			Preconditions.checkArgument(e instanceof AttributeExp);
 			AttributeExp v = (AttributeExp)e;
 			this.dataTypeId = v.getType().getDataTypeId();
 			this.value = v.toXacmlString();
 		}

		@Override
		public JAXBElement<?> build() {
			AttributeValueType exp = new AttributeValueType();
			exp.setDataType(dataTypeId);
			exp.getContent().add(value);
			return factory.createAttributeValue(exp);
		}
 	}

	static class AttributeDesignatorTypeBuilder extends ExpressionTypeBuilder
	{
		private String attributeId;
		private String category;
		private String issuer;
		private String dataType;
		private boolean mustBePresent = false;
		
		@Override
		public void from(Expression e){
			Preconditions.checkArgument(e instanceof AttributeDesignator);
			AttributeDesignator d = (AttributeDesignator)e;
			AttributeDesignatorKey k = d.getReferenceKey();
			this.attributeId = k.getAttributeId();
			this.category = k.getCategory().getId();
			this.dataType = k.getDataType().getDataTypeId();
			this.issuer = k.getIssuer();
			this.mustBePresent = d.isMustBePresent();
		}

		@Override
		public JAXBElement<?> build() {
			AttributeDesignatorType exp = new AttributeDesignatorType();
			exp.setAttributeId(attributeId);
			exp.setCategory(category);
			exp.setIssuer(issuer);
			exp.setDataType(dataType);
			exp.setMustBePresent(mustBePresent);
			return factory.createAttributeDesignator(exp);
		}
	}
	
	public static class AttributeSelectorTypeBuilder extends ExpressionTypeBuilder
	{
		private String xpath;
		private String category;
		private String contextSelectorId;
		private String dataType;
		private boolean mustBePresent = false;
		
		@Override
		public void from(Expression e){
			Preconditions.checkArgument(e instanceof AttributeSelector);
			AttributeSelector s = (AttributeSelector)e;
			AttributeSelectorKey k = s.getReferenceKey();
			this.xpath = k.getPath();
			this.category = k.getCategory().getId();
			this.dataType = k.getDataType().getDataTypeId();
			this.contextSelectorId = k.getContextSelectorId();
			this.mustBePresent = s.isMustBePresent();
		}

		@Override
		public JAXBElement<?> build() {
			AttributeSelectorType exp = new AttributeSelectorType();
			exp.setPath(xpath);
			exp.setCategory(category);
			exp.setDataType(dataType);
			exp.setMustBePresent(mustBePresent);
			exp.setContextSelectorId(contextSelectorId);
			return factory.createAttributeSelector(exp);
		}
	}
	
	public interface Maker
	{
		ExpressionTypeBuilder make();
	}
}
