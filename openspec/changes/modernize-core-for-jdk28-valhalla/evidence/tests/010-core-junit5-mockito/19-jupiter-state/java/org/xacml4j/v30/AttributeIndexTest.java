package org.xacml4j.v30;

import com.google.common.collect.ImmutableList;
import com.google.common.truth.Truth8;

import org.junit.jupiter.api.Test;
import org.xacml4j.v30.types.StringVal;
import org.xacml4j.v30.types.XacmlTypes;

public class AttributeIndexTest
{
    @Test
    public void testAttributeIndexFind(){
        AttributeIndex index = AttributeIndex.builder()
                .attribute(
                        Attribute.builder("testId")
                        .value(StringVal.of("a1"))
                                .build(),
                        Attribute.builder("testId")
                                .issuer("testIssuer")
                                .value(StringVal.of("a2"))
                                .build())
                .build();
        Truth8.assertThat(index.findValue("testId", XacmlTypes.STRING, null)).hasValue(StringVal.of("a1"));
        Truth8.assertThat(index.findValues("testId", XacmlTypes.STRING, null).stream())
                .containsAnyIn(ImmutableList.of(StringVal.of("a1"), StringVal.of("a2")));
        Truth8.assertThat(index.findValues("testId", XacmlTypes.STRING, "testIssuer").stream())
                .containsAnyIn(ImmutableList.of(StringVal.of("a2")));
    }
}
