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
import org.xacml4j.util.Xacml20XPathTo30Transformer;
import org.xacml4j.v30.Attribute;
import org.xacml4j.v30.Categories;
import org.xacml4j.v30.CategoryId;
import org.xacml4j.v30.AttributeExp;
import org.xacml4j.v30.AttributeExpType;
import org.xacml4j.v30.Category;
import org.xacml4j.v30.Decision;
import org.xacml4j.v30.Effect;
import org.xacml4j.v30.Entity;
import org.xacml4j.v30.RequestContext;
import org.xacml4j.v30.XacmlSyntaxException;
import org.xacml4j.v30.marshal.RequestUnmarshaller;
import org.xacml4j.v30.pdp.RequestSyntaxException;
import org.xacml4j.v30.types.XacmlTypes;

import com.google.common.base.Optional;
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

	public class Mapper
	{
		public RequestContext create(RequestType req) throws XacmlSyntaxException
		{
			Collection<Category> attributes = new LinkedList<Category>();
			if(!req.getResource().isEmpty()){
				Collection<ResourceType> resources = req.getResource();
				for(ResourceType r : resources){
					attributes.add(createResource(r, resources.size() > 1));
				}
			}
			if(!req.getSubject().isEmpty()){
				Multimap<CategoryId, Category> map = LinkedHashMultimap.create();
				for(SubjectType subject : req.getSubject()){
					Category attr =  createSubject(subject);
					map.put(attr.getCategoryId(), attr);
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

		private Collection<Category> normalize(Multimap<CategoryId, Category> attributes)
		{
			Collection<Category> normalized = new LinkedList<Category>();
			for(CategoryId categoryId : attributes.keySet()){
				Collection<Category> byCategory = attributes.get(categoryId);
				Entity.Builder b = Entity.builder();
				for(Category a : byCategory){
					b.copyOf(a.getEntity());
				}
				normalized.add(Category
						.builder(categoryId)
						.entity(b.build())
						.build());
			}
			return normalized;
		}

		private Category createSubject(SubjectType subject)
			throws XacmlSyntaxException
		{
			CategoryId category = getCategoryId(subject.getSubjectCategory());
			if(log.isDebugEnabled()){
				log.debug("Processing subject category=\"{}\"", category);
			}
			return Category.builder(category)
					.entity(Entity
							.builder()
							.attributes(create(subject.getAttribute(), category, false))
							.build())
					.build();
		}

		private CategoryId getCategoryId(String id)
			throws XacmlSyntaxException
		{
			CategoryId category = Categories.parse(id);
			if(category == null){
				throw new RequestSyntaxException("Unknown attribute category=\"%s\"", id);
			}
			return category;
		}

		private Category createEnviroment(EnvironmentType env)
			throws XacmlSyntaxException
		{
			return Category
					.builder(Categories.ENVIRONMENT)
					.entity(Entity
							.builder()
							.attributes(create(env.getAttribute(), Categories.ENVIRONMENT, false))
							.build())
					.build();
		}

		private Category createAction(ActionType subject) throws XacmlSyntaxException
		{
			return Category
					.builder(Categories.ACTION)
					.entity(Entity
							.builder()
							.attributes(create(subject.getAttribute(), Categories.ACTION, false))
							.build())
					.build();
		}

		private Category createResource(ResourceType resource,
				boolean multipleResources) throws XacmlSyntaxException
		{
			Node content = getResourceContent(resource);
			if(content != null){
				content = DOMUtil.copyNode(content);
			}
			return Category
					.builder(Categories.RESOURCE)
					.entity(Entity
							.builder()
							.attributes(create(resource.getAttribute(), Categories.RESOURCE, multipleResources))
							.content(content)
							.build())
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
				CategoryId category, boolean includeInResult)
			throws XacmlSyntaxException
		{
			Collection<Attribute> attributes = new LinkedList<Attribute>();
			for(AttributeType a : contextAttributes){
				attributes.add(createAttribute(a, category, includeInResult));
			}
			return attributes;
		}

		private Attribute createAttribute(AttributeType a, CategoryId category,
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
			v30.getOtherAttributes().putAll(av.getOtherAttributes());
			Optional<AttributeExpType> type = XacmlTypes.getType(a.getDataType());
			Preconditions.checkState(type.isPresent());
			Optional<TypeToXacml30> xacml30 = TypeToXacml30.Types.getIndex().get(type.get());			
			Preconditions.checkState(xacml30.isPresent());
			v30.setDataType(type.get().getDataTypeId());
			if(type.get().equals(XacmlTypes.XPATH)){
				if(av.getContent().size() > 0){
					v30.getContent().add(
							Xacml20XPathTo30Transformer.transform20PathTo30((String)av.getContent().get(0)));
					v30.getOtherAttributes().put(TypeToXacml30.Types.XPATH_CATEGORY_ATTR_NAME, 
							Categories.RESOURCE.getId());
					return xacml30.get().fromXacml30(v30);
				}
				throw new XacmlSyntaxException("Not content found for xpath expression");
			}
			v30.getContent().addAll(av.getContent());
			return xacml30.get().fromXacml30(v30);
		}
	}
}
