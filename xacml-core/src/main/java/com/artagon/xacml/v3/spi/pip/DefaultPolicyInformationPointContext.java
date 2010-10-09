package com.artagon.xacml.v3.spi.pip;

import java.util.Calendar;

import org.w3c.dom.Node;

import com.artagon.xacml.v3.AttributeCategory;
import com.artagon.xacml.v3.AttributeReference;
import com.artagon.xacml.v3.AttributeValue;
import com.artagon.xacml.v3.AttributeValueType;
import com.artagon.xacml.v3.BagOfAttributeValues;
import com.artagon.xacml.v3.EvaluationContext;
import com.artagon.xacml.v3.RequestContextCallback;
import com.google.common.base.Preconditions;

class DefaultPolicyInformationPointContext implements PolicyInformationPointContext
{
	private AttributeReference ref;
	private EvaluationContext context;
	private RequestContextCallback callback;
	
	public DefaultPolicyInformationPointContext(EvaluationContext context, 
			RequestContextCallback callback,
			AttributeReference ref){
		Preconditions.checkNotNull(context);
		Preconditions.checkNotNull(callback);
		Preconditions.checkNotNull(ref);
		this.context = context;
		this.ref = ref;
		this.callback = callback;
	}
	
	@Override
	public Calendar getCurrentDateTime() {
		return context.getCurrentDateTime();
	}
	
	@Override
	public Object getValue(Object key) {
		return context.getValue(ref.getCategory(), key);
	}

	@Override
	public Object setValue(Object key, Object v) {
		return context.setValue(ref.getCategory(), key, v);
	}	
	
	public BagOfAttributeValues getAttributeValues(
			AttributeCategory category, String attributeId,
			AttributeValueType dataType, String issuer) {
		return callback.getAttributeValues(category, attributeId, dataType,
				issuer);
	}

	public BagOfAttributeValues getAttributeValues(
			AttributeCategory category, String attributeId,
			AttributeValueType dataType) {
		return callback.getAttributeValues(category, attributeId, dataType);
	}

	public <AV extends AttributeValue> AV getAttributeValue(
			AttributeCategory categoryId, String attributeId,
			AttributeValueType dataType) {
		return callback.<AV>getAttributeValue(categoryId, attributeId, dataType);
	}

	public <AV extends AttributeValue> AV getAttributeValue(
			AttributeCategory categoryId, String attributeId,
			AttributeValueType dataType, String issuer) {
		return callback.<AV>getAttributeValue(categoryId, attributeId, dataType, issuer);
	}

	@Override
	public Node getContent(AttributeCategory category) {
		return callback.getContent(category);
	}
}
