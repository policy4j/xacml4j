package com.artagon.xacml.v30.pdp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.artagon.xacml.v30.EvaluationContext;
import com.artagon.xacml.v30.MatchResult;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

public class MatchAnyOf 
	implements PolicyElement, Matchable
{
	private final static Logger log = LoggerFactory.getLogger(MatchAllOf.class);
	
	private Collection<MatchAllOf> matches;
	
	public MatchAnyOf(Collection<MatchAllOf> allOffs){
		Preconditions.checkNotNull(allOffs);
		Preconditions.checkArgument(allOffs.size() >= 1);
		this.matches = new ArrayList<MatchAllOf>(allOffs.size());
		for(MatchAllOf allOff : allOffs){
			Preconditions.checkNotNull(allOff);
			this.matches.add(allOff);
		}
	}
	
	public MatchAnyOf(MatchAllOf ...allOfs){
		this(Arrays.asList(allOfs));
	}
	
	public Collection<MatchAllOf> getAllOf(){
		return Collections.unmodifiableCollection(matches);
	}
	
	public static Builder builder(){
		return new Builder();
	}
	
	@Override
	public MatchResult match(EvaluationContext context) 
	{
		MatchResult state = MatchResult.NOMATCH;
		for(Matchable m : matches){
			MatchResult result = m.match(context);
			if(result == MatchResult.INDETERMINATE){
				if(log.isDebugEnabled()){
					log.debug("AnyOf matchable " +
							"match result=\"{}\"", result);
				}
				state = MatchResult.INDETERMINATE;
				continue;
			}
			if(result == MatchResult.MATCH){
				if(log.isDebugEnabled()){
					log.debug("AnyOf matchable match result=\"{}\"", result);
				}
				state = MatchResult.MATCH;
				break;
			}
		}
		return state;
	}

	@Override
	public void accept(PolicyVisitor v) {		
		v.visitEnter(this);
		for(Matchable m : matches){
			m.accept(v);
		}
		v.visitLeave(this);
	}	
	
	@Override
	public String toString(){
		return Objects.toStringHelper(this)
				.add("AllOfs", matches)
				.toString();
	}
	
	@Override
	public int hashCode(){
		return matches.hashCode();
	}
	
	@Override
	public boolean equals(Object o){
		if(o == null){
			return false;
		}
		if(o == this){
			return true;
		}
		if(!(o instanceof MatchAnyOf)){
			return false;
		}
		MatchAnyOf m = (MatchAnyOf)o;
		return matches.equals(m.matches);
	}
	
	
	
	public static class Builder{
		private Collection<MatchAllOf> allMatchAllOfs = new LinkedList<MatchAllOf>();
		
		private Builder(){
		}
		
		public Builder withAllOf(MatchAllOf allOf){
			Preconditions.checkNotNull(allOf);
			this.allMatchAllOfs.add(allOf);
			return this;
		}
		
		public Builder withAllOf(MatchAllOf.Builder allOfBuilder){
			Preconditions.checkNotNull(allOfBuilder);
			this.allMatchAllOfs.add(allOfBuilder.create());
			return this;
		}
		
		public Builder withAllOf(Match ...matches){
			Preconditions.checkNotNull(matches);
			this.allMatchAllOfs.add(new MatchAllOf(matches));
			return this;
		}
		
		public MatchAnyOf create(){
			return new MatchAnyOf(allMatchAllOfs);
		}
	}
}
