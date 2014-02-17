package org.xacml4j.v30;

import java.util.Arrays;
import java.util.Collections;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

/**
 * Represents a XACML bag of attributes type.
 *
 * @author Giedrius Trumpickas
 */
public final class BagOfAttributeExpType implements ValueType
{
	private static final long serialVersionUID = 1317103379388105997L;

	private AttributeExpType type;

	/**
	 * Constructs bag of attributes types with a given
	 * attribute type.
	 *
	 * @param type an attribute type
	 */
	public BagOfAttributeExpType(AttributeExpType type){
		Preconditions.checkNotNull(type);
		this.type = type;
	}

	/**
	 * Gets bag attribute type.
	 *
	 * @return bag attribute type
	 */
	public AttributeExpType getDataType(){
		return type;
	}

	public BagOfAttributeExp.Builder builder(){
		return new BagOfAttributeExp.Builder(type);
	}

	/**
	 * Creates bag from given collection of attributes.
	 *
	 * @param attr a collection of attributes
	 * @return {@link BagOfAttributeExp} containing given attributes
	 */
	public BagOfAttributeExp create(
			Iterable<AttributeExp> attr){
		return new BagOfAttributeExp(this, attr);
	}

	/**
	 * Creates an empty bag.
	 *
	 * @return instance of {@link BagOfAttributeExp} with
	 * no {@link BagOfAttributeExp} instances
	 */
	public  BagOfAttributeExp createEmpty(){
		return new BagOfAttributeExp(this, Collections.<AttributeExp>emptyList());
	}

	/**
	 * Creates bag from given array of attributes.
	 *
	 * @param attr an array of attributes
	 * @return {@link BagOfAttributeExp} containing given attributes
	 */
	public BagOfAttributeExp create(AttributeExp ...attr){
		return new BagOfAttributeExp(this, Arrays.asList(attr));
	}

	@Override
	public boolean equals(Object o){
		if(o == this){
			return true;
		}
		if(o == null){
			return false;
		}
		if(!(o instanceof BagOfAttributeExpType)){
			return false;
		}
		BagOfAttributeExpType bt = (BagOfAttributeExpType)o;
		return type.equals(bt.type);
	}

	@Override
	public int hashCode(){
		return Objects.hashCode(type);
	}

	@Override
	public String toString(){
		return Objects.toStringHelper(this)
		.add("TypeId", type.getDataTypeId())
		.toString();
	}
}
