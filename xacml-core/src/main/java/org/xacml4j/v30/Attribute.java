package org.xacml4j.v30;

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

import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;

import com.google.common.base.*;
import com.google.common.collect.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.xacml4j.v30.types.XacmlTypes;

/**
 * A XACML request context category
 *
 * @author Giedrius Trumpickas
 */
public class Attribute
{
    private final static Logger log = LoggerFactory.getLogger(Attribute.class);

	private final String attributeId;
	private final ImmutableMultiset<AttributeExp> values;
	private final boolean includeInResult;
	private final String issuer;

	private Attribute(Builder b){
		Preconditions.checkArgument(!Strings.isNullOrEmpty(b.attributeId));
		this.attributeId = b.attributeId;
		this.includeInResult = b.includeInResult;
		this.issuer = b.issuer;
		this.values = b.valueBuilder.build();
	}

	public static Builder builder(String attributeId){
		return new Builder().attributeId(attributeId);
	}

    public static Builder builder(){
        return new Builder();
    }

	/**
	 * Gets this category issuer
	 *
	 * @return issuer of this category
	 * identifier or {@code null}
	 * if it's not available
	 */
	public String getIssuer(){
		return issuer;
	}

	/**
	 * Tests if this category needs
	 * to be included back to the
	 * evaluation result
	 *
	 * @return {@code true} if the category needs to be included
	 */
	public boolean isIncludeInResult(){
		return includeInResult;
	}

	/**
	 * Gets category identifier.
	 *
	 * @return category identifier
	 */
	public String getAttributeId(){
		return attributeId;
	}

    public boolean containsAll(Collection<AttributeExp> values){
        return this.values.containsAll(values);
    }

    public boolean containsAll(AttributeExp ...values){
        return this.values.containsAll(Arrays.asList(values));
    }

	/**
	 * Gets all instances of {@link AttributeExp} by type
	 *
	 * @param type an category type
	 * @return a collection of {@link AttributeExp} of given type
	 */
	public Iterable<AttributeExp> getValuesByType(final AttributeExpType type) {
		return FluentIterable
                .from(values)
                .filter(new Predicate<AttributeExp>() {
                    @Override
                    public boolean apply(AttributeExp a) {
                        return a.getType().equals(type);
                    }
                });
	}

    public Collection<AttributeExp> getValues(){
        return values;
    }

	@Override
	public final String toString(){
		return Objects.toStringHelper(this)
		.add("AttributeId", attributeId)
		.add("Issuer", issuer)
		.add("IncludeInResult", includeInResult)
		.add("Values", values)
		.toString();
	}

	@Override
	public final int hashCode(){
		return Objects.hashCode(
				attributeId, issuer, includeInResult, values);
	}

	@Override
	public final boolean equals(Object o){
		if (o == this) {
			return true;
		}
		if (!(o instanceof Attribute)) {
			return false;
		}
		Attribute a = (Attribute)o;
		return Objects.equal(attributeId, a.attributeId) &&
			includeInResult == a.includeInResult &&
			Objects.equal(issuer, a.issuer) &&
			values.equals(a.values);
	}

	public static class Builder
	{
		private String attributeId;
		private String issuer;
		private boolean includeInResult = false;
		private ImmutableMultiset.Builder<AttributeExp> valueBuilder = ImmutableMultiset.builder();

		public Builder issuer(String issuer){
			this.issuer = Strings.emptyToNull(issuer);
			return this;
		}

        public Builder attributeId(String attributeId){
            this.attributeId = Strings.emptyToNull(attributeId);
            return this;
        }

        public Builder copyOf(Attribute attr, Predicate<AttributeExp> p){
            this.issuer = attr.getIssuer();
            this.attributeId = attr.getAttributeId();
            this.valueBuilder.addAll(FluentIterable.from(attr.getValues()).filter(p));
            return this;
        }

        public Builder copyOf(Attribute attr){
            return copyOf(attr, Predicates.<AttributeExp>alwaysTrue());
        }

        public Builder copyOf(Attribute attr,
                              Function<AttributeExp, AttributeExp> f)
        {
            Preconditions.checkNotNull(attr);
            issuer(attr.getIssuer());
            attributeId(attr.getAttributeId());
            includeInResult(attr.isIncludeInResult());
            values(attr.getValues());
            return this;
        }

