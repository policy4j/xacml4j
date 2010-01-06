package org.oasis.xacml.azapi.constants;

/** The AzCategoryIdSubjectCodebase identifier indicates a 
 * system entity associated with a local or remote codebase 
 * that generated the request. Corresponding subject attributes 
 * might include the URL from which it was loaded and/or the 
 * identity of the code-signer. There may be more than one. 
 * No means is provided to specify the order in which they 
 * processed the request. 
 */
public enum AzCategoryIdSubjectCodebase implements AzCategoryId {
	/** XACML Category: <b>urn:oasis:names:tc:xacml:1.0:subject-category:codebase</b>. */
	AZ_CATEGORY_ID_SUBJECT_CODEBASE(
			"urn:oasis:names:tc:xacml:1.0:subject-category:codebase");
	private final String azCategoryId;

	AzCategoryIdSubjectCodebase(String azCategoryId) {
		this.azCategoryId = azCategoryId;
	}

	@Override
	public String toString() {
		return azCategoryId;
	}
}
