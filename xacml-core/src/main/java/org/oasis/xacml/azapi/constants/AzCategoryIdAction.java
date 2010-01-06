package org.oasis.xacml.azapi.constants;

public enum AzCategoryIdAction implements AzCategoryId {
	/** XACML Category:  <b>urn:oasis:names:tc:xacml:3.0:attribute-category:action</b> */ 
	AZ_CATEGORY_ID_ACTION(
	"urn:oasis:names:tc:xacml:3.0:attribute-category:action");
	private final String azCategoryId;
	AzCategoryIdAction(String azCategoryId){
		this.azCategoryId = azCategoryId;
	}
	@Override public String toString() {
		return azCategoryId;
	}
}
