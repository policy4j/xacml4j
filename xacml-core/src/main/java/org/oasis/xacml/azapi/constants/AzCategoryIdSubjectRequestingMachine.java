package org.oasis.xacml.azapi.constants;

/** The AzCategoryIdSubjectRequestingMachine identifier indicates 
 * a system entity associated with the computer that initiated the 
 * access request. An example would be an IPsec identity. 
 */
public enum AzCategoryIdSubjectRequestingMachine implements AzCategoryId {
	/** XACML Category: <b>urn:oasis:names:tc:xacml:1.0:subject-category:requesting-machine</b>. */ 
	AZ_CATEGORY_ID_SUBJECT_REQUESTING_MACHINE(
			"urn:oasis:names:tc:xacml:1.0:subject-category:requesting-machine");
	private final String azCategoryId;

	AzCategoryIdSubjectRequestingMachine(String azCategoryId) {
		this.azCategoryId = azCategoryId;
	}

	@Override
	public String toString() {
		return azCategoryId;
	}
}