package org.xacml4j.v30.content;

/*
 * #%L
 * Xacml4J Core Engine Implementation
 * %%
 * Copyright (C) 2009 - 2019 Xacml4J.org
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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Attr;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ProcessingInstruction;
import org.w3c.dom.Text;
import org.xacml4j.util.DOMUtil;
import org.xacml4j.v30.AttributeReferenceEvaluationException;
import org.xacml4j.v30.AttributeSelectorKey;
import org.xacml4j.v30.BagOfValues;
import org.xacml4j.v30.Content;
import org.xacml4j.v30.types.*;
import org.xacml4j.v30.EvaluationException;
import org.xacml4j.v30.PathEvaluationException;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

/**
 * {@link Content} implementation for XML content type
 *
 * @author Giedrius Trumpickas
 */
public final class XmlContent implements Content
{
    public static String CONTENT_SELECTOR = "urn:oasis:names:tc:xacml:3.0:content-selector";

    private final static Logger LOG = LoggerFactory.getLogger(XmlContent.class);

    private Document contextNode;

    private XPathProvider xPathProvider;

    private XmlContent(Node content,
                       XPathProvider xPathProvider){
        Objects.requireNonNull(content, "content");
        Preconditions.checkArgument(content instanceof Element || content instanceof Document, "Element or Document nodes only");
        this.contextNode = DOMUtil.copyNode(content.getNodeType() == Node.DOCUMENT_NODE?((Document)content).getDocumentElement():(Element)content);
        this.xPathProvider = Objects.requireNonNull(xPathProvider, "xPathProvider");
    }


    public String getNodeLocalName(){
        return contextNode.getLocalName();
    }

    public String getNodeNamespaceURI(){
        return contextNode.getNamespaceURI();
    }

    private static Node checkValidNode(Node content){
        Objects.requireNonNull(content, "content");
        if(!(content.getNodeType() == Node.DOCUMENT_NODE)){
            throw new IllegalArgumentException(
                    String.format("Only Document DOM nodes are supported"));
        }
        return content;
    }


    public static Node fromString(String xml){
        return DOMUtil.parseXml(xml).get();
    }

    public static Node fromStream(InputStream xml){
        return DOMUtil.parseXml(xml).get();
    }

    /**
     * Creates {@link XmlContent} from a given {@link Node}
     *
     * @param node an DOM node
     * @return {@link XmlContent} defaultProvider
     */
    public static  XmlContent of(Supplier<Node> supplier){
        return of(supplier, XPathProvider.defaultProvider());
    }

    public static  XmlContent of(Supplier<Node> supplier, XPathProvider xPathProvider){
        return new XmlContent(supplier.get(), xPathProvider);
    }

    public static  XmlContent of(String xmlDoc){
        return of(()->fromString(xmlDoc));
    }

    public static  XmlContent of(Node xmlDoc){
        return of(()->xmlDoc);
    }

    public static  XmlContent of(InputStream inputStream){
        return of(()->fromStream(inputStream));
    }



    @Override
    public Type getType() {
        return Type.XML_UTF8;
    }

    public boolean equals(Object o){
        if(o == this){
            return true;
        }
        if(!(o instanceof XmlContent)){
            return false;
        }
        XmlContent xml = (XmlContent)o;
        return DOMUtil.isEqual(contextNode, xml.contextNode);
    }

    public Object toNode(){
        return contextNode;
    }

    public String asString(){
        return DOMUtil.serializeToXmlString(
                contextNode, true);
    }

    public int hashCode(){
        return contextNode.hashCode();
    }

    public String toString(){
        return MoreObjects.toStringHelper(this)
                .add("content", asString())
                .add("type", getType())
                .toString();

    }

    @Override
    public Optional<BagOfValues> resolve(AttributeSelectorKey selectorKey) throws EvaluationException {
        return getAttributeValues(
                selectorKey,
                null);
    }

    @Override
    public Optional<BagOfValues> resolve(AttributeSelectorKey selectorKey,
                                         Supplier<Entity> entitySupplier) throws EvaluationException {
        return getAttributeValues(
                selectorKey,
                entitySupplier);
    }

