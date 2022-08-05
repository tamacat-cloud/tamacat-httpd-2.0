/*
 * Copyright 2010 tamacat.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cloud.tamacat.httpd.core;

/**
 * <p>Constants of HTTP Status Code and Reason Phrase.<br>
 * RFC 2616 Hypertext Transfer Protocol -- HTTP/1.1 (June 1999)
 * @see {@link http://www.ietf.org/rfc/rfc2616.txt}
 */
public interface HttpStatus {
	
	int getStatusCode();
	
	String getReasonPhrase();
	
	boolean isInformational();
	
	boolean isSuccess();
	
	boolean isRedirection();
	
	boolean isClientError();
	
	boolean isServerError();
}
