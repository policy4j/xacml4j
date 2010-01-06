package org.oasis.xacml.azapi.constants;

public enum AzCategoryIdResource implements AzCategoryId {
	/** XACML Category:  <b>urn:oasis:names:tc:xacml:3.0:attribute-category:resource</b> */ 
	AZ_CATEGORY_ID_RESOURCE(
	"urn:oasis:names:tc:xacml:3.0:attribute-category:resource");
	private final String azCategoryId;
	AzCategoryIdResource(String azCategoryId){
		this.azCategoryId = azCategoryId;
	}
	@Override public String toString() {
		return azCategoryId;
	}
}
