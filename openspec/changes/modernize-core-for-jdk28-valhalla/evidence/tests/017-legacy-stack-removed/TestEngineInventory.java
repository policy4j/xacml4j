import java.util.ServiceLoader;
import java.util.TreeSet;
import org.junit.platform.engine.TestEngine;

// Inspect the classpath copied from a fresh Surefire XML report.
class TestEngineInventory {
    public static void main(String[] args) {
        TreeSet<String> ids = new TreeSet<>();
        ServiceLoader.load(TestEngine.class).forEach(engine -> ids.add(engine.getId()));
        if (!ids.equals(new TreeSet<>(java.util.List.of("junit-jupiter")))) {
            throw new AssertionError(args[0] + " unexpected engines: " + ids);
        }
        System.out.println(args[0] + " TestEngine IDs: " + ids);
    }
}
