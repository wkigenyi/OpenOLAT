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
package org.olat.user.ui.identity;

import java.util.Map;

import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityModule;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.id.Preferences;
import org.olat.core.util.ArrayHelper;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.i18n.I18nManager;
import org.olat.core.util.i18n.I18nModule;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;


/**
 * Simplified preferences panel for invitee with only the language.
 * 
 * Initial date: 13 juil. 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ChangeInviteePrefsController extends FormBasicController {
	private Identity tobeChangedIdentity;
	
	private SingleSelection language;

	@Autowired
	private I18nModule i18nModule;
	@Autowired
	private I18nManager i18nManager;
	@Autowired
	private UserManager userManager;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private BaseSecurityModule securityModule;
	
	public ChangeInviteePrefsController(UserRequest ureq, WindowControl wControl, Identity changeableIdentity) {
		super(ureq, wControl, Util.createPackageTranslator(UserManager.class, ureq.getLocale()));
		this.tobeChangedIdentity = changeableIdentity;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("title.prefs");
		setFormContextHelp("manual_user/personal/Configuration/#settings");
		
		Preferences prefs = tobeChangedIdentity.getUser().getPreferences();
		
		// Username
		String name = securityManager.findAuthenticationName(tobeChangedIdentity);
		if(!StringHelper.containsNonWhitespace(name) && !securityModule.isIdentityNameAutoGenerated()) {
			name = tobeChangedIdentity.getName();
		}
		StaticTextElement username = uifactory.addStaticTextElement("form.username", name, formLayout);
		username.setElementCssClass("o_sel_home_settings_username");
		username.setEnabled(false);
		
		// Language
		Map<String, String> languages = i18nManager.getEnabledLanguagesTranslated();
		String[] langKeys = StringHelper.getMapKeysAsStringArray(languages);
		String[] langValues = StringHelper.getMapValuesAsStringArray(languages);
		ArrayHelper.sort(langKeys, langValues, false, true, false);
		language = uifactory.addDropdownSingleselect("form.language", formLayout, langKeys, langValues, null);
		language.setElementCssClass("o_sel_home_settings_language");
		String langKey = prefs.getLanguage();
		// Preselect the users language if available. Maye not anymore enabled on
		// this server
		if (prefs.getLanguage() != null && i18nModule.getEnabledLanguageKeys().contains(langKey)) {
			language.select(prefs.getLanguage(), true);
		} else {
			language.select(I18nModule.getDefaultLocale().toString(), true);
		}
		
		// Submit and cancel buttons
		final FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("button_layout", getTranslator());
		formLayout.add(buttonLayout);
		buttonLayout.setElementCssClass("o_sel_home_settings_prefs_buttons");
		uifactory.addFormSubmitButton("submit", buttonLayout);
		uifactory.addFormCancelButton("cancel", buttonLayout, ureq, getWindowControl());
	}

	@Override
	protected void formOK(UserRequest ureq) {
		tobeChangedIdentity = securityManager.loadIdentityByKey(tobeChangedIdentity.getKey());
		Preferences prefs = tobeChangedIdentity.getUser().getPreferences();
		prefs.setLanguage(language.getSelectedKey());
		
		if (userManager.updateUserFromIdentity(tobeChangedIdentity)) {
			// Language change needs logout / login
			showInfo("preferences.successful");
		} else {
			showInfo("preferences.unsuccessful");
		}
		
		fireEvent(ureq, Event.DONE_EVENT);
	}
}
