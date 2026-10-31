package org.xacml4j.v30;

/*
 * #%L
 * Xacml4J Core Engine Implementation
 * %%
 * Copyright (C) 2009 - 2026 Xacml4J.org
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

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.xacml4j.v30.types.Entity;
import org.xacml4j.v30.types.XacmlTypes;

/**
 * Today's answers of the request checks that run on every decision, which cycle E of change
 * optimize-pdp-evaluation must keep (design D9, review RVP): repeated categories, attribute
 * value checks, the attributes to include in the result, and the attribute searches of
 * {@link AttributeIndex}.
 */
public class RequestFixedCostCharacterizationTest
{
	private static Attribute attribute(String id, String issuer, boolean include, String... values)
	{
		Attribute.Builder b = Attribute.builder(id).issuer(issuer).includeInResult(include);
		for(String v : values){
			b.value(XacmlTypes.STRING.ofAny(v));
		}
		return b.build();
	}

	private static Category category(CategoryId id, Attribute... attributes)
	{
		return Category.builder(id).entity(Entity.builder().attribute(attributes).build()).build();
	}

	@Test
	public void repeatedCategories()
	{
		Category subject = category(CategoryId.SUBJECT_ACCESS, attribute("s", null, false, "u"));
		Category resource1 = category(CategoryId.RESOURCE, attribute("r", null, false, "1"));
		Category resource2 = category(CategoryId.RESOURCE, attribute("r", null, false, "2"));

		assertThat(RequestContext.builder().build().containsRepeatingCategories()).isFalse();
		assertThat(RequestContext.builder().attributes(subject, resource1).build().containsRepeatingCategories()).isFalse();
		assertThat(RequestContext.builder().attributes(subject, resource1, resource2).build().containsRepeatingCategories()).isTrue();
		assertThat(RequestContext.builder().attributes(resource1, subject, resource2).build().getCategoryOccurrences(CategoryId.RESOURCE)).isEqualTo(2);
	}

	@Test
	public void entityLookupUsesEqualIdentifiersAndTheFirstCategory()
	{
		for(int count = 0; count <= 6; count++){
			RequestContext.Builder builder = RequestContext.builder();
			List<Category> categories = new ArrayList<>();
			for(int i = 0; i < count; i++){
				CategoryId id = CategoryId.parse("urn:test:category:" + i).orElseThrow();
				Category entry = category(id, attribute("value", null, false, "value-" + i));
				categories.add(entry);
				builder.attributes(entry);
			}
			RequestContext request = builder.build();
			for(Category entry : categories){
				CategoryId equal = CategoryId.parse(new String(entry.getCategoryId().getId())).orElseThrow();
				assertThat(equal).isNotSameInstanceAs(entry.getCategoryId());
				assertThat(request.getEntity(equal).orElseThrow()).isEqualTo(entry.getEntity());
				assertThat(request.getEntity(entry.getCategoryId()).orElseThrow()).isEqualTo(entry.getEntity());
			}
			assertThat(request.getEntity(CategoryId.parse("urn:test:absent").orElseThrow()).isEmpty()).isTrue();
			assertThrows(NullPointerException.class, () -> request.getEntity(null));
			if(!categories.isEmpty()){
				Category first = categories.get(0);
				builder.attributes(category(CategoryId.parse(first.getCategoryId().getId()).orElseThrow(),
						attribute("value", null, false, "later")));
				RequestContext repeated = builder.build();
				assertThat(repeated.getEntity(first.getCategoryId()).orElseThrow()).isEqualTo(first.getEntity());
				assertThat(repeated.getCategoryOccurrences(first.getCategoryId())).isEqualTo(2);
			}
		}
	}

	@Test
	public void attributeValueChecksByIdTypeAndIssuer()
	{
		RequestContext request = RequestContext.builder()
				.attributes(category(CategoryId.SUBJECT_ACCESS, attribute("s", "issuer-1", false, "u")),
						category(CategoryId.RESOURCE, attribute("r", null, false, "1")))
				.build();

		assertThat(request.containsAttributeValues("s", XacmlTypes.STRING)).isTrue();
		assertThat(request.containsAttributeValues("s", "issuer-1", XacmlTypes.STRING)).isTrue();
		assertThat(request.containsAttributeValues("s", "issuer-2", XacmlTypes.STRING)).isFalse();
		assertThat(request.containsAttributeValues("s", XacmlTypes.INTEGER)).isFalse();
		assertThat(request.containsAttributeValues("missing", XacmlTypes.STRING)).isFalse();
	}

	@Test
	public void includeInResultKeepsCategoryOrderAndOnlyMarkedAttributes()
	{
		Attribute subjectShown = attribute("s1", null, true, "u");
		Attribute subjectHidden = attribute("s2", null, false, "v");
		Attribute resourceShown = attribute("r1", "issuer-1", true, "1", "2");
		RequestContext request = RequestContext.builder()
				.attributes(category(CategoryId.SUBJECT_ACCESS, subjectShown, subjectHidden),
						category(CategoryId.ACTION, attribute("a", null, false, "read")),
						category(CategoryId.RESOURCE, resourceShown))
				.build();

		List<Category> included = new ArrayList<>(request.getIncludeInResultAttributes());

		assertThat(included).hasSize(2);
		assertThat(included.get(0).getCategoryId()).isEqualTo(CategoryId.SUBJECT_ACCESS);
		assertThat(included.get(0).getEntity().attributes()).containsExactly(subjectShown);
		assertThat(included.get(1).getCategoryId()).isEqualTo(CategoryId.RESOURCE);
		assertThat(included.get(1).getEntity().attributes()).containsExactly(resourceShown);
		assertThat(RequestContext.builder()
				.attributes(category(CategoryId.ACTION, attribute("a", null, false, "read")))
				.build().getIncludeInResultAttributes()).isEmpty();
	}

