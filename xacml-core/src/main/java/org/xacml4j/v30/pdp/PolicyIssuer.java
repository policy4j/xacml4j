package org.xacml4j.v30.pdp;

import java.util.Arrays;
import java.util.Collection;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xacml4j.util.DOMUtil;
import org.xacml4j.v30.Attribute;
import org.xacml4j.v30.AttributeContainer;

import com.google.common.base.Objects;

public class PolicyIssuer extends AttributeContainer 
	implements PolicyElement
{
	private Document content;
	
	public PolicyIssuer(Node content, 
			Collection<Attribute> attributes){
		super(attributes);
		this.content = DOMUtil.copyNode(content);
	}
	
	public PolicyIssuer(Collection<Attribute> attributes){
		this(null ,attributes);
	}
	
	public PolicyIssuer(Node content, 
			Attribute ...attributes){
		this(content, Arrays.asList(attributes));
	}
	
	public PolicyIssuer(Attribute ...attributes){
		this(null, Arrays.asList(attributes));
	}
	
	/**
	 * Gets a free form XML 
	 * describing policy issuer.
	 * 
	 * @return {@link Node} or <code>null</code>
	 */
	public Node getContent(){
		return content;
	}
		
	@Override
	public boolean equals(Object o){
		if(o == this){
			return true;
		}
		if(o == null){
			return false;
		}
		if(!(o instanceof PolicyIssuer)){
			return false;
		}
		PolicyIssuer pi = (PolicyIssuer)o;
		return DOMUtil.isEqual(content, pi.content) && 
		attributes.equals(pi.attributes);
	}
	
	@Override
	public String toString(){
		return Objects.toStringHelper(this)
		.add("attributes", attributes)
		.add("content", 
				(content != null)?DOMUtil.toString(
						content.getDocumentElement()):content)
		.toString();
	}

	@Override
	public void accept(PolicyVisitor v) {
		v.visitEnter(this);
		v.visitLeave(this);
	}
}
