package com.artagon.xacml.v30.pdp;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.artagon.xacml.util.DOMUtil;
import com.artagon.xacml.v30.AttributeContainer;
import com.google.common.base.Objects;

public class PolicyIssuer extends AttributeContainer 
	implements PolicyElement
{
	private Document content;
	
	private PolicyIssuer(Builder b){
		super(b);
		this.content = b.content;
	}
	
	public static Builder builder(){
		return new Builder();
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
	
	public static class Builder extends AttributeContainerBuilder<Builder>
	{
		private Document content;
		
		public Builder content(Node content){
			this.content = DOMUtil.copyNode(content);
			return this;
		}
		
		@Override
		protected Builder getThis() {
			return this;
		}
		
		public PolicyIssuer build(){
			return new PolicyIssuer(this);
		}
	}
}
