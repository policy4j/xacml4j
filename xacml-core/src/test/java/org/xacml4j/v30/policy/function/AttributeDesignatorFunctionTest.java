package org.xacml4j.v30.policy.function;

import org.easymock.IMocksControl;
import static org.easymock.EasyMock.*;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Node;
import org.xacml4j.util.DOMUtil;
import org.xacml4j.v30.Categories;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.pdp.FunctionSpec;
import org.xacml4j.v30.spi.function.AnnotiationBasedFunctionProvider;
import org.xacml4j.v30.spi.function.FunctionProvider;
import org.xacml4j.v30.spi.xpath.XPathProvider;
import org.xacml4j.v30.types.AnyURIExp;
import org.xacml4j.v30.types.BooleanExp;
import org.xacml4j.v30.types.XacmlTypes;

public class AttributeDesignatorFunctionTest 
{
	
	
	private String testXml = "<md:record xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
			"xmlns:md=\"urn:example:med:schemas:record\">" +
			"<md:patient>" +
			"<md:patientDoB>1992-03-21</md:patientDoB>" +
			"<md:patient-number>555555</md:patient-number>" +
			"</md:patient>" +
			"</md:record>";

	private EvaluationContext context;
	private XPathProvider xpathProvider;
	private FunctionProvider provider;
	private IMocksControl c;
	private Node content;
	
	@Before
	public void init() throws Exception{
		this.c = createControl();
		this.context = c.createMock(EvaluationContext.class);
		this.content = DOMUtil.stringToNode(testXml);
		this.provider = new AnnotiationBasedFunctionProvider(AttributeDesignatorFunctions.class);
	}
	
	@Test
	public void testDesignatorFunction(){
		FunctionSpec func = provider.getFunction("urn:oasis:names:tc:xacml:3.0:function:attribute-designator");
	 
		expect(context.isValidateFuncParamsAtRuntime()).andReturn(false);
		c.replay();
		func.invoke(context, 
				AnyURIExp.valueOf(Categories.SUBJECT_ACCESS.getId()),
				AnyURIExp.valueOf("testId"),
				AnyURIExp.valueOf(XacmlTypes.STRING.getDataTypeId()),
				BooleanExp.valueOf(false),
				null);
		c.verify();
	}
}
