package org.xacml4j.opensaml;

import java.io.IOException;
import java.io.InputStream;

import org.joda.time.DateTime;
import org.opensaml.util.resource.Resource;
import org.opensaml.util.resource.ResourceException;

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
