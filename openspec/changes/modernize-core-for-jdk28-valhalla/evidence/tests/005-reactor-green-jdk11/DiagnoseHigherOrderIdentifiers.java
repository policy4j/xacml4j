import java.io.InputStream;
import org.xacml4j.v30.marshal.MediaType;
import org.xacml4j.v30.policy.PolicySyntaxException;
import org.xacml4j.v30.policy.combine.DecisionCombiningAlgorithmProvider;
import org.xacml4j.v30.policy.function.FunctionProvider;
import org.xacml4j.v30.spi.repository.InMemoryPolicyRepository;
import org.xacml4j.v30.spi.repository.PolicyImportTool;

/** Read-only registry and unchanged IIC164 fixture diagnostics. */
class DiagnoseHigherOrderIdentifiers {
    public static void main(String[] args) throws Exception {
        FunctionProvider functions = FunctionProvider.builder().withDefaultFunctions().build();
        for (String name : new String[]{"any-of", "all-of", "any-of-any", "map"}) {
            boolean legacy = functions.getFunction("urn:oasis:names:tc:xacml:1.0:function:" + name).isPresent();
            boolean current = functions.getFunction("urn:oasis:names:tc:xacml:3.0:function:" + name).isPresent();
            System.out.println(name + ": 1.0 present=" + legacy + "; 3.0 present=" + current);
            if (!legacy || current) throw new AssertionError("Unexpected registry state: " + name);
        }
        PolicyImportTool tool = new InMemoryPolicyRepository("IIC164-diagnostic", functions,
                DecisionCombiningAlgorithmProvider.builder().withDefaultAlgorithms().build()).newImportTool();
        String path = "xacml3.0-ct-v.0.4/IIC164Policy.xml";
        try {
            tool.importPolicy(MediaType.Type.XACML30_XML, () -> resource(path));
            throw new AssertionError("IIC164 unexpectedly imported");
        } catch (PolicySyntaxException expected) {
            if (!expected.getMessage().contains("urn:oasis:names:tc:xacml:3.0:function:any-of")) throw expected;
            System.out.println("IIC164 unchanged fixture import reproduced: " + expected.getMessage());
        }
    }

    private static InputStream resource(String path) {
        InputStream input = Thread.currentThread().getContextClassLoader().getResourceAsStream(path);
        if (input == null) throw new IllegalArgumentException("Missing fixture: " + path);
        return input;
    }
}
