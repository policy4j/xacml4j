package org.oasis.xacml.azapi.constants;

/** The AzCategoryIdSubjectIntermediary identifier indicates a 
 * system entity through which the access request was passed. 
 * There may be more than one. No means is provided to specify 
 * the order in which they passed the message. 
 */
public enum AzCategoryIdSubjectIntermediary implements AzCategoryId {
	/** XACML Category: <b>urn:oasis:names:tc:xacml:1.0:subject-category:intermediary-subject</b>. */
	AZ_CATEGORY_ID_SUBJECT_INTERMEDIARY(
			"urn:oasis:names:tc:xacml:1.0:subject-category:intermediary-subject");
	private final String azCategoryId;

	AzCategoryIdSubjectIntermediary(String azCategoryId) {
		this.azCategoryId = azCategoryId;
	}

	@Override
	public String toString() {
		return azCategoryId;
	}
}