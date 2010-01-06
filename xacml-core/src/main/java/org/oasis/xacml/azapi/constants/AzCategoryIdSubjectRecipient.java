package org.oasis.xacml.azapi.constants;

/** The AzCategoryIdSubjectRecipient identifier indicates the system
 * entity that will receive the results of the request (used when it 
 * is distinct from the access-subject). 
 */
public enum AzCategoryIdSubjectRecipient implements AzCategoryId {
	/** XACML Category: <b>urn:oasis:names:tc:xacml:1.0:subject-category:recipient-subject</b>. */
	AZ_CATEGORY_ID_SUBJECT_RECIPIENT(
			"urn:oasis:names:tc:xacml:1.0:subject-category:recipient-subject");
	private final String azCategoryId;

	AzCategoryIdSubjectRecipient(String azCategoryId) {
		this.azCategoryId = azCategoryId;
	}

	@Override
	public String toString() {
		return azCategoryId;
	}
}