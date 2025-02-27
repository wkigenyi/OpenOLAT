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
import java.util.Set;

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
import org.olat.modules.cemedia.ui.medias.CollectVideoMediaController;
import org.olat.modules.cemedia.ui.medias.UploadMedia;
import org.olat.modules.cemedia.ui.medias.VideoMediaController;
import org.olat.user.manager.ManifestBuilder;
import org.olat.util.logging.activity.LoggingResourceable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 11.07.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class VideoHandler extends AbstractMediaHandler implements InteractiveAddPageElementHandler {
	
	public static final String VIDEO_TYPE = "video";
	public static final Set<String> mimeTypes = Set.of("video/mp4");

	@Autowired
	private MediaDAO mediaDao;
	@Autowired
	private ContentEditorFileStorage fileStorage;
	@Autowired
	private VFSRepositoryService vfsRepositoryService;
	
	public VideoHandler() {
		super(VIDEO_TYPE);
	}
	
	@Override
	public String getIconCssClass() {
		return "o_icon_video";
	}
	
	@Override
	public PageElementCategory getCategory() {
		return PageElementCategory.embed;
	}

	@Override
	public boolean acceptMimeType(String mimeType) {
		return mimeTypes.contains(mimeType);
	}

	@Override
	public String getIconCssClass(MediaLight media) {
		String filename = media.getRootFilename();
		if (filename != null){
			return CSSHelper.createFiletypeIconCssClassFor(filename);
		}
		return "o_icon_video";
	}

	@Override
	public VFSLeaf getThumbnail(MediaLight media, Size size) {
		String storagePath = media.getStoragePath();

		VFSLeaf thumbnail = null;
		if(StringHelper.containsNonWhitespace(storagePath)) {
			VFSContainer storageContainer = fileStorage.getMediaContainer(media);
			VFSItem item = storageContainer.resolve(media.getRootFilename());
			if(item instanceof VFSLeaf leaf && leaf.canMeta() == VFSConstants.YES) {
				thumbnail = vfsRepositoryService.getThumbnail(leaf, size.getHeight(), size.getWidth(), true);
			}
		}
		
		return thumbnail;
	}
	
	public VFSItem getVideoItem(Media media) {
		VFSContainer storageContainer = fileStorage.getMediaContainer(media);
		return storageContainer.resolve(media.getRootFilename());
	}

	@Override
	public MediaInformations getInformations(Object mediaObject) {
		String title = null;
		String description = null;
		if (mediaObject instanceof VFSLeaf leaf && leaf.canMeta() == VFSConstants.YES) {
			VFSMetadata meta = leaf.getMetaInfo();
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
		Media media = mediaDao.createMedia(title, description, filename, VIDEO_TYPE, businessPath, null, 60, author);
		
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
		return new VideoMediaController(ureq, wControl, media, hints);
	}
	
	@Override
	public Controller getEditMediaController(UserRequest ureq, WindowControl wControl, Media media) {
		return new CollectVideoMediaController(ureq, wControl, media);
	}

	@Override
	public PageElementAddController getAddPageElementController(UserRequest ureq, WindowControl wControl) {
		return new CollectVideoMediaController(ureq, wControl);
	}
	
	@Override
	public void export(Media media, ManifestBuilder manifest, File mediaArchiveDirectory, Locale locale) {
		List<File> videos = new ArrayList<>();
		File mediaDir = fileStorage.getMediaDirectory(media);
		videos.add(new File(mediaDir, media.getRootFilename()));
		super.exportContent(media, null, videos, mediaArchiveDirectory, locale);
	}
}
