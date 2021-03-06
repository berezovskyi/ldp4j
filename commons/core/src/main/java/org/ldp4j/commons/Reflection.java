/**
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 *   This file is part of the LDP4j Project:
 *     http://www.ldp4j.org/
 *
 *   Center for Open Middleware
 *     http://www.centeropenmiddleware.com/
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 *   Copyright (C) 2014-2016 Center for Open Middleware.
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
 *   Artifact    : org.ldp4j.commons:ldp4j-commons-core:0.2.1
 *   Bundle      : ldp4j-commons-core-0.2.1.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.commons;


public final class Reflection {

	private Reflection() {
	}

	/**
	 * Ensures that the given classes are initialized, as described in <a href=
	 * "http://java.sun.com/docs/books/jls/third_edition/html/execution.html#12.4.2"
	 * > JLS Section 12.4.2</a>.
	 * 
	 * <p>
	 * WARNING: Normally it's a smell if a class needs to be explicitly
	 * initialized, because static state hurts system maintainability and
	 * testability. In cases when you have no choice while inter-operating with
	 * a legacy framework, this method helps to keep the code less ugly.
	 * 
	 * @throws ExceptionInInitializerError
	 *             if an exception is thrown during initialization of a class
	 * @throws AssertionError
	 *             if the classloader fails to find the already loaded class
	 */
	public static void initialize(Class<?>... classes) {
		for(Class<?> clazz:classes) {
			try {
				Class.forName(clazz.getName(),true,clazz.getClassLoader());
			} catch (ClassNotFoundException e) {
				throw new IllegalStateException("The class is already loaded, therefore it should be found",e);
			}
		}
	}

	/**
	 * TODO: Relax this constraint, it should be possible to do it from
	 * named-inner classes.
	 */
	public static Class<?> getSubclassInScope(Class<?> scopeClass) {
		String callerClass = Thread.currentThread().getStackTrace()[3].getClassName();
		try {
			Class<?> candidateClass = Class.forName(callerClass, false, Thread.currentThread().getContextClassLoader());
			if(!scopeClass.isAssignableFrom(candidateClass)) {
				return null;
			}
			return candidateClass;
		} catch (ClassNotFoundException e) {
			throw new IllegalStateException("The class should always exist",e);
		}
	}

	public static String getCallerClassName() {
		return Thread.currentThread().getStackTrace()[3].getClassName();
	}

}
