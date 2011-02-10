package com.artagon.xacml.v30;

/**
 * An XACML expression visitor
 * 
 * @author Giedrius Trumpickas
 */
public interface ExpressionVisitor 
{
	void visit(AttributeValue v);
	void visit(BagOfAttributeValues v);
	void visit(Apply v);
	void visit(FunctionReference v);
	void visit(AttributeDesignator v);
	void visit(AttributeSelector v);
	void visit(VariableReference var);
}
