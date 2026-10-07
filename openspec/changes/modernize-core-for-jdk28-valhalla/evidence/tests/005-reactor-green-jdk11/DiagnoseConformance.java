import java.io.InputStream;
import javax.xml.bind.JAXBElement;
import org.oasis.xacml.v30.jaxb.AttributesType;
import org.oasis.xacml.v30.jaxb.RequestType;
import org.oasis.xacml.v30.jaxb.ResponseType;
import org.oasis.xacml.v30.jaxb.ResultType;
import org.xacml4j.v20.Xacml30TestUtility;
import org.xacml4j.v30.CategoryId;
import org.xacml4j.v30.Decision;
import org.xacml4j.v30.RequestContext;
import org.xacml4j.v30.ResponseContext;
import org.xacml4j.v30.Result;
import org.xacml4j.v30.policy.function.impl.DateTimeArithmeticFunctions;
import org.xacml4j.v30.types.ISO8601DateTime;
import org.xacml4j.v30.types.ISO8601DayTimeDuration;
import org.xacml4j.v30.xml.JAXBUtils;
import org.xacml4j.v30.xml.Xacml30RequestContextUnmarshaller;
import org.xacml4j.v30.xml.Xacml30ResponseContextMarshaller;

/** Read-only diagnostics of production entrypoints and unchanged case fixtures. */
class DiagnoseConformance {
    public static void main(String[] args) throws Exception {
        ISO8601DateTime actual = DateTimeArithmeticFunctions.subtract(
                ISO8601DateTime.of("2002-03-22T08:23:47-05:00"),
                ISO8601DayTimeDuration.of("P5DT2H0M0S"));
        System.out.println("IIC104 expected subtraction: 2002-03-17T06:23:47-05:00");
        System.out.println("IIC104 actual subtraction: " + actual.toXacmlString());
        for (String id : new String[]{"IIIF001", "IIIG006"}) {
            String path = "xacml3.0-ct-v.0.4/" + id + "Request.xml";
            try (InputStream in = resource(path)) {
                RequestType request = (RequestType) ((JAXBElement<?>) JAXBUtils.getInstance()
                        .createUnmarshaller().unmarshal(in)).getValue();
                for (AttributesType category : request.getAttributes()) {
                    if (category.getContent() != null) {
                        System.out.println(id + " JAXB content category: " + category.getCategory());
                        for (Object part : category.getContent().getContent()) {
                            System.out.println(id + " JAXB content part: " + part.getClass().getName()
                                    + (part instanceof String ? "; whitespace=" + ((String) part).trim().isEmpty() : ""));
                        }
                    }
                }
            }
            try (InputStream in = resource(path)) {
                RequestContext request = new Xacml30RequestContextUnmarshaller().unmarshal(in);
                System.out.println(id + " production resource content present: "
                        + request.getEntity(CategoryId.RESOURCE).get().getContent().isPresent());
            }
        }
        ResultType expected = Xacml30TestUtility.getResponse(
                "xacml3.0-ct-v.0.4/IIA001Response.xml").getResult().get(0);
        ResponseContext source = ResponseContext.builder()
                .result(Result.ok(Decision.PERMIT).build()).build();
        ResultType marshalled = ((ResponseType) ((JAXBElement<?>)
                new Xacml30ResponseContextMarshaller().marshal(source)).getValue()).getResult().get(0);
        System.out.println("IIA001 expected policy identifiers absent: " + (expected.getPolicyIdentifierList() == null));
        System.out.println("Empty Permit actual policy identifier count: "
                + marshalled.getPolicyIdentifierList().getPolicyIdReferenceOrPolicySetIdReference().size());
        System.out.println("IIA001 expected obligations absent: " + (expected.getObligations() == null));
        System.out.println("Empty Permit actual obligation count: " + marshalled.getObligations().getObligation().size());
        try {
            Xacml30TestUtility.assertResponse(response(expected), response(marshalled));
            System.out.println("IIA001 equivalent response comparison: PASS");
        } catch (Exception e) {
            System.out.println("IIA001 equivalent response comparison: " + e);
            System.out.println("Comparison location: " + e.getStackTrace()[0]);
        }
    }

    private static ResponseType response(ResultType result) {
        ResponseType response = new ResponseType();
        response.getResult().add(result);
        return response;
    }

    private static InputStream resource(String path) {
        InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(path);
        if (in == null) throw new IllegalArgumentException("Missing fixture: " + path);
        return in;
    }
}
