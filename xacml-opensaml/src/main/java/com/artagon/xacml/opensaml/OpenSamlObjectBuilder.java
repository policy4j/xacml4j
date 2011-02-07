package com.artagon.xacml.opensaml;

import java.io.OutputStream;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;

import javax.xml.namespace.QName;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.joda.time.DateTime;
import org.opensaml.common.IdentifierGenerator;
import org.opensaml.common.SAMLObject;
import org.opensaml.common.SAMLObjectBuilder;
import org.opensaml.common.SAMLVersion;
import org.opensaml.common.binding.BasicSAMLMessageContext;
import org.opensaml.common.binding.SAMLMessageContext;
import org.opensaml.common.impl.SecureRandomIdentifierGenerator;
import org.opensaml.common.xml.SAMLConstants;
import org.opensaml.saml2.core.Action;
import org.opensaml.saml2.core.Artifact;
import org.opensaml.saml2.core.ArtifactResolve;
import org.opensaml.saml2.core.ArtifactResponse;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.Attribute;
import org.opensaml.saml2.core.AttributeStatement;
import org.opensaml.saml2.core.AttributeValue;
import org.opensaml.saml2.core.Audience;
import org.opensaml.saml2.core.AudienceRestriction;
import org.opensaml.saml2.core.AuthnContext;
import org.opensaml.saml2.core.AuthnContextClassRef;
import org.opensaml.saml2.core.AuthnRequest;
import org.opensaml.saml2.core.AuthnStatement;
import org.opensaml.saml2.core.AuthzDecisionQuery;
import org.opensaml.saml2.core.AuthzDecisionStatement;
import org.opensaml.saml2.core.Conditions;
import org.opensaml.saml2.core.Issuer;
import org.opensaml.saml2.core.NameID;
import org.opensaml.saml2.core.NameIDPolicy;
import org.opensaml.saml2.core.RequestAbstractType;
import org.opensaml.saml2.core.Response;
import org.opensaml.saml2.core.Statement;
import org.opensaml.saml2.core.Status;
import org.opensaml.saml2.core.StatusCode;
import org.opensaml.saml2.core.StatusMessage;
import org.opensaml.saml2.core.StatusResponseType;
import org.opensaml.saml2.core.Subject;
import org.opensaml.saml2.core.impl.StatusCodeUnmarshaller;
import org.opensaml.xacml.ctx.ActionType;
import org.opensaml.xacml.ctx.DecisionType;
import org.opensaml.xacml.ctx.EnvironmentType;
import org.opensaml.xacml.ctx.MissingAttributeDetailType;
import org.opensaml.xacml.ctx.RequestType;
import org.opensaml.xacml.ctx.ResourceContentType;
import org.opensaml.xacml.ctx.ResourceType;
import org.opensaml.xacml.ctx.ResponseType;
import org.opensaml.xacml.ctx.ResultType;
import org.opensaml.xacml.ctx.StatusCodeType;
import org.opensaml.xacml.ctx.StatusDetailType;
import org.opensaml.xacml.ctx.SubjectType;
import org.opensaml.xacml.ctx.impl.ActionTypeImplBuilder;
import org.opensaml.xacml.ctx.impl.ActionTypeMarshaller;
import org.opensaml.xacml.ctx.impl.ActionTypeUnmarshaller;
import org.opensaml.xacml.ctx.impl.AttributeTypeImpl;
import org.opensaml.xacml.ctx.impl.AttributeTypeImplBuilder;
import org.opensaml.xacml.ctx.impl.AttributeTypeMarshaller;
import org.opensaml.xacml.ctx.impl.AttributeTypeUnmarshaller;
import org.opensaml.xacml.ctx.impl.AttributeValueTypeImpl;
import org.opensaml.xacml.ctx.impl.AttributeValueTypeImplBuilder;
import org.opensaml.xacml.ctx.impl.AttributeValueTypeMarshaller;
import org.opensaml.xacml.ctx.impl.AttributeValueTypeUnmarshaller;
import org.opensaml.xacml.ctx.impl.DecisionTypeImplBuilder;
import org.opensaml.xacml.ctx.impl.DecisionTypeMarshaller;
import org.opensaml.xacml.ctx.impl.DecisionTypeUnmarshaller;
import org.opensaml.xacml.ctx.impl.EnvironmentTypeImplBuilder;
import org.opensaml.xacml.ctx.impl.EnvironmentTypeMarshaller;
import org.opensaml.xacml.ctx.impl.EnvironmentTypeUnmarshaller;
import org.opensaml.xacml.ctx.impl.MissingAttributeDetailTypeImplBuilder;
import org.opensaml.xacml.ctx.impl.MissingAttributeDetailTypeMarshaller;
import org.opensaml.xacml.ctx.impl.MissingAttributeDetailTypeUnmarshaller;
import org.opensaml.xacml.ctx.impl.RequestTypeImplBuilder;
import org.opensaml.xacml.ctx.impl.RequestTypeMarshaller;
import org.opensaml.xacml.ctx.impl.RequestTypeUnmarshaller;
import org.opensaml.xacml.ctx.impl.ResourceContentTypeImplBuilder;
import org.opensaml.xacml.ctx.impl.ResourceContentTypeMarshaller;
import org.opensaml.xacml.ctx.impl.ResourceContentTypeUnmarshaller;
import org.opensaml.xacml.ctx.impl.ResourceTypeImplBuilder;
import org.opensaml.xacml.ctx.impl.ResourceTypeMarshaller;
import org.opensaml.xacml.ctx.impl.ResourceTypeUnmarshaller;
import org.opensaml.xacml.ctx.impl.ResponseTypeImplBuilder;
import org.opensaml.xacml.ctx.impl.ResponseTypeMarshaller;
import org.opensaml.xacml.ctx.impl.ResponseTypeUnmarshaller;
import org.opensaml.xacml.ctx.impl.ResultTypeImplBuilder;
import org.opensaml.xacml.ctx.impl.ResultTypeMarshaller;
import org.opensaml.xacml.ctx.impl.ResultTypeUnmarshaller;
import org.opensaml.xacml.ctx.impl.StatusCodeTypeImplBuilder;
import org.opensaml.xacml.ctx.impl.StatusCodeTypeMarshaller;
import org.opensaml.xacml.ctx.impl.StatusDetailTypeImplBuilder;
import org.opensaml.xacml.ctx.impl.StatusDetailTypeMarshaller;
import org.opensaml.xacml.ctx.impl.StatusDetailTypeUnmarshaller;
import org.opensaml.xacml.ctx.impl.SubjectTypeImplBuilder;
import org.opensaml.xacml.ctx.impl.SubjectTypeMarshaller;
import org.opensaml.xacml.ctx.impl.SubjectTypeUnmarshaller;
import org.opensaml.xacml.profile.saml.XACMLAuthzDecisionQueryType;
import org.opensaml.xacml.profile.saml.XACMLAuthzDecisionStatementType;
import org.opensaml.xacml.profile.saml.impl.XACMLAuthzDecisionQueryTypeImplBuilder;
import org.opensaml.xacml.profile.saml.impl.XACMLAuthzDecisionQueryTypeMarshaller;
import org.opensaml.xacml.profile.saml.impl.XACMLAuthzDecisionQueryTypeUnmarshaller;
import org.opensaml.xacml.profile.saml.impl.XACMLAuthzDecisionStatementTypeImplBuilder;
import org.opensaml.xacml.profile.saml.impl.XACMLAuthzDecisionStatementTypeMarshaller;
import org.opensaml.xacml.profile.saml.impl.XACMLAuthzDecisionStatementTypeUnmarshaller;
import org.opensaml.xml.Configuration;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.XMLObjectBuilderFactory;
import org.opensaml.xml.io.Marshaller;
import org.opensaml.xml.io.MarshallerFactory;
import org.opensaml.xml.io.Unmarshaller;
import org.opensaml.xml.io.UnmarshallerFactory;
import org.opensaml.xml.io.UnmarshallingException;
import org.w3c.dom.Element;

