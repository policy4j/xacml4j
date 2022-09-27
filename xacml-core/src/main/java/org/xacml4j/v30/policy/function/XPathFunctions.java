package org.xacml4j.v30.policy.function;

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

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xacml4j.v30.EvaluationException;
import org.xacml4j.v30.XmlContent;
import org.xacml4j.v30.spi.function.*;
import org.xacml4j.v30.types.*;

import java.util.List;
import java.util.Optional;


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
public final class XPathFunctions
{
	/** Private constructor for utility class */
	private XPathFunctions() {}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:function:xpath-node-count")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#integer")
	public static IntegerValue xpathCount(
			@XacmlEvaluationContextParam org.xacml4j.v30.EvaluationContext context,
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:3.0:data-type:xpathExpression") PathValue path)
	{
		try{
			return XacmlTypes.INTEGER.of(context
							.resolve(
									 path.getCategory(),
									 path.getContentType())
							.map(
									c -> c.evaluateToNodeSet(
											path.getPath())
											.size())
					.orElse(0));
		}catch(EvaluationException e){
			return XacmlTypes.INTEGER.of(0);
		}
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:xpath-node-count")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#integer")
	public static IntegerValue xpathCountXacml2(
			@XacmlEvaluationContextParam org.xacml4j.v30.EvaluationContext context,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string") StringValue xpath)
	{
		return xpathCount(context,
				Xacml20XPathTo30Transformer.fromXacml20String(xpath));
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:function:xpath-node-equal")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValue xpathNodeEqual(
			@XacmlEvaluationContextParam org.xacml4j.v30.EvaluationContext context,
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:3.0:data-type:xpathExpression") PathValue path0,
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:3.0:data-type:xpathExpression") PathValue path1)
	{
		try{

			Optional<List<Node>> content0 =  getNodes(context, path0);
			Optional<List<Node>> content1 =  getNodes(context, path1);

			if(!content0.isPresent() ||
					!content1.isPresent()){
				return XacmlTypes.BOOLEAN.of(false);
			}
			List<Node> nodes0 = content0.get();
			List<Node> nodes1 = content1.get();
			for(int i = 0; i < nodes0.size(); i++){
				for(int j = 0; j < nodes1.size(); j++){
					if(nodes0.get(i).isSameNode(nodes1.get(j))){
						return XacmlTypes.BOOLEAN.of(true);
					}
				}
			}
			return XacmlTypes.BOOLEAN.of(false);
		}catch(EvaluationException e){
			return XacmlTypes.BOOLEAN.of(false);
		}
	}


	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:xpath-node-equal")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValue xpathNodeEqualXacml20(
			@XacmlEvaluationContextParam org.xacml4j.v30.EvaluationContext context,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string") StringValue xpath0,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string") StringValue xpath1)
	{
		return xpathNodeEqual(context,
				Xacml20XPathTo30Transformer.fromXacml20String(xpath0),
				Xacml20XPathTo30Transformer.fromXacml20String(xpath1));
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:function:xpath-node-match")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValue xpathNodeMatch(
			@XacmlEvaluationContextParam org.xacml4j.v30.EvaluationContext context,
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:3.0:data-type:xpathExpression") PathValue path0,
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:3.0:data-type:xpathExpression") PathValue path1)
	{
		try{
			Optional<List<Node>> content0 =  getNodes(context, path0);
			Optional<List<Node>> content1 =  getNodes(context, path1);
			if(!content0.isPresent()||
					content1.isPresent()){
				return XacmlTypes.BOOLEAN.of(false);
			}
			List<Node> nodes0 = content0.get();
			List<Node> nodes1 = content1.get();
			for(int i = 0; i < nodes0.size(); i++)
			{
				for(int j = 0; j < nodes1.size(); j++)
				{
					if(nodes0.get(i).isSameNode(nodes1.get(j))){
						return XacmlTypes.BOOLEAN.of(true);
					}
					Node node0 = nodes0.get(i);
					Node node1 = nodes1.get(j);
					NamedNodeMap a = node0.getAttributes();
					NamedNodeMap b = node1.getAttributes();
					if((a != null && b != null)){
						for(int ii = 0; ii < a.getLength(); ii++){
							for(int jj = 0; jj < b.getLength(); jj++){
								if(a.item(ii).isSameNode(b.item(jj))){
									return XacmlTypes.BOOLEAN.of(true);
								}
							}
						}
					}
					if(compareChildNodes(node0, node1)){
						return XacmlTypes.BOOLEAN.of(true);
					}
				}
			}
			return XacmlTypes.BOOLEAN.of(false);
		}catch(EvaluationException e){
			return XacmlTypes.BOOLEAN.of(false);
		}
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:xpath-node-match")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValue xpathNodeMatchXacml20(
			@XacmlEvaluationContextParam org.xacml4j.v30.EvaluationContext context,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string") StringValue xpath0,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string") StringValue xpath1)
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
	 * @return {@code true}, if some of the child nodes
	 * of the first node is equals to the second node
	 */
	static boolean compareChildNodes(Node a, Node b)
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

	static Optional<List<Node>> getNodes(org.xacml4j.v30.EvaluationContext context,
                                         PathValue path){
		return context
				.<XmlContent>resolve(
						path.getCategory(), path.getContentType())
				.map(c->c.evaluateToNodeSet(path.getPath()));
	}
}
