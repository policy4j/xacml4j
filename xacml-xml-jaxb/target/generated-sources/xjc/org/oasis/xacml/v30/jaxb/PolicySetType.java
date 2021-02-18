//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.3.0 
// See <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2021.02.02 at 03:27:19 PM EST 
//


package org.oasis.xacml.v30.jaxb;

/*
 * #%L
 * Xacml4J XML representation JAXB classes
 * %%
 * Copyright (C) 2009 - 2021 Xacml4J.org
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementRefs;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for PolicySetType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="PolicySetType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element ref="{urn:oasis:names:tc:xacml:3.0:core:schema:wd-17}Description" minOccurs="0"/&gt;
 *         &lt;element ref="{urn:oasis:names:tc:xacml:3.0:core:schema:wd-17}PolicyIssuer" minOccurs="0"/&gt;
 *         &lt;element ref="{urn:oasis:names:tc:xacml:3.0:core:schema:wd-17}PolicySetDefaults" minOccurs="0"/&gt;
 *         &lt;element ref="{urn:oasis:names:tc:xacml:3.0:core:schema:wd-17}Target"/&gt;
 *         &lt;element ref="{urn:oasis:names:tc:xacml:3.0:core:schema:wd-17}Condition" minOccurs="0"/&gt;
 *         &lt;choice maxOccurs="unbounded" minOccurs="0"&gt;
 *           &lt;element ref="{urn:oasis:names:tc:xacml:3.0:core:schema:wd-17}PolicySet"/&gt;
 *           &lt;element ref="{urn:oasis:names:tc:xacml:3.0:core:schema:wd-17}Policy"/&gt;
 *           &lt;element ref="{urn:oasis:names:tc:xacml:3.0:core:schema:wd-17}PolicySetIdReference"/&gt;
 *           &lt;element ref="{urn:oasis:names:tc:xacml:3.0:core:schema:wd-17}PolicyIdReference"/&gt;
 *           &lt;element ref="{urn:oasis:names:tc:xacml:3.0:core:schema:wd-17}CombinerParameters"/&gt;
 *           &lt;element ref="{urn:oasis:names:tc:xacml:3.0:core:schema:wd-17}PolicyCombinerParameters"/&gt;
 *           &lt;element ref="{urn:oasis:names:tc:xacml:3.0:core:schema:wd-17}PolicySetCombinerParameters"/&gt;
 *         &lt;/choice&gt;
 *         &lt;element ref="{urn:oasis:names:tc:xacml:3.0:core:schema:wd-17}ObligationExpressions" minOccurs="0"/&gt;
 *         &lt;element ref="{urn:oasis:names:tc:xacml:3.0:core:schema:wd-17}AdviceExpressions" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="PolicySetId" use="required" type="{http://www.w3.org/2001/XMLSchema}anyURI" /&gt;
 *       &lt;attribute name="Version" use="required" type="{urn:oasis:names:tc:xacml:3.0:core:schema:wd-17}VersionType" /&gt;
 *       &lt;attribute name="PolicyCombiningAlgId" use="required" type="{http://www.w3.org/2001/XMLSchema}anyURI" /&gt;
 *       &lt;attribute name="MaxDelegationDepth" type="{http://www.w3.org/2001/XMLSchema}integer" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PolicySetType", propOrder = {
    "description",
    "policyIssuer",
    "policySetDefaults",
    "target",
    "condition",
    "policySetOrPolicyOrPolicySetIdReference",
    "obligationExpressions",
    "adviceExpressions"
})
public class PolicySetType {

    @XmlElement(name = "Description")
    protected String description;
    @XmlElement(name = "PolicyIssuer")
    protected PolicyIssuerType policyIssuer;
    @XmlElement(name = "PolicySetDefaults")
    protected DefaultsType policySetDefaults;
    @XmlElement(name = "Target", required = true)
    protected TargetType target;
    @XmlElement(name = "Condition")
    protected ConditionType condition;
    @XmlElementRefs({
        @XmlElementRef(name = "PolicySet", namespace = "urn:oasis:names:tc:xacml:3.0:core:schema:wd-17", type = JAXBElement.class, required = false),
        @XmlElementRef(name = "Policy", namespace = "urn:oasis:names:tc:xacml:3.0:core:schema:wd-17", type = JAXBElement.class, required = false),
        @XmlElementRef(name = "PolicySetIdReference", namespace = "urn:oasis:names:tc:xacml:3.0:core:schema:wd-17", type = JAXBElement.class, required = false),
        @XmlElementRef(name = "PolicyIdReference", namespace = "urn:oasis:names:tc:xacml:3.0:core:schema:wd-17", type = JAXBElement.class, required = false),
        @XmlElementRef(name = "CombinerParameters", namespace = "urn:oasis:names:tc:xacml:3.0:core:schema:wd-17", type = JAXBElement.class, required = false),
        @XmlElementRef(name = "PolicyCombinerParameters", namespace = "urn:oasis:names:tc:xacml:3.0:core:schema:wd-17", type = JAXBElement.class, required = false),
        @XmlElementRef(name = "PolicySetCombinerParameters", namespace = "urn:oasis:names:tc:xacml:3.0:core:schema:wd-17", type = JAXBElement.class, required = false)
    })
    protected List<JAXBElement<?>> policySetOrPolicyOrPolicySetIdReference;
    @XmlElement(name = "ObligationExpressions")
    protected ObligationExpressionsType obligationExpressions;
    @XmlElement(name = "AdviceExpressions")
    protected AdviceExpressionsType adviceExpressions;
    @XmlAttribute(name = "PolicySetId", required = true)
    @XmlSchemaType(name = "anyURI")
    protected String policySetId;
    @XmlAttribute(name = "Version", required = true)
    protected String version;
    @XmlAttribute(name = "PolicyCombiningAlgId", required = true)
    @XmlSchemaType(name = "anyURI")
    protected String policyCombiningAlgId;
    @XmlAttribute(name = "MaxDelegationDepth")
    protected BigInteger maxDelegationDepth;

    /**
     * Gets the value of the description property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the value of the description property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDescription(String value) {
        this.description = value;
    }

    /**
     * Gets the value of the policyIssuer property.
     * 
     * @return
     *     possible object is
     *     {@link PolicyIssuerType }
     *     
     */
    public PolicyIssuerType getPolicyIssuer() {
        return policyIssuer;
    }

    /**
     * Sets the value of the policyIssuer property.
     * 
     * @param value
     *     allowed object is
     *     {@link PolicyIssuerType }
     *     
     */
    public void setPolicyIssuer(PolicyIssuerType value) {
        this.policyIssuer = value;
    }

    /**
     * Gets the value of the policySetDefaults property.
     * 
     * @return
     *     possible object is
     *     {@link DefaultsType }
     *     
     */
    public DefaultsType getPolicySetDefaults() {
        return policySetDefaults;
    }

    /**
     * Sets the value of the policySetDefaults property.
     * 
     * @param value
     *     allowed object is
     *     {@link DefaultsType }
     *     
     */
    public void setPolicySetDefaults(DefaultsType value) {
        this.policySetDefaults = value;
    }

    /**
     * Gets the value of the target property.
     * 
     * @return
     *     possible object is
     *     {@link TargetType }
     *     
     */
    public TargetType getTarget() {
        return target;
    }

    /**
     * Sets the value of the target property.
     * 
     * @param value
     *     allowed object is
     *     {@link TargetType }
     *     
     */
    public void setTarget(TargetType value) {
        this.target = value;
    }

    /**
     * Gets the value of the condition property.
     * 
     * @return
     *     possible object is
     *     {@link ConditionType }
     *     
     */
    public ConditionType getCondition() {
        return condition;
    }

    /**
     * Sets the value of the condition property.
     * 
     * @param value
     *     allowed object is
     *     {@link ConditionType }
     *     
     */
    public void setCondition(ConditionType value) {
        this.condition = value;
    }

    /**
     * Gets the value of the policySetOrPolicyOrPolicySetIdReference property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the policySetOrPolicyOrPolicySetIdReference property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getPolicySetOrPolicyOrPolicySetIdReference().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link JAXBElement }{@code <}{@link PolicySetType }{@code >}
     * {@link JAXBElement }{@code <}{@link PolicyType }{@code >}
     * {@link JAXBElement }{@code <}{@link IdReferenceType }{@code >}
     * {@link JAXBElement }{@code <}{@link IdReferenceType }{@code >}
     * {@link JAXBElement }{@code <}{@link CombinerParametersType }{@code >}
     * {@link JAXBElement }{@code <}{@link PolicyCombinerParametersType }{@code >}
     * {@link JAXBElement }{@code <}{@link PolicySetCombinerParametersType }{@code >}
     * 
     * 
     */
    public List<JAXBElement<?>> getPolicySetOrPolicyOrPolicySetIdReference() {
        if (policySetOrPolicyOrPolicySetIdReference == null) {
            policySetOrPolicyOrPolicySetIdReference = new ArrayList<JAXBElement<?>>();
        }
        return this.policySetOrPolicyOrPolicySetIdReference;
    }

    /**
     * Gets the value of the obligationExpressions property.
     * 
     * @return
     *     possible object is
     *     {@link ObligationExpressionsType }
     *     
     */
    public ObligationExpressionsType getObligationExpressions() {
        return obligationExpressions;
    }

    /**
     * Sets the value of the obligationExpressions property.
     * 
     * @param value
     *     allowed object is
     *     {@link ObligationExpressionsType }
     *     
     */
    public void setObligationExpressions(ObligationExpressionsType value) {
        this.obligationExpressions = value;
    }

    /**
     * Gets the value of the adviceExpressions property.
     * 
     * @return
     *     possible object is
     *     {@link AdviceExpressionsType }
     *     
     */
    public AdviceExpressionsType getAdviceExpressions() {
        return adviceExpressions;
    }

    /**
     * Sets the value of the adviceExpressions property.
     * 
     * @param value
     *     allowed object is
     *     {@link AdviceExpressionsType }
     *     
     */
    public void setAdviceExpressions(AdviceExpressionsType value) {
        this.adviceExpressions = value;
    }

    /**
     * Gets the value of the policySetId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPolicySetId() {
        return policySetId;
    }

    /**
     * Sets the value of the policySetId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPolicySetId(String value) {
        this.policySetId = value;
    }

    /**
     * Gets the value of the version property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getVersion() {
        return version;
    }

    /**
     * Sets the value of the version property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setVersion(String value) {
        this.version = value;
    }

    /**
     * Gets the value of the policyCombiningAlgId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPolicyCombiningAlgId() {
        return policyCombiningAlgId;
    }

    /**
     * Sets the value of the policyCombiningAlgId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPolicyCombiningAlgId(String value) {
        this.policyCombiningAlgId = value;
    }

    /**
     * Gets the value of the maxDelegationDepth property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getMaxDelegationDepth() {
        return maxDelegationDepth;
    }

    /**
     * Sets the value of the maxDelegationDepth property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setMaxDelegationDepth(BigInteger value) {
        this.maxDelegationDepth = value;
    }

}
