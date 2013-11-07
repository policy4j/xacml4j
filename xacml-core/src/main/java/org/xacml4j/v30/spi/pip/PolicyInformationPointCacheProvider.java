package org.xacml4j.v30.spi.pip;

import org.w3c.dom.Node;

public interface PolicyInformationPointCacheProvider
{
	/**
	 * Gets content from the cache for a given resolver context
	 *
	 * @param context resolver context
	 * @return {@link Node} or @{code null}
	 */
	Content getContent(ResolverContext context);

	void putContent(ResolverContext context, Content content);

	AttributeSet getAttributes(ResolverContext context);

	void putAttributes(ResolverContext context, AttributeSet v);
}
