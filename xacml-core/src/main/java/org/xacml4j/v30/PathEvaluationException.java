package org.xacml4j.v30;

/*
 * #%L
 * Xacml4J Core Engine Implementation
 * %%
 * Copyright (C) 2009 - 2014 Xacml4J.org
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

import java.util.Optional;

public final class PathEvaluationException extends EvaluationException
{
	private static final long serialVersionUID = 1511624494955246280L;

	private String path;
	private Content.PathType pathType;

	private PathEvaluationException(String path, Content.PathType pathType){
		super(Status.processingError()
		            .message(path)
		            .build(), path);
		this.pathType = pathType;
		this.path = path;
	}

	private PathEvaluationException(String path, Content.PathType pathType, Throwable t){
		super(Status.processingError()
		            .message(Optional.ofNullable(t).map(e->e.getMessage()).orElse(null))
		            .error(t)
		            .build(),
		      path, t);
		this.pathType = pathType;
		this.path = path;
	}

	private PathEvaluationException(String path, Content.PathType pathType, String message){
		super(Status.processingError()
		            .message(message)
		            .build(), message);
		this.pathType = pathType;
		this.path = path;
	}

	private PathEvaluationException(Status status, String path, Content.PathType pathType, Throwable t){
		super(Status.from(status)
		            .error(t)
		            .build(), path, t);
		this.pathType = pathType;
		this.path = path;
	}

	public String getPath() {
		return path;
	}

	public Content.PathType getPathType() {
		return pathType;
	}

	public static PathEvaluationException invalidXpath(String path, Status status, Throwable t){
		return new PathEvaluationException(status, path, Content.PathType.XPATH, t);
	}

	public static PathEvaluationException invalidXpath(String path, Throwable t){
		return new PathEvaluationException( path, Content.PathType.XPATH, t);
	}

	public static PathEvaluationException invalidXpath(String path, String message){
		return new PathEvaluationException( path, Content.PathType.XPATH, message);
	}

	public static PathEvaluationException invalidXpathPath(String path){
		return new PathEvaluationException(path, Content.PathType.JPATH);
	}

	public static PathEvaluationException invalidJsonPath(String path, Status status, Throwable t){
		return new PathEvaluationException(status, path, Content.PathType.JPATH, t);
	}

	public static PathEvaluationException invalidJsonPath(String path, Throwable t){
		return new PathEvaluationException(path, Content.PathType.JPATH,
		                                   format("Invalid path=\"%s\" type=\"%s\" error=\"%s\"", path,
		                                          Content.PathType.JPATH.name(), t.getMessage()));
	}

	public static PathEvaluationException invalidJsonPath(String path){
		return new PathEvaluationException(path, Content.PathType.XPATH);
	}
	public static PathEvaluationException invalidXpathContextSelectorId(String path, String contextSelectorId){
		return new PathEvaluationException(path, Content.PathType.XPATH,
		                                   format("Invalid path=\"%s\" type=\"%s\" contextSelectorId=\"%s\"", path,
		                                          Content.PathType.XPATH.name(), contextSelectorId));
	}

	public static PathEvaluationException invalidJsonPathContextSelectorId(String path, String contextSelectorId){
		return new PathEvaluationException(path, Content.PathType.JPATH,
		                                   format("Invalid path=\"%s\" type=\"%s\" contextSelectorId=\"%s\"", path,
		                                          Content.PathType.JPATH.name(), contextSelectorId));
	}
}
