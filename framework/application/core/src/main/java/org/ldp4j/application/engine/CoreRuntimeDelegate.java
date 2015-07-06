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
package org.ldp4j.application.engine;

import org.ldp4j.application.ApplicationContextException;
import org.ldp4j.application.engine.session.WriteSessionConfiguration;
import org.ldp4j.application.engine.session.WriteSessionService;
import org.ldp4j.application.engine.spi.PersistencyManager;
import org.ldp4j.application.engine.spi.Transaction;
import org.ldp4j.application.session.WriteSession;
import org.ldp4j.application.spi.RuntimeDelegate;

public final class CoreRuntimeDelegate extends RuntimeDelegate {

	private DefaultApplicationEngine applicationEngine() throws ApplicationEngineException {
		return
			ApplicationEngine.
				engine().
					unwrap(DefaultApplicationEngine.class);
	}

	private WriteSessionService sessionService() throws ApplicationEngineException {
		return applicationEngine().writeSessionService();
	}

	private PersistencyManager persistencyManager() throws ApplicationEngineException {
		return applicationEngine().persistencyManager();
	}

	@Override
	public boolean isOffline() {
		boolean result=false;
		try {
			result=!applicationEngine().state().isStarted();
		} catch (ApplicationEngineException e) {
			// NOTHING TO DO
		}
		return result;
	}

	@Override
	public WriteSession createSession() throws ApplicationContextException {
		try {
			WriteSession delegate =
				sessionService().
					createSession(
						WriteSessionConfiguration.
							builder().
								build());
			Transaction transaction=persistencyManager().currentTransaction();
			if(!transaction.isStarted()) {
				transaction.begin();
			}
			return new TransactionalWriteSession(transaction, delegate);
		} catch (ApplicationEngineException e) {
			throw new ApplicationContextException("Unsupported application engine implementation",e);
		}
	}

	@Override
	public void terminateSession(WriteSession session) throws ApplicationContextException {
		try {
			sessionService().terminateSession(session);
		} catch (ApplicationEngineException e) {
			throw new ApplicationContextException("Unsupported application engine implementation",e);
		}
	}

}