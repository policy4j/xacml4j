package com.artagon.xacml.v30.pdp;

import com.artagon.xacml.v30.CompositeDecisionRuleIDReference;
import com.artagon.xacml.v30.Version;
import com.artagon.xacml.v30.VersionMatch;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

abstract class BaseCompositeDecisionRuleIDReference
	implements CompositeDecisionRuleIDReference
{
	private String id;
	private VersionMatch version;
	private VersionMatch earliest;
	private VersionMatch latest;

	private int hashCode;

	protected BaseCompositeDecisionRuleIDReference(String id,
			VersionMatch version,
			VersionMatch earliest,
			VersionMatch latest){
		Preconditions.checkNotNull(id,
				"Decision rule identifier can not be null");
		this.id = id;
		this.version = version;
		this.latest = latest;
		this.earliest = earliest;
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
}
