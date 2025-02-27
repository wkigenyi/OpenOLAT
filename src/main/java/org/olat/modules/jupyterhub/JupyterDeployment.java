/**
 * <a href="http://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.modules.jupyterhub;

import org.olat.ims.lti13.LTI13ToolDeployment;

/**
 * Initial date: 2023-04-14<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public interface JupyterDeployment {

	Long getKey();

	String getDescription();

	void setDescription(String description);

	String getImage();

	void setImage(String image);

	Boolean getSuppressDataTransmissionAgreement();

	void setSuppressDataTransmissionAgreement(Boolean suppressDataTransmissionAgreement);

	JupyterHub getJupyterHub();

	void setJupyterHub(JupyterHub jupyterHub);

	LTI13ToolDeployment getLtiToolDeployment();

	void setLtiToolDeployment(LTI13ToolDeployment ltiToolDeployment);
}
