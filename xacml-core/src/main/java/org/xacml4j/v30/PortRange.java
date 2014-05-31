package org.xacml4j.v30;

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

import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

public final class PortRange implements Serializable
{
	private static final long serialVersionUID = -6044059747919356340L;

	private final static Logger log = LoggerFactory.getLogger(PortRange.class);

	private final Integer lowerBound;
	private final Integer upperBound;

	/**
	 * Default constructor used to represent an unbound range. This is typically
	 * used when an address has no port information.
	 */
	public PortRange() {
		this(null, null);
	}

	/**
	 * Creates a {@code PortRange} with upper and lower bounds. Either of
	 * the parameters may have the value {@code UNBOUND} meaning that there
	 * is no bound at the respective end.
	 *
	 * @param lowerBound
	 *            the lower-bound port number or {@code UNBOUND}
	 * @param upperBound
	 *            the upper-bound port number or {@code UNBOUND}
	 */
	public PortRange(Integer lowerBound, Integer upperBound) {
		this.lowerBound = lowerBound;
		this.upperBound = upperBound;
	}


	/**
	 * Creates a {@code PortRange} that represents a single port value
	 * instead of a range of values.
	 *
	 * @param singlePort
	 *            the single port number
	 */
	PortRange(Integer singlePort) {
		this(singlePort, singlePort);
	}

	public static PortRange getAnyPort(){
		return new PortRange();
	}

	public static PortRange getSinglePort(Integer port){
		return new PortRange(port);
	}

	public static PortRange getRangeFrom(Integer port){
		return new PortRange(port, null);
	}

	public static PortRange getRangeUntil(Integer port){
		return new PortRange(null, port);
	}

	public static PortRange getRange(Integer lower, Integer upper){
		return new PortRange(lower, upper);
	}


	/**
	 * Creates an instance of {@code PortRange} based on the given value.
	 *
	 * @param value
	 *            a {@code String} representing the range
	 *
	 * @return a new {@code PortRange}
	 *
	 * @throws NumberFormatException
	 *             if a port value isn't an integer
	 */
	public static PortRange valueOf(int index, String value)
	{
		if (value == null || (value.length() == 0) ||
				(value.equals("-"))){
			log.debug("Found any port range in the " +
					"given value=\"{}\" at index=\"{}\"", value, index);
			return getAnyPort();
		}
		int dashPos = value.indexOf('-', index);
		if (dashPos == -1) {
			log.debug("Found single port in the " +
					"given value=\"{}\" at index=\"{}\"", value, index);
			return getSinglePort(
					Integer.parseInt(value.substring(index, value.length())));
		}
		if (dashPos == index) {
			log.debug("Found range until in " +
					"the given value=\"{}\" at index=\"{}\"", value, index);
			return getRangeUntil(
					Integer.parseInt(value.substring(index + 1)));
		}
		if (dashPos == (value.length() - 1)) {
			log.debug("Found range from in the " +
					"given value=\"{}\" at index=\"{}\"", value, index);
			return getRangeFrom(Integer.parseInt(value.substring(index, dashPos)));
		}
		Integer lowerBound = Integer.parseInt(value.substring(index, dashPos));
		Integer upperBound = Integer.parseInt(value.substring(dashPos + 1, value.length()));
		return PortRange.getRange(lowerBound, upperBound);
	}

	public static PortRange valueOf(String v){
		return valueOf(0, v);
	}

	/**
	 * Returns the lower-bound port value. If the range is not lower-bound, then
	 * this returns {@code UNBOUND}. If the range is actually a single port
	 * number, then this returns the same value as {@code getUpperBound}.
	 *
	 * @return the upper-bound
	 */
	public int getLowerBound() {
		Preconditions.checkState(lowerBound != null);
		return lowerBound;
	}

	/**
	 * Returns the upper-bound port value. If the range is not upper-bound, then
	 * this returns {@code UNBOUND}. If the range is actually a single port
	 * number, then this returns the same value as {@code getLowerBound}.
	 *
	 * @return the upper-bound
	 */
	public int getUpperBound() {
		Preconditions.checkState(upperBound != null);
		return upperBound;
	}

	/**
	 * Returns whether the range is bounded by a lower port number.
	 *
	 * @return true if lower-bounded, false otherwise
	 */
	public boolean isLowerBounded() {
		return (lowerBound != null);
	}

	/**
	 * Returns whether the range is bounded by an upper port number.
	 *
	 * @return true if upper-bounded, false otherwise
	 */
	public boolean isUpperBounded() {
		return (upperBound != null);
	}

	/**
	 * Tests if a given port is in this range
	 *
	 * @param port a positive number indicating port
	 * @return {@code true} if a given port is
	 * in this range
	 */
	public boolean contains(int port){
		Preconditions.checkArgument(port > 0);
		return (isLowerBounded()?lowerBound <= port:true) &&
		(isUpperBounded()?port <= upperBound:true);
	}

	public boolean contains(PortRange range){
		return contains(range.getLowerBound()) &&
		contains(range.getUpperBound());
	}

	/**
	 * Returns whether the range is actually a single port number.
	 *
	 * @return true if the range is a single port number, false otherwise
	 */
	public boolean isSinglePort() {
		return isLowerBounded() && isUpperBounded() && lowerBound.equals(upperBound);
	}

	/**
	 * Returns whether the range is unbound, which means that it specifies no
	 * port number or range. This is typically used with addresses that include
	 * no port information.
	 *
	 * @return true if the range is unbound, false otherwise
	 */
	public boolean isUnbound() {
		return (lowerBound == null) && (upperBound == null);
	}

	/**
	 * Returns true if the input is an instance of this class and if its value
	 * equals the value contained in this class.
	 *
	 * @param o
	 *            the object to compare
	 *
	 * @return true if this object and the input represent the same value
	 */
	@Override
	public boolean equals(Object o) {
		if(o == this){
			return true;
		}
		if (!(o instanceof PortRange)){
			return false;
		}
		PortRange other = (PortRange) o;
		return Objects.equal(lowerBound, other.lowerBound) &&
		Objects.equal(upperBound, other.upperBound);
	}

	@Override
	public int hashCode(){
		return Objects.hashCode(lowerBound, upperBound);
	}

	@Override
	public String toString()
	{
		if (isUnbound()){
			return "";
		}
		StringBuilder b = new StringBuilder();
		if (isSinglePort()){
			b.append(getLowerBound());
			return b.toString();
		}
		if(isLowerBounded()){
			b.append(lowerBound).append('-');
		}
		if(!isLowerBounded()){
			b.append('-');
		}
		if (isUpperBounded()){
			b.append(getUpperBound());
		}
		return b.toString();
	}
}
