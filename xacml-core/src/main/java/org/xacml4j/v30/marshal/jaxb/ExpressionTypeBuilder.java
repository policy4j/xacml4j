package org.xacml4j.v30.marshal.jaxb;

/*
 * #%L
 * Xacml4J Core Engine Implementation
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

interface ExpressionTypeBuilder
{

 	JAXBElement<?> from(Expression e);

 	public enum Expressions implements ExpressionTypeBuilder
 	{
 		APPLY(Apply.class){
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
 		},
 		VARIABLE_REF(VariableReference.class){
 			@Override
 			public JAXBElement<?> from(Expression e){
 	 			Preconditions.checkArgument(e instanceof VariableReference);
 	 			VariableReference ref = (VariableReference)e;
 	 			VariableReferenceType exp = factory.createVariableReferenceType();
 	 			exp.setVariableId(ref.getVariableId());
 	 			return factory.createVariableReference(exp);
 	 		}
 		},
 		FUNCTION_REF(FunctionReference.class){
 			@Override
 			public JAXBElement<?> from(Expression e){
 	 			Preconditions.checkArgument(e instanceof FunctionReference);
 	 			FunctionReference ref = (FunctionReference)e;
 	 			FunctionType exp = factory.createFunctionType();
 				exp.setFunctionId(ref.getFunctionId());
 				return factory.createFunction(exp);
 	 		}
 		},
 		ATTRIBUTE(AttributeExp.class){
 			@Override
 			public JAXBElement<?> from(Expression e){
 	 			Preconditions.checkArgument(e instanceof AttributeExp);
 	 			AttributeExp v = (AttributeExp)e;
 	 			AttributeValueType exp = factory.createAttributeValueType();
 	 			exp.setDataType(v.getType().getDataTypeId());
 				exp.getContent().add(v.getValue());
 				return factory.createAttributeValue(exp);
 	 		}
 		},
 		ATTRIBUTE_DESIGNATOR(AttributeDesignator.class){
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
 		},
 		ATTRIBUTE_SELECTOR(AttributeSelector.class){
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

 		};

 		private static ImmutableMap<Class<? extends Expression>, ExpressionTypeBuilder> EXPRESSIONS;
 		static
 		{
 			ImmutableMap.Builder<Class<? extends Expression>, ExpressionTypeBuilder> b = ImmutableMap.builder();
 			for(Expressions exp : values()){
 				b.put(exp.getExpressionClass(), exp);
 			}
 			EXPRESSIONS = b.build();
 		}

 		private final static ObjectFactory factory = new ObjectFactory();
 		private Class<? extends Expression> expClass;

 		private Expressions(Class<? extends Expression> expClass){
 			this.expClass = expClass;
 		}

 		public Class<? extends Expression> getExpressionClass(){
 			return expClass;
 		}

 		public static ExpressionTypeBuilder getBuilder(Expression exp)
 	 	{
 	 		Preconditions.checkNotNull(exp);
 	 		if(exp instanceof AttributeExp){
 	 			return Expressions.ATTRIBUTE;
 	 		}
 	 		ExpressionTypeBuilder b = EXPRESSIONS.get(exp.getClass());
 	 		Preconditions.checkArgument(b != null,
 	 				"Failed to find builder for expression=\"%s\"", exp.getClass().getName());
 	 		return (b != null)?b:null;
 	 	}
 	}

}
