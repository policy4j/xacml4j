package org.oasis.xacml.azapi.constants;

public enum AzCategoryIdObligation implements AzCategoryId {
	/** XACML Implicit Category:  <b>urn:oasis:names:tc:xacml:3.0:attribute-category:obligation</b> */ 
	AZ_CATEGORY_ID_OBLIGATION(
			"urn:oasis:names:tc:xacml:3.0:attribute-category:obligation");
	private final String azCategoryId;

	AzCategoryIdObligation(String azCategoryId) {
		this.azCategoryId = azCategoryId;
	}

	@Override
	public String toString() {
		return azCategoryId;
	}
}