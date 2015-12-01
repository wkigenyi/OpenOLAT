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
package org.olat.course.certificate.restapi;

import static org.olat.restapi.security.RestSecurityHelper.isAdmin;

import java.io.File;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.olat.basesecurity.BaseSecurity;
import org.olat.core.CoreSpringFactory;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.StringHelper;
import org.olat.core.util.resource.OresHelper;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.certificate.Certificate;
import org.olat.course.certificate.CertificateTemplate;
import org.olat.course.certificate.CertificatesManager;
import org.olat.course.certificate.model.CertificateInfos;
import org.olat.repository.RepositoryEntry;
import org.olat.resource.OLATResource;
import org.olat.resource.OLATResourceManager;
import org.olat.restapi.support.MultipartReader;
import org.olat.restapi.support.ObjectFactory;

/**
 * 
 * Initial date: 17.11.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Path("repo/courses/{resourceKey}/certificates")
public class CertificationWebService {
	
	
	@HEAD
	@Path("{identityKey}")
	@Produces({"application/pdf"})
	public Response getCertificateInfo(@PathParam("identityKey") Long identityKey, @PathParam("resourceKey") Long resourceKey,
			@Context HttpServletRequest request) {
		if(!isAdmin(request)) {
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		}

		CertificatesManager certificatesManager = CoreSpringFactory.getImpl(CertificatesManager.class);
		BaseSecurity baseSecurity = CoreSpringFactory.getImpl(BaseSecurity.class);

		Identity identity = baseSecurity.loadIdentityByKey(identityKey);
		if(identity == null) {
			return Response.serverError().status(Response.Status.NOT_FOUND).build();
		}
		
		OLATResourceable courseOres = OresHelper.createOLATResourceableInstance("CourseModule", resourceKey);
		OLATResourceManager resourceManager = CoreSpringFactory.getImpl(OLATResourceManager.class);
		OLATResource resource = resourceManager.findResourceable(courseOres);
		if(resource == null) {
			resource = resourceManager.findResourceById(resourceKey);
		}
		if(resource == null) {
			return Response.serverError().status(Response.Status.NOT_FOUND).build();
		}
		
		Certificate certificate = certificatesManager.getLastCertificate(identity, resource.getKey());
		if(certificate == null) {
			return Response.serverError().status(Response.Status.NOT_FOUND).build();
		}

		VFSLeaf certificateFile = certificatesManager.getCertificateLeaf(certificate);
		if(certificateFile == null || !certificateFile.exists()) {
			return Response.serverError().status(Response.Status.NOT_FOUND).build();
		}
		return Response.ok().build();
	}

	/**
	 * Return the certificate as PDF file.
	 * 
	 * @response.representation.200.mediaType application/pdf
	 * @response.representation.200.doc The certificate as file
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
	 * @response.representation.404.doc The owner or the certificate cannot be found
	 * @param identityKey The owner of the certificate
	 * @param resourceKey The primary key of the resource of the repository entry of the course.
	 * @param request The request
	 * @return The certificate
	 */
	@GET
	@Path("{identityKey}")
	@Produces({"application/pdf"})
	public Response getCertificate(@PathParam("identityKey") Long identityKey, @PathParam("resourceKey") Long resourceKey,
			@Context HttpServletRequest request) {
		if(!isAdmin(request)) {
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		}

		CertificatesManager certificatesManager = CoreSpringFactory.getImpl(CertificatesManager.class);
		BaseSecurity baseSecurity = CoreSpringFactory.getImpl(BaseSecurity.class);

		Identity identity = baseSecurity.loadIdentityByKey(identityKey);
		if(identity == null) {
			return Response.serverError().status(Response.Status.NOT_FOUND).build();
		}
		Certificate certificate = certificatesManager.getLastCertificate(identity, resourceKey);
		if(certificate == null) {
			return Response.serverError().status(Response.Status.NOT_FOUND).build();
		}

		VFSLeaf certificateFile = certificatesManager.getCertificateLeaf(certificate);
		return Response.ok(certificateFile.getInputStream()).build();
	}
	
	/**
	 * Generate a new certificate.
	 * 
	 * @response.representation.200.doc If the certificate was created 
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
	 * @response.representation.404.doc The identity or the resource cannot be found
	 * @response.representation.500.doc An unexpected error happened during the creation of the certificate
	 * @param identityKey The owner of the certificate
	 * @param resourceKey The primary key of the resource of the repository entry of the course.
	 * @param score The score which appears in the certificate
	 * @param passed The passed/failed which appears in the certificate (true/false)
	 * @param creationDate The date of the certification
	 * @param request The request
	 * @return Nothing special
	 */
	@PUT
	@Path("{identityKey}")
	public Response putCertificate(@PathParam("identityKey") Long identityKey, @PathParam("resourceKey") Long resourceKey,
			@QueryParam("score") Float score, @QueryParam("passed") Boolean passed,
			@QueryParam("creationDate") String creationDate,
			@Context HttpServletRequest request) {
		
		if(!isAdmin(request)) {
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		}

		BaseSecurity baseSecurity = CoreSpringFactory.getImpl(BaseSecurity.class);
		Identity assessedIdentity = baseSecurity.loadIdentityByKey(identityKey);
		if(assessedIdentity == null) {
			return Response.serverError().status(Response.Status.NOT_FOUND).build();
		}

		OLATResourceManager resourceManager = CoreSpringFactory.getImpl(OLATResourceManager.class);
		OLATResource resource = resourceManager.findResourceById(resourceKey);
		if(resource == null) {
			resource = resourceManager.findResourceable(resourceKey, "CourseModule");
		}
		
		if(resource == null) {	
			return Response.serverError().status(Response.Status.NOT_FOUND).build();
		} else {
			CertificatesManager certificatesManager = CoreSpringFactory.getImpl(CertificatesManager.class);
			
			ICourse course = CourseFactory.loadCourse(resource);
			RepositoryEntry entry = course.getCourseEnvironment().getCourseGroupManager().getCourseEntry();

			CertificateTemplate template = null;
			Long templateId = course.getCourseConfig().getCertificateTemplate();
			if(templateId != null) {
				template = certificatesManager.getTemplateById(templateId);
			}
			
			CertificateInfos certificateInfos = new CertificateInfos(assessedIdentity, score, passed);
			if(StringHelper.containsNonWhitespace(creationDate)) {
				Date date = ObjectFactory.parseDate(creationDate);
				certificateInfos.setCreationDate(date);
			}
			Certificate certificate = certificatesManager.generateCertificate(certificateInfos, entry, template, false);
			if(certificate != null) {
				return Response.ok().build();
			}
			return Response.serverError().status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}	
	}

	/**
	 * Upload a new certificate.
	 * 
	 * @response.representation.200.doc if the certificate was uploaded 
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
	 * @response.representation.404.doc The identity or the resource cannot be found
	 * @param identityKey The owner of the certificate
	 * @param resourceKey The primary key of the resource of the repository entry of the course.
	 * @param request The request
	 * @return Nothing special
	 */
	@POST
	@Path("{identityKey}")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public Response postCertificate(@PathParam("identityKey") Long identityKey, @PathParam("resourceKey") Long resourceKey,
			@Context HttpServletRequest request) {
		
		if(!isAdmin(request)) {
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		}

		MultipartReader partsReader = null;
		try {
			partsReader = new MultipartReader(request);
			File tmpFile = partsReader.getFile();
			String courseTitle = partsReader.getValue("courseTitle");
			String creationDateStr = partsReader.getValue("creationDate");
			Date creationDate = null;
			if(StringHelper.containsNonWhitespace(creationDateStr)) {
				creationDate = ObjectFactory.parseDate(creationDateStr);
			}

			CertificatesManager certificatesManager = CoreSpringFactory.getImpl(CertificatesManager.class);
			BaseSecurity baseSecurity = CoreSpringFactory.getImpl(BaseSecurity.class);
			Identity assessedIdentity = baseSecurity.loadIdentityByKey(identityKey);
			if(assessedIdentity == null) {
				return Response.serverError().status(Response.Status.NOT_FOUND).build();
			}

			OLATResourceManager resourceManager = CoreSpringFactory.getImpl(OLATResourceManager.class);
			OLATResource resource = resourceManager.findResourceById(resourceKey);
			if(resource == null) {
				certificatesManager.uploadStandaloneCertificate(assessedIdentity, creationDate, courseTitle, resourceKey, tmpFile);
			} else {
				certificatesManager.uploadCertificate(assessedIdentity, creationDate, resource, tmpFile);
			}

			return Response.ok().build();
		} catch (Throwable e) {
			throw new WebApplicationException(e);
		}	finally {
			MultipartReader.closeQuietly(partsReader);
		}
	}
}