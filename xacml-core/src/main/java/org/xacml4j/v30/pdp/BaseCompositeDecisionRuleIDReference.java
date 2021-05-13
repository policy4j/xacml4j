package org.xacml4j.v30.pdp;

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

import org.xacml4j.v30.CompositeDecisionRuleIDReference;
import org.xacml4j.v30.Version;
import org.xacml4j.v30.VersionMatch;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

public abstract class BaseCompositeDecisionRuleIDReference
	implements CompositeDecisionRuleIDReference
{
	private final String id;
	private final VersionMatch version;
	private final VersionMatch earliest;
	private final VersionMatch latest;

	private final int hashCode;

	protected BaseCompositeDecisionRuleIDReference(Builder<?> b){
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
	 * @return {@link VersionMatch} or {@code null}
	 */
	@Override
	public final VersionMatch getEarliestVersion() {
		return earliest;
	}

	/**
	 * Gets latest version match
	 *
	 * @return {@link VersionMatch} or {@code null}
	 */
	@Override
	public final VersionMatch getLatestVersion() {
		return latest;
	}

	/**
	 * Gets version match
	 *
	 * @return {@link VersionMatch} or {@code null}
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
	 * @return {@code true} if given identifier
	 * and version matches this reference version constraints
	 */
	protected boolean matches(String id, Version v) {
		return this.id.equals(id) &&( (version == null || version.match(v)) &&
				(earliest == null || earliest.match(v)) &&
				(latest == null || latest.match(v)));
	}

	@Override
	public final String toString() {
		return Objects.toStringHelper(this)
		.add("id", id)
		.add("version", version)
		.add("earliest", earliest)
		.add("latest", latest).toString();
	}

	@Override
	public final int hashCode() {
		return hashCode;
	}

	protected boolean equalsTo(BaseCompositeDecisionRuleIDReference r) {
		return id.equals(r.id)
			&& Objects.equal(version, r.version)
			&& Objects.equal(earliest, r.earliest)
			&& Objects.equal(latest, r.latest);
	}

	public static abstract class Builder<T extends Builder<?>>
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
			this.version = version;
			return getThis();
		}

		/**
		 * Sets latest version if given latest
		 * version is {@code null} or empty string
		 * latest version is set to {@code null}
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
