package org.oasis.xacml.azapi.constants;

/**
 * AzCategoryIdSubject* objects correspond to the values defined
 * in XACML 2.0 Appendix B.4 
 * <pre>
 *   http://docs.oasis-open.org/xacml/2.0/access_control-xacml-2.0-core-spec-os.pdf,
 * </pre> 
 * which are copied in the specific definitions below.
 * <p>
 * The AzCategoryIdSubjectAccess identifier indicates the 
 * system entity that initiated the access request. That is, 
 * the initial entity in a request chain. 
 * <p>
 * <b>If subject category is not specified, this is the 
 * default value. </b>
 */
public enum AzCategoryIdSubjectAccess implements AzCategoryId {
	/** XACML Category: <b>urn:oasis:names:tc:xacml:1.0:subject-category:access-subject</b> (this is <b>default</b> that will be used if nothing else provided). */ 
	AZ_CATEGORY_ID_SUBJECT_ACCESS(
			"urn:oasis:names:tc:xacml:1.0:subject-category:access-subject");
	private final String azCategoryId;

	AzCategoryIdSubjectAccess(String azCategoryId) {
		this.azCategoryId = azCategoryId;
	}

	@Override
	public String toString() {
		return azCategoryId;
	}
}
