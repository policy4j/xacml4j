package org.oasis.xacml.azapi.constants;

public enum AzCategoryIdEnvironment implements AzCategoryId {
	/** XACML Category:  <b>urn:oasis:names:tc:xacml:3.0:attribute-category:environment</b> */ 
	AZ_CATEGORY_ID_ENVIRONMENT(
			"urn:oasis:names:tc:xacml:3.0:attribute-category:environment");
	private final String azCategoryId;

	AzCategoryIdEnvironment(String azCategoryId) {
		this.azCategoryId = azCategoryId;
	}

	@Override
	public String toString() {
		return azCategoryId;
	}
}
