package com.artagon.xacml.v30.pdp;

import java.util.LinkedList;
import java.util.List;

import com.google.common.collect.Iterables;


public class StatusDetail extends XacmlObject
{
	private List<Object> content;
	
	public StatusDetail(Iterable<Object> detail){
		this.content = new LinkedList<Object>();
		Iterables.addAll(content, detail);
	}
}
