import javax.xml.bind.JAXBElement;
import org.oasis.xacml.v30.jaxb.ResponseType;
import org.xacml4j.v20.Xacml20ConformanceAttributeResolver;
import org.xacml4j.v20.Xacml30TestUtility;
import org.xacml4j.v30.RequestContext;
import org.xacml4j.v30.ResponseContext;
import org.xacml4j.v30.marshal.MarshallingProvider;
import org.xacml4j.v30.marshal.MediaType;
import org.xacml4j.v30.marshal.ResponseMarshaller;
import org.xacml4j.v30.pdp.PolicyDecisionPointBuilder;
import org.xacml4j.v30.policy.combine.DecisionCombiningAlgorithmProvider;
import org.xacml4j.v30.policy.function.FunctionProvider;
import org.xacml4j.v30.spi.pip.PolicyInformationPoint;
import org.xacml4j.v30.spi.pip.ResolverRegistry;
import org.xacml4j.v30.spi.repository.InMemoryPolicyRepository;

/** Production policy/request/response comparison against unchanged IIIE301-303 fixtures. */
class DiagnoseXPathMultipleDecisions {
    @SuppressWarnings("unchecked")
    public static void main(String[] args) throws Exception {
        int failed = 0;
        for (String id : new String[]{"IIIE301", "IIIE302", "IIIE303"}) {
            try {
                InMemoryPolicyRepository repository = new InMemoryPolicyRepository(id,
                        FunctionProvider.builder().withDefaultFunctions().build(),
                        DecisionCombiningAlgorithmProvider.builder().withDefaultAlgorithms().build());
                String prefix = "xacml3.0-ct-v.0.4/" + id;
                RequestContext request = Xacml30TestUtility.getRequest(prefix + "Request.xml");
                ResponseContext response = PolicyDecisionPointBuilder.builder(id)
                        .defaultRequestHandlers().policyRepository(repository)
                        .pip(PolicyInformationPoint.builder(id + "-pip")
                                .registry(ResolverRegistry.builder().withDefaultResolvers()
                                        .withResolver(new Xacml20ConformanceAttributeResolver()).build()).build())
                        .rootPolicy(repository.newImportTool().importPolicy(MediaType.Type.XACML30_XML,
                                Xacml30TestUtility.getClasspathResource(prefix + "Policy.xml")))
                        .build().decide(request);
                ResponseMarshaller marshaller = MarshallingProvider.getProvider(MediaType.Type.XACML30_XML)
                        .flatMap(provider -> provider.newResponseMarshaller()).orElseThrow();
                ResponseType actual = ((JAXBElement<ResponseType>)marshaller.marshal(response)).getValue();
                ResponseType expected = Xacml30TestUtility.getResponse(prefix + "Response.xml");
                System.out.println("CASE " + id + " actual decisions=" + actual.getResult().stream()
                        .map(result -> result.getDecision().value()).collect(java.util.stream.Collectors.toList())
                        + "; expected=" + expected.getResult().stream().map(result -> result.getDecision().value())
                        .collect(java.util.stream.Collectors.toList()));
                Xacml30TestUtility.assertResponse(expected, actual);
                System.out.println("CASE " + id + " PASS: all unchanged response assertions");
            } catch (Throwable error) {
                failed++;
                System.out.println("CASE " + id + " FAIL: " + error);
                error.printStackTrace(System.out);
            }
        }
        System.out.println("CASES executed=3; failed=" + failed + "; passed=" + (3 - failed));
        if (failed != 0) throw new AssertionError("IIIE301-303 checks failed: " + failed);
    }
}
