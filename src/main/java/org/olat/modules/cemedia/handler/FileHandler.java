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
package org.olat.modules.cemedia.handler;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.olat.core.commons.services.image.Size;
import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.commons.services.vfs.VFSRepositoryService;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.util.CSSHelper;
import org.olat.core.id.Identity;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.util.FileUtils;
import org.olat.core.util.StringHelper;
import org.olat.core.util.vfs.VFSConstants;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.modules.ceditor.InteractiveAddPageElementHandler;
import org.olat.modules.ceditor.PageElementAddController;
import org.olat.modules.ceditor.PageElementCategory;
import org.olat.modules.ceditor.manager.ContentEditorFileStorage;
import org.olat.modules.cemedia.MediaLoggingAction;
import org.olat.modules.cemedia.Media;
import org.olat.modules.cemedia.MediaInformations;
import org.olat.modules.cemedia.MediaLight;
import org.olat.modules.cemedia.MediaRenderingHints;
import org.olat.modules.cemedia.manager.MediaDAO;
import org.olat.modules.cemedia.ui.medias.CollectFileMediaController;
import org.olat.modules.cemedia.ui.medias.FileMediaController;
import org.olat.modules.cemedia.ui.medias.UploadMedia;
import org.olat.user.manager.ManifestBuilder;
import org.olat.util.logging.activity.LoggingResourceable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 20.06.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class FileHandler extends AbstractMediaHandler implements InteractiveAddPageElementHandler {
	
	public static final String FILE_TYPE = "bc";
	
	private final boolean create;
	
	@Autowired
	private MediaDAO mediaDao;
	@Autowired
	private ContentEditorFileStorage fileStorage;
	@Autowired
	private VFSRepositoryService vfsRepositoryService;
	
	public FileHandler() {
		super(FILE_TYPE);
		this.create = false;
	}
	
	FileHandler(boolean create) {
		super(FILE_TYPE);
		this.create = create;
	}
	
	@Override
	public String getIconCssClass() {
		return "o_filetype_file";
	}
	
	@Override
	public PageElementCategory getCategory() {
		return PageElementCategory.embed;
	}

	@Override
	public boolean acceptMimeType(String mimeType) {
		return StringHelper.containsNonWhitespace(mimeType)
				&& !ImageHandler.mimeTypes.contains(mimeType)
				&& !VideoHandler.mimeTypes.contains(mimeType);
	}

	@Override
	public String getIconCssClass(MediaLight media) {
		String filename = media.getRootFilename();
		if (filename != null){
			return CSSHelper.createFiletypeIconCssClassFor(filename);
		}
		return "o_filetype_file";
	}

	@Override
	public VFSLeaf getThumbnail(MediaLight media, Size size) {
		String storagePath = media.getStoragePath();

		VFSLeaf thumbnail = null;
		if(StringHelper.containsNonWhitespace(storagePath)) {
			VFSContainer storageContainer = fileStorage.getMediaContainer(media);
			VFSItem item = storageContainer.resolve(media.getRootFilename());
			if(item instanceof VFSLeaf leaf && leaf.canMeta() == VFSConstants.YES
					&& vfsRepositoryService.isThumbnailAvailable(leaf)) {
				thumbnail = vfsRepositoryService.getThumbnail(leaf, size.getHeight(), size.getWidth(), true);
			}
		}
		
		return thumbnail;
	}
	
	public VFSItem getItem(Media media) {
		VFSContainer storageContainer = fileStorage.getMediaContainer(media);
		return storageContainer.resolve(media.getRootFilename());
	}
	
	@Override
	public MediaInformations getInformations(Object mediaObject) {
		String title = null;
		String description = null;
		if (mediaObject instanceof VFSItem item && item.canMeta() == VFSConstants.YES) {
			VFSMetadata meta = item.getMetaInfo();
			title = meta.getTitle();
			description = meta.getComment();
		}
		return new Informations(title, description);
	}

	@Override
	public Media createMedia(String title, String description, Object mediaObject, String businessPath, Identity author) {
		UploadMedia mObject = (UploadMedia)mediaObject;
		return createMedia(title, description, mObject.getFile(), mObject.getFilename(), businessPath, author);
	}

	public Media createMedia(String title, String description, File file, String filename, String businessPath, Identity author) {
		Media media = mediaDao.createMedia(title, description, filename, FILE_TYPE, businessPath, null, 60, author);
		ThreadLocalUserActivityLogger.log(MediaLoggingAction.CE_MEDIA_ADDED, getClass(),
				LoggingResourceable.wrap(media));
		File mediaDir = fileStorage.generateMediaSubDirectory(media);
		File mediaFile = new File(mediaDir, filename);
		FileUtils.copyFileToFile(file, mediaFile, false);
		String storagePath = fileStorage.getRelativePath(mediaDir);
		mediaDao.updateStoragePath(media, storagePath, filename);
		return media;
	}

	@Override
	public Controller getMediaController(UserRequest ureq, WindowControl wControl, Media media, MediaRenderingHints hints) {
		FileMediaController mediaCtrl = new FileMediaController(ureq, wControl, media, hints);
		if(create) {
			mediaCtrl.setEditable(true);
		}
		return mediaCtrl;
	}

	@Override
	public Controller getEditMediaController(UserRequest ureq, WindowControl wControl, Media media) {
		return new CollectFileMediaController(ureq, wControl, media);
	}

	@Override
	public PageElementAddController getAddPageElementController(UserRequest ureq, WindowControl wControl) {
		return new CollectFileMediaController(ureq, wControl);
	}
	
	@Override
	public void export(Media media, ManifestBuilder manifest, File mediaArchiveDirectory, Locale locale) {
		List<File> files = new ArrayList<>();
		if(StringHelper.containsNonWhitespace(media.getStoragePath()) && StringHelper.containsNonWhitespace(media.getRootFilename())) {
			File mediaDir = fileStorage.getMediaDirectory(media);
			files.add(new File(mediaDir, media.getRootFilename()));
		}
		super.exportContent(media, null, files, mediaArchiveDirectory, locale);
	}
}
