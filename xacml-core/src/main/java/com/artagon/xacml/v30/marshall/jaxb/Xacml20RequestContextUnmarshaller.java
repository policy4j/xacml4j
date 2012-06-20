package com.artagon.xacml.v30.marshall.jaxb;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBElement;

import org.oasis.xacml.v20.jaxb.context.ActionType;
import org.oasis.xacml.v20.jaxb.context.AttributeType;
import org.oasis.xacml.v20.jaxb.context.AttributeValueType;
import org.oasis.xacml.v20.jaxb.context.DecisionType;
import org.oasis.xacml.v20.jaxb.context.EnvironmentType;
import org.oasis.xacml.v20.jaxb.context.RequestType;
import org.oasis.xacml.v20.jaxb.context.ResourceContentType;
import org.oasis.xacml.v20.jaxb.context.ResourceType;
import org.oasis.xacml.v20.jaxb.context.SubjectType;
import org.oasis.xacml.v20.jaxb.policy.EffectType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.artagon.xacml.util.DOMUtil;
import com.artagon.xacml.util.Xacml20XPathTo30Transformer;
import com.artagon.xacml.v30.Attribute;
import com.artagon.xacml.v30.AttributeCategories;
import com.artagon.xacml.v30.AttributeCategory;
import com.artagon.xacml.v30.AttributeExp;
import com.artagon.xacml.v30.Attributes;
import com.artagon.xacml.v30.Decision;
import com.artagon.xacml.v30.Effect;
import com.artagon.xacml.v30.RequestContext;
import com.artagon.xacml.v30.marshall.RequestUnmarshaller;
import com.artagon.xacml.v30.pdp.RequestSyntaxException;
import com.artagon.xacml.v30.pdp.XacmlSyntaxException;
import com.artagon.xacml.v30.types.DataTypes;
import com.artagon.xacml.v30.types.XPathExpType;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;

public final class Xacml20RequestContextUnmarshaller extends 
	BaseJAXBUnmarshaller<RequestContext> 
