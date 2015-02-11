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

public class VersionMatch
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
    public VersionMatch(String versionMatchPattern) {
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
            if(token.isEndToken()){
                return true;
            }
            if(!token.match(it, t)) {
                return false;
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

    public boolean isEqualThan(Version v){
        return match(v, MatchType.EQUALS);
    }

    public boolean isLaterThan(Version v){
        return match(v, MatchType.LATER);
    }

    public boolean isEarlierThan(Version v){
        return match(v, MatchType.EARLIER);
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
        boolean match(Iterator<Integer> it, MatchType t);
        boolean isEndToken();
    }

    public static enum MatchType
    {
        EQUALS,
        LATER,
        EARLIER;
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
                    public boolean match(Iterator<Integer> it, MatchType t) {
                          if(it.hasNext()){
                              it.next();
                          }
                          return true;
                    };

                    @Override
                    public boolean isEndToken(){
                        return false;
                    }
                }
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
                    public boolean match(Iterator<Integer> it, MatchType t) {
                        Integer n = 0;
                        if(it.hasNext()){
                            n = it.next();
                        }
                        if(t == MatchType.EQUALS){
                            return n.equals(num);
                        }
                        if(t == MatchType.EARLIER){
                            return num <= n;
                        }
                        return num >= n;
                    }

                    @Override
                    public boolean isEndToken(){
                        return false;
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
                    public boolean match(Iterator<Integer> it, MatchType t) {
                        while(it.hasNext()){
                            it.next();
                        }
                        return true;
                    }

                    @Override
                    public boolean isEndToken(){
                        return true;
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
