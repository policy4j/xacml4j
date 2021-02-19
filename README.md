# Xacml4j XACML Implementation

XACML4j's reference implementation of the OASIS XACML 3.0 Standard. The framework represents the entire XACML 3.0 object set as a collection of Java interfaces and standard implementations of those interfaces.  The PDP engine is built on top of this framework and represents a complete implementation of a XACML 3.0 PDP, including all of the multi-decision profiles. In addition, the framework also contains an implementation of the OASIS XACML 3.0 RESTful API v1.0 and XACML JSON Profile v1.0 WD 14. The PEP API includes annotation functionality, allowing application developers to simply annotate a Java class to provide attributes for a request. The annotation support removes the need for application developers to learn much of the API.

The ramework also includes interfaces and implementations to standardize development of PIP engines that are used by the PDP implementation, and can be used by other implementations built on top of the framework. The framework also includes interfaces and implementations for a PAP distributed cloud infrastructure of PDP nodes that includes support for policy distribution and pip configurations. This PAP infrastructure includes a web application administrative console that contains a XACML 3.0 policy editor, attribute dictionary support, and management of PDP RESTful node instances. In addition, there are tools available for policy simulation.

Currently Xacml4j is used in production: Xfinty Wifi Acccess Control, Xfinity TV Everywhere Access Control


# Requirements

* Java JDK 1.8

*  Apache Maven to compile, install and run the software.

# Building the source code

From the directory you downloaded the source to, just type 'mvn clean install'.

# Running the projects





Continuous Integration status on Travis CI: [![Build Status](https://travis-ci.org/xacml4j-opensource/xacml4j.svg?branch=master)](https://travis-ci.org/xacml4j-opensource/xacml4j)

[![Analytics](https://ga-beacon.appspot.com/UA-56280504-2/xacml4j/readme?pixel)](https://github.com/igrigorik/ga-beacon)
