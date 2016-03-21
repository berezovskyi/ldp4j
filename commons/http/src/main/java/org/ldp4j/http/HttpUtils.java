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
 *   Artifact    : org.ldp4j.commons:ldp4j-commons-http:0.3.0-SNAPSHOT
 *   Bundle      : ldp4j-commons-http-0.3.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.http;

import static com.google.common.base.Preconditions.*;

final class HttpUtils {

	private static final char   PARAM_DELIMITER = ';';
	private static final char   DQUOTE          = '\"';

	private HttpUtils() {
	}

	static String trimWhitespace(final String token) {
		checkNotNull(token,"Token cannot be null");
		int startOffset=0;
		int endOffset=token.length();
		while(startOffset<endOffset) {
			final char lastChar=token.charAt(startOffset);
			if(!HttpUtils.isWhitespace(lastChar)) {
				break;
			}
			startOffset++;
		}
		while(endOffset>startOffset) {
			final char lastChar=token.charAt(endOffset-1);
			if(!HttpUtils.isWhitespace(lastChar)) {
				break;
			}
			endOffset--;
		}
		return token.substring(startOffset,endOffset);
	}

	static boolean isQuotedString(final String s) {
		boolean result=false;
		final int length = s.length();
		if(length>1) {
			result=
				s.charAt(0)       ==DQUOTE &&
				s.charAt(length-1)==DQUOTE;
		}
		return result;
	}

	static String unquote(final String s) {
		return
			isQuotedString(s) ?
				s.substring(1, s.length() - 1) :
				s;
	}

	static boolean isWhitespace(final char ch) {
		return ch==' ' || ch=='\t';
	}

	static boolean isParameterDelimiter(final char ch) {
		return ch==PARAM_DELIMITER;
	}

}
