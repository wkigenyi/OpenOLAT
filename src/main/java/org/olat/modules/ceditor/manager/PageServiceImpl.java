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
package org.olat.modules.ceditor.manager;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.logging.log4j.Logger;
import org.olat.basesecurity.manager.GroupDAO;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.core.util.FileUtils;
import org.olat.core.util.StringHelper;
import org.olat.modules.ceditor.Assignment;
import org.olat.modules.ceditor.Category;
import org.olat.modules.ceditor.ContentAuditLog;
import org.olat.modules.ceditor.ContentAuditLog.Action;
import org.olat.modules.ceditor.ContentEditorXStream;
import org.olat.modules.ceditor.Page;
import org.olat.modules.ceditor.PageBody;
import org.olat.modules.ceditor.PagePart;
import org.olat.modules.ceditor.PageService;
import org.olat.modules.ceditor.ContentRoles;
import org.olat.modules.ceditor.model.ContainerSettings;
import org.olat.modules.ceditor.model.jpa.ContainerPart;
import org.olat.modules.ceditor.model.jpa.MediaPart;
import org.olat.modules.cemedia.Media;
import org.olat.modules.cemedia.manager.MediaDAO;
import org.olat.modules.portfolio.manager.PageUserInfosDAO;
import org.olat.modules.taxonomy.TaxonomyCompetence;
import org.olat.modules.taxonomy.TaxonomyCompetenceLinkLocations;
import org.olat.modules.taxonomy.TaxonomyCompetenceTypes;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.modules.taxonomy.TaxonomyLevelRef;
import org.olat.modules.taxonomy.manager.TaxonomyCompetenceDAO;
import org.olat.modules.taxonomy.manager.TaxonomyLevelDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 23 mai 2023<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
@Service
public class PageServiceImpl implements PageService {

	private static final Logger log = Tracing.createLoggerFor(PageServiceImpl.class);
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private PageDAO pageDao;
	@Autowired
	private MediaDAO mediaDao;
	@Autowired
	private GroupDAO groupDao;
	@Autowired
	private CategoryDAO categoryDao;
	@Autowired
	private AssignmentDAO assignmentDao;
	@Autowired
	private PageUserInfosDAO pageUserInfosDao;
	@Autowired
	private ContentEditorFileStorage fileStorage;
	@Autowired
	private TaxonomyLevelDAO taxonomyLevelDAO;
	@Autowired
	private ContentAuditLogDAO contentAuditLogDao;
	@Autowired
	private TaxonomyCompetenceDAO taxonomyCompetenceDAO;
	@Autowired
	private PageToTaxonomyCompetenceDAO pageToTaxonomyCompetenceDao;
	
	@Override
	public Page getPageByKey(Long key) {
		return pageDao.loadByKey(key);
	}
	
	@Override
	public Page getFullPageByKey(Long key) {
		return pageDao.loadPageByKey(key);
	}

	@Override
	public Page updatePage(Page page) {
		return pageDao.updatePage(page);
	}

	@Override
	public Page copyPage(Identity owner, Page page) {
		String imagePath = page.getImagePath();
		Page copy = pageDao.createAndPersist(page.getTitle(), page.getSummary(), imagePath, page.getImageAlignment(), page.isEditable(), null, null);
		if(owner != null) {
			groupDao.addMembershipTwoWay(copy.getBaseGroup(), owner, ContentRoles.owner.name());
		}
		
		// Copy the parts but let the media untouched
		PageBody copyBody = copy.getBody();
		List<PagePart> parts = page.getBody().getParts();
		Map<String,String> mapKeys = new HashMap<>();
		for(PagePart part:parts) {
			PagePart newPart = part.copy();
			copyBody = pageDao.persistPart(copyBody, newPart);
			mapKeys.put(part.getKey().toString(), newPart.getKey().toString());
		}

		remapContainers(copyBody, mapKeys);
		dbInstance.commit();
		return copy;
	}

