package org.xacml4j.v30.types;

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

import org.w3c.dom.Node;
import org.xacml4j.v30.Attribute;
import org.xacml4j.v30.BagOfAttributeExp;
import org.xacml4j.v30.Entity;

import com.google.common.base.Predicate;

public class EntityExp extends BaseAttributeExp<Entity>
{
	private static final long serialVersionUID = 6188174758603655643L;

	private EntityExp(Entity entity){
		super(XacmlTypes.ENTITY, entity);
	}
	
	public static EntityExp valueOf(Entity entity){
		return new EntityExp(Entity.builder().copyOf(entity).build());
	}
	
	public static EntityExp valueOf(Attribute ...attrs){
		return new EntityExp(Entity.builder().attribute(attrs).build());
	}
	
	public static EntityExp copyOf(EntityExp e, Predicate<Attribute> filter){
		return new EntityExp(Entity
				.builder()
				.copyOf(e.getValue(), filter)
				.build());
	}
	
	public static EntityExp valueOf(Node content, Attribute ...attrs){
		return new EntityExp(Entity
				.builder()
				.content(content)
				.attribute(attrs)
				.build());
	}
	
	public static BagOfAttributeExp emptyBag(){
		return XacmlTypes.ENTITY.emptyBag();
	}
	
	public static BagOfAttributeExp.Builder bag(){
		return XacmlTypes.ENTITY.bag();
	}
}
