package com.artagon.xacml.spring;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Collection;
import java.util.LinkedList;

import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import com.google.common.base.Preconditions;

public class FileSystemResourceCollectionFactoryBean extends AbstractFactoryBean
{
	private File folder;
	private String fileNameFilter;
	
	@Override
	public Class<ResourceCollection> getObjectType() {
		return ResourceCollection.class;
	}
	
	public void setFolder(File folder)
	{
		this.folder = folder;
	}
	
	public void setNameFilter(String regexp)
	{
		this.fileNameFilter = regexp;
	}
	
	@Override
	protected Object createInstance() throws Exception 
	{
		Preconditions.checkState(folder != null);
		Preconditions.checkState(folder.exists());
		Preconditions.checkState(folder.isDirectory());
		Preconditions.checkState(fileNameFilter != null);
		String[] names = folder.list(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.matches(fileNameFilter);
			}
		});
		Collection<Resource> files = new LinkedList<Resource>();
		for(String name : names){
			files.add(new FileSystemResource(new File(folder, name)));
		}
		ResourceCollection collection = new ResourceCollection();
		collection.setResources(files);
		return collection;
	}
}
