package org.xacml4j.v30.xml;

/*
 * #%L
 * Xacml4J Core Engine Implementation
 * %%
 * Copyright (C) 2009 - 2014 Xacml4J.org
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;

import javax.xml.bind.JAXBElement;

import org.oasis.xacml.v20.jaxb.context.ActionType;
import org.oasis.xacml.v20.jaxb.context.AttributeType;
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
import org.xacml4j.v30.Attribute;
import org.xacml4j.v30.Category;
import org.xacml4j.v30.CategoryId;
import org.xacml4j.v30.Decision;
import org.xacml4j.v30.Effect;
import org.xacml4j.v30.Entity;
import org.xacml4j.v30.SyntaxException;
import org.xacml4j.v30.Value;
import org.xacml4j.v30.content.XmlContent;
import org.xacml4j.v30.marshal.RequestUnmarshaller;
import org.xacml4j.v30.RequestContext;
import org.xacml4j.v30.types.PathValue;
import org.xacml4j.v30.types.Xacml20XPathTo30Transformer;
import org.xacml4j.v30.types.XacmlTypes;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;

public final class Xacml20RequestContextUnmarshaller extends
	BaseJAXBUnmarshaller<RequestContext>
		implements RequestUnmarshaller {
	private final static String RESOURCE_ID = "urn:oasis:names:tc:xacml:1.0:resource:resource-id";

	private final static Map<Decision, DecisionType> V30_TO_V20_DECISION_MAPPING = ImmutableMap.<Decision, DecisionType>builder()
			.put(Decision.DENY, DecisionType.DENY)
			.put(Decision.PERMIT, DecisionType.PERMIT)
			.put(Decision.NOT_APPLICABLE, DecisionType.NOT_APPLICABLE)
			.put(Decision.INDETERMINATE, DecisionType.INDETERMINATE)
			.put(Decision.INDETERMINATE_D, DecisionType.INDETERMINATE)
			.put(Decision.INDETERMINATE_P, DecisionType.INDETERMINATE)
			.put(Decision.INDETERMINATE_DP, DecisionType.INDETERMINATE)
			.build();

	private final static Map<DecisionType, Decision> V20_TO_V30_DECISION_MAPPING = ImmutableMap.of(
			DecisionType.DENY, Decision.DENY,
			DecisionType.PERMIT, Decision.PERMIT,
			DecisionType.NOT_APPLICABLE, Decision.NOT_APPLICABLE,
			DecisionType.INDETERMINATE, Decision.INDETERMINATE);

	private final static Map<EffectType, Effect> V20_TO_V30_EFFECT_MAPPING = ImmutableMap.of(
			EffectType.DENY, Effect.DENY,
			EffectType.PERMIT, Effect.PERMIT);

	private final static Map<Effect, EffectType> V30_TO_V20_EFFECT_MAPPING = ImmutableMap.of(
			Effect.DENY, EffectType.DENY,
			Effect.PERMIT, EffectType.PERMIT);

	private final Mapper mapper20;

	public Xacml20RequestContextUnmarshaller(){
		super(JAXBUtils.getInstance());
		this.mapper20 = new Mapper();
	}

	@Override
	protected RequestContext create(JAXBElement<?> jaxbInstance)
			throws SyntaxException {
		Preconditions.checkArgument((jaxbInstance.getValue()
				instanceof org.oasis.xacml.v20.jaxb.context.RequestType));
		return mapper20.create((org.oasis.xacml.v20.jaxb.context.RequestType)jaxbInstance.getValue());
	}

	public static class Mapper
	{
		private static final Logger log = LoggerFactory.getLogger(Mapper.class);

		public RequestContext create(RequestType req) throws SyntaxException
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
				attributes.add(createEnvironment(req.getEnvironment()));
			}
			return RequestContext.builder().returnPolicyIdList(false).attributes(attributes).build();
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
			throws SyntaxException
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
			throws SyntaxException
		{
			return CategoryId.parse(id).get();
		}

		private Category createEnvironment(EnvironmentType env)
			throws SyntaxException
		{
			return Category
					.builder(CategoryId.ENVIRONMENT)
					.entity(Entity
							.builder()
							.attributes(create(env.getAttribute(), CategoryId.ENVIRONMENT, false))
							.build())
					.build();
		}

		private Category createAction(ActionType subject) throws SyntaxException
		{
			return Category
					.builder(CategoryId.ACTION)
					.entity(Entity
							.builder()
							.attributes(create(subject.getAttribute(), CategoryId.ACTION, false))
							.build())
					.build();
		}

		private Category createResource(ResourceType resource,
				boolean multipleResources) throws SyntaxException
		{
			Entity.Builder b = Entity
					.builder()
					.attributes(create(resource.getAttribute(),
							CategoryId.RESOURCE, multipleResources));
			Node content = getResourceContent(resource);
			if(content != null){
				b.content(XmlContent.of(content));
			}
			return Category
					.builder(CategoryId.RESOURCE)
					.entity(b.build())
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
			throws SyntaxException
		{
			Collection<Attribute> attributes = new LinkedList<Attribute>();
			for(AttributeType a : contextAttributes){
				attributes.add(createAttribute(a, category, includeInResult));
			}
			return attributes;
		}

		private Attribute createAttribute(AttributeType a, CategoryId category,
					boolean includeInResultResourceId)
			throws SyntaxException
		{
			Collection<Value> values = new LinkedList<Value>();
			for(org.oasis.xacml.v20.jaxb.context.AttributeValueType v : a.getAttributeValue()){
				Value value = createValue(a, v);
				if(log.isDebugEnabled()){
					log.debug("Found attribute value=\"{}\" in request", value);
				}
				values.add(value);
			}
			return Attribute.builder(a.getAttributeId())
					.issuer(a.getIssuer())
					.includeInResult(a.getAttributeId().equals(RESOURCE_ID)? includeInResultResourceId :false)
					.values(values)
					.build();
		}

		private Value createValue(AttributeType a, org.oasis.xacml.v20.jaxb.context.AttributeValueType av)
		{
			org.oasis.xacml.v30.jaxb.AttributeValueType v30 = new org.oasis.xacml.v30.jaxb.AttributeValueType();
			v30.getOtherAttributes().putAll(av.getOtherAttributes());
			return TypeToXacml30.forType(a.getDataType())
					.map(v->
					{
						v30.setDataType(v.getType().getTypeId());
						if(v.getType().equals(XacmlTypes.XPATH))
						{
							if(av.getContent().isEmpty()){
								throw SyntaxException
										.noContentFound();
							}

							PathValue xpath = Xacml20XPathTo30Transformer
									.transform20PathTo30((String)(av.getContent().get(0)));
							v30.getContent().add(xpath.getPath());
							v30.getOtherAttributes().put(
									TypeToXacml30.Types.XPATH_CATEGORY_ATTR_NAME,
									CategoryId.RESOURCE.getId());
						}
						v30.getContent().addAll(av.getContent());
						return v.fromXacml30(v30);
					}).orElseThrow(
							()-> SyntaxException.invalidDataTypeId(a.getDataType()));

		}
	}
}
