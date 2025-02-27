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
package org.olat.modules.project.manager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.olat.basesecurity.OrganisationRoles;
import org.olat.basesecurity.OrganisationService;
import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.commons.services.vfs.VFSRepositoryService;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.modules.project.ProjActivity.Action;
import org.olat.modules.project.ProjArtefact;
import org.olat.modules.project.ProjArtefactItems;
import org.olat.modules.project.ProjArtefactSearchParams;
import org.olat.modules.project.ProjArtefactToArtefact;
import org.olat.modules.project.ProjArtefactToArtefactSearchParams;
import org.olat.modules.project.ProjFile;
import org.olat.modules.project.ProjNote;
import org.olat.modules.project.ProjProject;
import org.olat.modules.project.ProjProjectRef;
import org.olat.modules.project.ProjTag;
import org.olat.modules.project.ProjTagSearchParams;
import org.olat.modules.project.ProjToDo;
import org.olat.modules.project.ProjectCopyService;
import org.olat.modules.project.ProjectService;
import org.olat.modules.project.ProjectStatus;
import org.olat.modules.todo.ToDoStatus;
import org.olat.modules.todo.ToDoTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 9 May 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
public class ProjectCopyServiceImpl implements ProjectCopyService {
	
	@Autowired
	private ProjectService projectService;
	@Autowired
	private ProjArtefactToArtefactDAO artefactToArtefactDao;
	@Autowired
	private ProjTagDAO tagDao;
	@Autowired
	private OrganisationService organisationService;
	@Autowired
	private VFSRepositoryService vfsRepositoryServcie;
	@Autowired
	private ProjActivityDAO activityDao;
	
	@Override
	public ProjProject copyProjectFromTemplate(Identity doer, ProjProjectRef projectTemplate) {
		ProjProject project = projectService.getProject(projectTemplate);
		if (project == null || ProjectStatus.deleted == project.getStatus()) {
			return null;
		}
		
		ProjProject projectCopy = projectService.createProject(doer, doer);
		projectCopy = projectService.updateProject(doer, projectCopy, 
				project.getExternalRef(),
				project.getTitle(),
				project.getTeaser(),
				project.getDescription(),
				false,
				false);
		
		List<Organisation> organisations = organisationService.getOrganisations(doer, OrganisationRoles.user);
		projectService.updateProjectOrganisations(doer, projectCopy, organisations);
		
		copyProjectArtefacts(doer, project, projectCopy);
		
		return projectCopy;
	}

	@Override
	public void copyProjectArtefacts(Identity doer, ProjProjectRef project, ProjProject projectCopy) {
		List<String> artefactTypes = List.of(ProjFile.TYPE, ProjNote.TYPE, ProjToDo.TYPE);
		
		ProjArtefactSearchParams sarchParams = new ProjArtefactSearchParams();
		sarchParams.setProject(project);
		sarchParams.setTypes(artefactTypes);
		sarchParams.setStatus(List.of(ProjectStatus.active));
		ProjArtefactItems artefactItems = projectService.getArtefactItems(sarchParams);
		
		ProjTagSearchParams tagSearchParams = new ProjTagSearchParams();
		tagSearchParams.setProject(project);
		tagSearchParams.setArtefactTypes(artefactTypes);
		tagSearchParams.setArtefactStatus(List.of(ProjectStatus.active));
		Map<ProjArtefact, List<String>> artefactToTagDisplayNames = tagDao.loadTags(tagSearchParams).stream()
				.collect(Collectors.groupingBy(
						ProjTag::getArtefact,
						Collectors.collectingAndThen(
								Collectors.toList(),
								tags -> tags.stream()
										.map(tag -> tag.getTag().getDisplayName())
										.distinct()
										.collect(Collectors.toList()))));
		
		Map<ProjArtefact, ProjArtefact> artefactToArtefactCopy = new HashMap<>();
		// The creation order has to be kept.
		List<ProjArtefact> artefacts = artefactItems.getArtefacts().stream()
				.sorted((a1, a2) -> a1.getCreationDate().compareTo(a2.getCreationDate()))
				.toList();
		for (ProjArtefact artefact : artefacts) {
			switch (artefact.getType()) {
			case ProjFile.TYPE: copyFile(doer, projectCopy, artefactToTagDisplayNames, artefactToArtefactCopy, artefactItems.getFile(artefact));
			case ProjNote.TYPE: copyNote(doer, projectCopy, artefactToTagDisplayNames, artefactToArtefactCopy, artefactItems.getNote(artefact));
			case ProjToDo.TYPE: copyToDo(doer, projectCopy, artefactToTagDisplayNames, artefactToArtefactCopy, artefactItems.getToDo(artefact));
			default: // do not copy 
			}
			
		}
		
		copyArtefactToArtefact(doer, project, artefactToArtefactCopy);
	}

