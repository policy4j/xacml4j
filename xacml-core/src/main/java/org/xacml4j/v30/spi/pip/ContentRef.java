package org.xacml4j.v30.spi.pip;

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

import com.google.common.base.MoreObjects;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xacml4j.util.DOMUtil;

import com.google.common.base.Preconditions;
import org.xacml4j.v30.Content;

import java.security.Timestamp;
import java.time.Clock;
import java.time.LocalDateTime;

public final class ContentRef
{
	private Content content;
	private ContentResolverDescriptor d;
	private long timestamp;

	private ContentRef(Builder b)
	{
		this.content = b.content;
		this.d = b.d;
		this.timestamp = b.ticker.millis();
	}

	public static Builder builder(){
		return new Builder();
	}

	public Content getContent(){
		return content;
	}

	public long getTimestamp(){
		return timestamp;
	}

	public ContentResolverDescriptor getDescriptor(){
		return d;
	}

	@Override
	public String toString(){
		return MoreObjects
				.toStringHelper(this)
				.add("resolverId", d.getId())
				.add("timestamp", timestamp)
				.add("content",content.asString())
				.add("contentType", content.getType())
				.toString();
	}

	public static class Builder
	{
		private Clock ticker = Clock.systemUTC();
		private ContentResolverDescriptor d;
		private Content content;

		public Builder content(Content node) {
			Preconditions.checkNotNull(node);
			this.content = node;
			return this;
		}

		public Builder clock(Clock ticker) {
			this.ticker = ticker;
			return this;
		}

		public Builder resolver(ContentResolverDescriptor d){
			Preconditions.checkNotNull(d);
			this.d = d;
			return this;
		}

		public Builder resolver(Resolver<ContentRef> r){
			Preconditions.checkNotNull(r);
			this.d = r.getDescriptor();
			return this;
		}

		public ContentRef build(){
			return new ContentRef(this);
		}
	}
}
