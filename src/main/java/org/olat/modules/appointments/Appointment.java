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
package org.olat.modules.appointments;

import java.util.Date;

import org.olat.core.id.CreateInfo;
import org.olat.core.id.ModifiedInfo;
import org.olat.modules.bigbluebutton.BigBlueButtonMeeting;

/**
 * 
 * Initial date: 11 Apr 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public interface Appointment extends AppointmentRef, ModifiedInfo, CreateInfo {
	
	public enum Status {
		planned,
		confirmed
	}
	
	public Status getStatus();
	
	public Date getStatusModified();
	
	public Date getStart();
	
	public void setStart(Date start);
	
	public Date getEnd();
	
	public void setEnd(Date end);
	
	public String getLocation();
	
	public void setLocation(String location);
	
	public String getDetails();
	
	public void setDetails(String details);
	
	public Integer getMaxParticipations();
	
	public void setMaxParticipations(Integer maxParticipations);
	
	public Topic getTopic();
	
	public BigBlueButtonMeeting getMeeting();
	
}
