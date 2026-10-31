import java.io.InputStream;
import org.xacml4j.v30.marshal.MediaType;
import org.xacml4j.v30.policy.PolicySyntaxException;
import org.xacml4j.v30.policy.combine.DecisionCombiningAlgorithmProvider;
import org.xacml4j.v30.policy.function.FunctionProvider;
import org.xacml4j.v30.spi.repository.InMemoryPolicyRepository;
import org.xacml4j.v30.spi.repository.PolicyImportTool;

/** Read-only lookup and unchanged IIC150 policy-import diagnostics. */
class DiagnoseDurationBagFunctions {
    public static void main(String[] args) throws Exception {
        FunctionProvider functions = FunctionProvider.builder().withDefaultFunctions().build();
        int missing = 0;
        for (String type : new String[]{"dayTimeDuration", "yearMonthDuration"}) {
            for (String suffix : new String[]{"one-and-only", "bag-size", "is-in", "bag"}) {
                String name = type + "-" + suffix;
                boolean legacy = functions.getFunction("urn:oasis:names:tc:xacml:1.0:function:" + name).isPresent();
                boolean current = functions.getFunction("urn:oasis:names:tc:xacml:3.0:function:" + name).isPresent();
                System.out.println(name + ": 1.0 present=" + legacy + "; 3.0 present=" + current);
                if (!legacy || current) throw new AssertionError("Unexpected registry state: " + name);
                missing++;
            }
        }
        PolicyImportTool tool = new InMemoryPolicyRepository("IIC150-diagnostic", functions,
                DecisionCombiningAlgorithmProvider.builder().withDefaultAlgorithms().build()).newImportTool();
        String path = "xacml3.0-ct-v.0.4/IIC150Policy.xml";
        try {
            tool.importPolicy(MediaType.Type.XACML30_XML, () -> resource(path));
            throw new AssertionError("IIC150 unexpectedly imported");
        } catch (PolicySyntaxException expected) {
            if (!expected.getMessage().contains("urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-one-and-only")) {
                throw expected;
            }
            System.out.println("IIC150 unchanged fixture import reproduced: " + expected.getMessage());
        }
        System.out.println("Missing standard duration bag identifiers confirmed: " + missing);
    }

    private static InputStream resource(String path) {
        InputStream input = Thread.currentThread().getContextClassLoader().getResourceAsStream(path);
        if (input == null) throw new IllegalArgumentException("Missing fixture: " + path);
        return input;
    }
}
