package org.xacml4j.v30.content;

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


import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.net.MediaType;
import com.sun.org.apache.xpath.internal.NodeSet;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xacml4j.util.DOMUtil;
import org.xacml4j.util.NodeNamespaceContext;
import org.xacml4j.v30.AttributeExpType;
import org.xacml4j.v30.BagOfAttributeExp;
import org.xacml4j.v30.types.XPathExp;
import org.xacml4j.v30.xpath.XPathEvaluationException;
import org.xml.sax.InputSource;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.Reader;
import java.io.Writer;
import java.util.Set;

public class XmlContentParser implements ContentProvider
{
    private final static org.slf4j.Logger log = LoggerFactory.getLogger(XmlContentParser.class);

    private final static ImmutableSet<MediaType>
            MIME_TYPES = ImmutableSet
            .<MediaType>builder()
            .add(MediaType.APPLICATION_XML_UTF_8)
            .add(MediaType.XML_UTF_8)
            .build();

    private static DocumentBuilderFactory DOCUMENTBUILDER_FACTORY;
    private static TransformerFactory TRANSFORMER_FACTORY;
    private static XPathFactory XPATH_PROVIDER;

    static {
        try {
            TRANSFORMER_FACTORY = TransformerFactory.newInstance();
            DOCUMENTBUILDER_FACTORY = DocumentBuilderFactory.newInstance();
            DOCUMENTBUILDER_FACTORY.setNamespaceAware(true);
            XPATH_PROVIDER = XPathFactory.newInstance();
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }

    private DocumentBuilderFactory builderFactory = DOCUMENTBUILDER_FACTORY;
    private XPathFactory xpathFactory = XPATH_PROVIDER;
    private TransformerFactory transformerFactory = TRANSFORMER_FACTORY;


    @Override
    public Set<MediaType> getSupportedMediaTypes() {
        return MIME_TYPES;
    }



    @Override
    public Content from(Reader input) throws Exception {
        Preconditions.checkNotNull(input);
        try
        {
            DocumentBuilder documentBuilder = builderFactory.newDocumentBuilder();
            // no need to copy DOM
            return new XmlNode(documentBuilder.parse(new InputSource(input)), false);
        }
        catch (ParserConfigurationException e) {
            throw new IllegalStateException(String.format("Failed to build %s",
                    DocumentBuilder.class.getName()), e);
        }
        catch (Exception e) {
            throw new IllegalArgumentException(String.format(
                    "Failed to parse DOM from string: \"%s\"",
                    input), e);
        }
    }

    private <T> T evaluate(XPathExp xpathExp,
                           Node context,
                           QName resultType)
            throws XPathEvaluationException
    {
        Preconditions.checkArgument(xpathExp != null);
        Preconditions.checkArgument(context != null);
        Preconditions.checkArgument(resultType != null);
        try
        {
            if(log.isDebugEnabled()){
                log.debug("Evaluating XPath=\"{}\", " +
                                "result type=\"{}\"",
                        xpathExp, resultType);
            }
            XPath xpath = xpathFactory.newXPath();
            xpath.setNamespaceContext(xpath.getNamespaceContext() == null?
                    new NodeNamespaceContext(context):xpath.getNamespaceContext());
            Object value = xpath.evaluate(xpathExp.getPath(), context, resultType);
            if(log.isDebugEnabled()){
                log.debug("XPath=\"{}\" result=\"{}\"",
                        xpathExp, value);
            }
            return (T)value;
        }catch(XPathExpressionException e){
            if(log.isDebugEnabled()){
                log.debug(xpathExp.getPath(), e);
            }
            throw new XPathEvaluationException(xpathExp.getPath(),
                    context, e);
        }
    }

    public class XmlNode implements Content
    {
        private Node node;

        XmlNode(Node source,
                boolean copyContent){
            Preconditions.checkNotNull(source);
            this.node = copyContent?DOMUtil.copyNode(source).getDocumentElement():source;
        }

        @Override
        public MediaType getMediaType() {
            return MediaType.XML_UTF_8;
        }

        @Override
        public BagOfAttributeExp select(XPathExp xpathExp, AttributeExpType type)
                throws Exception{
            NodeList nodeSet = evaluate(xpathExp,  node, XPathConstants.NODESET);
            if(nodeSet == null ||
                    nodeSet.getLength() == 0){
                return type.emptyBag();
            }
            return DOMUtil.toBag(xpathExp.getPath(), type, nodeSet);
        }

        @Override
        public Iterable<Content> selectNodes(XPathExp xpath)
                throws Exception {
            NodeList set = evaluate(xpath,  node, XPathConstants.NODESET);
            ImmutableList.Builder<Content> b = ImmutableList.builder();
            for(int i = 0; i < set.getLength(); i++){
                b.add(new XmlNode(set.item(i), false));
            }
            return b.build();
        }

        /**
         * Gets content node path/location
         *
         * @return a content node path/location
         */
        public String getPath() {
            return DOMUtil.getXPath(node);
        }

        @Override
        public Content selectNode(XPathExp xpath) throws Exception {
            Node childNode = evaluate(xpath, node, XPathConstants.NODE);
            return new XmlNode(childNode, false);
        }

        @Override
        public String toString(){
            return Objects.toStringHelper(this)
                    .add("xml", DOMUtil.toString(node))
                    .toString();
        }

        @Override
        public int hashCode(){
            return node.hashCode();
        }

        @Override
        public boolean equals(Object o)
        {
            if(o == this){
                return true;
            }
            if(!(o instanceof XmlNode)){
                return false;
            }
            XmlNode c = (XmlNode)o;
            return DOMUtil.isEqual(node, c.node);
        }

        @Override
        public void to(Writer writer) throws Exception {
            DOMUtil.nodeToString(node, new StreamResult(writer));
        }
    }
}

