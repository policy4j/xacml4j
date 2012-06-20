package com.artagon.xacml.v30;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterables;
import com.google.common.collect.LinkedHashMultiset;
import com.google.common.collect.Multiset;

/**
 * A XACML request context attribute
 * 
 * @author Giedrius Trumpickas
 */
public class Attribute
{
	private String attributeId;
	private Multiset<AttributeExp> values;
	private boolean includeInResult;
	private String issuer;
	
	/**
	 * Constructs attribute with given
	 * parameters
	 * 
	 * @param attributeId an attribute identifier
	 * @param issuer an attribute issuer
	 * @param includeInResult a flag indicating
	 * if attribute needs to be included in
	 * the result
	 * @param values a collection of 
	 * {@link AttributeExp} instances
	 */
	public Attribute(
			String attributeId,
			String issuer, 
			boolean includeInResult, 
			Iterable<AttributeExp> values){
		Preconditions.checkNotNull(attributeId);
		Preconditions.checkNotNull(values);
		this.attributeId = attributeId;
		this.issuer = issuer;
		this.values = LinkedHashMultiset.create();
		Iterables.addAll(this.values, values);
		this.includeInResult = includeInResult;
	}
	
	public Attribute(String attributeId,
			String issuer, 
			boolean includeInResult, 
			AttributeExp ...values){
		this(attributeId, issuer, 
				includeInResult, Arrays.asList(values));
	}
	
	public Attribute(String attributeId, 
			Collection<AttributeExp> values){
		this(attributeId, null, false, values);
	}
	
	public Attribute(String attributeId, 
			AttributeExp ...values){
		this(attributeId, null, false, Arrays.asList(values));
	}

	public static Attribute create(String attributeId,
			AttributeExpType attributeType, Object ...values) {
		return create(attributeId, null, false, attributeType, values);
	}

	public static Attribute create(String attributeId, String issuer, boolean includeInResult,
			AttributeExpType attributeType, Object ...values) {
		ArrayList<AttributeExp> attrValues = new ArrayList<AttributeExp>(values.length);
		if(values != null) {
			for(Object value: values) {
				attrValues.add(attributeType.create(value));
			}
		}
		return new Attribute(attributeId, issuer, includeInResult, attrValues);
	}

	/**
	 * Gets attribute identifier.
	 * 
	 * @return attribute identifier
	 */
	public String getAttributeId(){
		return attributeId;
	}
	
	/**
	 * Gets attribute values as collection of
	 * {@link AttributeExp} instances
	 * 
	 * @return collection of {@link AttributeExp} 
	 * instances
	 */
	public Collection<AttributeExp> getValues(){
		return Collections.unmodifiableCollection(values);
	}
	
	/**
	 * Gets all instances of {@link AttributeExp} by type
	 * 
	 * @param type an attribute type
	 * @return a collection of {@link AttributeExp} of given type
	 */
	public Collection<AttributeExp> getValuesByType(final AttributeExpType type)
	{
		return Collections2.filter(values, new Predicate<AttributeExp>() {
			@Override
			public boolean apply(AttributeExp a) {
				return a.getType().equals(type);
			}
			
		});
	}
	
	/**
	 * Gets this attribute issuer
	 * 
	 * @return issuer of this attribute
	 * identifier or <code>null</code>
	 * if it's not available
	 */
	public String getIssuer(){
		return issuer;
	}
	
	/**
	 * Tests if this attribute needs
	 * to be included back to the
	 * evaluation result
	 * 
	 * @return <code>true</code>
	 * if needs to be included
	 */
	public boolean isIncludeInResult(){
		return includeInResult;
	}
	
	@Override
	public final String toString(){
		return Objects.toStringHelper(this)
		.add("AttributeId", attributeId)
		.add("Issuer", issuer)
		.add("IncludeInResult", includeInResult)
		.add("Values", values).toString();
	}
	
	@Override
	public final int hashCode(){
		return Objects.hashCode(
				attributeId, issuer, includeInResult, values);
	}
	
	@Override
	public final boolean equals(Object o){
		if(o == this){
			return true;
		}
		if(o == null){
			return false;
		}
		if(!(o instanceof Attribute)){
			return false;
		}
		Attribute a = (Attribute)o;
		return Objects.equal(attributeId, a.attributeId) &&
			(!(includeInResult ^ a.includeInResult)) &&
			Objects.equal(issuer, a.issuer) && values.equals(a.values);
			
	}
}
