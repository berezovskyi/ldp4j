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
 *   Artifact    : org.ldp4j.framework:ldp4j-application-core:1.0.0-SNAPSHOT
 *   Bundle      : ldp4j-application-core-1.0.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.application.engine.endpoint;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Date;

import org.ldp4j.application.engine.context.EntityTag;
import org.ldp4j.application.engine.resource.Attachment;
import org.ldp4j.application.engine.resource.Container;
import org.ldp4j.application.engine.resource.Member;
import org.ldp4j.application.engine.resource.Resource;
import org.ldp4j.application.engine.resource.ResourceId;
import org.ldp4j.application.engine.resource.Slug;
import org.ldp4j.application.engine.spi.PersistencyManager;
import org.ldp4j.application.engine.spi.Service;
import org.ldp4j.application.engine.spi.ServiceBuilder;
import org.ldp4j.application.engine.template.AttachedTemplate;
import org.ldp4j.application.engine.template.ContainerTemplate;
import org.ldp4j.application.engine.template.ResourceTemplate;
import org.ldp4j.application.engine.util.ListenerManager;
import org.ldp4j.application.engine.util.Notification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class EndpointManagementService implements Service {

	private static final class EndpointCreationNotification implements Notification<EndpointLifecycleListener> {
		private final Endpoint endpoint;

		private EndpointCreationNotification(Endpoint endpoint) {
			this.endpoint = endpoint;
		}

		@Override
		public void propagate(EndpointLifecycleListener listener) {
			listener.endpointCreated(endpoint);
		}
	}

	private static final class EndpointDeletionNotification implements Notification<EndpointLifecycleListener> {
		private final Endpoint endpoint;

		private EndpointDeletionNotification(Endpoint endpoint) {
			this.endpoint = endpoint;
		}

		@Override
		public void propagate(EndpointLifecycleListener listener) {
			listener.endpointDeleted(endpoint);
		}
	}

	private static final class EndpointManagementServiceBuilder extends ServiceBuilder<EndpointManagementService> {

		private EndpointManagementServiceBuilder() {
			super(EndpointManagementService.class);
		}

		public EndpointManagementService build() {
			return new EndpointManagementService(persistencyManager());
		}

	}

	private static final int MAX_ENDPOINT_CREATION_FAILURE = 3;

	private final PersistencyManager persistencyManager;
	private final ListenerManager<EndpointLifecycleListener> listenerManager;


	private EndpointManagementService(PersistencyManager persistencyManager) {
		this.persistencyManager = persistencyManager;
		this.listenerManager=ListenerManager.<EndpointLifecycleListener>newInstance();
	}

	private String calculateResourcePath(Resource resource, String desiredPath) throws EndpointNotFoundException {
		if(resource.isRoot()) {
			throw new IllegalStateException("Cannot get path for root resource");
		}
		Resource parent=this.persistencyManager.resourceOfId(resource.parentId(),Resource.class);
		if(parent==null) {
			throw new IllegalStateException("Could not load resource '"+resource.parentId()+"' from the repository");
		}

		String result=
			parent instanceof Container?
				generatePathForMember(resource,(Container)parent,desiredPath):
				null;
		if(result==null) {
			result = generatePathForAttachment(resource,parent);
			if(result==null) {
				throw new IllegalStateException("Could not determine path for resource '"+resource.id()+"' with parent '"+parent.id()+"'");
			}

		}
		return result;
	}

	private String generatePathForAttachment(Resource child, Resource parent) throws EndpointNotFoundException {
		Attachment attachment=parent.findAttachment(child.id());
		if(attachment==null) {
			return null;
		}
		Endpoint endpoint=getResourceEndpoint(parent.id());
		ResourceTemplate parentTemplate=this.persistencyManager.templateOfId(parent.id().templateId());
		AttachedTemplate attachedTemplate = parentTemplate.attachedTemplate(attachment.id());
		return
			PathBuilder.
				create().
					addSegment(endpoint.path()).
					addSegment(attachedTemplate.path()).
					addSegment(attachment.version()>0?attachment.version():null).
					build();
	}

	private String generatePathForMember(Resource child, Container parent, String desiredPath) throws EndpointNotFoundException {
		Member member = parent.findMember(child.id());
		if(member!=null) {
			Endpoint endpoint=getResourceEndpoint(parent.id());
			ContainerTemplate parentTemplate=this.persistencyManager.templateOfId(parent.id().templateId(),ContainerTemplate.class);
			if(parentTemplate==null) {
				throw new IllegalStateException("Could not find template resource '"+parent+"'");
			}
			String slugPath=getSlugPath(parent, desiredPath);
			return
				PathBuilder.
					create().
						addSegment(endpoint.path()).
						addSegment(parentTemplate.memberPath().or("")).
						addSegment(member.number()).
						addSegment(slugPath).
						build();
		}
		return null;
	}

	private String getSlugPath(Container parent, String desiredPath) {
		String slugPath=null;
		if(desiredPath!=null) {
			Slug slug=parent.findSlug(desiredPath);
			if(slug==null) {
				slug=parent.addSlug(desiredPath);
			}
			slugPath=slug.nextPath();
		}
		return slugPath;
	}

	private static Logger LOGGER=LoggerFactory.getLogger(EndpointManagementService.class);

	private Endpoint createEndpoint(Resource resource, String relativePath, EntityTag entityTag, Date lastModified) throws EndpointCreationException {
		String candidatePath=relativePath;
		int repetitions=0;
		while(repetitions<MAX_ENDPOINT_CREATION_FAILURE) {
			LOGGER.debug("({}) Creating endpoint for {} [{},{},{}]",repetitions,resource.id(),entityTag,lastModified,relativePath);
			try {
				String resourcePath = calculateResourcePath(resource,candidatePath);
				LOGGER.debug("({}) Trying resource path {} ",repetitions,resourcePath);
				Endpoint newEndpoint = this.persistencyManager.createEndpoint(resource,resourcePath,entityTag,lastModified);
				this.persistencyManager.add(newEndpoint);
				return newEndpoint;
			} catch (EndpointNotFoundException e) {
				throw new EndpointCreationException("Could not calculate path for resource '"+resource.id()+"'",e);
			} catch (IllegalArgumentException e) {
				LOGGER.debug("Could not create endpoint",e);
				// TODO: Define a proper exception
				repetitions++;
				candidatePath=null;
			}
		}
		throw new EndpointCreationException("Could not create endpoint for resource '"+resource.id()+"'");
	}

	public void registerEndpointLifecycleListener(EndpointLifecycleListener listener) {
		this.listenerManager.registerListener(listener);
	}

	public void deregisterEndpointLifecycleListener(EndpointLifecycleListener listener) {
		this.listenerManager.deregisterListener(listener);
	}

	public Endpoint getResourceEndpoint(ResourceId resourceId) throws EndpointNotFoundException {
		checkNotNull(resourceId,"Resource identifier cannot be null");
		Endpoint endpoint = this.persistencyManager.endpointOfResource(resourceId);
		if(endpoint==null) {
			throw new EndpointNotFoundException(resourceId);
		}
		return endpoint;
	}

	public Endpoint resolveEndpoint(String path) {
		checkNotNull(path,"Path cannot be null");
		return this.persistencyManager.endpointOfPath(path);
	}

	/**
	 * TODO: Verify that http://tools.ietf.org/html/rfc7232#section-2.2
	 * holds: if the clock in the request is ahead of the clock of the origin
	 * server (e.g., I request from Spain the update of a resource held in USA)
	 * the last-modified data should be changed to that of the request and not
	 * a generated date from the origin server
	 */
	public Endpoint createEndpointForResource(Resource resource, String relativePath, EntityTag entityTag, Date lastModified) throws EndpointCreationException {
		checkNotNull(resource,"Resource cannot be null");
		checkNotNull(entityTag,"Entity tag cannot be null");
		checkNotNull(lastModified,"Last modified cannot be null");
		Endpoint newEndpoint = createEndpoint(resource, relativePath, entityTag, lastModified);
		this.listenerManager.notify(new EndpointCreationNotification(newEndpoint));
		return newEndpoint;
	}

	/**
	 * TODO: Verify that http://tools.ietf.org/html/rfc7232#section-2.2
	 * holds: if the clock in the request is ahead of the clock of the origin
	 * server (e.g., I request from Spain the update of a resource held in USA)
	 * the last-modified data should be changed to that of the request and not
	 * a generated date from the origin server
	 */
	public Endpoint modifyResourceEndpoint(Resource resource, EntityTag entityTag, Date lastModified) throws EndpointNotFoundException {
		checkNotNull(resource,"Resource cannot be null");
		checkNotNull(entityTag,"Entity tag cannot be null");
		checkNotNull(lastModified,"Last modified cannot be null");
		Endpoint endpoint = this.persistencyManager.endpointOfResource(resource.id());
		if(endpoint==null) {
			throw new EndpointNotFoundException(resource.id());
		}
		endpoint.modify(entityTag, lastModified);
		return endpoint;
	}

	public Endpoint deleteResourceEndpoint(Resource resource, Date deletionDate) throws EndpointNotFoundException {
		checkNotNull(resource,"Resource cannot be null");
		Endpoint endpoint = this.persistencyManager.endpointOfResource(resource.id());
		if(endpoint==null) {
			throw new EndpointNotFoundException(resource.id());
		}
		this.persistencyManager.remove(endpoint,deletionDate);
		this.listenerManager.notify(new EndpointDeletionNotification(endpoint));
		return endpoint;
	}

	public static ServiceBuilder<EndpointManagementService> serviceBuilder() {
		return new EndpointManagementServiceBuilder();
	}

	public static EndpointManagementService defaultService() {
		return serviceBuilder().build();
	}

}