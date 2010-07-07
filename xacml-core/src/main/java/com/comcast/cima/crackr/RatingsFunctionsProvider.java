package com.comcast.cima.crackr;

import com.artagon.xacml.v3.spi.function.XacmlFuncParam;
import com.artagon.xacml.v3.spi.function.XacmlFuncReturnType;
import com.artagon.xacml.v3.spi.function.XacmlFuncSpec;
import com.artagon.xacml.v3.spi.function.XacmlFunctionProvider;
import com.artagon.xacml.v3.types.XacmlDataTypes;
import com.artagon.xacml.v3.types.BooleanType.BooleanValue;
import com.artagon.xacml.v3.types.StringType.StringValue;
import com.google.common.base.Preconditions;

@XacmlFunctionProvider(description="Provides MPAA Movie and V-Chip TV ratings related functions")
public class RatingsFunctionsProvider 
{
	@XacmlFuncSpec(id="urn:comcast:names:tc:xacml:1.0:function:mpaa-is-greater-than")
	@XacmlFuncReturnType(type=XacmlDataTypes.BOOLEAN)
	public static BooleanValue isGreaterThanMPAA(
			@XacmlFuncParam(type=XacmlDataTypes.STRING)StringValue a, 
			@XacmlFuncParam(type=XacmlDataTypes.STRING)StringValue b)
	{
		MPAARatings ar = MPAARatings.parse(a.getValue());
		Preconditions.checkArgument(ar != null, 
				"Given value=\"%s\" can not be converted to MPAA rating", 
				a.getValue());
		MPAARatings br = MPAARatings.parse(b.getValue());
		Preconditions.checkArgument(br != null, 
				"Given value=\"%s\" can not be converted to MPAA rating", 
				b.getValue());
		int r = ar.compareTo(br);
		return XacmlDataTypes.BOOLEAN.create(r > 0);
	}
	
	
	@XacmlFuncSpec(id="urn:comcast:names:tc:xacml:1.0:function:vchip-is-greater-than")
	@XacmlFuncReturnType(type=XacmlDataTypes.BOOLEAN)
	public static BooleanValue isGreaterThanVChip(
			@XacmlFuncParam(type=XacmlDataTypes.STRING) StringValue a, 
			@XacmlFuncParam(type=XacmlDataTypes.STRING)StringValue b)
	{
		VChipRatings ar = VChipRatings.parse(a.getValue());
		Preconditions.checkArgument(ar != null, 
				"Given value=\"%s\" can not be converted to V-Chip rating", 
				a.getValue());
		VChipRatings br = VChipRatings.parse(b.getValue());
		Preconditions.checkArgument(ar != null, 
				"Given value=\"%s\" can not be converted to V-Chip rating", 
				b.getValue());
		int r = ar.compareTo(br);
		return XacmlDataTypes.BOOLEAN.create(r > 0);
	}
	
	
	public enum MPAARatings implements Comparable<MPAARatings>
	{
		G("G"),
		PG("PG"),
		PG13("PG13"),
		R("R"),
		NC17("NC17");
		
		private String id;
		
		private MPAARatings(String id){
			this.id = id;
		}
		
		/**
		 * Creates {@link MPAARatings} instance
		 * from a given string
		 * @param v a textual representation 
		 * of MPAA rating
		 * @return {@link MPAARatings} or <code>null</code>
		 */
		public static MPAARatings parse(String v)
		{
			for(MPAARatings r : values()){
				if(r.id.equalsIgnoreCase(v)){
					return r;
				}
			}
			return null;
		}
	}

	public enum VChipRatings
	{
		TV_Y("TV-Y"),
		TV_Y7("TV-Y7"),
		TV_Y7_FV("TV-Y7"),
		TV_G("TV-G"),
		TV_PG("TV-PG"),
		TV_14("TV-14"),
		TV_MA("TV-MA");
		
		private String id;
		
		private VChipRatings(String id){
			this.id = id;
		}
		
		/**
		 * Creates {@link VChipRatings} instance
		 * from a given string
		 * @param v a textual representation 
		 * of V-Chip rating
		 * @return {@link VChipRatings} or <code>null</code>
		 */
		public static VChipRatings parse(String v)
		{
			for(VChipRatings r : values()){
				if(r.id.equalsIgnoreCase(v)){
					return r;
				}
			}
			return null;
		}
		
		@Override
		public String toString(){
			return id;
		}
	}
}