import com.google.common.base.Preconditions;

public class OpenSamlObjectBuilder {

	private static final SAMLObjectBuilder<Assertion> assertionBuilder;
	private static final SAMLObjectBuilder<Attribute> attributeBuilder;
	
	private static final SAMLObjectBuilder<Audience> audienceBuilder;
	private static final SAMLObjectBuilder<AudienceRestriction> audienceRestrictionBuilder;

	private static final SAMLObjectBuilder<Conditions> conditionsBuilder;
	private static final SAMLObjectBuilder<Issuer> issuerBuilder;
	private static final SAMLObjectBuilder<NameID> nameIDBuilder;
	private static final SAMLObjectBuilder<NameIDPolicy> nameIdPolicyBuilder;
	private static final SAMLObjectBuilder<Response> responseBuilder;
	private static final SAMLObjectBuilder<Status> statusBuilder;
	private static final SAMLObjectBuilder<StatusCode> statusCodeBuilder;
	private static final SAMLObjectBuilder<StatusMessage> statusMessageBuilder;
	private static final SAMLObjectBuilder<Subject> subjectBuilder;
	private static final SAMLObjectBuilder<XACMLAuthzDecisionQueryType> xacml20SamlAuthzQueryBuilder;
	private static final XACMLAuthzDecisionQueryTypeMarshaller xacml20SamlAuthzQueryMarshaller;
	private static final SAMLObjectBuilder<XACMLAuthzDecisionStatementType> xacml20SamlAuthzStatementBuilder;
	
