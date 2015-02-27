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

import java.util.Iterator;
import java.util.List;

import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;

import com.google.common.collect.ImmutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class VersionMatch
{
    private final static Logger log = LoggerFactory.getLogger(VersionMatch.class);

    private final static CharMatcher ANY_NUM = CharMatcher.is('*');
    private final static CharMatcher ANY_SUB_NUM = CharMatcher.is('+');

    private final String pattern;
    private List<MatchToken> match;

    /**
     * Constructs version match constraint
     * from a given string representation.
     * A version match is '.'-separated,
     * like a version string. A number represents
     * a direct numeric match. A '*'means that any
     * single number is valid. A '+' means that any number,
     * and any subsequent numbers, are valid.
     * In this manner, the following four patterns
     * would all match the version string '1.2.3':
     * '1.2.3', '1.*.3', '1.2.*' and '1.+'.
     *
     * @param versionMatchPattern version match pattern
     */
    private VersionMatch(String versionMatchPattern) {
        if (Strings.isNullOrEmpty(versionMatchPattern)) {
            throw new XacmlSyntaxException("Version can't be null or empty");
        }
        this.pattern = versionMatchPattern;
        this.match = parseMatch(versionMatchPattern);
    }

    private boolean match(Version v, MatchType t){
        Iterator<Integer> it = v.iterator();
        Iterator<MatchToken> itTokens = match.iterator();
        while(itTokens.hasNext()){
            MatchToken token = itTokens.next();
            MatchToken.MatchResult r = token.match(it, t);
            if(r == MatchToken.MatchResult.STOP_NOMATCH) {
                return false;
            }
            if(r == MatchToken.MatchResult.STOP_MATCH){
                return true;
            }
            if(!itTokens.hasNext()){
                while(it.hasNext()){
                    Integer n = it.next();
                    if(n != 0){
                        return false;
                    }
                }
                return true;
            }
        }
        return false;
    }

    public boolean isEqual(Version v){
        return match(v, MatchType.EQUALS);
    }

    public boolean isLaterThan(Version v){
        return match(v, MatchType.LATER_THAN);
    }

    public boolean isEarlierThan(Version v){
        return match(v, MatchType.EARLIER_THAN);
    }

    /**
     * Gets version match constraint
     *
     * @return a version match constraint
     */
    public String getPattern() {
        return pattern;
    }

    /**
     * @param pattern a version match constraint
     * @return {@link VersionMatch} instance
     * @throws IllegalArgumentException if a given version
     *                                  match constraint can not be parsed
     * @see VersionMatch#VersionMatch(String)
     */
    public static VersionMatch parse(String pattern) {
        return new VersionMatch(pattern);
    }

    public static interface MatchToken
    {
        MatchResult match(Iterator<Integer> it, MatchType t);

        public enum MatchResult
        {
            CONTINUE,
            STOP_MATCH,
            STOP_NOMATCH;
        }
    }

    public static enum MatchType
    {
        EQUALS,
        LATER_THAN,
        EARLIER_THAN;
    }

    public enum MatchTokens
    {
        ANY{
            public boolean isApplicable(String seq){
                return ANY_NUM.matchesAllOf(seq);
            }

            public MatchToken create(final String seq){
                return new MatchToken() {
                    @Override
                    public MatchResult match(Iterator<Integer> it, MatchType t) {
                          if(!it.hasNext()){
                              return MatchResult.STOP_MATCH;
                          }
                          it.next();
                          return MatchResult.CONTINUE;
                    };
                };
            }
        },
        NUMBER{
            public boolean isApplicable(String seq){
                return CharMatcher.DIGIT.matchesAllOf(seq);
            }
            public MatchToken create(final String seq){
                final Integer num = Integer.parseInt(seq);
                return new MatchToken() {
                    @Override
                    public MatchResult match(Iterator<Integer> it, MatchType t) {
                        Integer n = 0;
                        // if version
                        // does not have any more
                        // numbers assumption is 0
                        if(it.hasNext()){
                            n = it.next();
                        }
                        // if numbers are equal
                        // continue match
                        if(n.equals(num)){
                           return MatchResult.CONTINUE;
                        }
                        // if numbers are not equal
                        // and match is for an equality
                        // stop matching - NO MATCH
                        if(t == MatchType.EQUALS){
                            return MatchResult.STOP_NOMATCH;
                        }
                        // match if version vector
                        // component is earlier
                        if(t == MatchType.EARLIER_THAN){
                            return num < n?MatchResult.STOP_MATCH:MatchResult.STOP_NOMATCH;
                        }
                        return num > n?MatchResult.STOP_MATCH:MatchResult.STOP_NOMATCH;
                    }
                };
            }
        },
        ALL_AFTER{
            public boolean isApplicable(String seq){
                return ANY_SUB_NUM.matchesAllOf(seq);
            };

            public MatchToken create(final String seq){
                return new MatchToken() {
                    @Override
                    public MatchResult match(Iterator<Integer> it, MatchType t) {
                        return MatchResult.STOP_MATCH;
                    }
                };
            }
        };

        public abstract boolean isApplicable(String seq);
        public abstract MatchToken create(String token);

        public static MatchToken createToken(String seq){
            for(MatchTokens t : values()){
                if(t.isApplicable(seq)){
                    return t.create(seq);
                }
            }
            return null;
        }

    }


    private static List<MatchToken> parseMatch(String pattern){
        List<String> match = Splitter.on('.').splitToList(pattern);
        ImmutableList.Builder<MatchToken> builder = ImmutableList.builder();
        for(int index = 0; index < match.size(); index++){
            MatchToken token = MatchTokens.createToken(match.get(index));
            if(token == null){
                throw new IllegalArgumentException(String.format("Incorrect XACML version match " +
                        "pattern=\"%s\" at index=\"%s\"", pattern, index ));
            }
            builder.add(token);
        }

        return builder.build();
    };

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof VersionMatch)) {
            return false;
        }
        VersionMatch vm = (VersionMatch) o;
        return pattern.equals(vm.pattern);
    }

    @Override
    public String toString() {
        return pattern;
    }

    @Override
    public int hashCode() {
        return pattern.hashCode();
    }
}