    private Optional<BagOfValues> getAttributeValues(
            AttributeSelectorKey selectorKey,
            Supplier<Entity> callback)
		    throws EvaluationException
    {
        try
        {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Evaluating selector={}", selectorKey);
            }
            Node context = null;
            if(callback != null) {
                Optional<Path> v = callback.get()
                        .findValue((selectorKey.getContextSelectorId() == null ? CONTENT_SELECTOR : selectorKey.getContextSelectorId()),
                        XacmlTypes.XPATH);
                if (!v.isPresent()) {
                    throw PathEvaluationException.invalidXpathContextSelectorId(selectorKey.getPath(),
                                                                                selectorKey.getContextSelectorId());
                }
                Path xpath = v.get();
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Evaluating " +
                            "contextSelector xpath=\"{}\"", xpath);
                }
                if(!Objects.equals(xpath.getCategory().orElse(null), selectorKey.getCategory())){
                    throw AttributeReferenceEvaluationException.forSelector(selectorKey,
                                                                            ()->String.format("and ContextSelectorId.Category=\"%s\"",
                                    xpath.getCategory().orElse(null)));
                }
                context = xPathProvider.evaluateToNode(xpath.getPath(), contextNode);
            }
            NodeList nodeSet = xPathProvider.evaluateToNodeSet(selectorKey.getPath(), context == null?contextNode:context);
            if(nodeSet == null ||
                    nodeSet.getLength() == 0){
                return Optional.empty();
            }
            if(LOG.isDebugEnabled()){
                LOG.debug("Found=\"{}\" nodes via xpath=\"{}\"",
                        new Object[]{nodeSet.getLength(),
                                selectorKey.getPath()});
            }
            return toBag(
                    selectorKey.getPath(),
                    selectorKey.getDataType(),
                    nodeSet);
        }
        catch(PathEvaluationException e){
            if(LOG.isDebugEnabled()){
                LOG.debug(e.getMessage(), e);
            }
            throw e;
        }
        catch(Exception e){
            if(LOG.isDebugEnabled()){
                LOG.debug(e.getMessage(), e);
            }
            throw PathEvaluationException.invalidXpath(selectorKey.getPath(), e);
        }
    }

    @Override
    public <T> List<T> evaluateToNodeSet(String path) {
        try{
            if (LOG.isDebugEnabled()) {
                LOG.debug("Evaluating path={}", path);
            }
            NodeList nodeList = xPathProvider.evaluateToNodeSet(path, contextNode);
            List<T> r = Optional.ofNullable(nodeList)
                                .map(list->(List<T>)DOMUtil.nodeListToList(list))
                                .orElse(Collections.emptyList());
            if (LOG.isDebugEnabled()) {
                LOG.debug("Evaluation of path=\"{}\" result=\"{}\"", path, r);
            }
            return r;

        }catch (PathEvaluationException e){
            LOG.debug(e.getMessage(), e);
            throw e;
        }catch (Exception e){
            LOG.debug(e.getMessage(), e);
            throw PathEvaluationException.invalidXpath(path, e);
        }
    }

    @Override
    public List<String> evaluateToNodePathList(String path) {
        try{
            if (LOG.isDebugEnabled()) {
                LOG.debug("Evaluating path={}", path);
            }
             NodeList nodeList =  xPathProvider.evaluateToNodeSet(path, contextNode);
             List<String> paths = new ArrayList<>(nodeList.getLength());
             for(int i = 0; i < nodeList.getLength(); i++){
                 paths.add(DOMUtil.getXPath(nodeList.item(i)));
             }
            if (LOG.isDebugEnabled()) {
                LOG.debug("Evaluation of path=\"{}\" result=\"{}\"", path, paths);
            }
             return paths;
        }catch (PathEvaluationException e){
            LOG.debug(e.getMessage(), e);
            throw e;
        }catch (Exception e){
            throw PathEvaluationException.invalidXpath(path, e);
        }
    }

    @Override
    public Optional<String> evaluateToNodePath(String path) {
        try{
            if (LOG.isDebugEnabled()) {
                LOG.debug("Evaluating path={}", path);
            }
            Optional<String> r = Optional.ofNullable(
                    xPathProvider.evaluateToNode(path, contextNode))
                    .map(DOMUtil::getXPath);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Evaluation of path=\"{}\" result=\"{}\"", path, r);
            }
            return r;
        }catch (PathEvaluationException e){
            LOG.debug(e.getMessage(), e);
            throw e;
        }catch (Exception e){
            LOG.debug(e.getMessage(), e);
            throw PathEvaluationException.invalidXpath(path, e);
        }
    }

    @Override
    public Optional<String> evaluateToString(String path) {
        try{
            if (LOG.isDebugEnabled()) {
                LOG.debug("Evaluating path={}", path);
            }
            Optional<String> r = Optional.ofNullable(xPathProvider
                                                             .evaluateToString(path, contextNode));
            if (LOG.isDebugEnabled()) {
                LOG.debug("Evaluation of path=\"{}\" result=\"{}\"", path, r);
            }
            return r;
        }catch (PathEvaluationException e){
            LOG.debug(e.getMessage(), e);
            throw e;
        }catch (Exception e){
            LOG.debug(e.getMessage(), e);
            throw PathEvaluationException.invalidXpath(path, e);
        }
    }

    @Override
    public <T> Optional<T> evaluateToNode(String path) {
        try{
            if (LOG.isDebugEnabled()) {
                LOG.debug("Evaluating path={}", path);
            }
            Optional<T> r = (Optional<T>) Optional.ofNullable(xPathProvider
                    .evaluateToNode(path, contextNode));
            if (LOG.isDebugEnabled()) {
                LOG.debug("Evaluation of path=\"{}\" result=\"{}\" node path=\"{}\"",
                          path, r, r.map(Node.class::cast)
                                    .map(v->DOMUtil.getXPath(v)).orElse(null));
            }
            return r;
        }catch (PathEvaluationException e){
            LOG.debug(e.getMessage(), e);
            throw e;
        }catch (Exception e){
            LOG.debug(e.getMessage(), e);
            throw PathEvaluationException.invalidXpath(path, e);
        }
    }

    @Override
    public <T extends Number> Optional<T> evaluateToNumber(String path) {
        try{
            if (LOG.isDebugEnabled()) {
                LOG.debug("Evaluating path={}", path);
            }
            Optional<T> r = (Optional<T>) Optional.ofNullable(xPathProvider
                    .evaluateToNumber(path, contextNode));
            if (LOG.isDebugEnabled()) {
                LOG.debug("Evaluation of path=\"{}\" result=\"{}\"", path, r);
            }
            return r;
        }catch (PathEvaluationException e){
            LOG.debug(e.getMessage(), e);
            throw e;
        }catch (Exception e){
            LOG.debug(e.getMessage(), e);
            throw PathEvaluationException.invalidXpath(path, e);
        }
    }

    /**
     * Converts a given node list to the {@link BagOfValues}
     *
     * @param xpath XPath for nodes
     * @param type attribute type
     * @param nodeSet a node set
     * @return {@link BagOfValues}
     * @throws EvaluationException
     */
    private Optional<BagOfValues> toBag(String xpath,
                                        ValueType type, NodeList nodeSet)
            throws PathEvaluationException
    {
        BagOfValues.Builder b = type.bagBuilder();
        for(int i = 0; i< nodeSet.getLength(); i++)
        {
            try
            {
                Node n = nodeSet.item(i);
                b.attribute(convertNodeToXacml(xpath, n, type));
            } catch (PathEvaluationException e) {
                throw e;
            } catch (Exception e) {
                throw PathEvaluationException.invalidXpath(xpath, e);
            }
        }
        return Optional.of(
                b.build());
    }

    /**
     * Tries to convert given DOM {@link Node} to XACML data type value
     * @param xpath an xpath
     * @param n a node
     * @param type a XACML type
     * @return {@Link AttributeValue} of the given type
     * @throws PathEvaluationException if an error occurs
     */
    private Value convertNodeToXacml(
            String xpath, Node n, ValueType type)
    {
        String v;
        switch (n.getNodeType()) {
            case Node.TEXT_NODE:
                v = ((Text) n).getData();
                break;
            case Node.PROCESSING_INSTRUCTION_NODE:
                v = ((ProcessingInstruction) n).getData();
                break;
            case Node.ATTRIBUTE_NODE:
                v = ((Attr) n).getValue();
                break;
            case Node.COMMENT_NODE:
                v = ((Comment) n).getData();
                break;
            default:
                throw PathEvaluationException.invalidXpath(
                        xpath,
                        String.format("Unsupported DOM node for xpath=\"%s\" domType=\"%s\"",
                                      xpath, DOMUtil.toString(n)));
        }
        Optional<TypeToString> toString = TypeToString.forType(type);
        if (!toString.isPresent()) {
            throw PathEvaluationException.invalidXpath(xpath,
                    String.format("Unsupported XACML type={} xpath=\"%s%\"",
                                  type.getTypeId(), xpath));
        }
        Value value = toString.get().fromString(v);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Node of domType=\"{}\" converted attribute=\"{}\"",
                    DOMUtil.toString(n), value);
        }
        return value;
    }
}