	@Override
	public Page importPage(Identity owner, Page page, ZipFile storage) {
		String imagePath = page.getImagePath();
		Page copy = pageDao.createAndPersist(page.getTitle(), page.getSummary(), imagePath, page.getImageAlignment(), page.isEditable(), null, null);
		if(owner != null) {
			groupDao.addMembershipTwoWay(copy.getBaseGroup(), owner, ContentRoles.owner.name());
		}
		
		// Copy the parts but let the media untouched
		PageBody copyBody = copy.getBody();
		List<PagePart> parts = page.getBody().getParts();
		Map<String,String> mapKeys = new HashMap<>();
		for(PagePart part:parts) {
			PagePart newPart = part.copy();
			if(newPart instanceof MediaPart mediaPart && mediaPart.getMedia() != null) {
				Media importedMedia = importMedia(mediaPart.getMedia(), owner, storage);
				mediaPart.setMedia(importedMedia);
			}
			copyBody = pageDao.persistPart(copyBody, newPart);
			mapKeys.put(part.getKey().toString(), newPart.getKey().toString());
		}
		
		remapContainers(copyBody, mapKeys);
		return copy;
	}
	
	private Media importMedia(Media media, Identity owner, ZipFile storage) {
		Media importedMedia = mediaDao.createMedia(media.getTitle(), media.getDescription(), media.getContent(), media.getType(), media.getBusinessPath(), null, 0, owner);
		String mediaZipPath = media.getStoragePath();
		if(StringHelper.containsNonWhitespace(mediaZipPath)) {
			File mediaDir = fileStorage.generateMediaSubDirectory(importedMedia);
			for(Enumeration<? extends ZipEntry> entries=storage.entries(); entries.hasMoreElements(); ) {
				ZipEntry entry=entries.nextElement();
				String entryPath = entry.getName();
				if(entryPath.startsWith(mediaZipPath)) {
					unzip(mediaZipPath, entry, storage, mediaDir);
				}
			}	
		}
		return importedMedia;
	}
	
	private void unzip(String mediaZipPath, ZipEntry entry, ZipFile storage, File mediaDir) {
		try(InputStream in=storage.getInputStream(entry)) {
			String entryPath = entry.getName();
			String fileName = entryPath.replace(mediaZipPath, "");
			File mediaFile = new File(mediaDir, fileName);
			FileUtils.copyToFile(in, mediaFile, "");
		} catch(IOException e) {
			log.error("", e);
		}
	}
	
	private void remapContainers(PageBody body, Map<String,String> mapKeys) {
		List<PagePart> newParts = body.getParts();
		for(PagePart newPart:newParts) {
			if(newPart instanceof ContainerPart container) {
				ContainerSettings settings = container.getContainerSettings();
				settings.remapElementIds(mapKeys);
				container.setLayoutOptions(ContentEditorXStream.toXml(settings));
			}
		}
	}