	@Test
	public void attributeIndexSearches()
	{
		Attribute a1 = attribute("a", "issuer-1", true, "1");
		Attribute a2 = attribute("a", "issuer-2", false, "2");
		Attribute a3 = attribute("a", null, true, "3");
		Attribute b = attribute("b", "issuer-1", false, "4");
		AttributeIndex index = AttributeIndex.builder().attributes(List.of(a1, a2, a3, b)).build();

		assertThat(index.single("a").get()).isEqualTo(a1);
		assertThat(index.single("missing").isPresent()).isFalse();
		assertThat(index.findByIssuer("a", null)).containsExactly(a1, a2, a3).inOrder();
		assertThat(index.findByIssuer("a", "issuer-2")).containsExactly(a2);
		assertThat(index.findByIssuer("a", "issuer-3")).isEmpty();
		assertThat(index.findByIssuer("missing", null)).isEmpty();
		assertThat(index.find(Attribute::isIncludeInResult)).containsExactly(a1, a3).inOrder();
		assertThat(index.find(a -> false)).isEmpty();
		assertThat(index.find("a", a -> !a.isIncludeInResult())).containsExactly(a2);
		assertThat(index.find("missing", a -> true)).isEmpty();
		assertThat(index.findFirst(a -> "issuer-1".equals(a.getIssuer())).get()).isEqualTo(a1);
		assertThat(index.findFirst(a -> false).isPresent()).isFalse();
		// The results are new modifiable lists, also when empty
		index.findByIssuer("missing", null).add(b);
		index.findByIssuer("a", null).add(b);
		index.find(a -> false).add(b);
		index.find("missing", a -> true).add(b);
		assertThat(index.findByIssuer("a", null)).containsExactly(a1, a2, a3).inOrder();
	}

	/** Review RVC2: a null predicate or filter is rejected where the streams rejected it. */
	@Test
	public void nullPredicatesAreRejectedAsBefore()
	{
		AttributeIndex empty = AttributeIndex.builder().build();
		AttributeIndex index = AttributeIndex.builder().attribute(attribute("a", null, false, "1")).build();

		org.junit.jupiter.api.Assertions.assertThrows(NullPointerException.class, () -> empty.find((java.util.function.Predicate<Attribute>) null));
		org.junit.jupiter.api.Assertions.assertThrows(NullPointerException.class, () -> empty.findFirst(null));
		org.junit.jupiter.api.Assertions.assertThrows(NullPointerException.class, () -> empty.find("a", null));
		// findValue: rejected once an attribute with the id and issuer is reached, even without values of the type
		org.junit.jupiter.api.Assertions.assertThrows(NullPointerException.class,
				() -> index.findValue("a", XacmlTypes.INTEGER, null, null));
		assertThat(index.findValue("missing", XacmlTypes.STRING, null, null).isPresent()).isFalse();
	}

	/**
	 * Review RVC1: {@link Attribute} declares no serialVersionUID, so its default one must stay the
	 * value of the base code (computed from the class built before change optimize-pdp-evaluation).
	 */
	@Test
	public void attributeKeepsItsSerialVersionUid()
	{
		assertThat(java.io.ObjectStreamClass.lookup(Attribute.class).getSerialVersionUID()).isEqualTo(7950110929829202273L);
	}

	/** Reviews RVC1 and RVC2: a subclass may change the protected identifier fields after hashing. */
	@Test
	public void identifierHashFollowsChangedFields()
	{
		MutableId changed = new MutableId("urn:a", "a");
		java.util.Set<MutableId> set = new java.util.HashSet<>();
		int before = changed.hashCode();
		changed.change("urn:b", "b");
		MutableId fresh = new MutableId("urn:b", "b");
		set.add(fresh);

		assertThat(changed).isEqualTo(fresh);
		assertThat(changed.hashCode()).isEqualTo(fresh.hashCode());
		assertThat(changed.hashCode()).isNotEqualTo(before);
		assertThat(set.contains(changed)).isTrue();

		// A null field hashes as Objects.hash did (review RVC2R)
		MutableId nulled = new MutableId("urn:c", "c");
		nulled.change("urn:c", null);
		MutableId otherNulled = new MutableId("urn:c", "c");
		otherNulled.change("urn:c", null);
		assertThat(nulled.hashCode()).isEqualTo(java.util.Objects.hash("urn:c", null, MutableId.class));
		assertThat(nulled).isEqualTo(otherNulled);
		assertThat(nulled.hashCode()).isEqualTo(otherNulled.hashCode());
	}

	private static final class MutableId extends SemanticIdentifier
	{
		MutableId(String id, String abbreviatedId)
		{
			super(id, abbreviatedId);
		}

		void change(String newId, String newAbbreviatedId)
		{
			this.id = newId;
			this.abbreviatedId = newAbbreviatedId;
		}
	}
}
