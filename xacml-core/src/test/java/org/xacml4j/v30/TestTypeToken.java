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

import com.google.common.reflect.TypeToken;
import org.junit.Test;
import static org.junit.Assert.*;

import java.lang.reflect.Type;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by trumpyla on 10/30/14.
 */
public class TestTypeToken
{
    private TypeToken<List<?>> list_of_strings = new TypeToken<List<?>>() {};
    private TypeToken<LinkedList<String>> other_list_of_strings = new TypeToken<LinkedList<String>>() {};

    @Test
    public void testTypes(){
        LinkedList<String> test = new LinkedList<String>();
        assertTrue(list_of_strings.isAssignableFrom(other_list_of_strings));
        assertTrue(list_of_strings.isAssignableFrom(TypeToken.of(test.getClass())));
    }
}
