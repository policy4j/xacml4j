package com.artagon.xacml.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Sets;

public class Cartesian 
{
	public static <T> List<Set<T>> cartesianProduct(List<Set<T>> list) 
	{
        List<Iterator<T>> iterators = new ArrayList<Iterator<T>>(list.size());
        List<T> elements = new ArrayList<T>(list.size());
        List<Set<T>> toRet = new ArrayList<Set<T>>();
        for (int i = 0; i < list.size(); i++) 
        {
        	iterators.add(list.get(i).iterator());
        	elements.add(iterators.get(i).next());
        }
        for (int j = 1; j >= 0;) 
        {
                toRet.add(Sets.newHashSet(elements));
                for (j = iterators.size()-1; j >= 0 && !iterators.get(j).hasNext(); j--) {
                        iterators.set(j, list.get(j).iterator());
                        elements.set(j, iterators.get(j).next());
                }
                elements.set(Math.abs(j), iterators.get(Math.abs(j)).next());
        }
        return toRet;
    }
}