	private static final RequestTypeUnmarshaller xacml20ReqUnmarshaller;
	private static final ResponseTypeUnmarshaller xacml20ResUnmarshaller;
	private static final RequestTypeMarshaller xacml20ReqMashaller;
	private static final ResponseTypeMarshaller xacml20ResMashaller;

	private static final XMLObjectBuilderFactory objectBuilderFactory;
	private static final UnmarshallerFactory unmarshallerFactory;
	private static final MarshallerFactory marshallerFactory;

	static {
		Configuration.registerObjectProvider(
				XACMLAuthzDecisionQueryType.DEFAULT_ELEMENT_NAME_XACML20,
				new XACMLAuthzDecisionQueryTypeImplBuilder(),
				new XACMLAuthzDecisionQueryTypeMarshaller(),
				new XACMLAuthzDecisionQueryTypeUnmarshaller());

		Configuration.registerObjectProvider(
				XACMLAuthzDecisionStatementType.DEFAULT_ELEMENT_NAME_XACML20,
				new XACMLAuthzDecisionStatementTypeImplBuilder(),
				new XACMLAuthzDecisionStatementTypeMarshaller(),
				new XACMLAuthzDecisionStatementTypeUnmarshaller());

		Configuration.registerObjectProvider(RequestType.DEFAULT_ELEMENT_NAME,
				new RequestTypeImplBuilder(), new RequestTypeMarshaller(),
				new RequestTypeUnmarshaller());

		Configuration.registerObjectProvider(ActionType.DEFAULT_ELEMENT_NAME,
				new ActionTypeImplBuilder(), new ActionTypeMarshaller(),
				new ActionTypeUnmarshaller());

		Configuration.registerObjectProvider(
				AttributeTypeImpl.DEFAULT_ELEMENT_NAME,
				new AttributeTypeImplBuilder(), new AttributeTypeMarshaller(),
				new AttributeTypeUnmarshaller());

		Configuration.registerObjectProvider(
				AttributeValueTypeImpl.DEFAULT_ELEMENT_NAME,
				new AttributeValueTypeImplBuilder(),
				new AttributeValueTypeMarshaller(),
				new AttributeValueTypeUnmarshaller());

		Configuration.registerObjectProvider(
				AttributeValueTypeImpl.DEFAULT_ELEMENT_NAME,
				new AttributeValueTypeImplBuilder(),
				new AttributeValueTypeMarshaller(),
				new AttributeValueTypeUnmarshaller());

		Configuration.registerObjectProvider(DecisionType.DEFAULT_ELEMENT_NAME,
				new DecisionTypeImplBuilder(), new DecisionTypeMarshaller(),
				new DecisionTypeUnmarshaller());

		Configuration.registerObjectProvider(
				EnvironmentType.DEFAULT_ELEMENT_NAME,
				new EnvironmentTypeImplBuilder(),
				new EnvironmentTypeMarshaller(),
				new EnvironmentTypeUnmarshaller());

		Configuration.registerObjectProvider(
				MissingAttributeDetailType.DEFAULT_ELEMENT_NAME,
				new MissingAttributeDetailTypeImplBuilder(),
				new MissingAttributeDetailTypeMarshaller(),
				new MissingAttributeDetailTypeUnmarshaller());

		Configuration.registerObjectProvider(
				ResourceContentType.DEFAULT_ELEMENT_NAME,
				new ResourceContentTypeImplBuilder(),
				new ResourceContentTypeMarshaller(),
				new ResourceContentTypeUnmarshaller());

		Configuration.registerObjectProvider(ResourceType.DEFAULT_ELEMENT_NAME,
				new ResourceTypeImplBuilder(), new ResourceTypeMarshaller(),
				new ResourceTypeUnmarshaller());

		Configuration.registerObjectProvider(ResponseType.DEFAULT_ELEMENT_NAME,
				new ResponseTypeImplBuilder(), new ResponseTypeMarshaller(),
				new ResponseTypeUnmarshaller());

		Configuration.registerObjectProvider(ResultType.DEFAULT_ELEMENT_NAME,
				new ResultTypeImplBuilder(), new ResultTypeMarshaller(),
				new ResultTypeUnmarshaller());

		Configuration.registerObjectProvider(
				StatusCodeType.DEFAULT_ELEMENT_NAME,
				new StatusCodeTypeImplBuilder(),
				new StatusCodeTypeMarshaller(), new StatusCodeUnmarshaller());

		Configuration.registerObjectProvider(
				StatusDetailType.DEFAULT_ELEMENT_NAME,
				new StatusDetailTypeImplBuilder(),
				new StatusDetailTypeMarshaller(),
				new StatusDetailTypeUnmarshaller());

		Configuration.registerObjectProvider(SubjectType.DEFAULT_ELEMENT_NAME,
				new SubjectTypeImplBuilder(), new SubjectTypeMarshaller(),
				new SubjectTypeUnmarshaller());

		objectBuilderFactory = Configuration.getBuilderFactory();
		unmarshallerFactory = Configuration.getUnmarshallerFactory();
		marshallerFactory = Configuration.getMarshallerFactory();

		assertionBuilder = makeSamlObjectBuilder(Assertion.DEFAULT_ELEMENT_NAME);
		attributeBuilder = makeSamlObjectBuilder(Attribute.DEFAULT_ELEMENT_NAME);
	
		audienceBuilder = makeSamlObjectBuilder(Audience.DEFAULT_ELEMENT_NAME);
		audienceRestrictionBuilder = makeSamlObjectBuilder(AudienceRestriction.DEFAULT_ELEMENT_NAME);
		
		conditionsBuilder = makeSamlObjectBuilder(Conditions.DEFAULT_ELEMENT_NAME);
		issuerBuilder = makeSamlObjectBuilder(Issuer.DEFAULT_ELEMENT_NAME);
		nameIDBuilder = makeSamlObjectBuilder(NameID.DEFAULT_ELEMENT_NAME);
		nameIdPolicyBuilder = makeSamlObjectBuilder(NameIDPolicy.DEFAULT_ELEMENT_NAME);
		responseBuilder = makeSamlObjectBuilder(Response.DEFAULT_ELEMENT_NAME);
		statusBuilder = makeSamlObjectBuilder(Status.DEFAULT_ELEMENT_NAME);
		statusCodeBuilder = makeSamlObjectBuilder(StatusCode.DEFAULT_ELEMENT_NAME);
		statusMessageBuilder = makeSamlObjectBuilder(StatusMessage.DEFAULT_ELEMENT_NAME);
		subjectBuilder = makeSamlObjectBuilder(Subject.DEFAULT_ELEMENT_NAME);
		xacml20SamlAuthzQueryBuilder = makeSamlObjectBuilder(XACMLAuthzDecisionQueryType.DEFAULT_ELEMENT_NAME_XACML20);
		xacml20SamlAuthzQueryMarshaller = makeSamlObjectMarshaller(XACMLAuthzDecisionQueryType.DEFAULT_ELEMENT_NAME_XACML20);
		xacml20SamlAuthzStatementBuilder = makeSamlObjectBuilder(XACMLAuthzDecisionStatementType.DEFAULT_ELEMENT_NAME_XACML20);
		
		xacml20ReqMashaller = makeSamlObjectMarshaller(RequestType.DEFAULT_ELEMENT_NAME);
		xacml20ResMashaller = makeSamlObjectMarshaller(ResponseType.DEFAULT_ELEMENT_NAME);
		
		xacml20ReqUnmarshaller = makeSamlObjectUnmarshaller(RequestType.DEFAULT_ELEMENT_NAME);
		xacml20ResUnmarshaller = makeSamlObjectUnmarshaller(ResponseType.DEFAULT_ELEMENT_NAME);
	}

