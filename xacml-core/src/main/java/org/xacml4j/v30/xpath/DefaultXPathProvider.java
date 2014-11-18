package org.xacml4j.v30.xpath;

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

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xacml4j.util.NodeNamespaceContext;

import com.google.common.base.Preconditions;

public class DefaultXPathProvider implements XPathProvider
{
	private final static Logger log = LoggerFactory.getLogger(DefaultXPathProvider.class);

    private XPathFactory xpathFactory;

    public DefaultXPathProvider(){
        this(XPathFactory.newInstance());
    }

    public DefaultXPathProvider(XPathFactory xpathFactory){
        Preconditions.checkNotNull(xpathFactory);
        this.xpathFactory = xpathFactory;
    }

    @Override
	public NodeList evaluateToNodeSet(String path, Node context)
			throws XPathEvaluationException {
        return evaluate(path, context, null, XPathConstants.NODESET);
    }

    @Override
    public Node evaluateToNode(String path, Node context)
            throws XPathEvaluationException
    {
        return evaluate(path, context, null, XPathConstants.NODESET);
    }

	@Override
	public String evaluateToString(String path, Node context)
			throws XPathEvaluationException {
		return evaluate(path, context, null, XPathConstants.STRING);
	}

	@Override
	public Number evaluateToNumber(String path, Node context)
			throws XPathEvaluationException {
        return evaluate(path, context, null, XPathConstants.NUMBER);
	}

    private <T> T evaluate(String path,
                            Node context,
                            NamespaceContext nsCtx,
                            QName resultType)
            throws XPathEvaluationException
    {
        Preconditions.checkArgument(path != null);
        Preconditions.checkArgument(context != null);
        try
        {
            if(log.isDebugEnabled()){
                log.debug("EvaluateToNode XPath=\"{}\"", path);
            }
            XPath xpath = xpathFactory.newXPath();
            xpath.setNamespaceContext(nsCtx == null?new NodeNamespaceContext(context):nsCtx);
            Object result = (Object)xpath.evaluate(path, context, resultType);
            return (T)result;
        }catch(XPathExpressionException e){
            if(log.isDebugEnabled()){
                log.debug(path, e);
            }
            throw new XPathEvaluationException(path, context, e);
        }
    }

}
