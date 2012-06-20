package com.artagon.xacml.v30.policy.function;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.artagon.xacml.util.Xacml20XPathTo30Transformer;
import com.artagon.xacml.v30.EvaluationContext;
import com.artagon.xacml.v30.EvaluationException;
import com.artagon.xacml.v30.spi.function.XacmlFuncParam;
import com.artagon.xacml.v30.spi.function.XacmlFuncParamEvaluationContext;
import com.artagon.xacml.v30.spi.function.XacmlFuncReturnType;
import com.artagon.xacml.v30.spi.function.XacmlFuncSpec;
import com.artagon.xacml.v30.spi.function.XacmlFunctionProvider;
import com.artagon.xacml.v30.types.BooleanExp;
import com.artagon.xacml.v30.types.BooleanType;
import com.artagon.xacml.v30.types.IntegerExp;
import com.artagon.xacml.v30.types.IntegerType;
import com.artagon.xacml.v30.types.StringExp;
import com.artagon.xacml.v30.types.XPathExp;

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
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#integer")
	public static IntegerExp xpathCount(
			@XacmlFuncParamEvaluationContext EvaluationContext context,
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:3.0:data-type:xpathExpression") XPathExp xpath) 
	{
		try{
			NodeList nodes = context.evaluateToNodeSet(xpath.getPath(), xpath.getCategory());
			if(nodes != null){
				return IntegerType.INTEGER.create(nodes.getLength());
			}
			return IntegerType.INTEGER.create(0);
		}catch(EvaluationException e){
			return IntegerType.INTEGER.create(0);
		}
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:xpath-node-count")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#integer")
	public static IntegerExp xpathCountXacml2(
			@XacmlFuncParamEvaluationContext EvaluationContext context,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string") StringExp xpath) 
	{
		return xpathCount(context, 
				Xacml20XPathTo30Transformer.fromXacml20String(xpath));
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:function:xpath-node-equal")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanExp xpathNodeEqual(
			@XacmlFuncParamEvaluationContext EvaluationContext context,
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:3.0:data-type:xpathExpression") XPathExp xpath0,
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:3.0:data-type:xpathExpression") XPathExp xpath1) 
	{		
		try{
			NodeList nodes0 = context.evaluateToNodeSet(xpath0.getPath(), xpath0.getCategory());
			NodeList nodes1 = context.evaluateToNodeSet(xpath1.getPath(), xpath1.getCategory());
			if(nodes0 == null || 
					nodes0  == null){
				return BooleanType.BOOLEAN.create(false);
			}
			for(int i = 0; i < nodes0.getLength(); i++){
				for(int j = 0; j < nodes1.getLength(); j++){
					if(nodes0.item(i).isSameNode(nodes1.item(j))){
						return BooleanType.BOOLEAN.create(true);
					}
				}
			}
			return BooleanType.BOOLEAN.create(false);
		}catch(EvaluationException e){
			return BooleanType.BOOLEAN.create(false);
		}
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:xpath-node-equal")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanExp xpathNodeEqualXacml20(
			@XacmlFuncParamEvaluationContext EvaluationContext context,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string") StringExp xpath0,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string") StringExp xpath1)
	{
		return xpathNodeEqual(context, 
				Xacml20XPathTo30Transformer.fromXacml20String(xpath0),
				Xacml20XPathTo30Transformer.fromXacml20String(xpath1));
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:function:xpath-node-match")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanExp xpathNodeMatch(
			@XacmlFuncParamEvaluationContext EvaluationContext context,
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:3.0:data-type:xpathExpression") XPathExp xpath0,
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:3.0:data-type:xpathExpression") XPathExp xpath1) 
	{		
		try{
			NodeList nodes0 = context.evaluateToNodeSet(xpath0.getPath(), 
					xpath0.getCategory());
			NodeList nodes1 = context.evaluateToNodeSet(xpath1.getPath(), 
					xpath1.getCategory());
			if(nodes0 == null || 
					nodes0  == null){
				return BooleanType.BOOLEAN.create(false);
			}
			for(int i = 0; i < nodes0.getLength(); i++)
			{
				for(int j = 0; j < nodes1.getLength(); j++)
				{
					if(nodes0.item(i).isSameNode(nodes1.item(j))){
						return BooleanType.BOOLEAN.create(true);
					}
					NamedNodeMap a = nodes0.item(i).getAttributes();
					NamedNodeMap b = nodes1.item(j).getAttributes();
					if((a != null && b != null)){
						for(int ii = 0; ii < a.getLength(); ii++){
							for(int jj = 0; jj < b.getLength(); jj++){
								if(a.item(ii).isSameNode(b.item(jj))){
									return BooleanType.BOOLEAN.create(true);
								}
							}
						}
					}
					if(compareChildNodes(nodes0.item(i), nodes1.item(j))){
						return BooleanType.BOOLEAN.create(true);
					}
				}
			}
			return BooleanType.BOOLEAN.create(false);
		}catch(EvaluationException e){
			return BooleanType.BOOLEAN.create(false);
		}
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:xpath-node-match")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanExp xpathNodeMatchXacml20(
			@XacmlFuncParamEvaluationContext EvaluationContext context,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string") StringExp xpath0,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string") StringExp xpath1) 
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
