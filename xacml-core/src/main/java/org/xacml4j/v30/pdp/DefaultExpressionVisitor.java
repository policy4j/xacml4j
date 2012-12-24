package org.xacml4j.v30.pdp;

import org.xacml4j.v30.Expression;
import org.xacml4j.v30.AttributeExp.AttributeExpVisitor;
import org.xacml4j.v30.BagOfAttributeExp.BagOfAttributeVisitor;
import org.xacml4j.v30.pdp.Apply.ApplyVistor;
import org.xacml4j.v30.pdp.AttributeDesignator.AttributeDesignatorVisitor;
import org.xacml4j.v30.pdp.AttributeSelector.AttributeSelectorVisitor;
import org.xacml4j.v30.pdp.FunctionReference.FunctionReferenceVisitor;
import org.xacml4j.v30.pdp.VariableReference.VariableReferenceVisitor;


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
