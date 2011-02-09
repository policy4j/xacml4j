package com.artagon.xacml.opensaml;

import java.security.KeyStore;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.core.io.Resource;

public class KeyStoreFactory implements FactoryBean<KeyStore>{

	private String ksType;
	private Resource ksLocation;
	private String ksPassword;

	@Override
	public KeyStore getObject() throws Exception {
		KeyStore ks = KeyStore.getInstance(ksType);
		ks.load(ksLocation.getInputStream(), ksPassword.toCharArray());
		return ks;
	}

	@Override
	public Class<?> getObjectType() {
		return KeyStore.class;
	}

	@Override
	public boolean isSingleton() {
		return false;
	}

	public void setType(String type) {
		this.ksType = type;
	}

	public void setLocation(Resource location) {
		this.ksLocation = location;
	}

	public void setPassword(String password) {
		this.ksPassword = password;
	}
}
