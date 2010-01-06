package org.oasis.xacml.azapi.constants;

public enum AzCategoryIdStatusDetail implements AzCategoryId {
	/** XACML Implicit Category:  <b>urn:oasis:names:tc:xacml:3.0:attribute-category:status-detail</b> */ 
	AZ_CATEGORY_ID_STATUSDETAIL(
			"urn:oasis:names:tc:xacml:3.0:attribute-category:status-detail");
	private final String azCategoryId;

	AzCategoryIdStatusDetail(String azCategoryId) {
		this.azCategoryId = azCategoryId;
	}

	@Override
	public String toString() {
		return azCategoryId;
	}
}