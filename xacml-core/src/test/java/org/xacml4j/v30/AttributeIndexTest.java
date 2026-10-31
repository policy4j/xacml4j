package org.xacml4j.v30;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.truth.Truth8;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.xacml4j.v30.types.Entity;
import org.xacml4j.v30.types.StringVal;
import org.xacml4j.v30.types.ValueType;
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

    @ParameterizedTest(name = "{0}")
    @MethodSource("attributeLists")
    void collectionIndexKeepsBuilderOrderEqualityHashAndLookups(String name, List<Attribute> input){
        AttributeIndex expected = AttributeIndex.builder().attributes(input).build();
        AttributeIndex actual = indexOf(input);
        ImmutableListMultimap.Builder<String, Attribute> model = ImmutableListMultimap.builder();
        for(Attribute attribute : input){
            model.put(attribute.getAttributeId(), attribute);
        }

        assertListParity(expected, actual);
        assertEquals(model.build().asMap().hashCode(), actual.hashCode());
        assertEquals(expected.toString(), indexOf(input).toString());
        for(Attribute attribute : input){
            String id = attribute.getAttributeId();
            assertEquals(expected.get(id), actual.get(id));
            assertEquals(expected.single(id), actual.single(id));
            assertEquals(expected.findByIssuer(id, attribute.getIssuer()), actual.findByIssuer(id, attribute.getIssuer()));
            assertEquals(expected.find(id, a -> a.isIncludeInResult()), actual.find(id, a -> a.isIncludeInResult()));
            for(var type : List.of(XacmlTypes.STRING, XacmlTypes.INTEGER)){
                assertEquals(expected.findValues(id, type, attribute.getIssuer()), actual.findValues(id, type, attribute.getIssuer()));
                assertEquals(expected.hasValues(id, type, attribute.getIssuer()), actual.hasValues(id, type, attribute.getIssuer()));
                assertEquals(expected.findValue(id, type, attribute.getIssuer()), actual.findValue(id, type, attribute.getIssuer()));
                assertEquals(expected.resolve(id, type, attribute.getIssuer()), actual.resolve(id, type, attribute.getIssuer()));
                assertEquals(expected.resolve(id, type, attribute.getIssuer()), indexOf(input).resolve(id, type, attribute.getIssuer()));
                AttributeDesignatorKey key = AttributeDesignatorKey.builder(id).category(CategoryId.RESOURCE)
                        .dataType(type).issuer(attribute.getIssuer()).build();
                assertEquals(expected.resolve(key), actual.resolve(key));
            }
        }
        assertTrue(actual.get("missing").isEmpty());
        assertTrue(actual.resolve("missing", XacmlTypes.STRING, null).isEmpty());
        assertTrue(indexOf(input).resolve("missing", XacmlTypes.STRING, null).isEmpty());
        assertThrows(UnsupportedOperationException.class, () -> actual.get("missing").add(text("missing", false)));
        assertListParity(expected, actual);
        assertEquals(expected.toString(), actual.toString());
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("attributeLists")
    void collectionEntityAndCategoryKeepBuilderValues(String name, List<Attribute> input){
        Entity expected = Entity.builder().attributes(input).build();
        for(Entity actual : List.of(entityOf(input), entityOf(indexOf(input)))){
            assertEquals(List.copyOf(expected.attributes()), List.copyOf(actual.attributes()));
            assertEquals(expected, actual);
            assertEquals(actual, expected);
            assertEquals(expected.hashCode(), actual.hashCode());
            assertEquals(expected.toString(), actual.toString());
            assertFalse(actual.hasContent());
            for(String ref : new String[]{ null, "request-category" }){
                Category expectedCategory = Category.builder(CategoryId.RESOURCE).id(ref).entity(expected).build();
                Category actualCategory = categoryOf(CategoryId.RESOURCE, ref, actual);
                assertEquals(expectedCategory, actualCategory);
                assertEquals(actualCategory, expectedCategory);
                assertEquals(expectedCategory.hashCode(), actualCategory.hashCode());
                assertEquals(expectedCategory.toString(), actualCategory.toString());
                assertEquals(expectedCategory.getReference(), actualCategory.getReference());
                assertEquals(expectedCategory.getReferenceId(), actualCategory.getReferenceId());
                assertEquals(List.copyOf(expectedCategory.getIncludeInResultAttributes()),
                        List.copyOf(actualCategory.getIncludeInResultAttributes()));
            }
            for(Attribute attribute : input){
                assertEquals(expected.find(attribute.getAttributeId()), actual.find(attribute.getAttributeId()));
            }
            assertEquals(expected, actual);
            assertEquals(expected.hashCode(), actual.hashCode());
            assertEquals(expected.toString(), actual.toString());
        }
    }

    @Test
    void interleavedIdsAreGroupedWithoutDroppingDuplicates(){
        Attribute first = text("a", true);
        Attribute other = text("b", false);
        Attribute issued = Attribute.builder("a").issuer("issuer").value(StringVal.of("issued")).build();
        AttributeIndex index = indexOf(List.of(first, other, issued, first));
        assertEquals(List.of(first, issued, first, other), List.copyOf(index.attributes()));
        assertEquals(List.of(first, first), List.copyOf(index.includeInResultAttributes()));
    }

    @Test
    void collectionInputIsSnapshottedAndItsAttributesAreImmutable(){
        Attribute first = text("a", true);
        List<Attribute> source = new ArrayList<>(List.of(first));
        AttributeIndex index = indexOf(source);
        source.add(text("b", false));
        source.clear();
        assertEquals(List.of(first), List.copyOf(index.attributes()));
        assertThrows(UnsupportedOperationException.class, () -> index.attributes().clear());
        assertThrows(UnsupportedOperationException.class, () -> index.includeInResultAttributes().clear());
        assertEquals(List.of(first), List.copyOf(index.get("a")));
    }

    @Test
    void nullCollectionsKeepTheBuildersEmptyBehavior(){
        AttributeIndex index = indexOf(null);
        assertListParity(AttributeIndex.empty(), index);
        assertEquals(Entity.builder().build(), entityOf((Collection<Attribute>)null));
    }

    @Test
    void equalityKeepsTheExistingMutualContainmentBehavior(){
        Attribute first = text("a", false);
        Attribute second = text("b", true);
        AttributeIndex forward = indexOf(List.of(first, second));
        AttributeIndex reversed = indexOf(List.of(second, first));
        AttributeIndex repeated = indexOf(List.of(first, first, second));
        assertEquals(forward, reversed);
        assertEquals(reversed, forward);
        assertEquals(forward, repeated);
        assertEquals(repeated, forward);
        assertEquals(AttributeIndex.builder().attribute(first, first, second).build().hashCode(), repeated.hashCode());
    }

    @Test
    void designatorAliasesStayBoundedAcrossCategories() throws Exception{
        AttributeIndex index = indexOf(List.of(issuedText("a")));
        Entity entity = Entity.of(index);
        Optional<BagOfValues> canonical = entity.resolve("a", XacmlTypes.STRING, "issuer");
        AttributeDesignatorKey normal = designator(CategoryId.RESOURCE, "a", XacmlTypes.STRING, "issuer");
        assertSame(canonical, entity.resolve(normal));
        AttributeDesignatorKey last = normal;
        for(int i = 0; i < 1024; i++){
            last = designator(CategoryId.of("urn:test:rv209:category:" + i), "a", XacmlTypes.STRING, "issuer");
            Optional<BagOfValues> actual = entity.resolve(last);
            assertSame(canonical, actual);
            assertSame(canonical.orElseThrow(), actual.orElseThrow());
            assertEquals(List.of(StringVal.of("a")), List.copyOf(actual.orElseThrow().values()));
        }
        Map<Object, Optional<BagOfValues>> kept = retainedBags(index);
        long aliases = retainedDesignatorAliases(index);
        assertTrue(aliases <= 64, "retained designator aliases: " + aliases);
        assertEquals(1, kept.size() - aliases, "only the category-free positive Lookup is canonical");
        assertSame(canonical, kept.get(normal));
        assertFalse(kept.containsKey(last), "an overflow designator must not be retained");
        int size = kept.size();
        for(int i = 0; i < 100; i++){
            assertSame(canonical, entity.resolve(normal));
            assertSame(canonical, entity.resolve(last));
        }
        assertEquals(size, kept.size());
    }

    @Test
    void missingDesignatorsDoNotRetainAliases() throws Exception{
        AttributeIndex index = indexOf(List.of(issuedText("a")));
        Entity entity = Entity.of(index);
        for(int phase = 0; phase < 2; phase++){
            for(int i = 0; i < 128; i++){
                CategoryId category = CategoryId.of("urn:test:rv209:missing:" + i);
                assertTrue(entity.resolve(designator(category, "unknown", XacmlTypes.STRING, "issuer")).isEmpty());
                assertTrue(entity.resolve(designator(category, "a", XacmlTypes.INTEGER, "issuer")).isEmpty());
                assertTrue(entity.resolve(designator(category, "a", XacmlTypes.STRING, "other")).isEmpty());
            }
            assertEquals(phase == 0 ? 0 : 2, retainedBags(index).size());
            assertEquals(phase, retainedDesignatorAliases(index));
            AttributeDesignatorKey positive = designator(CategoryId.RESOURCE, "a", XacmlTypes.STRING, "issuer");
            assertSame(entity.resolve("a", XacmlTypes.STRING, "issuer"), entity.resolve(positive));
        }
    }

    @Test
    void repeatedDesignatorsKeepTheirAliasesIndependentlyOfCanonicalLookupCount() throws Exception{
        List<Attribute> attributes = new ArrayList<>();
        for(int i = 0; i < 64; i++){
            attributes.add(issuedText("a-" + i));
        }
        AttributeIndex index = indexOf(attributes);
        Entity entity = Entity.of(index);
        List<AttributeDesignatorKey> keys = new ArrayList<>();
        List<Optional<BagOfValues>> canonical = new ArrayList<>();
        for(Attribute attribute : attributes){
            String id = attribute.getAttributeId();
            AttributeDesignatorKey key = designator(CategoryId.RESOURCE, id, XacmlTypes.STRING, "issuer");
            keys.add(key);
            canonical.add(entity.resolve(key));
            assertSame(entity.resolve(id, XacmlTypes.STRING, "issuer"), canonical.getLast());
            assertEquals(List.of(StringVal.of(id)), List.copyOf(canonical.getLast().orElseThrow().values()));
        }
        assertEquals(64, retainedDesignatorAliases(index));
        assertEquals(128, retainedBags(index).size());
        for(int round = 0; round < 4; round++){
            for(int i = 0; i < keys.size(); i++){
                assertSame(canonical.get(i), retainedBags(index).get(keys.get(i)));
                assertSame(canonical.get(i), entity.resolve(keys.get(i)));
            }
        }
        assertEquals(128, retainedBags(index).size());
    }

    @Test
    void concurrentFirstDesignatorsKeepCanonicalIdentityAndBound() throws Exception{
        AttributeIndex index = indexOf(List.of(issuedText("a")));
        Entity entity = Entity.of(index);
        AttributeDesignatorKey normal = designator(CategoryId.RESOURCE, "a", XacmlTypes.STRING, "issuer");
        int threads = 8;
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        try{
            CyclicBarrier start = new CyclicBarrier(threads);
            List<Future<List<Optional<BagOfValues>>>> done = new ArrayList<>();
            for(int thread = 0; thread < threads; thread++){
                int worker = thread;
                done.add(pool.submit(() -> {
                    start.await(10, TimeUnit.SECONDS);
                    List<Optional<BagOfValues>> resolved = new ArrayList<>();
                    resolved.add(entity.resolve(normal));
                    for(int i = 0; i < 128; i++){
                        CategoryId category = CategoryId.of("urn:test:rv209:concurrent:" + worker + ":" + i);
                        resolved.add(entity.resolve(designator(category, "a", XacmlTypes.STRING, "issuer")));
                    }
                    return resolved;
                }));
            }
            Optional<BagOfValues> canonical = done.getFirst().get(20, TimeUnit.SECONDS).getFirst();
            for(Future<List<Optional<BagOfValues>>> future : done){
                for(Optional<BagOfValues> actual : future.get(20, TimeUnit.SECONDS)){
                    assertSame(canonical, actual);
                    assertSame(canonical.orElseThrow(), actual.orElseThrow());
                    assertEquals(List.of(StringVal.of("a")), List.copyOf(actual.orElseThrow().values()));
                }
            }
            assertSame(canonical, entity.resolve("a", XacmlTypes.STRING, "issuer"));
            assertSame(canonical, entity.resolve(normal));
            assertSame(canonical, retainedBags(index).get(normal));
            long aliases = retainedDesignatorAliases(index);
            assertTrue(aliases <= 64, "retained designator aliases: " + aliases);
            assertEquals(1, retainedBags(index).size() - aliases);
        }finally{
            pool.shutdownNow();
        }
    }

    private static Attribute issuedText(String id){
        return Attribute.builder(id).issuer("issuer").value(StringVal.of(id)).build();
    }

    private static AttributeDesignatorKey designator(CategoryId category, String id, ValueType type, String issuer){
        return AttributeDesignatorKey.builder(id).category(category).dataType(type).issuer(issuer).build();
    }

    /** A value-only assertion cannot detect RV209's strong retention of category-bearing keys. */
    @SuppressWarnings("unchecked")
    private static Map<Object, Optional<BagOfValues>> retainedBags(AttributeIndex index) throws Exception{
        Field field = AttributeIndex.class.getDeclaredField("bags");
        field.setAccessible(true);
        Map<Object, Optional<BagOfValues>> kept = (Map<Object, Optional<BagOfValues>>)field.get(index);
        return kept != null ? kept : Map.of();
    }

    private static long retainedDesignatorAliases(AttributeIndex index) throws Exception{
        return retainedBags(index).keySet().stream().filter(AttributeDesignatorKey.class::isInstance).count();
    }

    private static void assertListParity(AttributeIndex expected, AttributeIndex actual){
        assertEquals(List.copyOf(expected.attributes()), List.copyOf(actual.attributes()));
        assertEquals(expected.isEmpty(), actual.isEmpty());
        assertEquals(expected.hasIncludeInResultAttributes(), actual.hasIncludeInResultAttributes());
        assertEquals(List.copyOf(expected.includeInResultAttributes()), List.copyOf(actual.includeInResultAttributes()));
        assertEquals(expected.find(a -> a.isIncludeInResult()), actual.find(a -> a.isIncludeInResult()));
        assertEquals(expected.findFirst(a -> a.isIncludeInResult()), actual.findFirst(a -> a.isIncludeInResult()));
        assertEquals(expected.stream().toList(), actual.stream().toList());
        assertEquals(expected, actual);
        assertEquals(actual, expected);
        assertEquals(expected.hashCode(), actual.hashCode());
    }

    static Stream<Arguments> attributeLists(){
        Attribute a = text("a", true);
        Attribute b = text("b", false);
        Attribute issued = Attribute.builder("a").issuer("issuer").value(StringVal.of("issued")).build();
        Attribute number = Attribute.builder("a").value(XacmlTypes.INTEGER.ofAny(7)).includeInResult(true).build();
        List<Attribute> eight = new ArrayList<>();
        for(int i = 0; i < 8; i++){
            eight.add(text("attribute-" + i, i % 2 == 0));
        }
        List<Attribute> nine = new ArrayList<>(eight);
        nine.add(Attribute.builder("attribute-0").issuer("other").value(StringVal.of("ninth")).build());
        return Stream.of(
                Arguments.of("empty", List.of()),
                Arguments.of("one", List.of(a)),
                Arguments.of("two distinct ids", List.of(a, b)),
                Arguments.of("shared id, issuers and types", List.of(a, issued, number, b)),
                Arguments.of("interleaved ids", List.of(a, b, issued, number)),
                Arguments.of("duplicates and all included", List.of(a, a, number)),
                Arguments.of("eight attributes", eight),
                Arguments.of("nine attributes", nine),
                Arguments.of("colliding id hashes", List.of(text("Aa", false), text("BB", true))));
    }

    private static Attribute text(String id, boolean included){
        return Attribute.builder(id).value(StringVal.of(id)).includeInResult(included).build();
    }

    private static AttributeIndex indexOf(Collection<Attribute> attributes){
        return AttributeIndex.of(attributes);
    }

    private static Entity entityOf(Collection<Attribute> attributes){
        return Entity.of(attributes);
    }

    private static Entity entityOf(AttributeIndex index){
        return Entity.of(index);
    }

    private static Category categoryOf(CategoryId id, String ref, Entity entity){
        return ref == null ? Category.of(id, entity) : Category.of(id, ref, entity);
    }
}
