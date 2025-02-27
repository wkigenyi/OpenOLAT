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
package org.olat.modules.todo.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.modules.todo.ToDoTask;

/**
 * 
 * Initial date: 21 Apr 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.comm
 *
 */
public class ToDoDeleteConfirmationController extends FormBasicController {
	
	private FormLink confirmLink;
	private MultipleSelectionElement confirmationEl;

	private final ToDoTask toDoTask;

	public ToDoDeleteConfirmationController(UserRequest ureq, WindowControl wControl, ToDoTask toDoTask) {
		super(ureq, wControl, "confirmation");
		this.toDoTask = toDoTask;
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(formLayout instanceof FormLayoutContainer) {
			FormLayoutContainer layout = (FormLayoutContainer)formLayout;
			layout.contextPut("message", translate("task.delete.conformation.message", toDoTask.getTitle()));
			
			FormLayoutContainer confirmCont = FormLayoutContainer.createDefaultFormLayout("confirm", getTranslator());
			formLayout.add("confirm", confirmCont);
			confirmCont.setRootForm(mainForm);
			
			String[] acknowledge = new String[] { translate("task.delete.confirmation.confirm") };
			confirmationEl = uifactory.addCheckboxesHorizontal("confirmation", "confirmation", confirmCont, new String[]{ "" }, acknowledge);
			
			FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
			confirmCont.add(buttonsCont);
			confirmLink = uifactory.addFormLink("delete", buttonsCont, Link.BUTTON);
			uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
		}
	}
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		confirmationEl.clearError();
		if (!confirmationEl.isAtLeastSelected(1)) {
			confirmationEl.setErrorKey("error.confirmation.mandatory");
			allOk &= false;
		}
		
		return allOk;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (confirmLink == source) {
			if (validateFormLogic(ureq)) {
				fireEvent(ureq, Event.DONE_EVENT);
			}
		}
	}
	
	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

}
