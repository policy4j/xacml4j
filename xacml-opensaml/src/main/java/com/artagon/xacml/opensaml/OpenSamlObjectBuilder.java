package com.artagon.xacml.opensaml;

import java.io.File;
import java.io.OutputStream;

import javax.xml.namespace.QName;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.joda.time.DateTime;
import org.opensaml.DefaultBootstrap;
import org.opensaml.common.IdentifierGenerator;
import org.opensaml.common.SAMLObject;
import org.opensaml.common.SAMLObjectBuilder;
import org.opensaml.common.SAMLVersion;
import org.opensaml.common.binding.BasicEndpointSelector;
import org.opensaml.common.binding.BasicSAMLMessageContext;
import org.opensaml.common.binding.SAMLMessageContext;
import org.opensaml.common.impl.SecureRandomIdentifierGenerator;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.Attribute;
import org.opensaml.saml2.core.Audience;
import org.opensaml.saml2.core.AudienceRestriction;
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
import org.opensaml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml2.metadata.RoleDescriptor;
import org.opensaml.saml2.metadata.provider.FilesystemMetadataProvider;
import org.opensaml.saml2.metadata.provider.MetadataProvider;
import org.opensaml.saml2.metadata.provider.MetadataProviderException;
import org.opensaml.xacml.ctx.RequestType;
import org.opensaml.xacml.ctx.ResponseType;
import org.opensaml.xacml.ctx.impl.RequestTypeMarshaller;
import org.opensaml.xacml.ctx.impl.RequestTypeUnmarshaller;
import org.opensaml.xacml.ctx.impl.ResponseTypeMarshaller;
import org.opensaml.xacml.ctx.impl.ResponseTypeUnmarshaller;
import org.opensaml.xacml.profile.saml.XACMLAuthzDecisionQueryType;
import org.opensaml.xacml.profile.saml.XACMLAuthzDecisionStatementType;
import org.opensaml.xacml.profile.saml.impl.XACMLAuthzDecisionQueryTypeMarshaller;
import org.opensaml.xacml.profile.saml.impl.XACMLAuthzDecisionQueryTypeUnmarshaller;
import org.opensaml.xml.Configuration;
import org.opensaml.xml.ConfigurationException;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.XMLObjectBuilder;
import org.opensaml.xml.XMLObjectBuilderFactory;
import org.opensaml.xml.io.Marshaller;
import org.opensaml.xml.io.MarshallerFactory;
import org.opensaml.xml.io.MarshallingException;
import org.opensaml.xml.io.Unmarshaller;
import org.opensaml.xml.io.UnmarshallerFactory;
import org.opensaml.xml.io.UnmarshallingException;
import org.opensaml.xml.parse.BasicParserPool;
import org.opensaml.xml.signature.Signature;
import org.w3c.dom.Document;
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
	private static final XMLObjectBuilder<Signature> signatureBuilder;
	private static final SAMLObjectBuilder<XACMLAuthzDecisionQueryType> xacml20SamlAuthzQueryBuilder;
	

	private static final XACMLAuthzDecisionQueryTypeMarshaller xacml20SamlAuthzQueryMarshaller;
	private static final XACMLAuthzDecisionQueryTypeUnmarshaller xacml20SamlAuthzQueryUnmarshaller;
	private static final SAMLObjectBuilder<XACMLAuthzDecisionStatementType> xacml20SamlAuthzStatementBuilder;

	private static final RequestTypeUnmarshaller xacml20ReqUnmarshaller;
	private static final ResponseTypeUnmarshaller xacml20ResUnmarshaller;
	private static final RequestTypeMarshaller xacml20ReqMashaller;
	private static final ResponseTypeMarshaller xacml20ResMashaller;

	private static final XMLObjectBuilderFactory objectBuilderFactory;
	private static final UnmarshallerFactory unmarshallerFactory;
	private static final MarshallerFactory marshallerFactory;

	static {
		try {
			DefaultBootstrap.bootstrap();
		} catch (ConfigurationException e) {
			throw new IllegalStateException(e);
		}

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
		xacml20SamlAuthzQueryUnmarshaller = makeSamlObjectUnmarshaller(XACMLAuthzDecisionQueryType.DEFAULT_ELEMENT_NAME_XACML20);
		xacml20SamlAuthzStatementBuilder = makeSamlObjectBuilder(XACMLAuthzDecisionStatementType.DEFAULT_ELEMENT_NAME_XACML20);
		signatureBuilder = makeXmlObjectBuilder(Signature.DEFAULT_ELEMENT_NAME);

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

	private static void initializeResponse(StatusResponseType response,
			Status status, RequestAbstractType request) {
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
		SAMLObjectBuilder<T> b = (SAMLObjectBuilder<T>) objectBuilderFactory
				.getBuilder(name);
		Preconditions.checkState(b != null);
		return b;
	}
	

	@SuppressWarnings("unchecked")
	private static <T extends XMLObject> XMLObjectBuilder<T> makeXmlObjectBuilder(
			QName name) {
		XMLObjectBuilder<T> b = (XMLObjectBuilder<T>) objectBuilderFactory
				.getBuilder(name);
		Preconditions.checkState(b != null);
		return b;
	}

	@SuppressWarnings("unchecked")
	private static <M extends Marshaller> M makeSamlObjectMarshaller(QName name) {
		M m = (M) marshallerFactory.getMarshaller(name);
		Preconditions.checkState(m != null);
		return m;
	}

	@SuppressWarnings("unchecked")
	private static <M extends Unmarshaller> M makeSamlObjectUnmarshaller(
			QName name) {
		M u = (M) unmarshallerFactory.getUnmarshaller(name);
		Preconditions.checkState(u != null);
		return u;

	}

	public static void serialize(XMLObject xml, OutputStream out,
			boolean identOutput) throws Exception {
		Marshaller m = Configuration.getMarshallerFactory().getMarshaller(xml);
		Preconditions.checkState(m != null);
		Transformer serializer = transformerFactory.newTransformer();
		serializer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
		serializer.setOutputProperty(OutputKeys.ENCODING, "utf-8");
		serializer.setOutputProperty(OutputKeys.METHOD, "xml");
		serializer.setOutputProperty(OutputKeys.INDENT, identOutput ? "yes"
				: "no");
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
	public static <TI extends SAMLObject, TO extends SAMLObject, TN extends SAMLObject> SAMLMessageContext<TI, TO, TN> makeSamlMessageContext() {
		return new BasicSAMLMessageContext<TI, TO, TN>();
	}

	/**
	 * Static factory for SAML <code>Assertion</code> objects.
	 * 
	 * @param issuer
	 *            The entity issuing this assertion.
	 * @return A new <code>Assertion</code> object.
	 */
	public static Assertion makeAssertion(String issuer) {
		Assertion assertion = assertionBuilder.buildObject();
		assertion.setID(generateIdentifier());
		assertion.setVersion(SAMLVersion.VERSION_20);
		assertion.setIssueInstant(new DateTime());
		assertion.setIssuer(makeIssuer(issuer));
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
	public static Assertion makeAssertion(String issuer, Subject subject) {
		Assertion assertion = makeAssertion(issuer);
		assertion.setSubject(subject);
		return assertion;
	}

	public static RequestType unmarshallXacml20Request(Element request)
			throws UnmarshallingException {
		return (RequestType) xacml20ReqUnmarshaller.unmarshall(request);
	}
	
	public static void marshallXacml20Request(RequestType request, Document doc)
		throws MarshallingException {
		xacml20ReqMashaller.marshall(request, doc);
	}
	
	public static XACMLAuthzDecisionQueryType unmarshallXacml20AuthzDecisionQuery(Element request)
		throws UnmarshallingException {
		return (XACMLAuthzDecisionQueryType) xacml20SamlAuthzQueryUnmarshaller.unmarshall(request);
	}

	public static ResponseType unmarshallXacml20Response(Element request)
			throws UnmarshallingException {
		return (ResponseType) xacml20ResUnmarshaller.unmarshall(request);
	}
	
	public static Signature makeSiganture()
	{
		return signatureBuilder.buildObject(Signature.DEFAULT_ELEMENT_NAME);
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
			String issuer, String destination, boolean combinePolicies,
			RequestType xacml20Request) {
		XACMLAuthzDecisionQueryType xacmlQuery = xacml20SamlAuthzQueryBuilder
				.buildObject(XACMLAuthzDecisionQueryType.DEFAULT_ELEMENT_NAME_XACML20);
		Preconditions.checkState(xacmlQuery != null);
		xacmlQuery.setID(generateIdentifier());
		xacmlQuery.setIssuer(makeIssuer(issuer));
		xacmlQuery.setIssueInstant(new DateTime());
		xacmlQuery.setVersion(SAMLVersion.VERSION_20);
		xacmlQuery.setRequest(xacml20Request);
		xacmlQuery.setCombinePolicies(combinePolicies);
		xacmlQuery.setDestination(destination);
		return xacmlQuery;
	}

	public static Response makeXacml20AuthzDecisionQueryResponse(String issuer,
			XACMLAuthzDecisionQueryType request, Assertion assertion) {
		Response response = responseBuilder.buildObject(Response.DEFAULT_ELEMENT_NAME);
		initializeResponse(response, makeStatus(StatusCode.SUCCESS_URI),
				request);
		response.setIssuer(makeIssuer(issuer));
		response.getAssertions().add(assertion);
		return response;
	}
	
	
	public static Assertion makeXacml20AuthzDecisionAssertion(String issuer, 
			RequestType xacml20Request, ResponseType xacml20Response)
	{	
		XACMLAuthzDecisionStatementType authzStatement = xacml20SamlAuthzStatementBuilder.buildObject(
				Statement.DEFAULT_ELEMENT_NAME, 
				XACMLAuthzDecisionStatementType.TYPE_NAME_XACML20);
		authzStatement.setRequest(xacml20Request);
		authzStatement.setResponse(xacml20Response);
		Assertion authzAssertion = makeAssertion(issuer);
		authzAssertion.getStatements(XACMLAuthzDecisionStatementType.DEFAULT_ELEMENT_NAME_XACML20).add(authzStatement);
		return authzAssertion;
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
		Preconditions.checkState(issuerBuilder != null);
		Issuer issuer = issuerBuilder.buildObject(Issuer.DEFAULT_ELEMENT_NAME);
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

	public static void initializeLocalEntity(
			SAMLMessageContext<? extends SAMLObject, ? extends SAMLObject, ? extends SAMLObject> context,
			EntityDescriptor entity, RoleDescriptor role, QName endpointType) 
	{
		context.setLocalEntityId(entity.getEntityID());
		context.setLocalEntityMetadata(entity);
		context.setLocalEntityRole(endpointType);
		context.setLocalEntityRoleMetadata(role);
		context.setOutboundMessageIssuer(entity.getEntityID());
	}

	public static void initializePeerEntity(
			SAMLMessageContext<? extends SAMLObject, ? extends SAMLObject, ? extends SAMLObject> context,
			EntityDescriptor entity, 
			RoleDescriptor role, 
			QName endpointType,
			String binding) {
		context.setPeerEntityId(entity.getEntityID());
		context.setPeerEntityMetadata(entity);
		context.setPeerEntityRole(endpointType);
		context.setPeerEntityRoleMetadata(role);
		{
			BasicEndpointSelector selector = new BasicEndpointSelector();
			selector.setEntityMetadata(entity);
			selector.setEndpointType(endpointType);
			selector.setEntityRoleMetadata(role);
			selector.getSupportedIssuerBindings().add(binding);
			context.setPeerEntityEndpoint(selector.selectEndpoint());
		}
	}
	
	public static MetadataProvider getMetadata(File input) throws MetadataProviderException
	{
		FilesystemMetadataProvider provider = new FilesystemMetadataProvider(input);
		provider.setParserPool(new BasicParserPool());
		provider.initialize();
		return provider;
	}
}
