package org.xacml4j.v30.types;

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
