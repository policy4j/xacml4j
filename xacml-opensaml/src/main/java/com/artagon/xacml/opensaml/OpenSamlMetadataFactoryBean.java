package com.artagon.xacml.opensaml;

import java.io.IOException;
import java.io.InputStream;
import java.util.Timer;

import org.joda.time.DateTime;
import org.opensaml.util.resource.Resource;
import org.opensaml.util.resource.ResourceException;

import org.opensaml.saml2.metadata.provider.MetadataProvider;
import org.opensaml.saml2.metadata.provider.ResourceBackedMetadataProvider;
import org.springframework.beans.factory.config.AbstractFactoryBean;

import com.google.common.base.Preconditions;

public class OpenSamlMetadataFactoryBean extends AbstractFactoryBean<MetadataProvider>
{
	
	private org.springframework.core.io.Resource metadata;
	
	public void setLocation(org.springframework.core.io.Resource location){
		this.metadata = location;
	}
	
	@Override
	public Class<?> getObjectType() {
		return MetadataProvider.class;
	}

	@Override
	protected MetadataProvider createInstance() throws Exception {
		Preconditions.checkState(metadata != null);
		ResourceBackedMetadataProvider mdp = new ResourceBackedMetadataProvider(
 				new SpringResourceWrapper(metadata),
        		new Timer(true), Integer.MAX_VALUE);
		mdp.initialize();
		return mdp;
	}

	public class SpringResourceWrapper implements Resource {

		private org.springframework.core.io.Resource source;

		public SpringResourceWrapper(org.springframework.core.io.Resource source) {
			this.source = source;
		}

		@Override public boolean exists() throws ResourceException {
			return source.exists();
		}

		@Override public InputStream getInputStream() throws ResourceException {
			try {
				return source.getInputStream();
			} catch (IOException e) {
				throw new ResourceException(e);
			}
		}

		@Override public DateTime getLastModifiedTime() throws ResourceException {
			try {
				return new DateTime(source.lastModified());
			} catch (IOException e) {
				throw new ResourceException(e);
			}
		}

		@Override public String getLocation() {
			return source.getDescription();
		}
	}
}
