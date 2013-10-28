package org.xacml4j.v30.pdp;

import org.xacml4j.v30.CompositeDecisionRuleIDReference;
import org.xacml4j.v30.Version;
import org.xacml4j.v30.VersionMatch;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

public abstract class BaseCompositeDecisionRuleIDReference
	implements CompositeDecisionRuleIDReference
{
	private String id;
	private VersionMatch version;
	private VersionMatch earliest;
	private VersionMatch latest;

	private int hashCode;

	protected BaseCompositeDecisionRuleIDReference(BaseCompositeDecisionRuleIDReferenceBuilder<?> b){
		Preconditions.checkNotNull(b.id,
				"Decision rule identifier can not be null");
		this.id = b.id;
		this.version = b.version;
		this.latest = b.latest;
		this.earliest = b.earliest;
		this.hashCode = Objects.hashCode(id, version, latest, earliest);
	}

	@Override
	public final String getId(){
		return id;
	}

	/**
	 * Gets earliest version match
	 *
	 * @return {@link VersionMatch} or <code>null</code>
	 */
	@Override
	public final VersionMatch getEarliestVersion() {
		return earliest;
	}

	/**
	 * Gets latest version match
	 *
	 * @return {@link VersionMatch} or <code>null</code>
	 */
	@Override
	public final VersionMatch getLatestVersion() {
		return latest;
	}

	/**
	 * Gets version match
	 *
	 * @return {@link VersionMatch} or <code>null</code>
	 */
	@Override
	public final VersionMatch getVersion() {
		return version;
	}

	/**
	 * Matches a given identifier and version
	 * against this reference
	 *
	 * @param id an identifier
	 * @param v a version
	 * @return <code>true</code> if given identifier
	 * and version matches this reference version constraints
	 */
	protected boolean matches(String id, Version v) {
		return this.id.equals(id) &&( (version == null || version.match(v)) &&
				(earliest == null || earliest.match(v)) &&
				(latest == null || latest.match(v)));
	}

	@Override
	public final String toString(){
		return Objects.toStringHelper(this)
		.add("id", id)
		.add("version", version)
		.add("earliest", earliest)
		.add("latest", latest).toString();
	}

	@Override
	public final int hashCode(){
		return hashCode;
	}

	public static abstract class BaseCompositeDecisionRuleIDReferenceBuilder<T extends BaseCompositeDecisionRuleIDReferenceBuilder<?>>
	{
		private String id;
		private VersionMatch version;
		private VersionMatch earliest;
		private VersionMatch latest;

		public T id(String identifier){
			Preconditions.checkArgument(!Strings.isNullOrEmpty(identifier));
			this.id = identifier;
			return getThis();
		}

		public T versionAsString(String version){
			if(Strings.isNullOrEmpty(version)){
				this.version = null;
				return getThis();
			}
			this.version = VersionMatch.parse(version);
			return getThis();
		}

		public T version(Version version){
			if(version == null){
				this.version = null;
				return getThis();
			}
			this.version = VersionMatch.parse(version.getValue());
			return getThis();
		}

		public T versionMatch(VersionMatch version){
			if(version == null){
				this.version = null;
				return getThis();
			}
			this.version = version;
			return getThis();
		}

		/**
		 * Sets latest version if given latest
		 * version is <code>null</code> or empty string
		 * latest version is set to <code>null</code>
		 *
		 * @param version a latest version textual representation
		 */
		public T latest(String version){
			if(Strings.isNullOrEmpty(version)){
				this.latest = null;
				return getThis();
			}
			this.latest = VersionMatch.parse(version);
			return getThis();
		}

		public T earliest(String version){
			if(Strings.isNullOrEmpty(version)){
				this.earliest = null;
				return getThis();
			}
			this.earliest = VersionMatch.parse(version);
			return getThis();
		}

		protected abstract T getThis();
	}
}
