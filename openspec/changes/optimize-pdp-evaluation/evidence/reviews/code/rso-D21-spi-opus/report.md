# rso-D21-spi-opus: code review of the interner SPI (Claude Opus 5.5)

FINAL REPORT (policy4j_implementer, Claude Opus 5.5): lane cc_1, R-SPI-opus, review of the interner SPI (Codex Terra), read-only

## Verdict

APPROVE WITH CHANGES.

- **P1 is a correctness defect in the SPI's design.** The default interner works. Any other interner that someone
  registers breaks category-less selector keys at runtime.
- **The P2 items make the SPI simpler.** The P3 items are cleanup.
- **A simpler shape fixes all of them and is verified** (see "Simpler code"):
  - Null-safe category equality in the two key classes.
  - A non-generic single-method interface.
  - The resolved interner in a field of the key base class.
  - Fail-fast discovery.
- No repository file is changed. The only file I wrote outside the scratchpad is this report.

## The `weak()` question

Neither check in `weak()` is needed.

1. **The null-category guard** (`AttributeReferenceKeyInterner.java:58-61`) works around `equals`:
   - `AttributeDesignatorKey.java:112` and `AttributeSelectorKey.java:103` call `category.equals(s.category)`. That
     throws `NullPointerException` when the key has no category.
   - With `Objects.equal(category, s.category)` (Guava's `Objects` is already imported in both files and used for
     `issuer` and `contextSelectorId`), keys without a category compare correctly.
   - Nothing then needs a bypass: not `weak()`, not the private `AttributeDesignatorKey.intern` (`:52-55`), and not a
     third-party interner.
   - The hash stays consistent: the designator hash leaves out the category (`:97`), and the selector hash is
     null-safe (`:88-89`).
   - Keys with a category compare exactly as before.
2. **The class check** (`:64`) cannot change anything for the built-in keys. Both classes are `final`, and their
   `equals` requires `instanceof` of their own class. Guava's interner looks up with `probe.equals(existing)`, so an
   equal canonical key always has the probe's class.
   - The check exists only because the generic method `<K extends AttributeReferenceKey> K intern(K)` forces an
     unchecked cast in every implementation.
   - A non-generic method removes the check, the cast, and `@SuppressWarnings`.
   - The two built-in builders cast their result. A broken implementation then fails fast with
     `ClassCastException`.

## Findings

**P1: The null-category guard is in one implementation of a pluggable SPI.**

- `AttributeSelectorKey.Builder.build()` (`:182-184`) sends category-less keys to whatever interner is registered.
  Only `weak()` knows to skip them.
- **Failure:** register an interner that wraps Guava's `Interners.newStrongInterner()`, then build the same
  category-less selector twice. The second `build()` throws
  `NullPointerException: Cannot invoke "CategoryId.equals(Object)" because "this.category" is null`. I verified this
  with a provider registered through `META-INF/services`.
- **Production path:** `attribute-selector` with an entity argument builds such a key on every evaluation
  (`AttributeDesignatorFunctions.java:99-109`: `path(xpath, true)` and no category). Its second evaluation then fails.
  The consequence at the function level is inferred from source; I did not run that function.
- Designators are protected only by their own bypass (`AttributeDesignatorKey.java:52-55`).
- **Fix:** null-safe category equality in both key classes. Then remove both bypasses.

**P2: The generic interface and the class check in `weak()`.**

- The generic method (`:43`) cannot be a lambda. Every implementation needs an anonymous or named class and an
  unchecked cast; `weak()` adds the class check (`:64`) and the comment that explains it.
- **Simpler:** `@FunctionalInterface` with `AttributeReferenceKey intern(AttributeReferenceKey key)`.
  - `weak()` becomes `Interners.<AttributeReferenceKey>newWeakInterner()::intern`.
  - `none()` becomes `key -> key`.
  - A plug-in can be one Guava method reference.

**P2: `ServiceLoader.findFirst()` with several providers.**

- With two registered implementations, the classpath order picks one silently (`:96-97`).
- The interner is a single process-wide choice. Two implementations is a misconfiguration, so it should fail loudly.
- **Proposal:** `.stream().map(ServiceLoader.Provider::get).collect(MoreCollectors.toOptional())`.
  - With two providers, the first key build fails with `ExceptionInInitializerError`, caused by
    `IllegalArgumentException: expected one element but was: <...>`. Verified.
  - Precedent: `MarshallingProvider.discoverProviders()` keeps the first provider per media type
    (`MarshallingProvider.java:72-77`, `(a,b)->a`). That is a per-key registry, not one global choice.
  - If the maintainer prefers that precedent, keep "first wins" but document it.

**P2: The test.**

- `internersPreserveDesignatorAndSelectorValues` (`AttributeReferenceKeyInternerTest.java:34-49`) cannot fail.
  - Its inputs come from the builders, so they are already canonical.
  - `none()`, and a fresh `weak()` seeing a key for the first time, both return the same instance.
  - So every assertion compares an object with itself.
- `selectorBuildersAcceptRepeatedKeysWithoutCategory` (`:52-63`) avoids `equals`, because `equals` throws for these
  keys. It checks paths and hashes instead.
- **Replace both** with `assertEquals(builder.build(), builder.build())` for a category-less selector.
- **Add the same line** to `AttributeDesignatorKeyTest.keysWithoutCategoryKeepTheirValues` (`:100-111`). That test
  also avoids `equals` today.
