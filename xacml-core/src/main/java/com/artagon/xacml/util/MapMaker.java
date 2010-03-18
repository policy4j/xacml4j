package com.artagon.xacml.util;

import java.util.Map;

public interface MapMaker 
{
	<K, V> Map<K, V> make();
}
