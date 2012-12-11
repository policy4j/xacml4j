package com.artagon.xacml.v30.pdp;

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.artagon.xacml.v30.EvaluationContext;
import com.artagon.xacml.v30.MatchResult;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

public class MatchAnyOf
	implements PolicyElement, Matchable
{
	private final static Logger log = LoggerFactory.getLogger(MatchAllOf.class);

	private Collection<MatchAllOf> anyOfs;

	private MatchAnyOf(Builder b){
		this.anyOfs = b.allMatchAllOfs.build();
		Preconditions.checkArgument(anyOfs.size() >= 1);
	}

	public Collection<MatchAllOf> getAllOf(){
		return anyOfs;
	}

	public static Builder builder(){
		return new Builder();
	}

	@Override
	public MatchResult match(EvaluationContext context)
	{
		MatchResult state = MatchResult.NOMATCH;
		for(Matchable m : anyOfs){
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
		for(Matchable m : anyOfs){
			m.accept(v);
		}
		v.visitLeave(this);
	}

	@Override
	public String toString(){
		return Objects.toStringHelper(this)
				.add("AllOfs", anyOfs)
				.toString();
	}

	@Override
	public int hashCode(){
		return anyOfs.hashCode();
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
		return anyOfs.equals(m.anyOfs);
	}



	public static class Builder{
		private ImmutableList.Builder<MatchAllOf> allMatchAllOfs = ImmutableList.builder();

		private Builder(){
		}

		public Builder anyOf(MatchAllOf ...allOf){
			Preconditions.checkNotNull(allOf);
			this.allMatchAllOfs.add(allOf);
			return this;
		}

		public Builder anyOf(Iterable<MatchAllOf> allOfs){
			this.allMatchAllOfs.addAll(allOfs);
			return this;
		}


		public MatchAnyOf build(){
			return new MatchAnyOf(this);
		}
	}
}