implements RequestUnmarshaller 
{
	private Mapper mapper20;
		
	public Xacml20RequestContextUnmarshaller(){
		super(JAXBContextUtil.getInstance());
		this.mapper20 = new Mapper();
	}

	@Override
	protected RequestContext create(JAXBElement<?> jaxbInstance)
			throws XacmlSyntaxException {
		Preconditions.checkArgument((jaxbInstance.getValue() 
				instanceof org.oasis.xacml.v20.jaxb.context.RequestType));
		return mapper20.create((org.oasis.xacml.v20.jaxb.context.RequestType)jaxbInstance.getValue());
	}
	
	static class Mapper
	{
		private final static Logger log = LoggerFactory.getLogger(Mapper.class);
		
		private final static String RESOURCE_ID = "urn:oasis:names:tc:xacml:1.0:resource:resource-id";
		
		private final static Map<Decision, DecisionType> v30ToV20DecisionMapping = new HashMap<Decision, DecisionType>();
		private final static Map<DecisionType, Decision> v20ToV30DecisionMapping = new HashMap<DecisionType, Decision>();
		
		private final static Map<EffectType, Effect> v20ToV30EffectnMapping = new HashMap<EffectType, Effect>();
		private final static Map<Effect, EffectType> v30ToV20EffectnMapping = new HashMap<Effect, EffectType>();
		
		static
		{
			v30ToV20DecisionMapping.put(Decision.DENY, DecisionType.DENY);
			v30ToV20DecisionMapping.put(Decision.PERMIT, DecisionType.PERMIT);
			v30ToV20DecisionMapping.put(Decision.NOT_APPLICABLE, DecisionType.NOT_APPLICABLE);
			v30ToV20DecisionMapping.put(Decision.INDETERMINATE, DecisionType.INDETERMINATE);
			v30ToV20DecisionMapping.put(Decision.INDETERMINATE_D, DecisionType.INDETERMINATE);
			v30ToV20DecisionMapping.put(Decision.INDETERMINATE_P, DecisionType.INDETERMINATE);
			v30ToV20DecisionMapping.put(Decision.INDETERMINATE_DP, DecisionType.INDETERMINATE);
			
			v20ToV30DecisionMapping.put(DecisionType.DENY, Decision.DENY);
			v20ToV30DecisionMapping.put(DecisionType.PERMIT, Decision.PERMIT);
			v20ToV30DecisionMapping.put(DecisionType.NOT_APPLICABLE, Decision.NOT_APPLICABLE);
			v20ToV30DecisionMapping.put(DecisionType.INDETERMINATE, Decision.INDETERMINATE);
			
			
			v20ToV30EffectnMapping.put(EffectType.DENY, Effect.DENY);
			v20ToV30EffectnMapping.put(EffectType.PERMIT, Effect.PERMIT);
			
			v30ToV20EffectnMapping.put(Effect.DENY, EffectType.DENY);
			v30ToV20EffectnMapping.put(Effect.PERMIT, EffectType.PERMIT);
		
		}
		
		public RequestContext create(RequestType req) throws XacmlSyntaxException
		{
			Collection<Attributes> attributes = new LinkedList<Attributes>();
			if(!req.getResource().isEmpty()){
				Collection<ResourceType> resources = req.getResource();
				for(ResourceType r : resources){
					attributes.add(createResource(r, resources.size() > 1));
				}
			}
			if(!req.getSubject().isEmpty()){
				Multimap<AttributeCategory, Attributes> map = LinkedHashMultimap.create();
				for(SubjectType subject : req.getSubject()){
					Attributes attr =  createSubject(subject);
					map.put(attr.getCategory(), attr);
				}
				attributes.addAll(normalize(map));
			}
			if(req.getAction() != null){
				attributes.add(createAction(req.getAction()));
			}
			if(req.getEnvironment() != null)
			{
				attributes.add(createEnviroment(req.getEnvironment()));
			}
			return new RequestContext(false, attributes);
		}
		
		private Collection<Attributes> normalize(Multimap<AttributeCategory, Attributes> attributes)
		{
			Collection<Attributes> normalized = new LinkedList<Attributes>();
			for(AttributeCategory categoryId : attributes.keySet()){
				Collection<Attributes> byCategory = attributes.get(categoryId);
				Collection<Attribute> categoryAttr = new LinkedList<Attribute>();
				for(Attributes a : byCategory){
					categoryAttr.addAll(a.getAttributes());
				}
				normalized.add(new Attributes(categoryId, categoryAttr));
			}
			return normalized;
		}
		
		private Attributes createSubject(SubjectType subject) 
			throws XacmlSyntaxException
		{
			AttributeCategory category = getCategoryId(subject.getSubjectCategory());
			if(log.isDebugEnabled()){
				log.debug("Processing subject category=\"{}\"", category);
			}
			return new Attributes(category, create(subject.getAttribute(), category, false));
		}
		
		private AttributeCategory getCategoryId(String id) 
			throws XacmlSyntaxException
		{
			AttributeCategory category = AttributeCategories.parse(id);
			if(category == null){
				throw new RequestSyntaxException("Unknown attribute category=\"%s\"", id);
			}
			return category;
		}
		
		private Attributes createEnviroment(EnvironmentType subject) 
			throws XacmlSyntaxException
		{
			return new Attributes(AttributeCategories.ENVIRONMENT, 
					null, create(subject.getAttribute(), AttributeCategories.ENVIRONMENT, false));
		}
		
		private Attributes createAction(ActionType subject) throws XacmlSyntaxException
		{
			return new Attributes(AttributeCategories.ACTION, 
					null, create(subject.getAttribute(), AttributeCategories.ACTION, false));
		}
		
		private Attributes createResource(ResourceType resource, 
				boolean multipleResources) throws XacmlSyntaxException
		{
			Node content = getResourceContent(resource);
			if(content != null){
				content = DOMUtil.copyNode(content);
			}
			return new Attributes(AttributeCategories.RESOURCE, 
					content, 
					create(resource.getAttribute(), 
							AttributeCategories.RESOURCE, multipleResources));
		}
		
		private Node getResourceContent(ResourceType resource)
		{
			ResourceContentType content = resource.getResourceContent();
			if(content == null){
				return null;
			}
			for(Object o : content.getContent())
			{
				if(o instanceof Element){
					Node node = (Node)o;
					return node;
				}
			}
			return null;
		}
		
		private Collection<Attribute> create(Collection<AttributeType> contextAttributes, 
				AttributeCategory category, boolean includeInResult) 
			throws XacmlSyntaxException
		{
			Collection<Attribute> attributes = new LinkedList<Attribute>();
			for(AttributeType a : contextAttributes){
				attributes.add(createAttribute(a, category, includeInResult));
			}
			return attributes;
		}
		
		private Attribute createAttribute(AttributeType a, AttributeCategory category, 
					boolean incudeInResultResourceId) 
			throws XacmlSyntaxException
		{
			Collection<AttributeExp> values = new LinkedList<AttributeExp>();
			for(AttributeValueType v : a.getAttributeValue()){
				AttributeExp value = createValue(a.getDataType(), v, category);
				if(log.isDebugEnabled()){
					log.debug("Found attribute value=\"{}\" in request", value);
				}
				values.add(value);
			}
			return new Attribute(a.getAttributeId(), a.getIssuer(), 
					((a.getAttributeId().equals(RESOURCE_ID))?incudeInResultResourceId:false), values);
		}
		
		private AttributeExp createValue(String dataTypeId, 
				AttributeValueType value, 
				AttributeCategory categoryId) 
			throws XacmlSyntaxException
		{
			List<Object> content = value.getContent();
			if(content == null || 
					content.isEmpty()){
				throw new RequestSyntaxException("Attribute does not have content");
			}
			com.artagon.xacml.v30.AttributeExpType dataType = DataTypes.getType(dataTypeId);
			if(dataType == null){
				throw new RequestSyntaxException(
						"DataTypeId=\"%s\" can be be " +
						"resolved to valid XACML type", dataTypeId);
			}
			Object o = Iterables.getOnlyElement(content);
			if(log.isDebugEnabled()){
				log.debug("Creating typeId=\"{}\" value=\"{}\"", dataType, o);
			}
			if(dataType.equals(XPathExpType.XPATHEXPRESSION)){
				String xpath = Xacml20XPathTo30Transformer.transform20PathTo30((String)o);
				return dataType.create(xpath, categoryId);
			}
			return dataType.create(o);
		}
	}
}