- No identity or ServiceLoader-mechanism tests.

**P3: The holder class beside the interface.**

- `AttributeReferenceKeyInternerHolder` (`:94-100`) makes this the only source file in `xacml-core/src/main` with
  two top-level types (an `rg` count of top-level declarations).
- **Proposal:** a package-private `static final` field in `AttributeReferenceKey`, the base class of both built-in
  keys (same package). It removes the holder class.
- `get()` has no callers except the two builders (`rg` over the main tree; Serena was unavailable), so drop it.
- If `get()` stays public API (the D21 amendment lists it), make it one line:
  `return AttributeReferenceKey.INTERNER;`.

**P3: `AttributeSelectorKey` fields are not `final`.**

- `path`, `contextSelectorId`, and `type` (`:41-44`) are assigned only in the constructor.
- Interned keys are shared across threads. Making the fields `final` makes the key immutable by construction, so
  safety does not depend on how a plug-in publishes it. The issue predates this patch but matters more now.

**P3: A stray `build()` in `AttributeDesignatorFunctions.java:105-106`.**

- `selectorKeyBuilder.category(categoryOrEntity).build();` throws its result away. Since this patch, every
  `attribute-selector` evaluation with a category does one extra interner lookup.
- Drop the `.build()`. The issue predates this patch.

## Simpler code (verified on a scratch copy)

The full diff against the main tree is
`scratchpad/proposal-SPI.diff`
(236 lines: 4 main files, 2 test files). The core:

```java
@FunctionalInterface
public interface AttributeReferenceKeyInterner
{
	/** @return the given key or an equal key of the same class */
	AttributeReferenceKey intern(AttributeReferenceKey key);

	static AttributeReferenceKeyInterner weak(){
		return Interners.<AttributeReferenceKey>newWeakInterner()::intern;
	}

	static AttributeReferenceKeyInterner none(){
		return key -> key;
	}
}

// AttributeReferenceKey
	/** The registered interner of the key builders, else {@link AttributeReferenceKeyInterner#weak()} (design D21) */
	static final AttributeReferenceKeyInterner INTERNER = ServiceLoader.load(AttributeReferenceKeyInterner.class)
			.stream()
			.map(ServiceLoader.Provider::get)
			.collect(MoreCollectors.toOptional())
			.orElseGet(AttributeReferenceKeyInterner::weak);

// AttributeDesignatorKey (the private intern(...) bypass is removed) and AttributeSelectorKey
		return Objects.equal(category, s.category) && ...
		public AttributeDesignatorKey build(){
			return (AttributeDesignatorKey) INTERNER.intern(new AttributeDesignatorKey(this));
		}
		public AttributeSelectorKey build(){
			return (AttributeSelectorKey) INTERNER.intern(new AttributeSelectorKey(this));
		}
```

Tests: one test, `assertEquals(builder.build(), builder.build())` for a category-less selector, and the same line
added to the existing designator test. With this shape, keys without a category are interned like all other keys.
The D21 text "a designator key without a category bypasses it" then needs a one-line amendment (conductor).

## Verification

Maven is unavailable locally. I used the scratch javac build:

- JDK 25.0.4, `--release 25`, jars from the Gradle cache, nothing fetched.
- The same 9 test files that need `Truth8` are excluded in both runs.
- It compiles the main tree's `xacml-core` and a scratch copy into the scratchpad. The main tree is only read.

| Run | Found | Passed | Failed | Skipped |
| --- | --- | --- | --- | --- |
| Terra's version (main tree) | 6,611 | 6,610 | 0 | 1 |
| The proposal (scratch copy) | 6,610 | 6,609 | 0 | 1 |

The proposal has one test fewer: the test that cannot fail is removed, and one assertion is added to an existing
test.

**Probes** (scratch classes; results printed):

- Terra's version:
  - `equals` on two category-less selectors throws `NullPointerException`.
  - A Guava strong interner behind the SPI throws on the second category-less selector and on the second designator.
  - Registered through `ServiceLoader`, `AttributeSelectorKey.Builder.build()` throws on the second build.
- The proposal:
  - With a registered strong interner, category-less selectors and designators are equal and interned, with no
    exception. A category still separates keys in both directions.
  - Two registered providers cause `ExceptionInInitializerError`, caused by
    `IllegalArgumentException: expected one element but was: <...>`.

**Not run:** Maven, the reactor, `xacml-pdp`, conformance, and JMH. Serena timed out at connect, so the claims about
callers and absence come from `rg` and reading the source (PARTIAL).

## Residual risk

1. Null-safe equality changes `equals` for category-less keys: it returns a result where it used to throw. Keys with a
   category behave as before. D21 needs the amendment above.
2. Dropping `get()` changes the public API list approved in D21. Keep it as a one-line delegate if wanted.
3. Fail-fast discovery makes a duplicate registration fatal at the first key build, and later builds then throw
   `NoClassDefFoundError`. That is intended, but loud.
4. Both versions use the thread context class loader of the first key build to load providers. In containers, that
   loader decides which provider is found.
5. `attribute-selector` now interns a key on every evaluation. The cost is not measured; the batch JMH covers it.
6. Unchanged and outside this patch: `toString()` of both keys throws `NullPointerException` when the key has no
   category (`getCategory().getAbbreviatedId()`). SLF4J catches failures in `toString()`, so debug logging does not
   break.
