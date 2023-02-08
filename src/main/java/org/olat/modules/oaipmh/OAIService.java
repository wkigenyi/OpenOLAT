/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.oaipmh;

import java.util.List;
import java.util.Map;

import org.olat.core.gui.media.MediaResource;

/**
 * @author Sumit Kapoor, sumit.kapoor@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public interface OAIService {

	/**
	 * handle incoming OAI-PMH Request and hand it over to the dataProvider
	 *
	 * @param requestParameter
	 * @return MediaResource of type XML in Dublin Core-Standard with OAI
	 */
	MediaResource handleOAIRequest(String requestParameter, String requestIdentifierParameter,
								   String requestMetadataPrefixParameter, String requestResumptionTokenParameter,
								   String requestFromParameter, String requestUntilParameter,
								   String requestSetParameter);

	/**
	 * Propagate resourceInfo pages from OO to selected searchEngines
	 *
	 * @param searchEngineUrls
	 * @return Map for searchEngine and the respective HTTP response code
	 */
	Map<String, Integer> propagateSearchEngines(List<String> searchEngineUrls);

}