		public Builder includeInResult(boolean include){
			this.includeInResult = include;
			return this;
		}

		public Builder noValues(){
			this.valueBuilder = ImmutableMultiset.builder();
			return this;
		}

		public Builder value(AttributeExp ...values){
           return values(FluentIterable.of(values));
		}

        public Builder values(Iterable<AttributeExp> values){
            this.valueBuilder.addAll(FluentIterable
                    .from(values)
                    .filter(Predicates.notNull()));
            return this;
        }

        private Builder values(Function<Object, AttributeExp> function,
                                                 Iterable<? extends Object> values){
            Iterable<AttributeExp> v = FluentIterable
                    .from(values)
                    .transform(function)
                    .filter(Predicates.notNull());
            this.valueBuilder.addAll(v);
            return this;
        }

		public Builder entityValue(Entity ...values){
            values(XacmlTypes.ENTITY, FluentIterable.of(values));
			return this;
		}

        public Builder entityValues(Iterable<Entity> it){
            return values(XacmlTypes.ENTITY, it);
        }

        public Builder stringValue(String ...values){
            values(XacmlTypes.STRING, FluentIterable.of(values));
            return this;
        }

        public Builder stringValues(Iterable<String> values){
            values(XacmlTypes.STRING, values);
            return this;
        }

        public Builder booleanValue(Boolean ...values){
            values(XacmlTypes.BOOLEAN, FluentIterable.of(values));
            return this;
        }

        public Builder booleanValues(Iterable<String> values){
            values(XacmlTypes.BOOLEAN, values);
            return this;
        }

        public Builder intValue(Integer ...values){
            values(XacmlTypes.INTEGER, FluentIterable.of(values));
            return this;
        }

        public Builder intValues(Iterable<Integer> values){
            values(XacmlTypes.INTEGER, values);
            return this;
        }

        public Builder shortValue(Short ...values){
            values(XacmlTypes.INTEGER, FluentIterable.of(values));
            return this;
        }

        public Builder byteValue(Byte ...values){
            values(XacmlTypes.INTEGER, FluentIterable.of(values));
            return this;
        }

        public Builder dateTimeValue(Calendar...values){
            values(XacmlTypes.DATETIME, FluentIterable.of(values));
            return this;
        }

        public Builder dateTimeValue(String...values){
            values(XacmlTypes.DATETIME, FluentIterable.of(values));
            return this;
        }

        public Builder dateTimeValues(Iterable<?> values){
            values(XacmlTypes.DATETIME, values);
            return this;
        }

        public Builder dateValue(Calendar...values){
            values(XacmlTypes.DATE, FluentIterable.of(values));
            return this;
        }

        public Builder dateValue(String...values){
            values(XacmlTypes.DATE, FluentIterable.of(values));
            return this;
        }

        public Builder hexValue(byte[]...values){
            values(XacmlTypes.HEXBINARY, FluentIterable.of(values));
            return this;
        }

        public Builder base64Value(byte[]...values){
            values(XacmlTypes.BASE64BINARY, FluentIterable.of(values));
            return this;
        }

        public Builder ipAddressValue(String...values){
            values(XacmlTypes.IPADDRESS, FluentIterable.of(values));
            return this;
        }

        public Builder ipAddressValue(IPAddress...values){
            values(XacmlTypes.IPADDRESS, FluentIterable.of(values));
            return this;
        }

        public Builder ipAddressValues(Iterable<?> values){
            values(XacmlTypes.IPADDRESS, values);
            return this;
        }

        public Builder rfc822NameValue(RFC822Name...values){
            values(XacmlTypes.RFC822NAME, FluentIterable.of(values));
            return this;
        }

        public Builder rfc822NameValue(String...values){
            values(XacmlTypes.RFC822NAME, FluentIterable.of(values));
            return this;
        }

        public Builder rfc822NameValue(Iterable<?> values){
            values(XacmlTypes.RFC822NAME, values);
            return this;
        }

        public Attribute build(){
			return new Attribute(this);
		}
	}
}
