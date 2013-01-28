package org.xacml4j.v30.spi.pip;

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
