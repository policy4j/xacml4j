package org.xacml4j.v30;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

import org.xacml4j.v30.types.DoubleExp;
import org.xacml4j.v30.types.IntegerExp;
import org.xacml4j.v30.types.StringExp;

import com.google.common.base.Predicate;

public class EntityTest 
{
	
	@Test
	public void testBuildEntity(){
		Entity e0 = Entity
				.builder()
				.attribute(
					Attribute.builder("testId1").value(new StringExp("a"), new StringExp("bb")).build(),
					Attribute.builder("testId2").value(new StringExp("a"), new StringExp("bb")).build(),
					Attribute.builder("testId3").value(new IntegerExp(10), new DoubleExp(0.1)).build())
				.build();
		Entity e1 = Entity.builder().copyOf(e0).build();
		Entity e2 = Entity.builder().copyOf(e0, new Predicate<Attribute>(){
			public boolean apply(Attribute a){
				return a.getAttributeId().equals("testId2");
			}
			
		} ).build();
		assertEquals(e0,  e1);
		
	}
}
