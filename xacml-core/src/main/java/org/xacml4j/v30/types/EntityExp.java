package org.xacml4j.v30.types;

import org.xacml4j.v30.Entity;

public class EntityExp extends BaseAttributeExp<Entity>
{
	private static final long serialVersionUID = 6188174758603655643L;

	public EntityExp(Entity entity){
		super(EntityType.ENTITY, entity);
	}
}
