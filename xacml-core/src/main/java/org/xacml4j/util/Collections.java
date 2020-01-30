package org.xacml4j.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

public class Collections
{
    public static <T> Collection<T> expand(Collection<T> collection, T another)
    {
        Objects.requireNonNull(collection, "collection");
        if(a == null){
            return collection;
        }
        ArrayList<T> a = new ArrayList<>(collection.size() + 1);
        a.add(another);
        return a;
    }
}
