package org.xacml4j.v30;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.xacml4j.v30.types.IntegerType;

/**
 * @author Valdas Sevelis
 */
public class ObligationTest {

	private final Obligation o1;
	private final Obligation o2;
	private final Obligation o3;

	public ObligationTest() {
		AttributeAssignment attr1 = AttributeAssignment
				.builder()
				.category(AttributeCategories.ACTION)
				.id("actionId1")
				.value(IntegerType.INTEGER.create(1))
				.build();
		AttributeAssignment attr2 = AttributeAssignment
				.builder()
				.category(AttributeCategories.ACTION)
				.id("actionId1")
				.value(IntegerType.INTEGER.create(2))
				.build();
		AttributeAssignment attr3 = AttributeAssignment
				.builder()
				.category(AttributeCategories.ACTION)
				.id("actionId1")
				.value(IntegerType.INTEGER.create(3))
				.build();

		o1 = Obligation.builder("id1").attribute(attr1).attribute(attr2).build();
		o2 = Obligation.builder("id1").attribute(attr1).attribute(attr2).build();
		o3 = Obligation.builder("id1").attribute(attr3).build();
	}

	@Test
	public void testEquals() {
		assertThat(o1.equals(o1), is(true));
		assertThat(o1.equals(o2), is(true));
		assertThat(o1.equals(o3), is(false));
		assertThat(o1.equals(null), is(false));
	}

	@Test
	public void testHashCode() {
		assertThat(o1.hashCode(), equalTo(o2.hashCode()));
		assertThat(o1.hashCode(), not(equalTo(o3.hashCode())));
	}

	@Test
	public void testToString() {
		assertThat(o1.toString(), equalTo(o2.toString()));
		assertThat(o1.toString(), not(equalTo(o3.toString())));
	}
}
