package org.xacml4j.v30.pdp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xacml4j.v30.AttributeCategories;
import org.xacml4j.v30.AttributeCategory;
import org.xacml4j.v30.AttributeDesignatorKey;
import org.xacml4j.v30.AttributeExpType;
import org.xacml4j.v30.BagOfAttributeExp;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.EvaluationException;
import org.xacml4j.v30.ExpressionVisitor;
import org.xacml4j.v30.StatusCode;
import org.xacml4j.v30.XacmlSyntaxException;
import org.xacml4j.v30.types.DataTypes;

import com.google.common.base.Objects;

/**
 * The {@link AttributeDesignator} retrieves a bag of values for a
 * named attribute from the request context. A named attribute is
 * considered present if there is at least one attribute that
 * matches the criteria set out below.
 *
 *
 * The {@Link AttributeDesignator} returns a bag containing all
 * the attribute values that are matched by the named attribute. In the
 * event that no matching attribute is present in the context, the
 * {@link AttributeDesignator#isMustBePresent()} governs whether it
 * evaluates to an empty bag or {@link EvaluationIndeterminateException} exception.
 *
 * See XACML 3.0 core section 7.3.5.
 *
 * @author Giedrius Trumpickas
 *
 * @param <T>
 */
public class AttributeDesignator extends AttributeReference
{
	private final static Logger log = LoggerFactory.getLogger(AttributeDesignator.class);

	private AttributeDesignatorKey designatorKey;

	/**
	 * Creates attribute designator
	 *
	 * @param category an attribute category
	 * @param attributeId an attribute identifier
	 * @param issuer an attribute issuer
	 * @param dataType an attribute data type
	 * @param mustBePresent a flag indicating
	 * that attribute must be present in the context
	 */
	public AttributeDesignator(
			AttributeCategory  category,
			String attributeId,
			String issuer,
			AttributeExpType dataType,
			boolean mustBePresent){
		super(mustBePresent);
		this.designatorKey = new AttributeDesignatorKey(category, attributeId, dataType, issuer);
	}


	public static AttributeDesignator create(AttributeCategory category,
			String attributeId, String issuer, String dataTypeId, boolean mustBePresent)
		throws XacmlSyntaxException
	{
		AttributeExpType type = DataTypes.getType(dataTypeId);
		return new AttributeDesignator(category, attributeId,
				issuer, type, mustBePresent);
	}

	public static AttributeDesignator create(String categoryId,
			String attributeId, String dataTypeId, boolean mustBePresent)
		throws XacmlSyntaxException
	{
		return create(categoryId, attributeId, null, dataTypeId, mustBePresent);
	}

	public static AttributeDesignator create(String categoryId,
			String attributeId, String issuer, String dataTypeId, boolean mustBePresent)
		throws XacmlSyntaxException
	{
		AttributeCategory category = AttributeCategories.parse(categoryId);
		return create(category, attributeId, issuer, dataTypeId, mustBePresent);
	}

	@Override
	public AttributeDesignatorKey getReferenceKey() {
		return designatorKey;
	}

	/**
	 * Gets attribute identifier
	 * in the request context
	 *
	 * @return attribute identifier
	 * in the request context
	 */
	public String getAttributeId(){
		return designatorKey.getAttributeId();
	}

	public String getIssuer(){
		return designatorKey.getIssuer();
	}

	/**
	 * Evaluates this attribute designator by resolving
	 * attribute via {@link EvaluationContext#resolveAttributeDesignator(String,
	 * String, AttributeExpType, String)
	 *
	 * @return {@link BagOfAttributeExp} instance
	 * @exception EvaluationIndeterminateException if attribute can't be resolved
	 * and {@link this#mustBePresent} is true
	 */
	public BagOfAttributeExp evaluate(EvaluationContext context)
			throws EvaluationException
	{
		BagOfAttributeExp v = null;
		try{
			v = designatorKey.resolve(context);
		}catch(AttributeReferenceEvaluationException e){
			if(log.isDebugEnabled()){
				log.debug("Reference=\"{}\" evaluation failed with error=\"{}\"",
						toString(), e.getMessage());
			}
			if(isMustBePresent()){
				log.debug("Re-throwing error");
				throw e;
			}
			return getDataType().bagType().createEmpty();
		}catch(Exception e){
			if(log.isDebugEnabled()){
				log.debug("Reference=\"{}\" evaluation failed with error=\"{}\"",
						toString(), e.getMessage());
			}
			if(isMustBePresent()){
				throw new AttributeReferenceEvaluationException(context,
						designatorKey,
						StatusCode.createMissingAttributeError(), e);
			}
			return getDataType().bagType().createEmpty();
		}
		if((v == null || v.isEmpty()) &&
				isMustBePresent()){
			if(log.isDebugEnabled()){
				log.debug("Failed to resolve attributeId=\"{}\", category=\"{}\"",
						getAttributeId(), getCategory());
			}
			throw new AttributeReferenceEvaluationException(context,
					designatorKey,
					"Failed to resolve categoryId=\"%s\", attributeId=\"%s\", issuer=\"%s\"",
					getCategory(), getAttributeId(), getIssuer());
		}
		return ((v == null)?getDataType().bagType().createEmpty():v);
	}

	@Override
	public void accept(ExpressionVisitor expv) {
		AttributeDesignatorVisitor v = (AttributeDesignatorVisitor)expv;
		v.visitEnter(this);
		v.visitLeave(this);
	}

	@Override
	public String toString(){
		return Objects.toStringHelper(this)
		.add("attributeId", designatorKey.getAttributeId())
		.add("category", designatorKey.getCategory())
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
		if(!(o instanceof AttributeDesignator)){
			return false;
		}
		AttributeDesignator d = (AttributeDesignator)o;
		return designatorKey.equals(d.designatorKey) &&
		(isMustBePresent() ^ d.isMustBePresent());
	}

	@Override
	public int hashCode(){
		return Objects.hashCode(
				designatorKey,
				isMustBePresent());
	}

	public interface AttributeDesignatorVisitor extends ExpressionVisitor
	{
		void visitEnter(AttributeDesignator v);
		void visitLeave(AttributeDesignator v);
	}
}