	@Override
	public void deletePage(Page page) {
		Page reloadedPage = pageDao.loadByKey(page.getKey());
		pageDao.deletePage(reloadedPage);
		pageUserInfosDao.delete(page);
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public <U extends PagePart> U updatePart(U part) {
		U mergedPart = (U)pageDao.merge(part);
		if(mergedPart instanceof MediaPart mediaPart) {
			// Prevent lazy loading issues
			Media media = mediaPart.getMedia();
			if(media != null) {
				media.getMetadataXml();
			}
		}
		return mergedPart;
	}
	
	@Override
	public File getPosterImage(Page page) {
		String imagePath = page.getImagePath();
		if(StringHelper.containsNonWhitespace(imagePath)) {
			File bcroot = fileStorage.getRootDirectory();
			return new File(bcroot, imagePath);
		}
		return null;
	}

	@Override
	public String addPosterImageForPage(File file, String filename) {
		File dir = fileStorage.generatePageSubDirectory();
		File destinationFile = new File(dir, filename);
		String renamedFile = FileUtils.rename(destinationFile);
		if(renamedFile != null) {
			destinationFile = new File(dir, renamedFile);
		}
		FileUtils.copyFileToFile(file, destinationFile, false);
		return fileStorage.getRelativePath(destinationFile);
	}

	@Override
	public void removePosterImage(Page page) {
		String imagePath = page.getImagePath();
		if(StringHelper.containsNonWhitespace(imagePath)) {
			File bcroot = fileStorage.getRootDirectory();
			File file = new File(bcroot, imagePath);
			FileUtils.deleteFile(file);
		}
	}

	@Override
	public <U extends PagePart> U appendNewPagePart(Page page, U part) {
		PageBody body = pageDao.loadPageBodyByKey(page.getBody().getKey());
		pageDao.persistPart(body, part);
		return part;
	}

	@Override
	public <U extends PagePart> U appendNewPagePartAt(Page page, U part, int index) {
		PageBody body = pageDao.loadPageBodyByKey(page.getBody().getKey());
		pageDao.persistPart(body, part, index);
		return part;
	}

	@Override
	public void removePagePart(Page page, PagePart part) {
		PageBody body = pageDao.loadPageBodyByKey(page.getBody().getKey());
		pageDao.removePart(body, part);
	}

	@Override
	public void moveUpPagePart(Page page, PagePart part) {
		PageBody body = pageDao.loadPageBodyByKey(page.getBody().getKey());
		pageDao.moveUpPart(body, part);
	}

	@Override
	public void moveDownPagePart(Page page, PagePart part) {
		PageBody body = pageDao.loadPageBodyByKey(page.getBody().getKey());
		pageDao.moveDownPart(body, part);
	}

	@Override
	public void movePagePart(Page page, PagePart partToMove, PagePart sibling, boolean after) {
		PageBody body = pageDao.loadPageBodyByKey(page.getBody().getKey());
		pageDao.movePart(body, partToMove, sibling, after);
	}

	@Override
	public Page removePage(Page page) {
		// will take care of the assignments
		return pageDao.removePage(page);
	}	

	@Override
	public List<PagePart> getPageParts(Page page) {
		return pageDao.getParts(page.getBody());
	}
	

	@Override
	public Assignment getAssignment(PageBody body) {
		return assignmentDao.loadAssignment(body);
	}

	@Override
	public void createLog(Page page, Identity doer) {
		contentAuditLogDao.create(Action.CREATE, page, doer);
	}

	@Override
	public void updateLog(Page page, Identity doer) {
		contentAuditLogDao.create(Action.UPDATE, page, doer);
	}
	
	@Override
	public ContentAuditLog lastChange(Page page) {
		return contentAuditLogDao.lastChange(page);
	}

	@Override
	public List<TaxonomyCompetence> getRelatedCompetences(Page page, boolean fetchTaxonomies) {
		return pageToTaxonomyCompetenceDao.getCompetencesToPage(page, fetchTaxonomies);
	}
	
	@Override
	public Page getPageToCompetence(TaxonomyCompetence competence) {
		return pageToTaxonomyCompetenceDao.getPageToCompetence(competence);
	}
	
	@Override
	public void linkCompetence(Page page, TaxonomyCompetence competence) {
		pageToTaxonomyCompetenceDao.createRelation(page, competence);
		
	}
	
	@Override
	public void unlinkCompetence(Page page, TaxonomyCompetence competence) {
		pageToTaxonomyCompetenceDao.deleteRelation(page, competence);
	}
	
	@Override
	public void linkCompetences(Page page, Identity identity, Set<? extends TaxonomyLevelRef> taxonomyLevels) {
		List<TaxonomyCompetence> relatedCompetences = getRelatedCompetences(page, true);
		List<TaxonomyLevel> relatedCompetenceLevels = relatedCompetences.stream().map(TaxonomyCompetence::getTaxonomyLevel).collect(Collectors.toList());
		
		List<Long> newTaxonomyLevelKeys = taxonomyLevels.stream()
				.map(TaxonomyLevelRef::getKey)
				.collect(Collectors.toList());
		
		List<TaxonomyLevel> newTaxonomyLevels = taxonomyLevelDAO.loadLevelsByKeys(newTaxonomyLevelKeys);
		
		// Remove old competences
		for (TaxonomyCompetence competence : relatedCompetences) {
			if (!newTaxonomyLevels.contains(competence.getTaxonomyLevel())) {
				unlinkCompetence(page, competence);
			}
		}
		
		// Create new competences
		for (TaxonomyLevel newLevel : newTaxonomyLevels) {
			if (!relatedCompetenceLevels.contains(newLevel)) {
				TaxonomyCompetence competence = taxonomyCompetenceDAO.createTaxonomyCompetence(TaxonomyCompetenceTypes.have, newLevel, identity, null, TaxonomyCompetenceLinkLocations.PORTFOLIO);
				linkCompetence(page, competence);
			}
		}		
	}
	
	@Override
	public Map<TaxonomyLevel, Long> getCompetencesAndUsage(List<Page> pages) {
		return pageToTaxonomyCompetenceDao.getCompetencesAndUsage(pages);
	}
	
	@Override
	public Map<Category, Long> getCategoriesAndUsage(List<Page> pages) {
		return categoryDao.getCategoriesAndUsage(pages);
	}
}
