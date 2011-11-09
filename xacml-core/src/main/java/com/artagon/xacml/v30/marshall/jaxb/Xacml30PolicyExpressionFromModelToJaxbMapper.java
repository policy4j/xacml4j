package com.artagon.xacml.v30.marshall.jaxb;

import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import javax.xml.bind.JAXBElement;

import org.oasis.xacml.v30.jaxb.ApplyType;
import org.oasis.xacml.v30.jaxb.AttributeDesignatorType;
import org.oasis.xacml.v30.jaxb.AttributeSelectorType;
import org.oasis.xacml.v30.jaxb.AttributeValueType;
import org.oasis.xacml.v30.jaxb.FunctionType;
import org.oasis.xacml.v30.jaxb.ObjectFactory;
import org.oasis.xacml.v30.jaxb.VariableReferenceType;

import com.artagon.xacml.v30.pdp.Apply;
import com.artagon.xacml.v30.pdp.AttributeDesignator;
import com.artagon.xacml.v30.pdp.AttributeExp;
import com.artagon.xacml.v30.pdp.AttributeSelector;
import com.artagon.xacml.v30.pdp.DefaultExpressionVisitor;
import com.artagon.xacml.v30.pdp.Expression;
import com.artagon.xacml.v30.pdp.FunctionReference;
import com.artagon.xacml.v30.pdp.VariableReference;

final class Xacml30PolicyExpressionFromModelToJaxbMapper 
	extends DefaultExpressionVisitor
{
	private ObjectFactory objectFactory;
	private Stack<List<JAXBElement<?>>> argStack;
	private Stack<JAXBElement<?>> expStack;
	
	public Xacml30PolicyExpressionFromModelToJaxbMapper()
	{
		this.argStack = new Stack<List<JAXBElement<?>>>();
		this.expStack = new Stack<JAXBElement<?>>();
		this.objectFactory = new ObjectFactory();
	}
	
	public JAXBElement<?> create(Expression exp)
	{
		exp.accept(this);
		return expStack.pop();
	}
	
	@Override
	public void visitEnter(AttributeExp v) 
	{
		AttributeValueType exp = objectFactory.createAttributeValueType();
		exp.setDataType(v.getType().getDataTypeId());
		exp.getContent().add(v.toXacmlString());
		add(objectFactory.createAttributeValue(exp));
	}
	
	@Override
	public void visitEnter(FunctionReference v) 
	{
		FunctionType exp = new FunctionType();
		exp.setFunctionId(v.getFunctionId());
		add(objectFactory.createFunction(exp));
	}

	@Override
	public void visitEnter(AttributeDesignator v) {
		AttributeDesignatorType exp = objectFactory.createAttributeDesignatorType();
		exp.setAttributeId(v.getAttributeId());
		exp.setCategory(v.getCategory().getId());
		exp.setDataType(v.getDataType().getDataTypeId());
		exp.setMustBePresent(v.isMustBePresent());
		exp.setIssuer(v.getIssuer());
		add(objectFactory.createAttributeDesignator(exp));
	}



	@Override
	public void visitEnter(AttributeSelector v) {
		AttributeSelectorType exp = objectFactory.createAttributeSelectorType();
		exp.setContextSelectorId(v.getContextSelectorId());
		exp.setCategory(v.getCategory().getId());
		exp.setDataType(v.getDataType().getDataTypeId());
		exp.setMustBePresent(v.isMustBePresent());
		exp.setPath(v.getPath());
		add(objectFactory.createAttributeSelector(exp));
	}



	@Override
	public void visitEnter(VariableReference var) {
		VariableReferenceType exp = objectFactory.createVariableReferenceType();
		exp.setVariableId(var.getVariableId());
		add(objectFactory.createVariableReference(exp));
	}

	@Override
	public void visitEnter(Apply v) 
	{
		ApplyType exp = objectFactory.createApplyType();
		exp.setFunctionId(v.getFunctionId());
		expStack.push(objectFactory.createApply(exp));
		argStack.push(new LinkedList<JAXBElement<?>>());
	}
	
	@Override
	public void visitLeave(Apply v) {
		JAXBElement<?> exp = expStack.peek();
		ApplyType applyExp = (ApplyType)exp.getValue();
		applyExp.getExpression().addAll(argStack.pop());
		if(!argStack.isEmpty()){
			expStack.pop();
			argStack.peek().add(exp);
			return;
		}
	}
	
	private void add(JAXBElement<?> jaxb)
	{
		if(expStack.isEmpty()){
			expStack.push(jaxb);
			return;
		}
		List<JAXBElement<?>> args = argStack.peek();
		args.add(jaxb);
	}
}
