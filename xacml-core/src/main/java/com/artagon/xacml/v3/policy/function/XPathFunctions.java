package com.artagon.xacml.v3.policy.function;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.artagon.xacml.util.Xacml20XPathTo30Transformer;
import com.artagon.xacml.v3.EvaluationContext;
import com.artagon.xacml.v3.EvaluationException;
import com.artagon.xacml.v3.spi.function.XacmlFuncParam;
import com.artagon.xacml.v3.spi.function.XacmlFuncParamEvaluationContext;
import com.artagon.xacml.v3.spi.function.XacmlFuncReturnType;
import com.artagon.xacml.v3.spi.function.XacmlFuncSpec;
import com.artagon.xacml.v3.spi.function.XacmlFunctionProvider;
import com.artagon.xacml.v3.types.BooleanType;
import com.artagon.xacml.v3.types.BooleanType.BooleanValue;
import com.artagon.xacml.v3.types.IntegerType;
import com.artagon.xacml.v3.types.IntegerType.IntegerValue;
import com.artagon.xacml.v3.types.StringType.StringValue;
import com.artagon.xacml.v3.types.XPathExpressionType.XPathExpressionValue;
import com.artagon.xacml.v3.types.XacmlDataTypes;

/**
 * This class implements functions that take XPath expressions for arguments. 
 * An XPath expression evaluates to a node-set, which is a set of XML nodes 
 * that match the expression. A node or node-set is not in the formal data-type 
 * system of XACML. All comparison or other operations on node-sets are performed 
 * in isolation of the particular function specified. The context nodes and namespace mappings 
 * of the XPath expressions are defined by the XPath data-type, see section B.3 (XACML 3.0 core). 
 * 
 * @author Giedrius Trumpickas
 */
