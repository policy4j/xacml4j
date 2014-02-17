package org.xacml4j.v30.types;

import org.xacml4j.v30.RFC822Name;

public final class RFC822NameExp extends BaseAttributeExp<RFC822Name>
{
	private static final long serialVersionUID = -1983511364298319436L;

	public RFC822NameExp(RFC822Name value) {
		super(RFC822NameType.RFC822NAME, value);
	}
}