	private static final IdentifierGenerator idGenerator;
	
	private static final TransformerFactory transformerFactory;
	
	
	static {
		try {
			transformerFactory = TransformerFactory.newInstance();
			idGenerator = new SecureRandomIdentifierGenerator();
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

	private static void initializeResponse(
			StatusResponseType response, 
			Status status,
			RequestAbstractType request) {
		response.setID(generateIdentifier());
		response.setVersion(SAMLVersion.VERSION_20);
		response.setIssueInstant(new DateTime());
		response.setStatus(status);
		if (request != null) {
			response.setInResponseTo(request.getID());
		}
	}

	/**
	 * A convenience method for generating a random identifier.
	 * 
	 * @return A new identifier string.
	 */
	public static String generateIdentifier() {
		return idGenerator.generateIdentifier();
	}

	@SuppressWarnings("unchecked")
	private static <T extends SAMLObject> SAMLObjectBuilder<T> makeSamlObjectBuilder(
			QName name) {
		return (SAMLObjectBuilder<T>) objectBuilderFactory.getBuilder(name);
	}
	
	@SuppressWarnings("unchecked")
	private static <M extends Marshaller> M makeSamlObjectMarshaller(
			QName name) {
		return (M)marshallerFactory.getMarshaller(name);
	}
	
	@SuppressWarnings("unchecked")
	private static <M extends Unmarshaller> M makeSamlObjectUnmarshaller(
			QName name) {
		return (M)unmarshallerFactory.getUnmarshaller(name);
	}

	public static void serialize(XMLObject xml, OutputStream out, boolean identOutput) 
		throws Exception
	{		
		Marshaller m = Configuration.getMarshallerFactory().getMarshaller(xml);
		Preconditions.checkState(m != null);
		Transformer serializer = transformerFactory.newTransformer();
		serializer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
		serializer.setOutputProperty(OutputKeys.ENCODING, "utf-8");
		serializer.setOutputProperty(OutputKeys.METHOD, "xml");		
		serializer.setOutputProperty(OutputKeys.INDENT, identOutput?"yes":"no");
		Source source = new DOMSource(m.marshall(xml));		
		Result result = new StreamResult(out);		
		serializer.transform(source, result);		
	}
	
	
	
	/**
	 * Static factory for OpenSAML message-context objects.
	 * 
	 * @param <TI>
	 *            The type of the request object.
	 * @param <TO>
	 *            The type of the response object.
	 * @param <TN>
	 *            The type of the name identifier used for subjects.
	 * @return A new message-context object.
	 */
	public static <TI extends SAMLObject, TO extends SAMLObject, TN extends SAMLObject> 
		SAMLMessageContext<TI, TO, TN> makeSamlMessageContext() {
		return new BasicSAMLMessageContext<TI, TO, TN>();
	}

	/**
	 * Static factory for SAML <code>Assertion</code> objects.
	 * 
	 * @param issuer
	 *            The entity issuing this assertion.
	 * @return A new <code>Assertion</code> object.
	 */
	public static Assertion makeAssertion(Issuer issuer, Statement ...statements) {
		Assertion assertion = assertionBuilder.buildObject();
		assertion.setID(generateIdentifier());
		assertion.setVersion(SAMLVersion.VERSION_20);
		assertion.setIssueInstant(new DateTime());
		assertion.setIssuer(issuer);
		return assertion;
	}

	/**
	 * Static factory for SAML <code>Assertion</code> objects.
	 * 
	 * @param issuer
	 *            The entity issuing this assertion.
	 * @param subject
	 *            The subject of the assertion.
	 * @return A new <code>Assertion</code> object.
	 */
	public static Assertion makeAssertion(Issuer issuer, Subject subject) {
		Assertion assertion = makeAssertion(issuer);
		assertion.setSubject(subject);
		return assertion;
	}
	
	public static RequestType unmarshallXacml20Request(Element request) 
		throws UnmarshallingException
	{
		return (RequestType)xacml20ReqUnmarshaller.unmarshall(request);
	}
	
	public static ResponseType unmarshallXacml20Response(Element request) 
		throws UnmarshallingException
	{
		return (ResponseType)xacml20ResUnmarshaller.unmarshall(request);
	}
	
	/**
	 * Static factory for SAML <code>Audience</code> objects.
	 * 
	 * @param uri
	 *            The audience URI.
	 * @return A new <code>Audience</code> object.
	 */
	public static Audience makeAudience(String uri) {
		Audience audience = audienceBuilder.buildObject();
		audience.setAudienceURI(uri);
		return audience;
	}
	
	public static XACMLAuthzDecisionQueryType makeXacml20SamlAuthzDecisionQuery(
			String issuer, 
			String destination, 
			boolean combinePolicies, 
			RequestType xacml20Request)
	{
		XACMLAuthzDecisionQueryType xacmlQuery = xacml20SamlAuthzQueryBuilder.buildObject(
				XACMLAuthzDecisionQueryType.DEFAULT_ELEMENT_NAME_XACML20);
		Preconditions.checkState(xacmlQuery != null);
		xacmlQuery.setID(generateIdentifier());
		xacmlQuery.setIssueInstant(new DateTime());
		xacmlQuery.setVersion(SAMLVersion.VERSION_20);
		xacmlQuery.setRequest(xacml20Request);
		xacmlQuery.setCombinePolicies(combinePolicies);
		xacmlQuery.setDestination(destination);
		return xacmlQuery;
	}
	
	public static Response makeXacml20AuthzDecisionQueryResponse(
			Issuer issuer,
			XACMLAuthzDecisionQueryType request, 
			RequestType xacml20Request, 
			ResponseType xacml20Response)
	{
		Response response = responseBuilder.buildObject();
		initializeResponse(response, makeStatus(StatusCode.SUCCESS_URI), request);
		Assertion authzAssertion = makeAssertion(issuer);
		XACMLAuthzDecisionStatementType authzStatement = xacml20SamlAuthzStatementBuilder.buildObject();
		authzStatement.setRequest(xacml20Request);
		authzStatement.setResponse(xacml20Response);
		authzAssertion.getStatements().add(authzStatement);
		return response;
	}

	/**
	 * Static factory for SAML <code>AudienceRestriction</code> objects.
	 * 
	 * @return A new <code>AudienceRestriction</code> object.
	 */
	public static AudienceRestriction makeAudienceRestriction() {
		return audienceRestrictionBuilder.buildObject();
	}

	/**
	 * Static factory for SAML <code>Conditions</code> objects.
	 * 
	 * @return A new <code>Conditions</code> object.
	 */
	public static Conditions makeConditions() {
		return conditionsBuilder.buildObject();
	}

	/**
	 * Static factory for SAML <code>Issuer</code> objects.
	 * 
	 * @param name
	 *            The issuer of a response object. In the absence of a specific
	 *            format, this is a URI identifying the issuer.
	 * @return A new <code>Issuer</code> object.
	 */
	public static Issuer makeIssuer(String name) {
		Issuer issuer = issuerBuilder.buildObject();
		issuer.setValue(name);
		return issuer;
	}

	/**
	 * Static factory for SAML <code>NameID</code> objects.
	 * 
	 * @param name
	 *            The name represented by this object.
	 * @return A new <code>NameID</code> object.
	 */
	public static NameID makeNameId(String name) {
		NameID id = nameIDBuilder.buildObject();
		id.setValue(name);
		return id;
	}

	/**
	 * Static factory for SAML <code>NameIDPolicy</code> objects.
	 * 
	 * @param format
	 *            The URI specifying the format of a class of names.
	 * @return A new <code>NameIDPolicy</code> object.
	 */
	public static NameIDPolicy makeNameIdPolicy(String format) {
		NameIDPolicy idPolicy = nameIdPolicyBuilder.buildObject();
		idPolicy.setFormat(format);
		return idPolicy;
	}

	/**
	 * Static factory for SAML <code>Response</code> objects.
	 * 
	 * @param request
	 *            The request that this is a response to.
	 * @param status
	 *            The <code>Status</code> object indicating the success of
	 *            requested action.
	 * @return A new <code>Response</code> object.
	 */
	public static Response makeResponse(RequestAbstractType request,
			Status status) {
		Response response = responseBuilder.buildObject();
		initializeResponse(response, status, request);
		return response;
	}
	
	/**
	 * Static factory for SAML <code>Status</code> objects.
	 * 
	 * @param code
	 *            A <code>StatusCode</code> object containing the SAML
	 *            status-code URI.
	 * @return A new <code>Status</code> object.
	 */
	public static Status makeStatus(StatusCode code) {
		Status status = statusBuilder.buildObject();
		status.setStatusCode(code);
		return status;
	}

	/**
	 * Static factory for SAML <code>Status</code> objects.
	 * 
	 * A convenience method that generates a status object with a message.
	 * 
	 * @param value
	 *            A URI specifying one of the standard SAML status codes.
	 * @param message
	 *            A string describing the status.
	 * @return A new <code>Status</code> object.
	 */
	public static Status makeStatus(String value, String message) {
		Status status = makeStatus(value);
		status.setStatusMessage(makeStatusMessage(message));
		return status;
	}

	/**
	 * Static factory for SAML <code>Status</code> objects.
	 * 
	 * A convenience method that wraps the given URI in a
	 * <code>StatusCode</code> object.
	 * 
	 * @param value
	 *            A URI specifying one of the standard SAML status codes.
	 * @return A new <code>Status</code> object.
	 */
	public static Status makeStatus(String value) {
		return makeStatus(makeStatusCode(value));
	}

	/**
	 * Static factory for SAML <code>StatusCode</code> objects.
	 * 
	 * @return A new <code>StatusCode</code> object.
	 */
	public static StatusCode makeStatusCode() {
		return statusCodeBuilder.buildObject();
	}

	/**
	 * Static factory for SAML <code>StatusCode</code> objects.
	 * 
	 * @param value
	 *            A URI specifying one of the standard SAML status codes.
	 * @return A new <code>StatusCode</code> object.
	 */
	public static StatusCode makeStatusCode(String value) {
		StatusCode code = makeStatusCode();
		code.setValue(value);
		return code;
	}

	/**
	 * Static factory for SAML <code>StatusMessage</code> objects.
	 * 
	 * @param value
	 *            A status message string.
	 * @return A new <code>StatusMessage</code> object.
	 */
	public static StatusMessage makeStatusMessage(String value) {
		StatusMessage message = statusMessageBuilder.buildObject();
		message.setMessage(value);
		return message;
	}

	/**
	 * Static factory for SAML <code>Subject</code> objects.
	 * 
	 * Returns a subject that has no distinguished identifier. The caller is
	 * expected to fill in one or more <code>SubjectConfirmation</code>
	 * elements.
	 * 
	 * @return A new <code>Subject</code> object.
	 */
	public static Subject makeSubject() {
		return subjectBuilder.buildObject();
	}

	/**
	 * Static factory for SAML <code>Subject</code> objects.
	 * 
	 * Returns a subject that has the given identifier. The caller may
	 * optionally fill in any number of <code>SubjectConfirmation</code>
	 * elements.
	 * 
	 * @param id
	 *            The identifier for this subject.
	 * @return A new <code>Subject</code> object.
	 */
	public static Subject makeSubject(NameID id) {
		Subject samlSubject = makeSubject();
		samlSubject.setNameID(id);
		return samlSubject;
	}

	/**
	 * Static factory for SAML <code>Subject</code> objects.
	 * 
	 * A convenience method that wraps the given name in a <code>NameId</code>
	 * object.
	 * 
	 * @param name
	 *            The name identifying the subject.
	 * @return A new <code>Subject</code> object.
	 */
	public static Subject makeSubject(String name) {
		return makeSubject(makeNameId(name));
	}

}
