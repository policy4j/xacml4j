package org.xacml4j.v30;

import java.util.LinkedList;
import java.util.List;

import com.google.common.collect.Iterables;

public class StatusDetail
{
	private List<Object> content;

	public StatusDetail(Iterable<Object> detail){
		this.content = new LinkedList<Object>();
		Iterables.addAll(content, detail);
	}
}
