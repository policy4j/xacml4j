
package com.artagon.xacml.v30.spi.function;

import static org.easymock.EasyMock.capture;
import static org.easymock.EasyMock.createControl;
import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertEquals;

import java.util.List;

import org.easymock.Capture;
import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.artagon.xacml.util.Invocation;
import com.artagon.xacml.v30.AttributeValue;
import com.artagon.xacml.v30.EvaluationContext;
import com.artagon.xacml.v30.Expression;
import com.artagon.xacml.v30.FunctionInvocationException;
import com.artagon.xacml.v30.FunctionSpec;
import com.artagon.xacml.v30.ValueExpression;
import com.artagon.xacml.v30.types.IntegerType;
import com.artagon.xacml.v30.types.StringType;
import com.google.common.collect.ImmutableList;

public class DefaultFunctionInocationTest 
{
	private Invocation<ValueExpression> inv0;
	private Invocation<ValueExpression> inv1;
	private FunctionSpec spec;
	private IMocksControl c;
	private FunctionInvocation f0;
	private FunctionInvocation f1;
	private EvaluationContext context;
	
	@SuppressWarnings("unchecked")
	@Before
	public void init(){
		this.c = createControl();
		this.inv0 = c.createMock(Invocation.class); 
		this.inv1 = c.createMock(Invocation.class);
		this.spec = c.createMock(FunctionSpec.class);
		this.f0 = new DefaultFunctionInvocation(inv0, false);
		this.f1 = new DefaultFunctionInvocation(inv1, true);
		this.context = c.createMock(EvaluationContext.class);
	}
	
	@Test
	public void testInvokeNoEvaluationContextFuncionIsNotVariadic() throws Exception
	{	
		List<Expression>  p = ImmutableList.<Expression>builder()
		.add(IntegerType.INTEGER.create(1))
		.add(IntegerType.INTEGER.create(2))
		.build();
		expect(spec.getNumberOfParams()).andReturn(2);
		expect(spec.isVariadic()).andReturn(false).times(2);
		expect(inv0.invoke(p.toArray())).andReturn(IntegerType.INTEGER.create(1));
		c.replay();
		ValueExpression v = f0.invoke(spec, context, p);
		assertEquals(IntegerType.INTEGER.create(1), v);
		c.verify();
	}
	
	@Test(expected=FunctionInvocationException.class)
	public void testInvokeInvocationThrowsRuntimeException() throws Exception
	{	
		List<Expression>  p = ImmutableList.<Expression>builder()
		.add(IntegerType.INTEGER.create(1))
		.add(IntegerType.INTEGER.create(2))
		.build();
		expect(spec.getNumberOfParams()).andReturn(2);
		expect(spec.isVariadic()).andReturn(false).times(2);
		expect(inv0.invoke(p.toArray())).andThrow(new RuntimeException());
		c.replay();
		f0.invoke(spec, context, p);
		c.verify();
	}
	
	@Test
	public void testInvokeWithEvaluationContextFuncionIsNotVariadic() throws Exception
	{	
		List<Expression>  p = ImmutableList.<Expression>builder()
		.add(IntegerType.INTEGER.create(1))
		.add(IntegerType.INTEGER.create(2))
		.build();
		expect(spec.getNumberOfParams()).andReturn(2);
		expect(spec.isVariadic()).andReturn(false).times(2);
		expect(inv1.invoke(new Object[]{context, 
				IntegerType.INTEGER.create(1), 
				IntegerType.INTEGER.create(2)})).andReturn(
						IntegerType.INTEGER.create(1));
		c.replay();
		ValueExpression v = f1.invoke(spec, context, p);
		assertEquals(IntegerType.INTEGER.create(1), v);
		c.verify();
	}
	
	@Test
	@Ignore
	public void testInvokeWithEvaluationContextFuncionIsVariadic() throws Exception
	{	
		List<Expression>  p = ImmutableList.<Expression>builder()
		.add(IntegerType.INTEGER.create(1))
		.add(IntegerType.INTEGER.create(2))
		.add(StringType.STRING.create("aaa"))
		.add(StringType.STRING.create("bbb"))
		.build();
		Object[] pArray = new Object[]{
				context, 
				IntegerType.INTEGER.create(1), 
				IntegerType.INTEGER.create(2),
				new Object[]{
					StringType.STRING.create("aaa"), 
					StringType.STRING.create("bbb")}
				};
		expect(spec.getNumberOfParams()).andReturn(3);
		expect(spec.isVariadic()).andReturn(true).times(2);
		expect(inv1.invoke(pArray))
		.andReturn(IntegerType.INTEGER.create(1));
		c.replay();
		ValueExpression v = f1.invoke(spec, context, p);
		assertEquals(IntegerType.INTEGER.create(1), v);
		c.verify();
	}
	
	@Test
	@Ignore
	public void testInvokeWithNoEvaluationContextFuncionIsVariadicAndMoreThanZeroVariadicParams() throws Exception
	{	
		List<Expression>  p = ImmutableList.<Expression>builder()
		.add(IntegerType.INTEGER.create(1))
		.add(IntegerType.INTEGER.create(2))
		.add(StringType.STRING.create("aaa"))
		.add(StringType.STRING.create("bbb"))
		.build();
		Object[] pArray = new Object[]{
				IntegerType.INTEGER.create(1), 
				IntegerType.INTEGER.create(2),
				new Object[]{
					StringType.STRING.create("aaa"), 
					StringType.STRING.create("bbb")}
				};
		expect(spec.getNumberOfParams()).andReturn(3);
		expect(spec.isVariadic()).andReturn(true).times(2);
		expect(inv0.invoke(pArray)).andReturn(IntegerType.INTEGER.create(1));
		c.replay();
		ValueExpression v = f0.invoke(spec, context, p);
		assertEquals(IntegerType.INTEGER.create(1), v);
		c.verify();
	}
	
	@Test
	public void testInvokeWithNoEvaluationContextFuncionIsVariadicAndZeroVariadicParams() throws Exception
	{	
		List<Expression>  p = ImmutableList.<Expression>builder()
		.add(IntegerType.INTEGER.create(1))
		.add(IntegerType.INTEGER.create(2))
		.build();
		Object[] pArray = new Object[]{
				IntegerType.INTEGER.create(1), 
				IntegerType.INTEGER.create(2), 
				null};
		expect(spec.getNumberOfParams()).andReturn(3);
		expect(spec.isVariadic()).andReturn(true).times(2);
		expect(inv0.invoke(pArray)).andReturn(IntegerType.INTEGER.create(1));
		c.replay();
		ValueExpression v = f0.invoke(spec, context, p);
		assertEquals(IntegerType.INTEGER.create(1), v);
		c.verify();
	}
}
