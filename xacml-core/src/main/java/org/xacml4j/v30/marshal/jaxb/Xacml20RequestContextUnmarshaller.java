package org.xacml4j.v30.marshal.jaxb;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
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
import org.xacml4j.util.DOMUtil;
import org.xacml4j.v30.Attribute;
import org.xacml4j.v30.AttributeCategories;
import org.xacml4j.v30.AttributeCategory;
import org.xacml4j.v30.AttributeExp;
import org.xacml4j.v30.Attributes;
import org.xacml4j.v30.Decision;
import org.xacml4j.v30.Effect;
import org.xacml4j.v30.RequestContext;
import org.xacml4j.v30.XacmlSyntaxException;
import org.xacml4j.v30.marshal.RequestUnmarshaller;
import org.xacml4j.v30.pdp.RequestSyntaxException;
import org.xacml4j.v30.types.TypeToXacml30;
import org.xacml4j.v30.types.Types;
import org.xacml4j.v30.types.XPathExpType;

import com.google.common.base.Preconditions;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;

public final class Xacml20RequestContextUnmarshaller extends
	BaseJAXBUnmarshaller<RequestContext>
implements RequestUnmarshaller
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

	private Mapper mapper20;

	private Types xacmlTypes;

	public Xacml20RequestContextUnmarshaller(Types types){
		super(JAXBContextUtil.getInstance());
		Preconditions.checkNotNull(types);
		this.xacmlTypes = types;
		this.mapper20 = new Mapper();
	}

	@Override
	protected RequestContext create(JAXBElement<?> jaxbInstance)
			throws XacmlSyntaxException {
		Preconditions.checkArgument((jaxbInstance.getValue()
				instanceof org.oasis.xacml.v20.jaxb.context.RequestType));
		return mapper20.create((org.oasis.xacml.v20.jaxb.context.RequestType)jaxbInstance.getValue());
	}

	public class Mapper
	{
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
				normalized.add(Attributes
						.builder(categoryId)
						.attributes(categoryAttr)
						.build());
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
			return Attributes.builder(category)
					.attributes(create(subject.getAttribute(), category, false))
					.build();
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
			return Attributes
					.builder(AttributeCategories.ENVIRONMENT)
					.attributes(create(subject.getAttribute(), AttributeCategories.ENVIRONMENT, false))
					.build();
		}

		private Attributes createAction(ActionType subject) throws XacmlSyntaxException
		{
			return Attributes
					.builder(AttributeCategories.ACTION)
					.attributes(create(subject.getAttribute(), AttributeCategories.ACTION, false))
					.build();
		}

		private Attributes createResource(ResourceType resource,
				boolean multipleResources) throws XacmlSyntaxException
		{
			Node content = getResourceContent(resource);
			if(content != null){
				content = DOMUtil.copyNode(content);
			}
			return Attributes
					.builder(AttributeCategories.RESOURCE)
					.content(content)
					.attributes(create(resource.getAttribute(), AttributeCategories.RESOURCE, multipleResources))
					.build();
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

		private Collection<Attribute> create(
				Collection<AttributeType> contextAttributes,
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
				AttributeExp value = createValue(a, v);
				if(log.isDebugEnabled()){
					log.debug("Found attribute value=\"{}\" in request", value);
				}
				values.add(value);
			}
			return Attribute.builder(a.getAttributeId())
					.issuer(a.getIssuer())
					.includeInResult(a.getAttributeId().equals(RESOURCE_ID)?incudeInResultResourceId:false)
					.values(values)
					.build();
		}
		
		private AttributeExp createValue(AttributeType a, AttributeValueType av)
		{
			org.oasis.xacml.v30.jaxb.AttributeValueType v30 = new org.oasis.xacml.v30.jaxb.AttributeValueType();
			v30.setDataType(a.getDataType());
			v30.getOtherAttributes().putAll(av.getOtherAttributes());
			v30.getContent().addAll(av.getContent());
			if(a.getDataType().equals(XPathExpType.XPATHEXPRESSION.getDataTypeId())){
				v30.getOtherAttributes().put(XPathExpType.XPATH_CATEGORY_ATTR_NAME, AttributeCategories.RESOURCE.getId());
			}
			TypeToXacml30 xacml30 = xacmlTypes.getCapability(a.getDataType(), TypeToXacml30.class);
			Preconditions.checkState(xacml30 != null);
			return xacml30.fromXacml30(xacmlTypes, v30);
		}
	}
}