@XacmlFunctionProvider(description="XACML XPath functions")
public class XPathFunctions 
{
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:function:xpath-node-count")
	@XacmlFuncReturnType(type=XacmlDataTypes.INTEGER)
	public static IntegerValue xpathCount(
			@XacmlFuncParamEvaluationContext EvaluationContext context,
			@XacmlFuncParam(type=XacmlDataTypes.XPATHEXPRESSION) XPathExpressionValue xpath) 
	{
		try{
			NodeList nodes = context.evaluateToNodeSet(xpath.getValue(), xpath.getCategory());
			if(nodes != null){
				return IntegerType.Factory.create(nodes.getLength());
			}
			return IntegerType.Factory.create(0);
		}catch(EvaluationException e){
			return IntegerType.Factory.create(0);
		}
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:xpath-node-count")
	@XacmlFuncReturnType(type=XacmlDataTypes.INTEGER)
	public static IntegerValue xpathCountXacml2(
			@XacmlFuncParamEvaluationContext EvaluationContext context,
			@XacmlFuncParam(type=XacmlDataTypes.STRING) StringValue xpath) 
	{
		return xpathCount(context, 
				Xacml20XPathTo30Transformer.fromXacml20String(xpath));
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:function:xpath-node-equal")
	@XacmlFuncReturnType(type=XacmlDataTypes.BOOLEAN)
	public static BooleanValue xpathNodeEqual(
			@XacmlFuncParamEvaluationContext EvaluationContext context,
			@XacmlFuncParam(type=XacmlDataTypes.XPATHEXPRESSION) XPathExpressionValue xpath0,
			@XacmlFuncParam(type=XacmlDataTypes.XPATHEXPRESSION) XPathExpressionValue xpath1) 
	{		
		try{
			NodeList nodes0 = context.evaluateToNodeSet(xpath0.getValue(), xpath0.getCategory());
			NodeList nodes1 = context.evaluateToNodeSet(xpath1.getValue(), xpath1.getCategory());
			if(nodes0 == null || 
					nodes0  == null){
				return BooleanType.Factory.create(false);
			}
			for(int i = 0; i < nodes0.getLength(); i++){
				for(int j = 0; j < nodes1.getLength(); j++){
					if(nodes0.item(i).isSameNode(nodes1.item(j))){
						return BooleanType.Factory.create(true);
					}
				}
			}
			return BooleanType.Factory.create(false);
		}catch(EvaluationException e){
			return BooleanType.Factory.create(false);
		}
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:xpath-node-equal")
	@XacmlFuncReturnType(type=XacmlDataTypes.BOOLEAN)
	public static BooleanValue xpathNodeEqualXacml20(
			@XacmlFuncParamEvaluationContext EvaluationContext context,
			@XacmlFuncParam(type=XacmlDataTypes.STRING) StringValue xpath0,
			@XacmlFuncParam(type=XacmlDataTypes.STRING) StringValue xpath1)
	{
		return xpathNodeEqual(context, 
				Xacml20XPathTo30Transformer.fromXacml20String(xpath0),
				Xacml20XPathTo30Transformer.fromXacml20String(xpath1));
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:function:xpath-node-match")
	@XacmlFuncReturnType(type=XacmlDataTypes.BOOLEAN)
	public static BooleanValue xpathNodeMatch(
			@XacmlFuncParamEvaluationContext EvaluationContext context,
			@XacmlFuncParam(type=XacmlDataTypes.XPATHEXPRESSION) XPathExpressionValue xpath0,
			@XacmlFuncParam(type=XacmlDataTypes.XPATHEXPRESSION) XPathExpressionValue xpath1) 
	{		
		try{
			NodeList nodes0 = context.evaluateToNodeSet(xpath0.getValue(), 
					xpath0.getCategory());
			NodeList nodes1 = context.evaluateToNodeSet(xpath1.getValue(), 
					xpath1.getCategory());
			if(nodes0 == null || 
					nodes0  == null){
				return BooleanType.Factory.create(false);
			}
			for(int i = 0; i < nodes0.getLength(); i++)
			{
				for(int j = 0; j < nodes1.getLength(); j++)
				{
					if(nodes0.item(i).isSameNode(nodes1.item(j))){
						return BooleanType.Factory.create(true);
					}
					NamedNodeMap a = nodes0.item(i).getAttributes();
					NamedNodeMap b = nodes1.item(j).getAttributes();
					if((a != null && b != null)){
						for(int ii = 0; ii < a.getLength(); ii++){
							for(int jj = 0; jj < b.getLength(); jj++){
								if(a.item(ii).isSameNode(b.item(jj))){
									return BooleanType.Factory.create(true);
								}
							}
						}
					}
					if(compareChildNodes(nodes0.item(i), nodes1.item(j))){
						return BooleanType.Factory.create(true);
					}
				}
			}
			return BooleanType.Factory.create(false);
		}catch(EvaluationException e){
			return BooleanType.Factory.create(false);
		}
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:xpath-node-match")
	@XacmlFuncReturnType(type=XacmlDataTypes.BOOLEAN)
	public static BooleanValue xpathNodeMatchXacml20(
			@XacmlFuncParamEvaluationContext EvaluationContext context,
			@XacmlFuncParam(type=XacmlDataTypes.STRING) StringValue xpath0,
			@XacmlFuncParam(type=XacmlDataTypes.STRING) StringValue xpath1) 
	{
		return xpathNodeMatch(context, 
				Xacml20XPathTo30Transformer.fromXacml20String(xpath0),
				Xacml20XPathTo30Transformer.fromXacml20String(xpath1));
	}
	
	/**
	 * Recursively compares the child nodes of the first 
	 * element with the second element
	 * 
	 * @param a a first node to search recursively
	 * @param b a second node to compare to
	 * @return <code>true</code>, if some of the child nodes 
	 * of the first node is equals to the second node
	 */
	private static boolean compareChildNodes(Node a, Node b)
	{
		NodeList c = a.getChildNodes();
		if(c == null || c.getLength() == 0){
			return false;
		}
		for(int i = 0; i < c.getLength(); i++){
			if(c.item(i).isSameNode(b) || 
					compareChildNodes(c.item(i), b)){
				return true;
			}
		}
		return false;
	}
}
