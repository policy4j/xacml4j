package org.oasis.xacml.azapi.constants;

/**
 * AzCategoryId is a marker interface to identify the XACML Category
 * of a collection of AzAttributes (an AzEntity) and to identify
 * individual AzAttributes as being members of a collection of
 * a specific category.
 * <p>
 * The XACML Category represents an "entity" in the overall semantics
 * of the XACML authorization process. These "entities" have counterparts
 * in the real world and may be generally regarded as a collection
 * of attributes describing some specific distinct member of a set
 * of entities involved in the authorization process. The XACML
 * entities that have been identified as specific categories are the
 * following:
 * <pre>
 *  	environment, 
 *  	resource, 
 *  	action, 
 *  	access-subject, 
 *  	recipient-subject, 
 *  	intermediary-subject, 
 *  	codebase, 
 *  	requesting-machine
 *  </pre>
 * <p>
 * AzCategoryId represents the XACML "category" attribute, which has
 * both explicit and implicit representations in XACML 2.0 and 3.0.
 * <p>
 * In XACML 2.0 there are 4 types of Attribute collections submitted
 * in a request, which are: Subject, Resource, Action, and Environment.
 * <p>
 * These may be thought of as "implicit" categories. Within the Subject
 * element, XACML defines 5 "explicit" categories of Subject, which have
 * explicit identifiers, which are explicit values of the 
 * AzCategoryIdSubject* subtypes of AzCategoryId. 
 * <p>
 * There are no corresponding identifiers in XACML 2.0 for the 
 * Resource, Action, or Environment elements, and they
 * may be thought of as having one "implicit" category identifier 
 * each.
 * <p>
 * In XACML 3.0, these "implicit" identifiers are made explicit, and those
 * are the values that are used for both 2.0 and 3.0 here. These identifiers
 * in the implicit case are not used in the XACML 2.0 Request, however they
 * serve the purpose of explicitly identifying the objects
 * <p>
 * From this quote in the XACML 2.0 specification on Subject Category, we may
 * infer that the format of CategoryId is a URI: "it narrows the attribute 
 * search space to the specific categorized subject such that the value of 
 * this element’s SubjectCategory attribute matches, by URI equality, the 
 * value of the request context’s <Subject> element’s SubjectCategory 
 * attribute"
 * 
 * @author Rich
 *
 */
public interface AzCategoryId {

}
