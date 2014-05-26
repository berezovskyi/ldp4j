/**
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 *   This file is part of the LDP4j Project:
 *     http://www.ldp4j.org/
 *
 *   Center for Open Middleware
 *     http://www.centeropenmiddleware.com/
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 *   Copyright (C) 2014 Center for Open Middleware.
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *             http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 *   Artifact    : org.ldp4j.framework:ldp4j-client-api:1.0.0-SNAPSHOT
 *   Bundle      : ldp4j-client-api-1.0.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.client;

import java.net.URL;

/**
 * A proxy to a <b>Linked Data Platform Resource</b>.
 * 
 * @author Miguel Esteban Gutiérrez
 * @since 1.0.0
 * @version 1.0
 */
public interface ILDPResource {

	/**
	 * Get the identity of the resource.
	 * 
	 * @return The identity of the resource or <code>null</code> if the resource
	 *         is not published yet.
	 */
	URL getIdentity();
	
	/**
	 * Get the content of the resource using the specified format.
	 * 
	 * @param format
	 *            The syntax in which the content has to be formatted.
	 * @return The content of the resource
	 *            formated according to the specified format.
	 * @throws LDPResourceException if the contents cannot be retrieved.
	 * @see Format
	 */
	IRepresentation getContent(Format format) throws LDPResourceException;

	/**
	 * Get the content of the resource using the specified format.
	 * 
	 * @param format
	 *            The syntax in which the content has to be formatted.
	 * @return The content of the resource
	 *            formated according to the specified format.
	 * @throws LDPResourceException if the contents cannot be retrieved.
	 * @see Format
	 */
	IRepresentation updateContent(IContent content, Format format) throws LDPResourceException;

	/**
	 * Delete the resource.
	 * 
	 * @return The result of the deletion request.
	 * @throws LDPResourceException if the resource could not be deleted.
	 */
	DeletionResult delete() throws LDPResourceException;
}