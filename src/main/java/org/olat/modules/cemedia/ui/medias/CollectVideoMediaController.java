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
package org.olat.modules.cemedia.ui.medias;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FileElement;
import org.olat.core.gui.components.form.flexible.elements.RichTextElement;
import org.olat.core.gui.components.form.flexible.elements.TextBoxListElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.textboxlist.TextBoxItem;
import org.olat.core.gui.components.textboxlist.TextBoxItemImpl;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.vfs.JavaIOItem;
import org.olat.core.util.vfs.VFSItem;
import org.olat.modules.ceditor.Category;
import org.olat.modules.ceditor.PageElement;
import org.olat.modules.ceditor.PageElementAddController;
import org.olat.modules.ceditor.model.jpa.MediaPart;
import org.olat.modules.ceditor.ui.AddElementInfos;
import org.olat.modules.cemedia.Media;
import org.olat.modules.cemedia.MediaService;
import org.olat.modules.cemedia.handler.VideoHandler;
import org.olat.modules.cemedia.ui.MediaCenterController;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 11.07.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CollectVideoMediaController extends FormBasicController implements PageElementAddController {

	private static final Set<String> videoMimeTypes = Set.of("video/quicktime", "video/mp4");
	
	private FileElement fileEl;
	private TextElement titleEl;
	private RichTextElement descriptionEl;
	private TextBoxListElement categoriesEl;

	private Media mediaReference;
	private List<TextBoxItem> categories = new ArrayList<>();
	
	private final String businessPath;
	private AddElementInfos userObject;
	
	@Autowired
	private VideoHandler fileHandler;
	@Autowired
	private MediaService mediaService;

	public CollectVideoMediaController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(MediaCenterController.class, getLocale(), getTranslator()));
		businessPath = "[HomeSite:" + getIdentity().getKey() + "][PortfolioV2:0][MediaCenter:0]";
		initForm(ureq);
	}
	
	public CollectVideoMediaController(UserRequest ureq, WindowControl wControl, Media media) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(MediaCenterController.class, getLocale(), getTranslator()));
		businessPath = media.getBusinessPath();
		mediaReference = media;
		
		List<Category> categoryList = mediaService.getCategories(media);
		for(Category category:categoryList) {
			categories.add(new TextBoxItemImpl(category.getName(), category.getName()));
		}
		
		initForm(ureq);
	}
	
	public Media getMediaReference() {
		return mediaReference;
	}

	@Override
	public AddElementInfos getUserObject() {
		return userObject;
	}

	@Override
	public void setUserObject(AddElementInfos userObject) {
		this.userObject = userObject;
	}

	@Override
	public PageElement getPageElement() {
		MediaPart part = new MediaPart();
		part.setMedia(mediaReference);
		return part;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		String title = mediaReference == null ? null : mediaReference.getTitle();
		titleEl = uifactory.addTextElement("artefact.title", "artefact.title", 255, title, formLayout);
		titleEl.setMandatory(true);
		
		String desc = mediaReference == null ? null : mediaReference.getDescription();
		descriptionEl = uifactory.addRichTextElementForStringDataMinimalistic("artefact.descr", "artefact.descr", desc, 8, 60, formLayout, getWindowControl());
		descriptionEl.getEditorConfiguration().setPathInStatusBar(false);
		
		fileEl = uifactory.addFileElement(getWindowControl(), getIdentity(), "artefact.file", "artefact.file", formLayout);
		fileEl.limitToMimeType(videoMimeTypes, "error.video.mimetype", null);
		fileEl.addActionListener(FormEvent.ONCHANGE);
		fileEl.setMaxUploadSizeKB(250000, null, null);
		fileEl.setPreview(ureq.getUserSession(), true);
		fileEl.setDeleteEnabled(true);
		if(mediaReference != null) {
			fileEl.setEnabled(false);
			
			VFSItem item = fileHandler.getVideoItem(mediaReference);
			if(item instanceof JavaIOItem) {
				fileEl.setInitialFile(((JavaIOItem)item).getBasefile());
			}
		}
		
		categoriesEl = uifactory.addTextBoxListElement("categories", "categories", "categories.hint", categories, formLayout, getTranslator());
		categoriesEl.setHelpText(translate("categories.hint"));
		categoriesEl.setElementCssClass("o_sel_ep_tagsinput");
		categoriesEl.setAllowDuplicates(false);
		
		Date collectDate = mediaReference == null ? new Date() : mediaReference.getCollectionDate();
		String date = Formatter.getInstance(getLocale()).formatDate(collectDate);
		uifactory.addStaticTextElement("artefact.collect.date", "artefact.collect.date", date, formLayout);

		if(StringHelper.containsNonWhitespace(businessPath)) {
			String link = BusinessControlFactory.getInstance().getURLFromBusinessPathString(businessPath);
			uifactory.addStaticTextElement("artefact.collect.link", "artefact.collect.link", link, formLayout);
		}
		
		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add(buttonsCont);
		uifactory.addFormSubmitButton("save", "save", buttonsCont);
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
	}
	
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		fileEl.clearError();
		if(fileEl.getInitialFile() == null && (fileEl.getUploadFile() == null || fileEl.getUploadSize() < 1)) {
			fileEl.setErrorKey("form.legende.mandatory");
			allOk &= false;
		} else {
			allOk &= fileEl.validate();
		}
		
		titleEl.clearError();
		if (titleEl.isEmpty()) {
			titleEl.setErrorKey("form.legende.mandatory");
			allOk &= false;
		}

		return allOk;
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(fileEl == source) {
			if (titleEl.isEmpty()) {
				titleEl.setValue(fileEl.getUploadFileName());
				titleEl.getComponent().setDirty(true);
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		if(mediaReference == null) {
			String title = titleEl.getValue();
			String description = descriptionEl.getValue();
			File uploadedFile = fileEl.getUploadFile();
			String uploadedFilename = fileEl.getUploadFileName();
			mediaReference = fileHandler.createMedia(title, description, uploadedFile, uploadedFilename, businessPath, getIdentity());
		} else {
			mediaReference.setTitle(titleEl.getValue());
			mediaReference.setDescription(descriptionEl.getValue());
			mediaReference = mediaService.updateMedia(mediaReference);
		}

		List<String> updatedCategories = categoriesEl.getValueList();
		mediaService.updateCategories(mediaReference, updatedCategories);
		
		fireEvent(ureq, Event.DONE_EVENT);
	}
	
	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
}
