package com.artagon.xacml.v3.types;

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.artagon.xacml.v3.AttributeValue;
import com.artagon.xacml.v3.AttributeValueType;
import com.artagon.xacml.v3.BagOfAttributeValues;
import com.artagon.xacml.v3.BagOfAttributeValuesType;
import com.artagon.xacml.v3.XacmlObject;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

/**
 * A base class for all XACML attributes.
 * 
 * @author Giedrius Trumpickas
 */
public abstract class BaseAttributeType<V extends AttributeValue> extends XacmlObject 
	implements AttributeValueType
{
	private String typeId;
	private BagOfAttributeValuesType bagType;
	protected final Logger log = LoggerFactory.getLogger(this.getClass());
	
	/**
	 * Constructs attribute type with given type identifier.
	 * 
	 * @param typeId an attribute type identifier
	 */
	protected BaseAttributeType(String typeId)
	{
		Preconditions.checkNotNull(typeId);
		this.typeId = typeId;		
		this.bagType = new BagOfAttributeValuesType(this);
	}
	
	public final String getDataTypeId(){
		return typeId;
	}
	
	@Override
	public final BagOfAttributeValuesType bagType(){
		return bagType;
	}
	
	@Override
	public final BagOfAttributeValues bagOf(AttributeValue... values) 
	{	
		return bagType().create(values);
	}

	@Override
	public final BagOfAttributeValues bagOf(
			Collection<AttributeValue> values) {
		return bagType().create(values);
	}

	@Override
	public BagOfAttributeValues emptyBag() {
		// TODO Auto-generated method stub
		return bagType().createEmpty();
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this).add("TypeId=", typeId).toString();
	}
}
