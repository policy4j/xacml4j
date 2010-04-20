package com.artagon.xacml.v3;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.artagon.xacml.util.Preconditions;

public class RequestProcessingPipeline 
{
	private final static Logger log = LoggerFactory.getLogger(RequestProcessingPipeline.class);
	
	private List<RequestProcessingProfile> profiles;
	
	public RequestProcessingPipeline(){
		this.profiles = new LinkedList<RequestProcessingProfile>();
	}
	
	public void addProfile(RequestProcessingProfile profile){
		Preconditions.checkNotNull(profile);
		this.profiles.add(profile);
	}
	
	public Collection<Result> process(Request request, PolicyDecisionPointCallback pdp)
	{
		Collection<Result> results = new LinkedList<Result>();
		log.debug("Pipeline has=\"{}\" profiles", profiles.size());
		for(int index = 0; index < profiles.size(); index++)
		{
			RequestProcessingProfile profile = profiles.get(index);
			log.debug("Processing profile=\"{}\" at index=\"{}\"", 
					profile.getId(), index);
			results.addAll(profile.process(request, new PipelineCallback(index + 1, pdp)));
		}
		return results.isEmpty()?Collections.singleton(pdp.decide(request)):results;
	}
	
	public class PipelineCallback 
		implements RequestProcessingCallback
	{
		private int index;
		private PolicyDecisionPointCallback pdp;
		
		public PipelineCallback(int index, 
				PolicyDecisionPointCallback pdp){
			Preconditions.checkArgument(index >= 0);
			Preconditions.checkNotNull(pdp);
			this.index = index;
			this.pdp = pdp;
		}
		
		@Override
		public Collection<Result> invokeNext(Request request) 
		{
			if(index >= profiles.size()){
				log.debug("No more profiles to invoke at index=\"{}\", invoking PDP", index);
				return Collections.singleton(pdp.decide(request));
			}
			RequestProcessingProfile profile = profiles.get(index);
			log.debug("Invoking profile=\"{}\" at index=\"{}\" via callback", 
					profile.getId(), index);
			index++;
			return profile.process(request, this);
			
		}
	}
}
