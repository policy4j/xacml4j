package org.xacml4j.v30.types;

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xacml4j.v30.AttributeExp;
import org.xacml4j.v30.AttributeExpType;
import org.xacml4j.v30.BagOfAttributeExp;
import org.xacml4j.v30.BagOfAttributeExpType;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

/**
 * A base class for all XACML attributes.
 *
 * @author Giedrius Trumpickas
 */
@Deprecated
public abstract class BaseAttributeExpType<V extends AttributeExp> implements AttributeExpType
{
	private static final long serialVersionUID = -8812998715997567246L;

	private final String typeId;
	private final BagOfAttributeExpType bagType;
	protected final Logger log = LoggerFactory.getLogger(this.getClass());

	/**
	 * Constructs attribute type with given type identifier.
	 *
	 * @param typeId an attribute type identifier
	 */
	protected BaseAttributeExpType(String typeId)
	{
		Preconditions.checkNotNull(typeId);
		this.typeId = typeId;
		this.bagType = new BagOfAttributeExpType(this);
	}

	/**
	 * Gets data type identifier
	 *
	 * @return data type identifier
	 */
	public final String getDataTypeId(){
		return typeId;
	}

	public BagOfAttributeExp.Builder bag(){
		return new BagOfAttributeExp.Builder(this);
	}

	@Override
	public final BagOfAttributeExpType bagType(){
		return bagType;
	}

	@Override
	public final BagOfAttributeExp bagOf(AttributeExp... values){
		return bagType().create(values);
	}

	@Override
	public final BagOfAttributeExp bagOf(
			Collection<AttributeExp> values) {
		return bagType().create(values);
	}

	@Override
	public BagOfAttributeExp emptyBag() {
		return bagType().createEmpty();
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
				.add("TypeId", typeId)
				.toString();
	}

	@Override
	public int hashCode(){
		return typeId.hashCode();
	}
}