	private void copyFile(Identity doer, ProjProject projectCopy, Map<ProjArtefact, List<String>> artefactToTagDisplayNames,
			Map<ProjArtefact, ProjArtefact> artefactToArtefactCopy,
			ProjFile file) {
		if (file == null) return;
		
		VFSMetadata vfsMetadata = file.getVfsMetadata();
		VFSItem item = vfsRepositoryServcie.getItemFor(vfsMetadata);
		if (item instanceof VFSLeaf vfsLeaf) {
			ProjFile fileCopy = projectService.createFile(doer, projectCopy, vfsMetadata.getFilename(),
					vfsLeaf.getInputStream(), false);
			activityDao.create(Action.fileCopyInitialized, null, null, doer, fileCopy.getArtefact());
			projectService.updateFile(doer, fileCopy,
					vfsMetadata.getFilename(),
					vfsMetadata.getTitle(),
					vfsMetadata.getComment());
			projectService.updateTags(doer, fileCopy.getArtefact(), artefactToTagDisplayNames.getOrDefault(file.getArtefact(), List.of()));
			artefactToArtefactCopy.put(file.getArtefact(), fileCopy.getArtefact());
		}
	}

	private void copyNote(Identity doer, ProjProject projectCopy, Map<ProjArtefact, List<String>> artefactToTagDisplayNames,
			Map<ProjArtefact, ProjArtefact> artefactToArtefactCopy,
			ProjNote note) {
		if (note == null) return;
		
		ProjNote noteCopy = projectService.createNote(doer, projectCopy);
		activityDao.create(Action.noteCopyInitialized, null, null, doer, noteCopy.getArtefact());
		projectService.updateNote(doer, noteCopy, null, note.getTitle(), note.getText());
		projectService.updateTags(doer, noteCopy.getArtefact(), artefactToTagDisplayNames.getOrDefault(note.getArtefact(), List.of()));
		artefactToArtefactCopy.put(note.getArtefact(), noteCopy.getArtefact());
	}
	
	private void copyToDo(Identity doer, ProjProject projectCopy, Map<ProjArtefact, List<String>> artefactToTagDisplayNames,
			Map<ProjArtefact, ProjArtefact> artefactToArtefactCopy,
			ProjToDo toDo) {
		if (toDo == null) return;
		
		ProjToDo toDoCopy = projectService.createToDo(doer, projectCopy);
		activityDao.create(Action.toDoCopyInitialized, null, null, doer, toDoCopy.getArtefact());
		ToDoTask toDoTask = toDo.getToDoTask();
		projectService.updateToDo(doer, toDoCopy,
				toDoTask.getTitle(),
				ToDoStatus.open,
				toDoTask.getPriority(),
				null,
				null,
				toDoTask.getExpenditureOfWork(),
				toDoTask.getDescription());
		projectService.updateTags(doer, toDoCopy, artefactToTagDisplayNames.getOrDefault(toDo.getArtefact(), List.of()));
		artefactToArtefactCopy.put(toDo.getArtefact(), toDoCopy.getArtefact());
	}
	
	private void copyArtefactToArtefact(Identity doer, ProjProjectRef project, Map<ProjArtefact, ProjArtefact> artefactToArtefactCopy) {
		ProjArtefactToArtefactSearchParams searchParams = new ProjArtefactToArtefactSearchParams();
		searchParams.setProject(project);
		List<ProjArtefactToArtefact> artefactToArtefacts = artefactToArtefactDao.loadArtefactToArtefacts(searchParams).stream()
				.sorted((ata1, ata2) -> ata1.getCreationDate().compareTo(ata2.getCreationDate()))
				.toList();
		
		for (ProjArtefactToArtefact artefactToArtefact : artefactToArtefacts) {
			ProjArtefact artefactCopy1 = artefactToArtefactCopy.get(artefactToArtefact.getArtefact1());
			ProjArtefact artefactCopy2 = artefactToArtefactCopy.get(artefactToArtefact.getArtefact2());
			if (artefactCopy1 != null && artefactCopy2 != null) {
				projectService.linkArtefacts(doer, artefactCopy1, artefactCopy2);
			}
		}
	}

}
