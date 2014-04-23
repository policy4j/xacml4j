package org.xacml4j.v30.pdp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xacml4j.v30.AttributeReferenceKey;
import org.xacml4j.v30.AttributeSelectorKey;
import org.xacml4j.v30.BagOfAttributeExp;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.EvaluationException;
import org.xacml4j.v30.ExpressionVisitor;

import com.google.common.base.Objects;

public class AttributeSelector extends
	AttributeReference
{
	private final static Logger log = LoggerFactory.getLogger(AttributeSelector.class);

	private final AttributeSelectorKey selectorKey;

	private AttributeSelector(Builder b){
		super(b);
		this.selectorKey = b.keyBuilder.build();
	}

	public static Builder builder(){
		return new Builder();
	}

	@Override
	public AttributeSelectorKey getReferenceKey() {
		return selectorKey;
	}

	@Override
	public String toString(){
		return Objects.toStringHelper(this)
		.add("selectorKey", selectorKey)
		.add("mustBePresent", isMustBePresent())
		.toString();
	}

	@Override
	public boolean equals(Object o){
		if(o == this){
			return true;
		}
		if(o == null){
			return false;
		}
		if(!(o instanceof AttributeSelector)){
			return false;
		}
		AttributeSelector s = (AttributeSelector)o;
		return selectorKey.equals(s.selectorKey) &&
		(isMustBePresent() ^ s.isMustBePresent());
	}

	@Override
	public int hashCode(){
		return Objects.hashCode(selectorKey, isMustBePresent());
	}

	@Override
	public void accept(ExpressionVisitor visitor) {
		AttributeSelectorVisitor v = (AttributeSelectorVisitor) visitor;
		v.visitEnter(this);
		v.visitLeave(this);
	}

	@Override
	public BagOfAttributeExp evaluate(EvaluationContext context)
			throws EvaluationException
	{
		BagOfAttributeExp v = null;
		try{
			v =  selectorKey.resolve(context);
		}catch(AttributeReferenceEvaluationException e){
			if(isMustBePresent()){
				throw e;
			}
			return getDataType().bagType().createEmpty();
		}catch(Exception e){
			if(isMustBePresent()){
				throw new AttributeReferenceEvaluationException(selectorKey);
			}
			return getDataType().bagType().createEmpty();
		}
		if((v == null ||
				v.isEmpty())
				&& isMustBePresent()){
			if(log.isDebugEnabled()){
				log.debug("Failed to resolve xpath=\"{}\", category=\"{}\"",
						selectorKey.getPath(), 
						selectorKey.getCategory());
			}
			throw new AttributeReferenceEvaluationException(selectorKey);
		}
		return ((v == null)?getDataType().bagType().createEmpty():v);
	}

	public interface AttributeSelectorVisitor extends ExpressionVisitor
	{
		void visitEnter(AttributeSelector v);
		void visitLeave(AttributeSelector v);
	}

	public static class Builder extends AttributeReference.Builder<Builder>
	{
		private final AttributeSelectorKey.Builder keyBuilder = AttributeSelectorKey.builder();

		public Builder xpath(String xpath){
			keyBuilder.xpath(xpath);
			return this;
		}

		public Builder contextSelectorId(String id){
			keyBuilder.contextSelectorId(id);
			return this;
		}

		public AttributeSelector build(){
			return new AttributeSelector(this);
		}

		@Override
		protected Builder getThis() {
			return this;
		}

		@Override
		protected AttributeReferenceKey.Builder<?> getBuilder() {
			return keyBuilder;
		}
	}
}
