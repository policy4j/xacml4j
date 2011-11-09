package com.artagon.xacml.v30.types;

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.artagon.xacml.v30.AttributeExp;
import com.artagon.xacml.v30.AttributeExpType;
import com.artagon.xacml.v30.BagOfAttributeExp;
import com.artagon.xacml.v30.BagOfAttributeExpType;
import com.artagon.xacml.v30.XacmlObject;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

/**
 * A base class for all XACML attributes.
 * 
 * @author Giedrius Trumpickas
 */
public abstract class BaseAttributeExpType<V extends AttributeExp> extends XacmlObject 
	implements AttributeExpType
{
	private static final long serialVersionUID = -8812998715997567246L;
	
	private String typeId;
	private BagOfAttributeExpType bagType;
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
	
	public final String getDataTypeId(){
		return typeId;
	}
	
	@Override
	public final BagOfAttributeExpType bagType(){
		return bagType;
	}
	
	@Override
	public final BagOfAttributeExp bagOf(AttributeExp... values) 
	{	
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
		return Objects.toStringHelper(this).add("TypeId=", typeId).toString();
	}
}
