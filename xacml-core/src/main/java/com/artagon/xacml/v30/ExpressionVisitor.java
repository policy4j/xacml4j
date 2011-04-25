package com.artagon.xacml.v30;

/**
 * An XACML expression visitor
 * 
 * @author Giedrius Trumpickas
 */
public interface ExpressionVisitor 
{
	void visitEnter(AttributeValue v);
	void visitLeave(AttributeValue v);
	void visitEnter(BagOfAttributeValues v);
	void visitLeave(BagOfAttributeValues v);
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
