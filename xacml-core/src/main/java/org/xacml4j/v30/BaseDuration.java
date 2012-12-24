package org.xacml4j.v30;

import java.io.Serializable;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;

import com.google.common.base.Preconditions;

public abstract class BaseDuration <T extends BaseDuration<?>> 
	implements Comparable<T>, Serializable
{
	private static final long serialVersionUID = 6573551346951236604L;

	private Duration value;
	
	protected static DatatypeFactory df = null;
	
	static{
		try{
			df = DatatypeFactory.newInstance();
		}catch(DatatypeConfigurationException e){
		}
	}
	
	protected BaseDuration(Duration value){
		Preconditions.checkNotNull(value);
		this.value = value;
	}
	
	protected static Duration parseDuration(Object any)
	{
		if(String.class.isInstance(any)){
			return df.newDuration((String)any);
		}
		return (Duration)any;
	}
	
	public final boolean isPositive(){
		return value.getSign() == 1;
	}
	
	public final boolean isNegative(){
		return value.getSign() == -1;
	}
	
	public final boolean isZero(){
		return value.getSign() == 0;
	}
	
	final Duration getDuration(){
		return value;
	}
	
	public final String toString(){
		return value.toString();
	}
	
	@Override
	public final int hashCode(){
		return value.hashCode();
	}
	
	@Override
	public final boolean equals(Object o){
		if(o == null){
			return false;
		}
		if(o == this){
			return true;
		}
		if(!(o instanceof BaseDuration)){
			return false;
		}
		BaseDuration<?> d = (BaseDuration<?>)o;
		return value.equals(d.value);
	}

	@Override
	public final int compareTo(T o) {
		return value.compare(o.value);
	}
	
	public final T add(T d){
		return makeDuration(getDuration().add(d.getDuration()));
	}
	
	public final T substract(T d){
		return makeDuration(getDuration().subtract(d.getDuration()));
	}
	
	public final T negate(){
		return makeDuration(getDuration().negate());
	}
	
	protected abstract T makeDuration(Duration d);
}
