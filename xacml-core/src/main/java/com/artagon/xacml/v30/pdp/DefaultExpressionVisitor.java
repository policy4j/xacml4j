package com.artagon.xacml.v30.pdp;

import com.artagon.xacml.v30.AttributeExp.AttributeExpVisitor;
import com.artagon.xacml.v30.BagOfAttributeExp.BagOfAttributeVisitor;
import com.artagon.xacml.v30.Expression;
import com.artagon.xacml.v30.pdp.Apply.ApplyVistor;
import com.artagon.xacml.v30.pdp.AttributeDesignator.AttributeDesignatorVisitor;
import com.artagon.xacml.v30.pdp.AttributeSelector.AttributeSelectorVisitor;
import com.artagon.xacml.v30.pdp.FunctionReference.FunctionReferenceVisitor;
import com.artagon.xacml.v30.pdp.VariableReference.VariableReferenceVisitor;

/**
 * A default interface for {@link Expression} visitors
 *
 * @author Giedrius Trumpickas
 */
public interface DefaultExpressionVisitor extends
ApplyVistor, BagOfAttributeVisitor, AttributeExpVisitor, AttributeSelectorVisitor,
AttributeDesignatorVisitor, VariableReferenceVisitor, FunctionReferenceVisitor
{

}
