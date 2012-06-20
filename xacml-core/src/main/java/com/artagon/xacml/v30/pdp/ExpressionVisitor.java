package com.artagon.xacml.v30.pdp;

import com.artagon.xacml.v30.AttributeExp;
import com.artagon.xacml.v30.BagOfAttributeExp;

/**
 * An XACML expression visitor
 * 
 * @author Giedrius Trumpickas
 */
public interface ExpressionVisitor 
{
	void visitEnter(AttributeExp v);
	void visitLeave(AttributeExp v);
	void visitEnter(BagOfAttributeExp v);
	void visitLeave(BagOfAttributeExp v);
	void visitEnter(Apply v);
	void visitLeave(Apply v);
	void visitEnter(FunctionReference v);
	void visitLeave(FunctionReference v);
	void visitEnter(AttributeDesignator v);
	void visitLeave(AttributeDesignator v);
	void visitEnter(AttributeSelector v);
	void visitLeave(AttributeSelector v);
	void visitEnter(VariableReference var);
	void visitLeave(VariableReference var);
}
