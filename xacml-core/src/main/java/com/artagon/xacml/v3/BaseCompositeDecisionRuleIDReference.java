package com.artagon.xacml.v3;

import com.google.common.base.Preconditions;

abstract class BaseCompositeDecisionRuleIDReference extends XacmlObject 
	implements CompositeDecisionRule, DecisionRuleIDReference
{
	private String id;
	private VersionMatch version;
	private VersionMatch earliest;
	private VersionMatch latest;
	
	protected BaseCompositeDecisionRuleIDReference(String id, 
			VersionMatch version, 
			VersionMatch earliest, 
			VersionMatch latest){
		Preconditions.checkNotNull(id);
		this.id = id;
		this.version = version;
		this.latest = latest;
		this.earliest = earliest;
	}
	
	@Override
	public final String getId(){
		return id;
	}

	
	public final VersionMatch getEarliestVersion() {
		return earliest;
	}


	public final VersionMatch getLatestVersion() {
		return latest;
	}

	public final VersionMatch getVersion() {
		return version;
	}
	
	public boolean matches(String id,  Version v)
	{
		return this.id.equals(id) &&( (version == null || version.match(v)) &&
		(earliest == null || earliest.match(v)) &&
		(latest == null || latest.match(v)));
	}
}
