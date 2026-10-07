import java.util.Optional;
import com.google.common.truth.Truth;
import com.google.common.truth.Truth8;

// Probe the retained assertion APIs on the actual Surefire test classpath.
class TruthWithoutJunit {
    public static void main(String[] args) throws Exception {
        for (String name : java.util.List.of("org.junit.Test", "org.junit.ComparisonFailure",
                "org.junit.vintage.engine.VintageTestEngine", "org.easymock.EasyMock",
                "org.objenesis.Objenesis")) {
            try {
                Class.forName(name);
                throw new AssertionError("Legacy class is still available: " + name);
            } catch (ClassNotFoundException expected) {
                // Absence is required by task 2.7.
            }
        }
        Truth.assertThat("same").isEqualTo("same");
        Truth8.assertThat(Optional.empty()).isEmpty();
        expectAssertion(() -> Truth.assertThat("actual").isEqualTo("expected"), "expected", "actual");
        expectAssertion(() -> Truth8.assertThat(Optional.of("value")).isEmpty(), "value");
        System.out.println("Truth and Truth8: positive assertions pass; both negative oracles throw AssertionError; legacy classes absent.");
    }

    private static void expectAssertion(Runnable action, String... messageParts) {
        AssertionError failure = null;
        try {
            action.run();
        } catch (AssertionError expected) {
            failure = expected;
        }
        if (failure == null) {
            throw new AssertionError("The deliberately false assertion passed.");
        }
        for (String part : messageParts) {
            if (!failure.getMessage().contains(part)) {
                throw new AssertionError("Missing assertion message part: " + part, failure);
            }
        }
    }
}
