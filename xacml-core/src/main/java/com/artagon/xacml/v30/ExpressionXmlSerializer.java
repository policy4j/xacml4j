package com.artagon.xacml.v30;

import javax.xml.stream.XMLStreamWriter;

public class ExpressionXmlSerializer 
{
	private String namespaceURI;
	
	public ExpressionXmlSerializer(String namespaceURI){
		this.namespaceURI = namespaceURI;
	}
	
	public void serialize(final Expression exp, 
			final XMLStreamWriter w)
	{
		exp.accept(new DefaultExpressionVisitor()
		{
			private ExpressionTraverser t;
			
			@Override
			public void visit(Apply v) {
				t.traverse(v, this);
			}
		});
	}
	
	
}
