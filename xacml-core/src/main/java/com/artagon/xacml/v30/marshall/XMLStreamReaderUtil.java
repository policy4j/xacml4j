package com.artagon.xacml.v30.marshall;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import com.google.common.base.Strings;

public class XMLStreamReaderUtil 
{
	public static Boolean getBooleanAttributeValue(String localName,
			XMLStreamReader r) throws XMLStreamException {
		String v = r.getAttributeValue(null, localName);
		return Strings.isNullOrEmpty(v) ? null : Boolean.parseBoolean(v);
	}

	public static Integer getIntegerAttributeValue(String localName,
			XMLStreamReader r) throws XMLStreamException {
		String v = r.getAttributeValue(null, localName);
		return Strings.isNullOrEmpty(v) ? null : Integer.parseInt(v);
	}

	public static String getStringAttributeValue(String localName,
			XMLStreamReader r) throws XMLStreamException {
		return Strings.emptyToNull(r.getAttributeValue(null, localName));
	}
	
	public static void writeStartElement(
			QName name, XMLStreamWriter w)
	{
		writeStartElement(name.getPrefix(), name, w);
	}
	
	public static void writeCharacters(String v, XMLStreamWriter w)
	{
		try{
			if(!Strings.isNullOrEmpty(v)){
				w.writeCharacters(v);
			}
		}catch(XMLStreamException e){
			throw new IllegalStateException(e);
		}
	}
	
	public static void writeAttribute(
			String name, String v, XMLStreamWriter w)
	{
		try{
			if(!Strings.isNullOrEmpty(v)){
				w.writeAttribute(name, v);
			}
		}catch(XMLStreamException e){
			throw new IllegalStateException(e);
		}
	}
	
	public static void writeEndElement(XMLStreamWriter w){
		try{
			w.writeEndElement();
		}catch(XMLStreamException e){
			throw new IllegalStateException(e);
		}
	}
	
	public static void writeStartElement(String prefix, QName name, 
			XMLStreamWriter w)
	{
		try{
			w.writeStartElement(prefix, name.getLocalPart(), name.getNamespaceURI());
		}catch(XMLStreamException e){
			throw new IllegalStateException(e);
		}
	}
	
	public static void writeEmptyElement(QName name, 
			XMLStreamWriter w){
		writeEmptyElement(name.getPrefix(), name, w);
	}
	
	public static void writeEmptyElement(String prefix, QName name, 
			XMLStreamWriter w)
	{
		try{
			w.writeEmptyElement(prefix, name.getLocalPart(), name.getNamespaceURI());
		}catch(XMLStreamException e){
			throw new IllegalStateException(e);
		}
	}
}
