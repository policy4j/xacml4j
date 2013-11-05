package org.xacml4j.v30.marshal.jaxb;

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

abstract class ExpressionTypeBuilder
{
	private final static ObjectFactory factory = new ObjectFactory();
	private static ImmutableMap<Class<? extends Expression>, ExpressionTypeBuilder> builders = ImmutableMap.of(
			Apply.class, new ApplyTypeBuilder(),
			AttributeDesignator.class, new AttributeDesignatorTypeBuilder(),
			AttributeSelector.class, new AttributeSelectorTypeBuilder(),
			VariableReference.class, new VariableReferenceTypeBuilder(),
			FunctionReference.class, new FunctionTypeBuilder());

 	public abstract JAXBElement<?> from(Expression e);

 	public static void addBuilder(Class<? extends Expression> expType, ExpressionTypeBuilder expBuilder){
		Preconditions.checkNotNull(expBuilder);
		ImmutableMap.Builder<Class<? extends Expression>, ExpressionTypeBuilder> b = ImmutableMap.builder();
		b.putAll(builders);
		b.put(expType, expBuilder);
		builders = b.build();
 	}

 	public static ExpressionTypeBuilder getBuilder(Expression exp){
 		Preconditions.checkNotNull(exp);
 		if(exp instanceof AttributeExp){
 			return new AttributeValueTypeBuilder();
 		}
 		ExpressionTypeBuilder b = builders.get(exp.getClass());
 		Preconditions.checkArgument(b != null,
 				"Failed to find builder for expressio=\"%s\"", exp.getClass().getName());
 		return (b != null)?b:null;
 	}

 	static class ApplyTypeBuilder extends ExpressionTypeBuilder
 	{
 		@Override
		public JAXBElement<?> from(Expression e)
 		{
 			Preconditions.checkArgument(e instanceof Apply);
 			Apply apply = (Apply)e;
 			ApplyType applyJaxb = new ApplyType();
 			applyJaxb.setFunctionId(apply.getFunctionId());
 			for(Expression exp : apply.getArguments()){
 				ExpressionTypeBuilder builder = getBuilder(exp);
 				builder.from(exp);
 				applyJaxb.getExpression().add(builder.from(exp));
 			}
 			return factory.createApply(applyJaxb);
 		}
 	}

 	static class VariableReferenceTypeBuilder extends ExpressionTypeBuilder
 	{

 		@Override
		public JAXBElement<?> from(Expression e){
 			Preconditions.checkArgument(e instanceof VariableReference);
 			VariableReference ref = (VariableReference)e;
 			VariableReferenceType exp = factory.createVariableReferenceType();
 			exp.setVariableId(ref.getVariableId());
 			return factory.createVariableReference(exp);
 		}
 	}

 	public static class FunctionTypeBuilder extends ExpressionTypeBuilder
 	{
 		@Override
		public JAXBElement<?> from(Expression e){
 			Preconditions.checkArgument(e instanceof FunctionReference);
 			FunctionReference ref = (FunctionReference)e;
 			FunctionType exp = factory.createFunctionType();
			exp.setFunctionId(ref.getFunctionId());
			return factory.createFunction(exp);
 		}
 	}

 	static class AttributeValueTypeBuilder extends ExpressionTypeBuilder
 	{
 		@Override
		public JAXBElement<?> from(Expression e){
 			Preconditions.checkArgument(e instanceof AttributeExp);
 			AttributeExp v = (AttributeExp)e;
 			AttributeValueType exp = factory.createAttributeValueType();
 			exp.setDataType(v.getType().getDataTypeId());
			exp.getContent().add(v.getValue());
			return factory.createAttributeValue(exp);
 		}
 	}

	static class AttributeDesignatorTypeBuilder extends ExpressionTypeBuilder
	{
		@Override
		public JAXBElement<?> from(Expression e){
			Preconditions.checkArgument(e instanceof AttributeDesignator);
			AttributeDesignator d = (AttributeDesignator)e;
			AttributeDesignatorKey k = d.getReferenceKey();
			AttributeDesignatorType exp = factory.createAttributeDesignatorType();
			exp.setAttributeId(k.getAttributeId());
			exp.setCategory(k.getCategory().getId());
			exp.setIssuer(k.getIssuer());
			exp.setDataType(k.getDataType().getDataTypeId());
			exp.setMustBePresent(d.isMustBePresent());
			return factory.createAttributeDesignator(exp);
		}
	}

	public static class AttributeSelectorTypeBuilder extends ExpressionTypeBuilder
	{
		@Override
		public JAXBElement<?> from(Expression e){
			Preconditions.checkArgument(e instanceof AttributeSelector);
			AttributeSelector s = (AttributeSelector)e;
			AttributeSelectorType exp = factory.createAttributeSelectorType();
			AttributeSelectorKey k = s.getReferenceKey();
			exp.setPath(k.getPath());
			exp.setCategory(k.getCategory().getId());
			exp.setDataType(k.getDataType().getDataTypeId());
			exp.setMustBePresent(s.isMustBePresent());
			exp.setContextSelectorId(k.getContextSelectorId());
			return factory.createAttributeSelector(exp);
		}
	}
}
