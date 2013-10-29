package org.xacml4j.v30.pdp;

import org.xacml4j.v30.XPathVersion;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;

abstract class BaseCompositeDecisionRuleDefaults
	implements PolicyElement
{
	public static final String XPATH_VERSION = "XPathVersion";

	protected ImmutableMap<String, Object> values;

	protected BaseCompositeDecisionRuleDefaults(
			BaseCompositeDecisionRuleDefaultsBuilder<?> b){
		Preconditions.checkNotNull(b);
		this.values = b.values.build();
	}

	public final XPathVersion getXPathVersion(){
		return (XPathVersion)values.get(XPATH_VERSION);
	}

	@SuppressWarnings("unchecked")
	public final <T> T getValue(String name){
		return (T)values.get(name);
	}

	@Override
	public String toString(){
		return Objects
				.toStringHelper(this)
				.add("values", values)
				.toString();
	}

	@Override
	public int hashCode(){
		return values.hashCode();
	}

	public static abstract class BaseCompositeDecisionRuleDefaultsBuilder<T extends BaseCompositeDecisionRuleDefaultsBuilder<?>>
	{
		private ImmutableMap.Builder<String, Object> values = ImmutableMap.builder();

		public T xpathVersion(String xpathVersion){
			XPathVersion v = null;
			if(xpathVersion != null){
				v = XPathVersion.parse(xpathVersion);
			}
			values.put(XPATH_VERSION, v == null?XPathVersion.XPATH1:v);
			return getThis();
		}

		public T value(String name, Object v){
			this.values.put(name, v);
			return getThis();
		}

		protected abstract T getThis();
	}
}
