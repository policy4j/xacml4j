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

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xacml4j.util.DOMUtil;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Ticker;

public final class Content
{
	private Node content;
	private ContentResolverDescriptor d;
	private long timestamp;

	private Content(Builder b)
	{
		this.content = b.content;
		this.d = b.d;
		this.timestamp = b.ticker.read();
	}

	public static Builder builder(){
		return new Builder();
	}

	public Node getContent(){
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
		return Objects.toStringHelper(this)
		.add("id", d.getId())
		.add("content", DOMUtil.toString((Element)content))
		.toString();
	}

	public static class Builder
	{
		private Ticker ticker = Ticker.systemTicker();
		private ContentResolverDescriptor d;
		private Node content;

		public Builder content(Node node){
			Preconditions.checkNotNull(content);
			this.content = node;
			return this;
		}

		public Builder ticker(Ticker ticker){
			this.ticker = ticker;
			return this;
		}

		public Builder resolver(ContentResolverDescriptor d){
			Preconditions.checkNotNull(d);
			this.d = d;
			return this;
		}

		public Builder resolver(ContentResolver r){
			Preconditions.checkNotNull(r);
			this.d = r.getDescriptor();
			return this;
		}

		public Content build(){
			return new Content(this);
		}
	}
}
