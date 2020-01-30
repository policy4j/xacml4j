package org.xacml4j.v30.types;

/*
 * #%L
 * Xacml4J Core Engine Implementation
 * %%
 * Copyright (C) 2009 - 2019 Xacml4J.org
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

import com.google.common.base.MoreObjects;
import org.xacml4j.v30.AttributeValue;
import org.xacml4j.v30.AttributeValueType;
import org.xacml4j.v30.ExpressionVisitor;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Objects;

/**
 * Abstract implementation of {@Link AttributeValue}
 *
 * @param <T>
 */
public class BaseAttributeValue<T>
    implements AttributeValue, Externalizable
{
    private static final long serialVersionUID = 4131180767511036271L;

    private T value;
    private AttributeValueType type;

    protected BaseAttributeValue(AttributeValueType attrType,
                                 T attrValue) {
        this.type = Objects.requireNonNull(attrType);
        this.value = Objects.requireNonNull(attrValue);
    }

    @Override
    public final AttributeValueType getType(){
        return type;
    }

    @Override
    public final T value(){
        return value;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("value", value)
                .add("type", type.getAbbrevDataTypeId())
                .toString();
    }

    @Override
    public final int hashCode(){
        return Objects.hash(value, type);
    }


    @Override
    public boolean equals(Object o) {
        if(o == this){
            return true;
        }
        if(!(o instanceof AttributeValue)){
            return false;
        }
        AttributeValue e = (AttributeValue)o;
        return type.equals(e.getType()) &&
                value.equals(e.value());
    }

    @Override
    public final void accept(ExpressionVisitor expv) {
        AttributeExpVisitor v = (AttributeExpVisitor)expv;
        v.visitEnter(this);
        v.visitLeave(this);
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(value);
        out.writeObject(type);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        this.value = (T)in.readObject();
        this.type = (AttributeValueType)in.readObject();
    }
}
