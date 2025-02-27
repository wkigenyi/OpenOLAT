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
package org.olat.selenium;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.DayOfWeek;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.olat.course.learningpath.FullyAssessedTrigger;
import org.olat.modules.ceditor.model.ContainerLayout;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.selenium.page.LoginPage;
import org.olat.selenium.page.NavigationPage;
import org.olat.selenium.page.core.AdministrationPage;
import org.olat.selenium.page.core.ContactPage;
import org.olat.selenium.page.core.FolderPage;
import org.olat.selenium.page.core.MenuTreePageFragment;
import org.olat.selenium.page.course.AppointmentPage;
import org.olat.selenium.page.course.AssessmentToolPage;
import org.olat.selenium.page.course.BigBlueButtonPage;
import org.olat.selenium.page.course.CheckListConfigPage;
import org.olat.selenium.page.course.CheckListPage;
import org.olat.selenium.page.course.ContactConfigPage;
import org.olat.selenium.page.course.CourseEditorPageFragment;
import org.olat.selenium.page.course.CoursePageFragment;
import org.olat.selenium.page.course.DialogConfigurationPage;
import org.olat.selenium.page.course.DialogPage;
import org.olat.selenium.page.course.DocumentConfigurationPage;
import org.olat.selenium.page.course.DocumentPage;
import org.olat.selenium.page.course.ForumCEPage;
import org.olat.selenium.page.course.InfoMessageCEPage;
import org.olat.selenium.page.course.LTIConfigurationPage;
import org.olat.selenium.page.course.LTIPage;
import org.olat.selenium.page.course.MemberListConfigurationPage;
import org.olat.selenium.page.course.MemberListPage;
import org.olat.selenium.page.course.MembersPage;
import org.olat.selenium.page.course.ParticipantFolderPage;
import org.olat.selenium.page.course.STConfigurationPage;
import org.olat.selenium.page.course.STConfigurationPage.DisplayType;
import org.olat.selenium.page.course.SinglePage;
import org.olat.selenium.page.course.SinglePageConfigurationPage;
import org.olat.selenium.page.course.TeamsPage;
import org.olat.selenium.page.course.ZoomConfigurationPage;
import org.olat.selenium.page.course.ZoomPage;
import org.olat.selenium.page.forum.ForumPage;
import org.olat.selenium.page.graphene.OOGraphene;
import org.olat.selenium.page.repository.AuthoringEnvPage;
import org.olat.selenium.page.repository.AuthoringEnvPage.ResourceType;
import org.olat.selenium.page.repository.FeedPage;
import org.olat.selenium.page.repository.RepositoryEditDescriptionPage;
import org.olat.selenium.page.repository.ScormPage;
import org.olat.selenium.page.repository.UserAccess;
import org.olat.selenium.page.survey.SurveyEditorPage;
import org.olat.selenium.page.survey.SurveyPage;
import org.olat.selenium.page.user.UserToolsPage;
import org.olat.test.JunitTestHelper;
import org.olat.test.rest.UserRestClient;
import org.olat.user.restapi.UserVO;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.dumbster.smtp.SmtpMessage;

/**
 * 
 * Initial date: 27 août 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@RunWith(Arquillian.class)
public class CourseElementTest extends Deployments {

	private WebDriver browser = getWebDriver(0);
	@ArquillianResource
	private URL deploymentUrl;
	

	/**
	 * Create a course, create a CP, go the the course editor,
	 * create a course element of type CP, select the CP which just created,
	 * close the course editor and check the presence of the CP with the
	 * default title of the first page.
	 * 
	 * @param loginPage
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void courseWithCP()
	throws IOException, URISyntaxException {
		
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		LoginPage loginPage = LoginPage.load(browser, deploymentUrl);
		loginPage.loginAs(author.getLogin(), author.getPassword());
		
		//create a course
		String courseTitle = "Course-With-CP-" + UUID.randomUUID();
		NavigationPage navBar = NavigationPage.load(browser);
		navBar
			.openAuthoringEnvironment()
			.createCourse(courseTitle)
			.clickToolbarBack();
		
		//go the authoring environment to create a CP
		String cpTitle = "CP for a course - " + UUID.randomUUID();
		navBar
			.openAuthoringEnvironment()
			.createCP(cpTitle)
			.assertOnInfos();
		
		navBar.openCourse(courseTitle);
		
		String cpNodeTitle = "CPNode-1";
		//create a course element of type CP with the CP that we create above
		CourseEditorPageFragment courseEditor = CoursePageFragment.getCourse(browser)
			.edit();
		courseEditor
			.createNode("cp")
			.nodeTitle(cpNodeTitle)
			.selectTabCPContent()
			.chooseCP(cpTitle);

		//publish the course
		courseEditor
			.publish()
			.quickPublish();
		
		//open the course and see the CP
		CoursePageFragment course = courseEditor
			.clickToolbarBack();
		
		course
			.tree()
			.selectWithTitle(cpNodeTitle);
		
		//check that the default title of CP (Lorem Ipsum) is visible in the iframe
		By iframe = By.cssSelector("div.o_iframedisplay>iframe");
		OOGraphene.waitElement(iframe, browser);
		OOGraphene.waitingALittleBit();
		WebElement cpIframe = browser.findElement(iframe);
		browser.switchTo().frame(cpIframe);
		OOGraphene.waitElement(By.xpath("//h2[text()='Lorem Ipsum']"), browser);
	}
	

	/**
	 * This test an edge case where a course start automatically its first
	 *  course element, which is a structure node which start itself its first
	 *  element, which is a SCORM which launch itself automatically.
	 * 
	 * @param loginPage
	 */
	@Test
	@RunAsClient
	public void courseWithSCORM_fullAuto()
	throws IOException, URISyntaxException {
		
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		UserVO participant = new UserRestClient(deploymentUrl).createRandomUser("Ryomou");

		LoginPage loginPage = LoginPage.load(browser, deploymentUrl);
		loginPage.loginAs(author.getLogin(), author.getPassword());
		
		URL zipUrl = JunitTestHelper.class.getResource("file_resources/scorm/SCORM_course_full_auto.zip");
		File zipFile = new File(zipUrl.toURI());
		//go the authoring environment to import our course
		String zipTitle = "SCORM - " + UUID.randomUUID();
		NavigationPage navBar = NavigationPage.load(browser);
		navBar
			.openAuthoringEnvironment()
			.uploadResource(zipTitle, zipFile);
		
		// publish the course
		new RepositoryEditDescriptionPage(browser)
			.clickToolbarBack();
		CoursePageFragment course = CoursePageFragment.getCourse(browser)
				.edit()
				.autoPublish();
		
		//scorm is auto started -> back
		ScormPage.getScormPage(browser)
			.assertOnCoachingOverview();
		
		// make the author a participant too
		MembersPage members = course
			.members();
		members
			.addMember()
			.searchMember(participant, true)
			.nextUsers()
			.nextOverview()
			.nextPermissions()
			.finish();
		members
			.clickToolbarBack();
		
		course
			.settings()
			.accessConfiguration()
			.setAccessToRegisteredUser()
			.clickToolbarBack();
		
		course
			.changeStatus(RepositoryEntryStatusEnum.published);
		
		String courseUrl = browser.getCurrentUrl();
		if(courseUrl.indexOf("CourseNode") >= 0) {
			courseUrl = courseUrl.substring(0, courseUrl.indexOf("CourseNode"));
		}

		//log out
		new UserToolsPage(browser)
			.logout();
		
		// participant log in and go directly to the course with the SCORM
		LoginPage participantLoginPage = LoginPage.load(browser, new URL(courseUrl));		
		
		participantLoginPage
			.loginAs(participant.getLogin(), participant.getPassword(), By.className("o_scorm_content"));
		
		// direct jump in SCORM content
		ScormPage.getScormPage(browser)
			.passVerySimpleScorm()
			.assertOnScormPassed()
			.assertOnScormScore(33);
	}
	
	/**
	 * Create a course, create a wiki, go the the course editor,
	 * create a course element of type wiki, select the wiki which just created,
	 * close the course editor and select the index page of the wiki.
	 * 
	 * @param loginPage
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void courseWithWiki()
	throws IOException, URISyntaxException {
		
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		LoginPage loginPage = LoginPage.load(browser, deploymentUrl);
		loginPage.loginAs(author.getLogin(), author.getPassword());
		
		//create a course
		String courseTitle = "Course-With-Wiki-" + UUID.randomUUID().toString();
		NavigationPage navBar = NavigationPage.load(browser);
		navBar
			.openAuthoringEnvironment()
			.createCourse(courseTitle)
			.clickToolbarBack();
		
		//go the authoring environment to create a CP
		String wikiTitle = "Wiki for a course - " + UUID.randomUUID().toString();
		navBar
			.openAuthoringEnvironment()
			.createWiki(wikiTitle)
			.assertOnInfos();
		
		navBar.openCourse(courseTitle);
		
		String wikiNodeTitle = "WikiNode-1";
		//create a course element of type CP with the CP that we create above
		CourseEditorPageFragment courseEditor = CoursePageFragment.getCourse(browser)
			.edit();
		courseEditor
			.createNode("wiki")
			.nodeTitle(wikiNodeTitle)
			.selectTabWikiContent()
			.chooseWiki(wikiTitle);

		//publish the course
		courseEditor
			.publish()
			.quickPublish();
		
		//open the course and see the CP
		CoursePageFragment course = courseEditor
			.clickToolbarBack();
		
		course
			.clickTree()
			.selectWithTitle(wikiNodeTitle)
			.selectWithTitle("Index");
		
		//check that the title of the index article/page is visible
		WebElement indexArticleTitle = browser.findElement(By.className("o_wikimod_heading"));
		Assert.assertEquals("Index", indexArticleTitle.getText().trim());
	}
	
	/**
	 * Create a course, create a course element of type wiki. Open
	 * the resource chooser, create a wiki, close the editor, show the 
	 * index page of the wiki.
	 * 
	 * @param loginPage
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void courseWithWiki_createInCourseEditor()
	throws IOException, URISyntaxException {
		
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		LoginPage loginPage = LoginPage.load(browser, deploymentUrl);
		loginPage.loginAs(author.getLogin(), author.getPassword());
		
		//create a course
		String courseTitle = "Course-With-Wiki-" + UUID.randomUUID().toString();
		NavigationPage navBar = NavigationPage.load(browser);
		navBar
			.openAuthoringEnvironment()
			.createCourse(courseTitle)
			.clickToolbarBack();
		
		String wikiNodeTitle = "WikiNode-1";
		String wikiTitle = "Wiki for a course - " + UUID.randomUUID().toString();
		
		//create a course element of type CP with the CP that we create above
		CourseEditorPageFragment courseEditor = CoursePageFragment.getCourse(browser)
			.edit();
		courseEditor
			.createNode("wiki")
			.nodeTitle(wikiNodeTitle)
			.selectTabWikiContent()
			.createWiki(wikiTitle);

		//publish the course
		courseEditor
			.publish()
			.quickPublish();
		
		//open the course and see the CP
		CoursePageFragment course = courseEditor
			.clickToolbarBack();
		
		course
			.clickTree()
			.selectWithTitle(wikiNodeTitle)
			// the course node select automatically the index page of the wiki
			.assertWithTitleSelected("Index")
			.selectWithTitle("Index");
		
		//check that the title of the index article/page is visible
		WebElement indexArticleTitle = browser.findElement(By.className("o_wikimod_heading"));
		Assert.assertEquals("Index", indexArticleTitle.getText().trim());
	}
	

	@Test
	@RunAsClient
	public void courseWithQTITest()
	throws IOException, URISyntaxException {
		
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		LoginPage loginPage = LoginPage.load(browser, deploymentUrl);
		loginPage.loginAs(author.getLogin(), author.getPassword());
		
		//create a course
		String courseTitle = "Course-With-QTI-Test-1.2-" + UUID.randomUUID().toString();
		NavigationPage navBar = NavigationPage.load(browser);
		navBar
			.openAuthoringEnvironment()
			.createCourse(courseTitle)
			.clickToolbarBack();
		
		String testNodeTitle = "QTITest-1";
		String testTitle = "Test - " + UUID.randomUUID().toString();
		
		//create a course element of type CP with the CP that we create above
		CourseEditorPageFragment courseEditor = CoursePageFragment.getCourse(browser)
			.edit();
		courseEditor
			.createNode("iqtest")
			.nodeTitle(testNodeTitle)
			.selectTabTestContent()
			.createQTITest(testTitle);

		//publish the course
		courseEditor
			.publish()
			.quickPublish();
		
		//open the course and see the CP
		CoursePageFragment course = courseEditor
			.clickToolbarBack();
		
		course
			.tree()
			.selectWithTitle(testNodeTitle);
		
		//check that the title of the start page of test is correct
		WebElement testH2 = browser.findElement(By.cssSelector("div.o_course_run h2"));
		Assert.assertEquals(testNodeTitle, testH2.getText().trim());
	}
	
	/**
	 * Create a course with a course element of type podcast. Create
	 * a podcast, publish the course, go the the course and configure
	 * the podcast to read an external feed.
	 * 
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void courseWithPodcast_externalFeed()
	throws IOException, URISyntaxException {
		
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		LoginPage loginPage = LoginPage.load(browser, deploymentUrl);
		loginPage.loginAs(author.getLogin(), author.getPassword());
		
		//create a course
		String courseTitle = "Course-With-Podcast-" + UUID.randomUUID();
		NavigationPage navBar = NavigationPage.load(browser);
		navBar
			.openAuthoringEnvironment()
			.createCourse(courseTitle)
			.clickToolbarBack();
		
		String podcastNodeTitle = "PodcatNode-1";
		String podcastTitle = "ThePodcast - " + UUID.randomUUID();
		
		//create a course element of type podcast
		CourseEditorPageFragment courseEditor = CoursePageFragment.getCourse(browser)
			.edit();
		courseEditor
			.createNode("podcast")
			.nodeTitle(podcastNodeTitle)
			.selectTabFeedContent()
			.createFeed(podcastTitle);

		//publish the course
		courseEditor
			.publish()
			.quickPublish();
		
		//open the course and see the podcast
		CoursePageFragment course = courseEditor
			.clickToolbarBack();
		course
			.tree()
			.selectWithTitle(podcastNodeTitle);
		
		//check that the title of the podcast is correct
		By podcastElementBy = By.cssSelector("div.o_podcast_info>h2>i.o_FileResource-PODCAST_icon");
		OOGraphene.waitElement(podcastElementBy, browser);

		FeedPage.getFeedPage(browser)
			.newExternalPodcast(podcastTitle, "https://www.srf.ch/feed/podcast/sd/6e633013-c03d-4f49-a1b7-d5b58cfed837.xml")
			.assertOnPodcastEpisodeTitle();
	}
	
	
	/**
	 * Create a course with a course element of type blog. Create
	 * a blog, publish the course, go the the course and configure
	 * the blog to read the OpenOlat RSS feed.
	 * 
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void courseWithBlog_externalFeed()
	throws IOException, URISyntaxException {
		
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		LoginPage loginPage = LoginPage.load(browser, deploymentUrl);
		loginPage.loginAs(author.getLogin(), author.getPassword());
		
		//create a course
		String courseTitle = "Course-With-Blog-" + UUID.randomUUID().toString();
		NavigationPage navBar = NavigationPage.load(browser);
		navBar
			.openAuthoringEnvironment()
			.createCourse(courseTitle)
			.clickToolbarBack();
		
		String blogNodeTitle = "BlogNode-1";
		String blogTitle = "Blog - " + UUID.randomUUID();
		
		//create a course element of type blog
		CourseEditorPageFragment courseEditor = CoursePageFragment.getCourse(browser)
			.edit();
		courseEditor
			.createNode("blog")
			.nodeTitle(blogNodeTitle)
			.selectTabFeedContent()
			.createFeed(blogTitle);

		//publish the course
		courseEditor
			.publish()
			.quickPublish();
		
		//open the course and see the blog
		CoursePageFragment course = courseEditor
			.clickToolbarBack();
		course
			.tree()
			.selectWithTitle(blogNodeTitle);
		
		//check that the title of the podcast is correct
		WebElement podcastH2 = browser.findElement(By.cssSelector("div.o_blog_info>h2>i.o_FileResource-BLOG_icon"));
		Assert.assertNotNull(podcastH2);
		//Assert.assertEquals(blogTitle, podcastH2.getText().trim());
		
		FeedPage.getFeedPage(browser)
			.newExternalBlog(blogTitle, "https://www.openolat.com/feed/")
			.assertOnBlogPostTitle();
	}

	/**
	 * An author create a course with a blog, open it, add a post. A student
	 * open the course, see the blog post. The author add a new post, the
	 * student must see it.
	 * 
	 * @param loginPage
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void courseWithBlog_multipleUsers()
	throws IOException, URISyntaxException {
		WebDriver participantDrone = getWebDriver(1);
		
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		UserVO participant = new UserRestClient(deploymentUrl).createRandomUser("Ryomou");

		LoginPage loginPage = LoginPage.load(browser, deploymentUrl);
		loginPage.loginAs(author.getLogin(), author.getPassword());
		
		//create a course with a blog
		String courseTitle = "Course-Blog-1-" + UUID.randomUUID().toString();
		NavigationPage navBar = NavigationPage.load(browser);
		navBar
			.openAuthoringEnvironment()
			.createCourse(courseTitle)
			.clickToolbarBack();
				
		String blogNodeTitle = "BlogNode-RW-1";
		String blogTitle = "Blog - RW - " + UUID.randomUUID().toString();
				
		//create a course element of type blog with a blog
		CourseEditorPageFragment courseEditor = CoursePageFragment.getCourse(browser)
			.edit();
		courseEditor
			.createNode("blog")
			.nodeTitle(blogNodeTitle)
			.selectTabFeedContent()
			.createFeed(blogTitle);
		//publish the course
		courseEditor
			.publish()
			.quickPublish();
		
		//open the course and see the blog
		CoursePageFragment course = courseEditor
			.clickToolbarBack();
		course
			.tree()
			.selectWithTitle(blogNodeTitle);
		
		String postTitle = "BlogPost-RW-1-" + UUID.randomUUID();
		String postSummary = "Some explanations as teaser";
		String postContent = "Content of the post";
		FeedPage feed = FeedPage.getFeedPage(browser);
		feed
			.newBlog()
			.fillPostForm(postTitle, postSummary, postContent)
			.publishPost();

		//participant go to the blog
		participantDrone.navigate().to(deploymentUrl);
		LoginPage participantLogin = LoginPage.load(participantDrone, deploymentUrl);
		participantLogin.loginAs(participant.getLogin(), participant.getPassword());
		//search the course in "My courses"
		NavigationPage participantNavigation = NavigationPage.load(participantDrone);
		participantNavigation
			.openMyCourses()
			.openSearch()
			.extendedSearch(courseTitle)
			.select(courseTitle)
			.start();
		//Navigate the course to the blog
		CoursePageFragment participantCourse = new CoursePageFragment(participantDrone);
		participantCourse
			.tree()
			.selectWithTitle(blogNodeTitle);
		FeedPage participantFeed = FeedPage.getFeedPage(participantDrone);
		participantFeed.assertOnBlogPost(postTitle);
		
		//the author publish a second post in its blog
		String post2Title = "Blog-RW-2-" + UUID.randomUUID();
		String post2Summary = "Some explanations as teaser";
		String post2Content = "Content of the post";
		feed.addBlogPost()
			.fillPostForm(post2Title, post2Summary, post2Content)
			.publishPost();
		
		//the participant must see the new post after some click
		participantFeed
			.clickFirstMonthOfPager()
			.clickFirstMonthOfPager()
			.assertOnBlogPost(post2Title);
	}
	

	/**
     * Login, create a course, select "Messages Course", insert an info message
     * course element, publish the course, add messages, count if the messages
     * are there, show older messages, count the messages, show current messages,
     * count the messages, edit a message and delete an other, count the messages.
     * 
	 * @param authorLoginPage
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void courseWithInfoMessages()
	throws IOException, URISyntaxException {
		
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		LoginPage authorLoginPage = LoginPage.load(browser, deploymentUrl);
		authorLoginPage.loginAs(author.getLogin(), author.getPassword());
		
		//go to authoring
		NavigationPage navBar = NavigationPage.load(browser);
		AuthoringEnvPage authoringEnv = navBar
			.assertOnNavigationPage()
			.openAuthoringEnvironment();
		
		String title = "Course Msg " + UUID.randomUUID().toString();
		//create course
		authoringEnv
			.openCreateDropDown()
			.clickCreate(ResourceType.course)
			.fillCreateCourseForm(title, false)
			.assertOnInfos()
			.clickToolbarBack();
		
		String infoNodeTitle = "Infos - News";
		//open course editor
		CoursePageFragment course = CoursePageFragment.getCourse(browser);
		CourseEditorPageFragment editor = course
			.assertOnCoursePage()
			.assertOnTitle(title)
			.edit()
			.createNode("info")
			.nodeTitle(infoNodeTitle);
		
		//configure the info messages
		InfoMessageCEPage infoMsgConfig = new InfoMessageCEPage(browser);
		infoMsgConfig
			.selectConfiguration()
			.configure(3);
		
		//publish
		editor
			.publish()
			.quickPublish(UserAccess.registred);
		editor.clickToolbarBack();
		
		course
			.tree()
			.selectWithTitle(infoNodeTitle);
		//set a message
		infoMsgConfig
			.createMessage()
			.setMessage("Information 0", "A very important info")
			.next()
			.finish()
			.assertOnMessageTitle("Information 0");
		
		for(int i=1; i<=3; i++) {
			infoMsgConfig.quickMessage("Information " + i, "More informations");
		}
		
		int numOfMessages = infoMsgConfig.countMessages();
		Assert.assertEquals(3, numOfMessages);
		
		// count old messages
		int numOfOldMessages = infoMsgConfig
				.oldMessages()
				.countMessages();
		Assert.assertEquals(4, numOfOldMessages);
		
		//new messages
		infoMsgConfig.newMessages();
		int numOfNewMessages = infoMsgConfig.countMessages();
		Assert.assertEquals(3, numOfNewMessages);
		
		//edit
		infoMsgConfig
			.oldMessages();
		infoMsgConfig
			.editMessage("Information 2")
			.setMessage("The latest information", "A very important info")
			.next()
			.finish()
			.assertOnMessageTitle("The latest information");

		//delete
		infoMsgConfig
			.deleteMessage("Information 3")
			.confirmDelete();
		
		int numOfSurvivingMessages = infoMsgConfig.countMessages();
		Assert.assertEquals(3, numOfSurvivingMessages);
	}
	
	/**
	 * An author create a course with a dialog course element. It
	 * add a participant to the course, a file to the dialog in
	 * the course element configuration and after publishing the course
	 * in the view of the dialog. It opens the forum of one of the files,
	 * create a new thread.<br>
	 * The participant log in, open the course and the dialog element. It
	 * reads the thread and make a reply. The author answers to the reply.
	 * 
	 * @param loginPage
	 */
	@Test
	@RunAsClient
	public void courseWithDialog()
	throws IOException, URISyntaxException {
		WebDriver participantBrowser = getWebDriver(1);
		
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		UserVO participant = new UserRestClient(deploymentUrl).createRandomUser("Rei");
		LoginPage authorLoginPage = LoginPage.load(browser, deploymentUrl);
		authorLoginPage.loginAs(author.getLogin(), author.getPassword());
		
		//go to authoring
		NavigationPage navBar = NavigationPage.load(browser);
		AuthoringEnvPage authoringEnv = navBar
			.assertOnNavigationPage()
			.openAuthoringEnvironment();
		
		String title = "Course dialog " + UUID.randomUUID();
		//create course
		authoringEnv
			.openCreateDropDown()
			.clickCreate(ResourceType.course)
			.fillCreateCourseForm(title, false)
			.assertOnInfos()
			.clickToolbarBack();
		
		//add a participant
		MembersPage members = new CoursePageFragment(browser)
			.members();
		members
			.addMember()
			.searchMember(participant, true)
			.nextUsers()
			.nextOverview()
			.selectRepositoryEntryRole(false, false, true)
			.nextPermissions()
			.finish();
		members
			.clickToolbarBack();

		String dialogNodeTitle = "DialogNode";
		//open course editor
		CoursePageFragment course = CoursePageFragment.getCourse(browser);
		CourseEditorPageFragment editor = course
			.assertOnCoursePage()
			.assertOnTitle(title)
			.edit()
			.createNode("dialog")
			.nodeTitle(dialogNodeTitle);
		
		//upload a file in the configuration
		URL imageUrl = JunitTestHelper.class.getResource("file_resources/IMG_1484.jpg");
		File imageFile = new File(imageUrl.toURI());
		DialogConfigurationPage dialogConfig = new DialogConfigurationPage(browser);
		dialogConfig
			.selectConfiguration()
			.uploadFile(imageFile);
		
		//publish and go to the course element
		editor
			.publish()
			.quickPublish(UserAccess.membersOnly);
		editor
			.clickToolbarBack();
		course
			.tree()
			.selectWithTitle(dialogNodeTitle);
		
		// upload a second file
		URL imageRunUrl = JunitTestHelper.class.getResource("file_resources/IMG_1483.png");
		File imageRunFile = new File(imageRunUrl.toURI());
		DialogPage dialog = new DialogPage(browser);
		dialog
			.assertOnFile(imageFile.getName())
			.uploadFile(imageRunFile)
			.assertOnFile(imageRunFile.getName())
			.openForum(imageRunFile.getName())
			.createThread("JPEG vs PNG", "Which is the best format", null);
		
		// The participant come in
		LoginPage participantLoginPage = LoginPage.load(participantBrowser, deploymentUrl);
		participantLoginPage
			.loginAs(participant.getLogin(), participant.getPassword())
			.resume();
			
		// The participant find the course
		NavigationPage participantNavBar = NavigationPage.load(participantBrowser);
		participantNavBar
			.assertOnNavigationPage()
			.openMyCourses()
			.select(title);
		// And opens the dialog course element
		CoursePageFragment participantCourse = CoursePageFragment.getCourse(participantBrowser);
		participantCourse
			.tree()
			.selectWithTitle(dialogNodeTitle);
		DialogPage participantDialog = new DialogPage(participantBrowser);
		participantDialog
			.assertOnFile(imageRunFile.getName())
			.openForum(imageRunFile.getName())
			.openThread("JPEG vs PNG")
			.replyToMessage("JPEG vs PNG", "PNG for sure", "Not a loosy format");
		
		//The author reload the messages
		dialog
			.back()
			.openForum(imageRunFile.getName())
			.openThread("JPEG vs PNG")
			.assertMessageBody("Not a loosy format")
			.replyToMessage("PNG for sure", "JPEG smaller", "JPEG is smaller");
		
		//The participant check the reply
		participantDialog
			.back()
			.openForum(imageRunFile.getName())
			.openThread("JPEG vs PNG")
			.assertMessageBody("JPEG is smaller");
	}
	

	/**
	 * An author create a course with a member list course element.
	 * It add two participants and a coach. It publish the course and
	 * check that it sees the authors, coaches and participants.<br>
	 * After that, it edits the course and change the settins to only
	 * show the participants. It checks that only the participants are
	 * visible.<br>
	 * At least, it changes the settings a second time to only show
	 * the course coaches.
	 * 
	 * @param authorLoginPage
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void courseWithMemberList()
	throws IOException, URISyntaxException {
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		UserVO coach = new UserRestClient(deploymentUrl).createRandomUser("Rei");
		UserVO participant1 = new UserRestClient(deploymentUrl).createRandomUser("Kanu");
		UserVO participant2 = new UserRestClient(deploymentUrl).createRandomUser("Ryomou");
		LoginPage authorLoginPage = LoginPage.load(browser, deploymentUrl);
		authorLoginPage.loginAs(author.getLogin(), author.getPassword());
		
		//go to authoring
		NavigationPage navBar = NavigationPage.load(browser);
		AuthoringEnvPage authoringEnv = navBar
			.assertOnNavigationPage()
			.openAuthoringEnvironment();
		
		String title = "Course partilist " + UUID.randomUUID();
		//create course
		authoringEnv
			.openCreateDropDown()
			.clickCreate(ResourceType.course)
			.fillCreateCourseForm(title, false)
			.assertOnInfos()
			.clickToolbarBack();
		
		//add 2 participants
		CoursePageFragment course = new CoursePageFragment(browser);
		MembersPage members = course
			.members();
		members
			.addMember()
			.importList()
			.setMembers(participant1, participant2)
			.nextUsers()
			.nextOverview()
			.nextPermissions()
			.finish();
		//add a coach
		members
			.addMember()	
			.searchMember(coach, true)
			.nextUsers()
			.nextOverview()
			.selectRepositoryEntryRole(false, true, false)
			.nextPermissions()
			.finish();
		members
			.clickToolbarBack();
		
		String memberListTitle = "MemberList";
		//open course editor
		CourseEditorPageFragment editor = course
			.assertOnCoursePage()
			.assertOnTitle(title)
			.edit()
			.createNode("cmembers")
			.nodeTitle(memberListTitle);
		//publish
		editor
			.publish()
			.quickPublish(UserAccess.membersOnly);
		editor
			.clickToolbarBack();
		
		course
			.tree()
			.selectWithTitle(memberListTitle);
		
		//check the default configuration with authors, coaches and participants
		MemberListPage memberList = new MemberListPage(browser);
		memberList
			.assertOnOwner(author.getFirstName())
			.assertOnCoach(coach.getFirstName())
			.assertOnParticipant(participant1.getFirstName())
			.assertOnParticipant(participant2.getFirstName());
		
		//the author is not satisfied with the configuration
		editor = course
			.edit()
			.selectNode(memberListTitle);
		MemberListConfigurationPage memberListConfig = new MemberListConfigurationPage(browser);
		memberListConfig
			.selectSettings()
			.setOwners(Boolean.FALSE)
			.setCoaches(Boolean.FALSE)
			.save();
		
		//go check the results
		course = editor
			.autoPublish();
		course
			.tree()
			.selectWithTitle(memberListTitle);
		
		memberList
			.assertOnMembers()
			.assertOnNotOwner(author.getFirstName())
			.assertOnNotCoach(coach.getFirstName())
			.assertOnParticipant(participant1.getFirstName())
			.assertOnParticipant(participant2.getFirstName());
		
		// perhaps only the coaches
		editor = course
			.edit()
			.selectNode(memberListTitle);
		memberListConfig = new MemberListConfigurationPage(browser);
		memberListConfig
				.selectSettings()
				.setCoaches(Boolean.TRUE)
				.setCourseCoachesOnly()
				.setParticipants(Boolean.FALSE)
				.save();
		
		//go check that we see only the coaches results
		course = editor
			.autoPublish();
		course
			.tree()
			.selectWithTitle(memberListTitle);
		
		memberList
			.assertOnMembers()
			.assertOnNotOwner(author.getFirstName())
			.assertOnCoach(coach.getFirstName())
			.assertOnNotParticipant(participant1.getFirstName())
			.assertOnNotParticipant(participant2.getFirstName());
	}
	
	
	/**
	 * An author create a course with a course element
	 * to show the member list. It add coaches and
	 * participants. It navigates to the member list,
	 * switch to the table view and send and an E-mail
	 * to the participants.
	 * 
	 * @param authorLoginPage The login page
	 * @param ryomouBrowser A browser for the student
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void courseWithMemberList_sendMail()
	throws IOException, URISyntaxException {
						
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		UserVO kanu = new UserRestClient(deploymentUrl).createRandomUser("kanu");
		UserVO ryomou = new UserRestClient(deploymentUrl).createRandomUser("ryomou");
		UserVO student1 = new UserRestClient(deploymentUrl).createRandomUser("student1");
		UserVO student2 = new UserRestClient(deploymentUrl).createRandomUser("student2");

		LoginPage authorLoginPage = LoginPage.load(browser, deploymentUrl);
		authorLoginPage.loginAs(author.getLogin(), author.getPassword());
		
		//create a course
		String courseTitle = "Course-with-member-list-" + UUID.randomUUID();
		NavigationPage navBar = NavigationPage.load(browser);
		CoursePageFragment courseRuntime = navBar
			.openAuthoringEnvironment()
			.createCourse(courseTitle)
			.clickToolbarBack();
		
		//add coaches as course member
		MembersPage members = courseRuntime
			.members();
		members
			.addMember()
			.importList()
			.setMembers(ryomou, kanu)
			.nextUsers()
			.nextOverview()
			.selectRepositoryEntryRole(false, true, false)
			.nextPermissions()
			.finish();
		//add participatns
		members
			.addMember()
			.importList()
			.setMembers(ryomou, student1, student2)
			.nextUsers()
			.nextOverview()
			.selectRepositoryEntryRole(false, false, true)
			.nextPermissions()
			.finish();
		// back to course
		members
			.clickToolbarBack();
		
		getSmtpServer().reset();// reset e-mails
		
		//create a course element of type Test with the test that we create above
		String nodeTitle = "Members 2";
		CourseEditorPageFragment courseEditor = CoursePageFragment.getCourse(browser)
			.edit();
		
		courseEditor
			.createNode("cmembers")
			.nodeTitle(nodeTitle);
		
		MemberListConfigurationPage memberListConfig = new MemberListConfigurationPage(browser);
		memberListConfig
				.selectSettings()
				.setCoaches(Boolean.TRUE)
				.setCourseCoachesOnly()
				.setParticipants(Boolean.TRUE)
				.save();
		
		courseEditor
			.selectRoot();
		
		STConfigurationPage stConfig = new STConfigurationPage(browser);
		stConfig
			.selectOverview()
			.setDisplay(DisplayType.peekview);
		
		 courseEditor
			.publish()
			.quickPublish(UserAccess.membersOnly);
		 courseRuntime = courseEditor
			.clickToolbarBack();
		MenuTreePageFragment menuTree = courseRuntime
			.tree();

		MemberListPage memberList = new MemberListPage(browser);
		// check peek view
		memberList
			.assertOnPeekview(1, 1)
			.assertOnPeekview(2, 2)
			.assertOnPeekview(3, 2);
		
		menuTree
			.selectWithTitle(nodeTitle);
		
		memberList
			.assertOnMembers()
			.assertOnOwner(author.getFirstName())
			.assertOnCoach(kanu.getFirstName())
			.assertOnCoach(ryomou.getFirstName())
			.assertOnParticipant(student1.getFirstName())
			.assertOnParticipant(student2.getFirstName())
			.assertOnNotParticipant(ryomou.getFirstName());
		
		// switch to the table view
		memberList
			.switchToTableView()
			.assertOnTableOwner(author.getFirstName())
			.assertOnTableCoach(kanu.getFirstName())
			.assertOnTableCoach(ryomou.getFirstName())
			.assertOnTableParticipant(student1.getFirstName())
			.assertOnTableParticipant(student2.getFirstName())
			.assertOnTableNotParticipant(ryomou.getFirstName());
		
		// send mail
		memberList
			.emailAll()
			.contactAllParticipants()
			.contactExternal("openolat@frentix.com")
			.contactSubject("Hello my friends")
			.send();
		
		List<SmtpMessage> messages = getSmtpServer().getReceivedEmails();
		Assert.assertEquals(1, messages.size());
	}
	
	
	/**
	 * An author create a course with a participant folder course
	 * element. It add a participant to the course and upload file
	 * in the return box of this participant.<br>
	 * The participant come in and open the course, see the file
	 * uploaded by the author in its return box and it uploads an
	 * image in its drop box. The author go the see the image.
	 * 
	 * @param authorLoginPage
	 * @param participantBrowser
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void courseWithParticipantFolder()
	throws IOException, URISyntaxException {
		WebDriver participantBrowser = getWebDriver(1);
		
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		UserVO participant = new UserRestClient(deploymentUrl).createRandomUser("Ryomou");
		LoginPage authorLoginPage = LoginPage.load(browser, deploymentUrl);
		authorLoginPage.loginAs(author.getLogin(), author.getPassword());
		
		//go to authoring
		NavigationPage navBar = NavigationPage.load(browser);
		AuthoringEnvPage authoringEnv = navBar
			.assertOnNavigationPage()
			.openAuthoringEnvironment();
		
		String title = "Course partilist " + UUID.randomUUID();
		//create course
		authoringEnv
			.openCreateDropDown()
			.clickCreate(ResourceType.course)
			.fillCreateCourseForm(title, false)
			.assertOnInfos()
			.clickToolbarBack();
		
		String participantFolderTitle = "ParticipantList";
		//open course editor
		CoursePageFragment course = CoursePageFragment.getCourse(browser);
		CourseEditorPageFragment editor = course
			.assertOnCoursePage()
			.assertOnTitle(title)
			.edit()
			.createNode("pf")
			.nodeTitle(participantFolderTitle);
		//publish
		editor
			.publish()
			.quickPublish(UserAccess.membersOnly);
		editor
			.clickToolbarBack();
		
		//add a participant
		MembersPage members = new CoursePageFragment(browser)
			.members();
		members
			.addMember()
			.searchMember(participant, true)
			.nextUsers()
			.nextOverview()
			.selectRepositoryEntryRole(false, false, true)
			.nextPermissions()
			.finish();
		members
			.clickToolbarBack();
		
		//go to the course element
		course
			.tree()
			.selectWithTitle(participantFolderTitle);
		// open the return box of the participant and upload a file
		URL coachImageUrl = JunitTestHelper.class.getResource("file_resources/IMG_1484.jpg");
		File coachImageFile = new File(coachImageUrl.toURI());
		ParticipantFolderPage folder = new ParticipantFolderPage(browser);
		folder
			.assertOnParticipantsList()
			.assertOnParticipant(participant.getFirstName())
			.openParticipantFolder(participant.getFirstName());
		FolderPage directory = folder
			.openReturnBox()
			.uploadFile(coachImageFile)
			.assertOnFile(coachImageFile.getName());
		
		// The participant come in
		LoginPage participantLoginPage = LoginPage.load(participantBrowser, deploymentUrl);
		participantLoginPage
			.loginAs(participant.getLogin(), participant.getPassword())
			.resume();
		
		// The participant find the course
		NavigationPage participantNavBar = NavigationPage.load(participantBrowser);
		participantNavBar
			.assertOnNavigationPage()
			.openMyCourses()
			.select(title);
		// And opens the participant folder
		CoursePageFragment participantCourse = CoursePageFragment.getCourse(participantBrowser);
		participantCourse
			.tree()
			.selectWithTitle(participantFolderTitle);
		ParticipantFolderPage participantFolder = new ParticipantFolderPage(participantBrowser);
		participantFolder
			.openReturnBox()
			.assertOnFile(coachImageFile.getName())
			.selectRootDirectory();
		// Participant upload a file in its drop box
		URL participantImageUrl = JunitTestHelper.class.getResource("file_resources/IMG_1484.jpg");
		File participantImageFile = new File(participantImageUrl.toURI());
		participantFolder
			.openDropBox()
			.uploadFile(participantImageFile)
			.assertOnFile(participantImageFile.getName());
		
		//Author check the image in the participant drop box
		directory
			.selectRootDirectory();
		folder.openDropBox()
			.assertOnFile(participantImageFile.getName());
	}
	

	/**
	 * An author creates a course with a document course element,
	 * upload a PDF in the editor, publish the course and go to
	 * the course element check if the document is there.
	 * 
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void courseWithDocument()
	throws IOException, URISyntaxException {
						
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();

		LoginPage loginPage = LoginPage.load(browser, deploymentUrl);
		loginPage.loginAs(author.getLogin(), author.getPassword());
		
		//create a course
		String courseTitle = "DocCourse" + UUID.randomUUID();
		NavigationPage navBar = NavigationPage.load(browser);
		navBar
			.openAuthoringEnvironment()
			.createCourse(courseTitle)
			.clickToolbarBack();
		
		//create a course element of type Test with the test that we create above
		String nodeTitle = "Document";
		CourseEditorPageFragment courseEditor = CoursePageFragment.getCourse(browser)
			.edit();
		courseEditor
			.createNode("document")
			.nodeTitle(nodeTitle);
		
		URL pdfDocumentUrl = JunitTestHelper.class.getResource("file_resources/handInTopic1.pdf");
		File pdfDocumentFile = new File(pdfDocumentUrl.toURI());
		
		DocumentConfigurationPage docConfig = new DocumentConfigurationPage(browser);
		docConfig
			.selectConfiguration()
			.uploadDocument(pdfDocumentFile);
		
		courseEditor
			.autoPublish();
		
		DocumentPage doc = new DocumentPage(browser);
		doc.assertDocumentLink(pdfDocumentFile.getName());
	}
	

	/**
	 * An author upload a PDF document as learn resource, after that she creates
	 * a course with a document course element, selects the PDF in the editor,
	 * publish the course and go to the course element see if the document is
	 * there.
	 * 
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void courseWithDocumentFromRepository()
	throws IOException, URISyntaxException {
						
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();

		LoginPage loginPage = LoginPage.load(browser, deploymentUrl);
		loginPage.loginAs(author.getLogin(), author.getPassword());
		
		URL pdfDocumentUrl = JunitTestHelper.class.getResource("file_resources/handInTopic1.pdf");
		File pdfDocumentFile = new File(pdfDocumentUrl.toURI());
		
		//create a course
		String docTitle = "PDF-" + UUID.randomUUID();
		NavigationPage navBar = NavigationPage.load(browser);
		navBar
			.openAuthoringEnvironment()
			.uploadResource(docTitle, pdfDocumentFile);

		//create a course
		String courseTitle = "PDFDoc-" + UUID.randomUUID();
		navBar
			.openAuthoringEnvironment()
			.createCourse(courseTitle)
			.clickToolbarBack();
		
		//create a course element of type Test with the test that we create above
		String nodeTitle = "PDF";
		CourseEditorPageFragment courseEditor = CoursePageFragment.getCourse(browser)
			.edit();
		courseEditor
			.createNode("document")
			.nodeTitle(nodeTitle);
		
		DocumentConfigurationPage docConfig = new DocumentConfigurationPage(browser);
		docConfig
			.selectConfiguration()
			.selectDocument(docTitle);
		
		courseEditor
			.autoPublish();
		
		DocumentPage doc = new DocumentPage(browser);
		doc.assertDocumentLink(pdfDocumentFile.getName());
	}


	/**
	 * An author creates a course with a forum, publish it, open a new thread.
	 * A first user come to see the thread. A second come via the peekview.
	 * The three make a reply at the same time. And they check that they see
	 * the replies, and the ones of the others.
	 * 
	 * @param loginPage
	 * @param kanuBrowser
	 * @param reiBrowser
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void courseWithForum_concurrent()
	throws IOException, URISyntaxException {
		WebDriver kanuBrowser = getWebDriver(1);
		WebDriver reiBrowser = getWebDriver(2);
		
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		UserVO kanu = new UserRestClient(deploymentUrl).createRandomUser("Kanu");
		UserVO rei = new UserRestClient(deploymentUrl).createRandomUser("Rei");
		LoginPage loginPage = LoginPage.load(browser, deploymentUrl);
		loginPage.loginAs(author.getLogin(), author.getPassword());
		
		//create a course
		String courseTitle = "Course FO " + UUID.randomUUID();
		NavigationPage navBar = NavigationPage.load(browser);
		navBar
			.openAuthoringEnvironment()
			.createCourse(courseTitle)
			.clickToolbarBack();
	
		//go the authoring environment to create a forum
		String foTitle = "FO - " + UUID.randomUUID();
		CourseEditorPageFragment courseEditor = CoursePageFragment.getCourse(browser)
			.edit();
		
		// create forum
		courseEditor
			.createNode("fo")
			.nodeTitle(foTitle);
		
		// setup peekview
		courseEditor
			.selectRoot();
		STConfigurationPage stConfig = new STConfigurationPage(browser);
		stConfig
			.selectOverview()
			.setDisplay(DisplayType.peekview);
		
		// publish the course
		courseEditor
			.publish()
			.quickPublish(UserAccess.membersOnly);
	
		MembersPage membersPage = courseEditor		
			.clickToolbarBack()
			.members();
	
		membersPage
			.addMember()
			.importList()
			.setMembers(kanu, rei)
			.nextUsers()
			.nextOverview()
			.nextPermissions()
			.finish();
		
		//go to the forum
		courseEditor
			.clickToolbarBack()
			.tree()
			.selectWithTitle(foTitle.substring(0, 20));
		
		ForumPage authorForum = ForumPage
			.getCourseForumPage(browser);
		authorForum
			.createThread("The best anime ever", "What is the best anime ever?", null);
		
		//First user go to the course
		LoginPage kanuLoginPage = LoginPage.load(kanuBrowser, deploymentUrl);
		kanuLoginPage
			.loginAs(kanu.getLogin(), kanu.getPassword())
			.resume();

		NavigationPage kanuNavBar = NavigationPage.load(kanuBrowser);
		kanuNavBar
			.openMyCourses()
			.openSearch()
			.extendedSearch(courseTitle)
			.select(courseTitle);
		
		//go to the forum
		new CoursePageFragment(kanuBrowser)
			.tree()
			.selectWithTitle(foTitle.substring(0, 20))
			.assertWithTitleSelected(foTitle.substring(0, 20));
		
		ForumPage kanuForum = ForumPage
			.getCourseForumPage(kanuBrowser)
			.openThread("The best anime ever");

		//First user go to the course
		LoginPage reiLoginPage = LoginPage.load(reiBrowser, deploymentUrl);
		reiLoginPage
			.loginAs(rei)
			.resume();

		NavigationPage reiNavBar = NavigationPage.load(reiBrowser);
		reiNavBar
			.openMyCourses()
			.openSearch()
			.extendedSearch(courseTitle)
			.select(courseTitle);
		
		//select the thread in peekview
		ForumPage reiForum = new ForumPage(reiBrowser)
			.openThreadInPeekview("The best anime ever");
		
		//concurrent reply
		String kanuReply = "Ikki Touzen";
		String reiReply = "Neon Genesis Evangelion";
		String authorReply = "Lain, serial experiment";
		
		authorForum
			.replyToMessageNoWait("The best anime ever", null, authorReply);
		reiForum
			.replyToMessageNoWait("The best anime ever", null, reiReply);
		kanuForum
			.replyToMessageNoWait("The best anime ever", null, kanuReply);
	
		//wait the responses
		OOGraphene.waitModalDialogDisappears(browser);
		OOGraphene.waitModalDialogDisappears(kanuBrowser);
		OOGraphene.waitModalDialogDisappears(reiBrowser);
		
		//check own responses
		authorForum.assertMessageBody(authorReply);
		kanuForum.assertMessageBody(kanuReply);
		reiForum.assertMessageBody(reiReply);

		//check others responses
		authorForum
			.flatView()
			.waitMessageBody(kanuReply);
		reiForum
			.flatView()
			.waitMessageBody(kanuReply);
		kanuForum
			.flatView()
			.waitMessageBody(reiReply);
	}
	

	/**
	 * An administrator create a category in catalog. It creates a new course
	 * with a forum open to guests. it publish the course in the
	 * catalog.<br>
	 * The guest find the course, create a new thread. The administrator reply
	 * to the message, the guest to its reply.<br>
	 * The administrator checks the last message in its new messages, click
	 * back, use the list of users to see the messages of the guest. It clicks
	 * back to the threads list and checks the thread has 3 messages.
	 * 
	 * @param loginPage
	 * @param guestBrowser
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void courseWithForum_guest()
	throws IOException, URISyntaxException {
		WebDriver guestBrowser = getWebDriver(1);

		LoginPage loginPage = LoginPage.load(browser, deploymentUrl);
		loginPage
			.loginAs("administrator", "openolat")
			.resume();
		
		String node1 = "Forums " + UUID.randomUUID();
		String node1Short = "Forum " + JunitTestHelper.miniRandom();
		NavigationPage navBar = NavigationPage.load(browser);
		navBar
			.openCatalogAdministration()
			.addCatalogNode(node1, node1Short, "First level of the catalog");
		
		//create a course
		String courseTitle = "Guest FO " + UUID.randomUUID();
		navBar
			.openAuthoringEnvironment()
			.createCourse(courseTitle)
			.clickToolbarBack();
		
		//go the authoring environment to create a forum
		String foTitle = "GFO - " + UUID.randomUUID();
		CourseEditorPageFragment courseEditor = CoursePageFragment.getCourse(browser)
			.edit();
		courseEditor
			.createNode("fo")
			.nodeTitle(foTitle);
		//configure for guest
		ForumCEPage forumConfig = new ForumCEPage(browser);
		forumConfig
			.selectConfiguration()
			.allowGuest();
		
		//publish the course
		courseEditor
			.publish()
			.nextSelectNodes()
			.selectAccess(UserAccess.guest)
			.nextAccess()
			.selectCatalog(true)
			.selectCategory(null, node1)
			.nextCatalog() // -> no problem found
			.finish();
		//back in course
		courseEditor.clickToolbarBack();
		
		// guest go to the catalog and find the course
		LoginPage guestLogin = LoginPage.load(guestBrowser, deploymentUrl);
		guestLogin
			.asGuest();

		NavigationPage guestNavBar = NavigationPage.load(guestBrowser);
		guestNavBar
			.openCatalog()
			.selectCatalogEntry(node1, node1Short)
			.select(courseTitle)
			.start();
		
		//go to the forum
		new CoursePageFragment(guestBrowser)
			.tree()
			.selectWithTitle(foTitle.substring(0, 20));
		
		String guestAlias = "Guest-" + UUID.randomUUID();
		ForumPage guestForum = ForumPage
			.getCourseForumPage(guestBrowser)
			.createThread("Your favorite author", "Name your favorite author", guestAlias);
	
		// admin go to the forum
		new CoursePageFragment(browser)
			.tree()
			.selectWithTitle(foTitle.substring(0, 20));
		//admin reply to the thread of guest
		ForumPage adminForum = ForumPage
			.getCourseForumPage(browser)
			.openThread("Your favorite author")
			.assertOnGuestPseudonym(guestAlias)
			.newMessages()
			.assertOnGuestPseudonym(guestAlias)
			.replyToMessage("Your favorite author", "Huxley is my favorite author", "My favorite author is Huxley");
		
		//guest refresh the view and reply to admin
		guestForum
			.flatView()
			.assertMessageBody("Huxley")
			.replyToMessage("Huxley is my favorite author", " I prefer Orwell", "Orwell is my favorite author");

		//admin see its new messages, see the list of users, select the guest and its messages
		OOGraphene.waitingALittleLonger();//JMS message need to be delivered
		adminForum
			.newMessages()
			.assertMessageBody("Orwell")
			.clickBack()
			.userFilter()
			.selectFilteredUser(guestAlias)
			.assertMessageBody("Orwell")
			.clickBack()
			.clickBack()
			.assertThreadListOnNumber("Your favorite author", 3);
	}

	/**
	 * An author setup a course with a LTI course element with score enabled.
	 * A participant take the course and see the LTI content. The back channel
	 * need the url of the OpenOLAT instance which is currently difficult
	 * for a selenium test. The grading is not tested until a LTI server
	 * can be installed on localhost.
	 * 
	 * @param authorLoginPage
	 * @param participantBrowser
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void courseWithLTI()
	throws IOException, URISyntaxException {
		
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		UserVO participant = new UserRestClient(deploymentUrl).createRandomUser("Ryomou");

		LoginPage authorLoginPage = LoginPage.load(browser, deploymentUrl);
		authorLoginPage.loginAs(author.getLogin(), author.getPassword());
		
		//create a course
		String courseTitle = "Course-LTI-" + UUID.randomUUID();
		NavigationPage navBar = NavigationPage.load(browser);
		navBar
			.openAuthoringEnvironment()
			.createCourse(courseTitle)
			.clickToolbarBack();

		//create a course element of type Test with the test that we create above
		String ltiTitle = "LTI-Node";
		CourseEditorPageFragment courseEditor = CoursePageFragment.getCourse(browser)
			.edit()
			.createNode("lti")
			.nodeTitle(ltiTitle);
		
		//configure assessment
		LTIConfigurationPage ltiConfig = new LTIConfigurationPage(browser);
		ltiConfig
			.selectConfiguration()
			.setLtiPage("http://lti.frentix.com/tool.php", "123456", "secret")
			.enableScore(10.0d, 5.0d)
			.save();
		//set the score / passed calculation in root node and publish
		courseEditor
			.selectRoot()
			.selectTabScore()
			.enableRootScoreByNodes()
			.autoPublish()
			.settings()
			.accessConfiguration()
			.setAccessToRegisteredUser();
		
		//go to members management
		CoursePageFragment courseRuntime = courseEditor.clickToolbarBack();
		courseRuntime
			.publish();
		MembersPage members = courseRuntime
			.members();
		members
			.addMember()
			.searchMember(participant, true)
			.nextUsers()
			.nextOverview()
			.nextPermissions()
			.finish();
		
		//Participant login
		LoginPage participantLoginPage = LoginPage.load(browser, deploymentUrl);
		participantLoginPage
			.loginAs(participant.getLogin(), participant.getPassword())
			.resume();

		NavigationPage participantNavBar = NavigationPage.load(browser);
		participantNavBar
			.openMyCourses()
			.openSearch()
			.extendedSearch(courseTitle)
			.select(courseTitle);
		
		CoursePageFragment participantCourse = new CoursePageFragment(browser);
		participantCourse
			.tree()
			.selectWithTitle(ltiTitle);
		LTIPage lti = new LTIPage(browser);
		lti
			.start()
			.outcomeToolProvider();
			//.sendGrade(0.8d);
	}
	
	/**
	 * An author create a course with a course element
	 * to contact all members of the course. It add some
	 * participants. A participant log in, go to the
	 * course to use the contact form and send an E-mail.
	 * 
	 * @param loginPage The login page
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void courseWithContact()
	throws IOException, URISyntaxException {
						
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		UserVO ryomou = new UserRestClient(deploymentUrl).createRandomUser("ryomou");
		UserVO student = new UserRestClient(deploymentUrl).createRandomUser("student");

		LoginPage loginPage = LoginPage.load(browser, deploymentUrl);
		loginPage.loginAs(author.getLogin(), author.getPassword());
		
		//create a course
		String courseTitle = "Contact Course" + UUID.randomUUID();
		NavigationPage navBar = NavigationPage.load(browser);
		CoursePageFragment courseRuntime = navBar
			.openAuthoringEnvironment()
			.createCourse(courseTitle)
			.clickToolbarBack();
		
		//add participants
		MembersPage members = courseRuntime
			.members();
		members
			.addMember()
			.importList()
			.setMembers(ryomou, student)
			.nextUsers()
			.nextOverview()
			.selectRepositoryEntryRole(false, false, true)
			.nextPermissions()
			.finish();
		// back to course
		members
			.clickToolbarBack();
		
		getSmtpServer().reset();// reset e-mails
		
		//create a course element of type Test with the test that we create above
		String nodeTitle = "ContactNode";
		CourseEditorPageFragment courseEditor = CoursePageFragment.getCourse(browser)
			.edit();
		courseEditor
			.createNode("co")
			.nodeTitle(nodeTitle);
		
		ContactConfigPage contactConfig = new ContactConfigPage(browser);
		contactConfig
				.selectConfiguration()
				.wantAllOwners()
				.wantAllCoaches()
				.wantAllParticipants()
				.save();
		
		courseEditor
			.autoPublish()
			.publish()
			.settings()
			.accessConfiguration()
			.setAccessToMembersOnly()
			.save()
			.clickToolbarBack();
		
		
		//log out
		new UserToolsPage(browser)
			.logout();
		
		// participant comes in
		loginPage.loginAs(ryomou.getLogin(), ryomou.getPassword());


		NavigationPage ryomouNavBar = NavigationPage.load(browser);
		ryomouNavBar
			.openMyCourses()
			.select(courseTitle);
		
		CoursePageFragment course = new CoursePageFragment(browser);
		course
			.tree()
			.selectWithTitle(nodeTitle);

		ContactPage contactPage = new ContactPage(browser);
		// check peek view
		contactPage
			.setContent("Hello", "Hello, are you fine?")
			.send()
			.assertSend();
		
		List<SmtpMessage> messages = getSmtpServer().getReceivedEmails();
		Assert.assertEquals(1, messages.size());
	}
	
	/**
	 * An author creates a course with a single page course element,
	 * create the HTML page with the default button, publish the
	 * course and go to the page, and edit it.
	 * 
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void courseWithSinglePage()
	throws IOException, URISyntaxException {
						
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();

		LoginPage loginPage = LoginPage.load(browser, deploymentUrl);
		loginPage.loginAs(author.getLogin(), author.getPassword());
		
		//create a course
		String courseTitle = "Single Course" + UUID.randomUUID();
		NavigationPage navBar = NavigationPage.load(browser);
		navBar
			.openAuthoringEnvironment()
			.createCourse(courseTitle)
			.clickToolbarBack();
		
		//create a course element of type Test with the test that we create above
		String nodeTitle = "SinglePage";
		CourseEditorPageFragment courseEditor = CoursePageFragment.getCourse(browser)
			.edit();
		courseEditor
			.createNode("sp")
			.nodeTitle(nodeTitle);

		String content = "A new single page with some content";
		SinglePageConfigurationPage spConfiguration = new SinglePageConfigurationPage(browser);
		spConfiguration
			.selectConfiguration()
			.createEditPage("sp.html", content)
			.assertOnPreview();
		
		CoursePageFragment courseRuntime = courseEditor
			.autoPublish();
		
		courseRuntime
			.clickTree()
			.selectWithTitle(nodeTitle);
		
		SinglePage singlePage = new SinglePage(browser);
		singlePage
			.assertInPage(content);
		
		String newContent = "Newer content in a single page for you";
		singlePage
			.edit(newContent)
			.assertInPage(newContent);
	}
	
	
	/**
	 * An author creates a course with a single page course element,
	 * upload a PDF, publish the course and go to the page to check
	 * if the file is available.
	 * 
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void courseWithSinglePageWithPDF()
	throws IOException, URISyntaxException {
						
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();

		LoginPage loginPage = LoginPage.load(browser, deploymentUrl);
		loginPage.loginAs(author.getLogin(), author.getPassword());
		
		//create a course
		String courseTitle = "Single PDF Course" + UUID.randomUUID();
		NavigationPage navBar = NavigationPage.load(browser);
		navBar
			.openAuthoringEnvironment()
			.createCourse(courseTitle)
			.clickToolbarBack();
		
		//create a course element of type Test with the test that we create above
		String nodeTitle = "SinglePDF";
		CourseEditorPageFragment courseEditor = CoursePageFragment.getCourse(browser)
			.edit();
		courseEditor
			.createNode("sp")
			.nodeTitle(nodeTitle);
		
		URL pdfUrl = JunitTestHelper.class.getResource("file_resources/handInTopic1.pdf");
		File pdfFile = new File(pdfUrl.toURI());

		SinglePageConfigurationPage spConfiguration = new SinglePageConfigurationPage(browser);
		spConfiguration
			.selectConfiguration()
			.uploadFile(pdfFile)
			.assertOnPreview();
		
		CoursePageFragment courseRuntime = courseEditor
			.autoPublish();
		
		courseRuntime
			.clickTree()
			.selectWithTitle(nodeTitle);
		
		SinglePage singlePage = new SinglePage(browser);
		singlePage
			.assertInFile("handInTopic1.pdf");	
	}
	
	/**
	 * An author creates a course with a single page course element,
	 * create the HTML page in the course editor with the default button,
	 * add an image and publish the course and go to the page to verify
	 * if the content and the image are there.
	 * 
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void courseEditSinglePage()
	throws IOException, URISyntaxException {
						
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();

		LoginPage loginPage = LoginPage.load(browser, deploymentUrl);
		loginPage.loginAs(author.getLogin(), author.getPassword());
		
		//create a course
		String courseTitle = "Single Course" + UUID.randomUUID();
		NavigationPage navBar = NavigationPage.load(browser);
		navBar
			.openAuthoringEnvironment()
			.createCourse(courseTitle)
			.clickToolbarBack();
		
		//create a course element of type Test with the test that we create above
		String nodeTitle = "EditableSinglePage";
		CourseEditorPageFragment courseEditor = CoursePageFragment.getCourse(browser)
			.edit();
		courseEditor
			.createNode("sp")
			.nodeTitle(nodeTitle);

		String content = "A new single page with some content";
		URL imageUrl = JunitTestHelper.class.getResource("file_resources/IMG_1483.png");
		File imageFile = new File(imageUrl.toURI());
		
		SinglePageConfigurationPage spConfiguration = new SinglePageConfigurationPage(browser);
		spConfiguration
			.selectConfiguration()
			.createEditPage("nsp.html", "")
			.editPage()
			.setContent(content)
			.uploadImage(imageFile)
			.saveContent();
		
		spConfiguration
			.assertOnPreview();
		
		CoursePageFragment courseRuntime = courseEditor
			.autoPublish();
		
		courseRuntime
			.tree()
			.selectWithTitle(nodeTitle);
		
		SinglePage singlePage = new SinglePage(browser);
		singlePage
			.assertInPage(content)
			.assertImageInPage(imageFile.getName());
	}
	

	/**
	 * An author creates a survey with a multiple choice
	 * and a single choice. He uses it in a course. A
	 * participant of the course participates to the
	 * survey.
	 * 
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void courseWithSurvey()
	throws IOException, URISyntaxException {
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		UserVO user = new UserRestClient(deploymentUrl).createRandomUser("Maximilien");
		LoginPage authorLoginPage = LoginPage.load(browser, deploymentUrl);
		authorLoginPage.loginAs(author.getLogin(), author.getPassword());
		
		//create a survey
		String surveyTitle = "Survey-1-" + UUID.randomUUID();
		NavigationPage navBar = NavigationPage.load(browser);
		navBar
			.openAuthoringEnvironment()
			.createSurvey(surveyTitle)
			.assertOnInfos()
			.clickToolbarBack();
		
		SurveyPage survey = SurveyPage
			.loadPage(browser);
		SurveyEditorPage surveyEditor = survey
			.edit();
		surveyEditor
			.addLayout(ContainerLayout.block_3rows)
			.openElementsChooser(1, 1)
			.addTitle("My survey")
			.setTitleSize(1)
			.closeEditFragment()
			.assertOnTitle("My survey", 1);
		
		surveyEditor
			.openElementsChooser(1, 2)
			.addMultipleChoiceElement()
			.addMultipleChoice("Jupiter", 2)
			.addMultipleChoice("Saturn", 3)
			.closeEditFragment();
		
		surveyEditor
			.openElementsChooser(1, 3)
			.addSingleChoiceElement()
			.addSingleChoice("Mercury", 2)
			.addSingleChoice("Venus", 3)
			.closeEditFragment();
		
		surveyEditor
			.close();

		//create a course
		String courseTitle = "Course-With-Survey-" + UUID.randomUUID().toString();
		navBar
			.openAuthoringEnvironment()
			.createCourse(courseTitle)
			.clickToolbarBack();
		
		navBar.openCourse(courseTitle);
		
		String surveyNodeTitle = "SurveyNode-1";
		//create a course element of type CP with the CP that we create above
		CourseEditorPageFragment courseEditor = CoursePageFragment.getCourse(browser)
			.edit();
		courseEditor
			.createNode("survey")
			.nodeTitle(surveyNodeTitle)
			.selectTabSurveyContent()
			.chooseSurvey(surveyTitle);

		//publish the course
		courseEditor
			.publish()
			.quickPublish(UserAccess.membersOnly);
		
		MembersPage membersPage = courseEditor
			.clickToolbarBack()
			.members();
			
		membersPage
			.addMember()
			.importList()
			.setMembers(user)
			.nextUsers()
			.nextOverview()
			.nextPermissions()
			.finish();
		
		//open the course and see the survey
		CoursePageFragment course = courseEditor
			.clickToolbarBack();
		course
			.clickTree()
			.selectWithTitle(surveyNodeTitle);
		
		LoginPage userLoginPage = LoginPage.load(browser, deploymentUrl);
		userLoginPage
			.loginAs(user.getLogin(), user.getPassword())
			.resume();
		
		//open the course
		NavigationPage userNavBar = NavigationPage.load(browser);
		userNavBar
			.openMyCourses()
			.select(courseTitle);
		
		//go to the group task
		CoursePageFragment userCourse = new CoursePageFragment(browser);
		userCourse
			.clickTree()
			.selectWithTitle(surveyNodeTitle);
		
		SurveyPage userSurvey = SurveyPage.loadPage(browser)
			.assertOnSurvey();
		
		userSurvey
			.answerMultipleChoice("Saturn")
			.answerSingleChoice("Venus")
			.saveAndCloseSurvey()
			.assertOnSurveyClosed();
	}
	
	
	/**
	 * An author creates a course with a course element of type BigBlueButton,
	 * add a meeting in the edit list, go the meetings list and goes to the page
	 * dedicated to join the meeting.
	 * 
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void courseWithBigBlueButton()
	throws IOException, URISyntaxException {

		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		LoginPage loginPage = LoginPage.load(browser, deploymentUrl);
		loginPage.loginAs(author.getLogin(), author.getPassword());
		
		//create a course
		String courseTitle = "Course-With-BBB-" + UUID.randomUUID();
		NavigationPage navBar = NavigationPage.load(browser);
		navBar
			.openAuthoringEnvironment()
			.createCourse(courseTitle, true)
			.clickToolbarBack();
		
		String nodeTitle = "BBB Node-1";
		//create a course element of type CP with the CP that we create above
		CoursePageFragment course = CoursePageFragment.getCourse(browser);
		CourseEditorPageFragment courseEditor = course
			.edit();
		courseEditor
			.createNode("bigbluebutton")
			.nodeTitle(nodeTitle);
		
		//publish the course
		courseEditor
			.publish()
			.quickPublish(UserAccess.membersOnly);
		courseEditor
			.clickToolbarBack();
		
		course
			.tree()
			.selectWithTitle(nodeTitle);
		
		String meetingName = "Quick meeting";
		BigBlueButtonPage bigBlueButton = new BigBlueButtonPage(browser);
		bigBlueButton
			.assertOnRuntime()
			.selectEditMeetingsList()
			.addSingleMeeting(meetingName, "Classroom")
			.assertOnList(meetingName)
			.selectMeetingsList()
			.assertOnList(meetingName)
			.selectMeeting(meetingName)
			.assertOnMeeting(meetingName)
			.assertOnJoin();
	}
	

	/**
	 * First an administrator enables a template with external URL.<br>
	 * Than an author creates a course with a course element of type BigBlueButton,
	 * add a meeting in the edit list, go the meetings list and select the meeting
	 * where she picks the external URL. Log out, navigate to the external
	 * URL, check the login button and use it.
	 * 
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void courseWithBigBlueButtonGuestLogin()
	throws IOException, URISyntaxException {
		// open meeting for guest
		LoginPage adminLoginPage = LoginPage.load(browser, deploymentUrl);
		adminLoginPage
			.loginAs("administrator", "openolat")
			.resume();
		
		NavigationPage.load(browser)
			.openAdministration()
			.openBigBlueButtonSettings()
			.selectTemplates()
			.editTemplate("Interview")
			.enableTemplate()
			.enableGuestLink()
			.save();

		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		LoginPage loginPage = LoginPage.load(browser, deploymentUrl);
		loginPage.loginAs(author.getLogin(), author.getPassword());
		
		//create a course
		String courseTitle = "Course-BBB-" + UUID.randomUUID();
		NavigationPage navBar = NavigationPage.load(browser);
		navBar
			.openAuthoringEnvironment()
			.createCourse(courseTitle, true)
			.clickToolbarBack();
		
		String nodeTitle = "BBB Guest-2";
		//create a course element of type CP with the CP that we create above
		CoursePageFragment course = CoursePageFragment.getCourse(browser);
		CourseEditorPageFragment courseEditor = course
			.edit();
		courseEditor
			.createNode("bigbluebutton")
			.nodeTitle(nodeTitle);
		
		//publish the course
		courseEditor
			.publish()
			.quickPublish(UserAccess.membersOnly);
		courseEditor
			.clickToolbarBack();
		
		course
			.tree()
			.selectWithTitle(nodeTitle);
		
		String meetingName = "Quick meeting";
		BigBlueButtonPage bigBlueButton = new BigBlueButtonPage(browser);
		bigBlueButton
			.assertOnRuntime()
			.selectEditMeetingsList()
			.addSingleMeeting()
			.editMeeting(meetingName, "Interview")
			.assertEditMeetingExternalUrl()
			.saveMeeting()
			.assertOnList(meetingName)
			.selectMeetingsList()
			.assertOnList(meetingName)
			.selectMeeting(meetingName)
			.assertOnMeeting(meetingName);
		
		String externalUrl = bigBlueButton.getExternalUrl();
		
		// log out
		new UserToolsPage(browser)
			.logout();
		// return to login
		LoginPage.load(browser, deploymentUrl)
			.assertOnLoginPage();
		
		browser.navigate().to(externalUrl);
		
		bigBlueButton = new BigBlueButtonPage(browser);
		bigBlueButton
			.assertOnWaitGuestMeeting()
			.loginToGuestJoin(author)
			.assertOnGuestJoinMeetingActive();
	}
	
	
	/**
	 * An author creates a course with a course element of type BigBlueButton,
	 * add 4 meetings planned for next month in the edit list. Than it goes the meetings list
	 * and goes to the page dedicated to join the meeting.
	 * 
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void courseWithBigBlueButtonMultipleMeetings()
	throws IOException, URISyntaxException {

		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		LoginPage loginPage = LoginPage.load(browser, deploymentUrl);
		loginPage.loginAs(author.getLogin(), author.getPassword());
		
		//create a course
		String courseTitle = "Course-With-BBB-" + UUID.randomUUID();
		NavigationPage navBar = NavigationPage.load(browser);
		navBar
			.openAuthoringEnvironment()
			.createCourse(courseTitle, true)
			.clickToolbarBack();
		
		String nodeTitle = "BBB Node-2";
		//create a course element of type CP with the CP that we create above
		CoursePageFragment course = CoursePageFragment.getCourse(browser);
		CourseEditorPageFragment courseEditor = course
			.edit();
		courseEditor
			.createNode("bigbluebutton")
			.nodeTitle(nodeTitle);
		
		//publish the course
		courseEditor
			.autoPublish();
		
		course
			.tree()
			.selectWithTitle(nodeTitle);
		
		String meetingName = "Recurring meeting";
		BigBlueButtonPage bigBlueButton = new BigBlueButtonPage(browser);
		bigBlueButton
			.assertOnRuntime()
			.selectEditMeetingsList()
			.addMultipleDailyMeetings(meetingName, 5, 10, "Classroom")
			.nextToDatesList()
			.assertOnDatesList(3, 5)
			.finishRecurringMeetings()
			.assertOnList(meetingName, 3, 5)
			.selectMeetingsList()
			.selectMeeting(meetingName, 1)
			.assertOnMeeting(meetingName);
	}
	
	
	/**
	 * An author creates a course with a course element of type Microsoft Teams,
	 * add a meeting in the edit list, go the meetings list and goes to the page
	 * dedicated to the meeting. The selenium works without teams to be configured
	 * and wait for the configuration errors.
	 * 
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void courseWithTeams()
	throws IOException, URISyntaxException {

		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		LoginPage loginPage = LoginPage.load(browser, deploymentUrl);
		loginPage.loginAs(author.getLogin(), author.getPassword());
		
		//create a course
		String courseTitle = "Teams-" + UUID.randomUUID();
		NavigationPage navBar = NavigationPage.load(browser);
		navBar
			.openAuthoringEnvironment()
			.createCourse(courseTitle, true)
			.clickToolbarBack();
		
		String nodeTitle = "Teams-1";
		//create a course element of type CP with the CP that we create above
		CoursePageFragment course = CoursePageFragment.getCourse(browser);
		CourseEditorPageFragment courseEditor = course
			.edit();
		courseEditor
			.createNode("msteams")
			.nodeTitle(nodeTitle);
		
		//publish the course
		courseEditor
			.publish()
			.quickPublish(UserAccess.membersOnly);
		courseEditor
			.clickToolbarBack();
		
		course
			.clickTree()
			.selectWithTitle(nodeTitle);
		
		String meetingName = "Teams meeting";
		TeamsPage teams = new TeamsPage(browser);
		teams
			.assertOnRuntime()
			.selectEditMeetingsList()
			.addSingleMeeting(meetingName)
			.assertOnList(meetingName)
			.selectMeetingsList()
			.assertOnList(meetingName)
			.selectMeeting(meetingName)
			.assertOnMeeting(meetingName);
		
		// Teams is not configured, errors
		OOGraphene.closeErrorBox(browser);
		
		teams
			.assertOnJoinDisabled();
	}
	
	

	/**
	 * An author creates a course with a course element of type Teams, add a
	 * serie of weekly meetings in the edit list, then goes in the meetings list
	 * and goes to the page dedicated to join the meeting.
	 * 
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void courseWithTeamsWeeklyMeetings()
	throws IOException, URISyntaxException {

		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		LoginPage loginPage = LoginPage.load(browser, deploymentUrl);
		loginPage.loginAs(author.getLogin(), author.getPassword());
		
		//create a course
		String courseTitle = "Teams-2-" + UUID.randomUUID();
		NavigationPage navBar = NavigationPage.load(browser);
		navBar
			.openAuthoringEnvironment()
			.createCourse(courseTitle, true)
			.clickToolbarBack();
		
		String nodeTitle = "Teams-2";
		//create a course element of type CP with the CP that we create above
		CoursePageFragment course = CoursePageFragment.getCourse(browser);
		CourseEditorPageFragment courseEditor = course
			.edit();
		courseEditor
			.createNode("msteams")
			.nodeTitle(nodeTitle);
		
		//publish the course
		courseEditor
			.autoPublish();
		
		course
			.clickTree()
			.selectWithTitle(nodeTitle);
		

		String meetingName = "Teams meeting";
		TeamsPage teams = new TeamsPage(browser);
		teams
			.assertOnRuntime()
			.selectEditMeetingsList()
			.addMultipleWeeklyMeetings(meetingName)
			.nextToDatesList()
			.assertOnDatesList(5)
			.finishRecurringMeetings()
			.assertOnList(meetingName, 4)
			.selectMeetingsList()
			.selectMeeting(meetingName)
			.assertOnMeeting(meetingName);
		
		// Teams is not configured, errors
		OOGraphene.closeErrorBox(browser);
		
		teams
			.assertOnJoinDisabled();
	}
	

	/**
	 * The test doesn't test really Zoom itself. It enables LTI 1.3 and Zoom,
	 * create a dummy Zoom profile, create a course with a Zoom course element
	 * with the above created profile and check that the panel appears but
	 * Zoom will not accept the fake profile.
	 * 
	 */
	@Test
	public void courseWithZoom() {
		String profile = UUID.randomUUID().toString();
		
		// configure the lectures module
		LoginPage loginPage = LoginPage.load(browser, deploymentUrl);
		loginPage
			.loginAs("administrator", "openolat")
			.resume();
		NavigationPage navBar = NavigationPage.load(browser);
		AdministrationPage administration = navBar
			.openAdministration();
		administration
			.openLti13Settings()
			.enableLTI()
			.saveConfiguration();
		administration
			.openZoomSettings(false)
			.enableZoom()
			.addProfile(profile, "key-" + profile)
			.saveConfiguration();
		 
		 OOGraphene.scrollTop(browser);
		 
		 String courseTitle = "Course with Zoom " + UUID.randomUUID().toString();
		 navBar
		 	.openAuthoringEnvironment()
		 	.createCourse(courseTitle, true)
		 	.assertOnInfos();
		
		String nodeTitle = "Zoom meeting";
		CoursePageFragment course = new CoursePageFragment(browser);
		CourseEditorPageFragment courseEditor = course
			.edit();
		courseEditor
			.createNode("zoom")
			.nodeTitle(nodeTitle);
		
		ZoomConfigurationPage zoomConfiguration = new ZoomConfigurationPage(browser);
		zoomConfiguration
			.selectConfiguration()
			.selectProfile(profile)
			.saveConfiguration();
		
		//publish the course
		courseEditor
			.publish()
			.quickPublish();
		
		course = courseEditor
			.clickToolbarBack();
		
		ZoomPage zoom = new ZoomPage(browser);
		zoom
			.assertOnZoomPanel();
	}
	

	/**
	 * An author creates a course with an appointment course element, add a topic,
	 * add herself to the appointment and confirm the event.
	 * 
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void courseWithAppointment()
	throws IOException, URISyntaxException {
						
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();

		LoginPage loginPage = LoginPage.load(browser, deploymentUrl);
		loginPage.loginAs(author.getLogin(), author.getPassword());
		
		//create a course
		String courseTitle = "App-" + UUID.randomUUID();
		NavigationPage navBar = NavigationPage.load(browser);
		navBar
			.openAuthoringEnvironment()
			.createCourse(courseTitle, true)
			.clickToolbarBack();
		
		//create a course element of type appointment
		String nodeTitle = "App-Week";
		CourseEditorPageFragment courseEditor = CoursePageFragment.getCourse(browser)
			.edit();
		CoursePageFragment course = courseEditor
			.createNode("appointments")
			.nodeTitle(nodeTitle)
			.autoPublish();
		
		course
			.clickTree()
			.assertWithTitle(nodeTitle);
		
		String topicTitle = "Author topic";
		Date topicDate = new Date();
		int day = AppointmentPage.getDay(topicDate);
		
		AppointmentPage appointment = new AppointmentPage(browser);
		appointment
			.addTopic(topicTitle)
			.saveTopic()
			.assertOnTopic(topicTitle)
			.addUser(day)
			.searchUserByFirstName(author)
			.selectAll()
			.choose();
		
		appointment
			.assertOnConfirmAppointmentByDay(day)
			.confirmAppointmentByDay(day);
	}
	

	/**
	 * An author creates a course with an appointment course element, add a topic
	 * with multiple appointment, add herself to the appointment and confirm the event.
	 * The course has a second participant which selects an appointment, the author
	 * confirms it, and the participant checks the confirmation of the appointment.
	 * 
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void courseWithAppointmentRecurring()
	throws IOException, URISyntaxException {
		WebDriver participantBrowser = getWebDriver(1);
		
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		UserVO participant = new UserRestClient(deploymentUrl).createRandomUser("Alfred");

		LoginPage loginPage = LoginPage.load(browser, deploymentUrl);
		loginPage.loginAs(author.getLogin(), author.getPassword());
		
		//create a course
		String courseTitle = "App-" + UUID.randomUUID();
		NavigationPage navBar = NavigationPage.load(browser);
		navBar
			.openAuthoringEnvironment()
			.createCourse(courseTitle, true)
			.clickToolbarBack();
		
		//create a course element of type appointment
		String nodeTitle = "App-Week";
		CourseEditorPageFragment courseEditor = CoursePageFragment.getCourse(browser)
			.edit();
		CoursePageFragment course = courseEditor
			.createNode("appointments")
			.nodeTitle(nodeTitle)
			.autoPublish();
		course
			.publish()
			.settings()
			.accessConfiguration()
			.setAccessToMembersOnly()
			.save()
			.clickToolbarBack();
		
		//add participant
		MembersPage members = course
			.members();
		members
			.addMember()
			.importList()
			.setMembers(participant)
			.nextUsers()
			.nextOverview()
			.selectRepositoryEntryRole(false, false, true)
			.nextPermissions()
			.finish();
		// back to course
		members
			.clickToolbarBack();
		
		course
			.clickTree()
			.assertWithTitle(nodeTitle);
		
		String topicTitle = "Multi topic";
		
		AppointmentPage appointment = new AppointmentPage(browser);
		appointment
			.addTopic(topicTitle)
			.setRecurringTopic(1, 28, 13, 14, DayOfWeek.MONDAY)
			.saveTopic()
			.assertOnTopicMultipleMeetings(topicTitle, 3)
			.addUserToAppointment(1)
			.searchUserByFirstName(author)
			.selectAll()
			.choose();
		
		appointment
			.assertOnConfirmAppointmentByPosition(1)
			.confirmAppointmentByPosition(1);

		
		// participant comes in book an appointment
		LoginPage.load(participantBrowser, deploymentUrl)
			.loginAs(participant.getLogin(), participant.getPassword());

		NavigationPage participantNavBar = NavigationPage.load(participantBrowser);
		participantNavBar
			.openMyCourses()
			.select(courseTitle);
		
		CoursePageFragment participantCourse = new CoursePageFragment(participantBrowser);
		participantCourse
			.tree()
			.selectWithTitle(nodeTitle);
		
		AppointmentPage participantAppointment = new AppointmentPage(participantBrowser);
		participantAppointment
			.assertOnTopicMultipleMeetings(topicTitle, 3)
			.assertOnSelectAppointmentByPosition(1)
			.selectAppointmentByPosition(1);
		
		// author confirm the participant's appointment
		course
			.clickTreeNode(nodeTitle)
			.assertWithTitle(nodeTitle);
		
		appointment
			.selectTopicAsCoach(topicTitle)
			.assertOnPlannedAppointmentByPosition(2)
			.confirmPlannedAppointmentByPosition(2)
			.assertOnConfirmedAppointmentByPosition(2);
		
		// participant check the confirmation
		participantCourse
			.clickTree();
		
		participantAppointment
			.selectTopicAsParticipant(topicTitle)
			.assertOnConfirmedAppointmentByPosition(1);
	}
	

	/**
	 * An author creates a course with an appointment course element, add a topic
	 * to find an hour to meet. The course has a second participant which selects
	 * the proposed appointment, the author confirms it, and the participant checks
	 * the confirmation of the appointment.
	 * 
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void courseWithAppointmentFinding()
	throws IOException, URISyntaxException {
		WebDriver participantBrowser = getWebDriver(1);
		
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		UserVO participant = new UserRestClient(deploymentUrl).createRandomUser("Alfred");

		LoginPage loginPage = LoginPage.load(browser, deploymentUrl);
		loginPage.loginAs(author.getLogin(), author.getPassword());
		
		//create a course
		String courseTitle = "App-" + UUID.randomUUID();
		NavigationPage navBar = NavigationPage.load(browser);
		navBar
			.openAuthoringEnvironment()
			.createCourse(courseTitle, true)
			.clickToolbarBack();
		
		//create a course element of type appointment
		String nodeTitle = "App-Finding";
		CourseEditorPageFragment courseEditor = CoursePageFragment.getCourse(browser)
			.edit();
		CoursePageFragment course = courseEditor
			.createNode("appointments")
			.nodeTitle(nodeTitle)
			.autoPublish();
		
		course
			.publish()
			.settings()
			.accessConfiguration()
			.setAccessToMembersOnly()
			.save()
			.clickToolbarBack();
		
		//add participant
		MembersPage members = course
			.members();
		members
			.addMember()
			.importList()
			.setMembers(participant)
			.nextUsers()
			.nextOverview()
			.selectRepositoryEntryRole(false, false, true)
			.nextPermissions()
			.finish();
		// back to course
		members
			.clickToolbarBack();
		
		course
			.clickTree()
			.assertWithTitle(nodeTitle);
		
		String topicTitle = "Find topic";
		int today = AppointmentPage.getDay(new Date());
		
		AppointmentPage appointment = new AppointmentPage(browser);
		appointment
			.addTopic(topicTitle)
			.setFinding()
			.saveTopic()
			.assertOnTopic(topicTitle);
		
		// participant comes in book an appointment
		LoginPage.load(participantBrowser, deploymentUrl)
			.loginAs(participant.getLogin(), participant.getPassword());

		NavigationPage participantNavBar = NavigationPage.load(participantBrowser);
		participantNavBar
			.openMyCourses()
			.select(courseTitle);
		
		CoursePageFragment participantCourse = new CoursePageFragment(participantBrowser);
		participantCourse
			.tree()
			.selectWithTitle(nodeTitle);
		
		AppointmentPage participantAppointment = new AppointmentPage(participantBrowser);
		participantAppointment
			.assertOnTopic(topicTitle)
			.selectAppointmentByDay(today);
		
		// author confirm the participant's appointment
		course
			.clickTree()
			.assertWithTitle(nodeTitle);
		
		appointment
			.selectTopicAsCoach(topicTitle)
			.confirmAppointmentFindingByDay(today, participant)
			.assertOnConfirmedAppointmentByDay(today);
		
		// participant check the confirmation
		participantCourse
			.clickTree();
		
		participantAppointment
			.selectTopicAsParticipant(topicTitle)
			.assertOnConfirmedAppointmentByDay(today);
	}
	
	
	/**
	 * An author create a course with a course element
	 * with one check box. It add one participant. The
	 * participant log in, go to the course to check its
	 * box and see if it has done the course with 100%.
	 * 
	 * @param loginPage The login page
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void courseWithCheckboxWithScore()
	throws IOException, URISyntaxException {
		WebDriver participantBrowser = getWebDriver(1);
		
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		UserVO participant = new UserRestClient(deploymentUrl).createRandomUser("nezuko");

		LoginPage loginPage = LoginPage.load(browser, deploymentUrl);
		loginPage.loginAs(author.getLogin(), author.getPassword());
		
		//create a course
		String courseTitle = "Check Course" + UUID.randomUUID();
		NavigationPage navBar = NavigationPage.load(browser);
		CoursePageFragment courseRuntime = navBar
			.openAuthoringEnvironment()
			.createCourse(courseTitle, true)
			.clickToolbarBack();
		
		//add participant
		MembersPage members = courseRuntime
			.members();
		members
			.addMember()
			.importList()
			.setMembers(participant)
			.nextUsers()
			.nextOverview()
			.selectRepositoryEntryRole(false, false, true)
			.nextPermissions()
			.finish();
		// back to course
		members
			.clickToolbarBack();
		
		//create a course element of type Test with the test that we create above
		String nodeTitle = "CheckNode";
		CourseEditorPageFragment courseEditor = CoursePageFragment.getCourse(browser)
			.edit();
		courseEditor
			.createNode("checklist")
			.nodeTitle(nodeTitle);
		
		String checkboxTitle = "Do some programming";
		
		CheckListConfigPage checkConfig = new CheckListConfigPage(browser);
		checkConfig
			.selectListConfiguration()
			.addCheckbox(checkboxTitle, 4)
			.assertOnCheckboxInList(checkboxTitle);
		
		checkConfig
			.selectAssessmentConfiguration()
			.setScoring(0, 4, 3)
			.saveAssessmentConfiguration();
		
		courseEditor
			.selectTabLearnPath()
			.setCompletionCriterion(FullyAssessedTrigger.passed)
			.save();
		
		courseEditor
			.autoPublish()
			.publish()
			.settings()
			.accessConfiguration()
			.setAccessToMembersOnly()
			.save()
			.clickToolbarBack();
		
		// participant comes in

		LoginPage participantLoginPage = LoginPage.load(participantBrowser, deploymentUrl);
		participantLoginPage
			.loginAs(participant.getLogin(), participant.getPassword());

		NavigationPage participantNavBar = NavigationPage.load(participantBrowser);
		participantNavBar
			.openMyCourses()
			.select(courseTitle);
		
		CoursePageFragment course = new CoursePageFragment(participantBrowser);
		course
			.tree()
			.selectWithTitle(nodeTitle);

		CheckListPage checkPage = new CheckListPage(participantBrowser);
		checkPage
			.assertOnCheckbox(checkboxTitle)
			.check(checkboxTitle);
		// student has done the course
		course
			.assertOnLearnPathNodeDone(nodeTitle)
			.assertOnLearnPathPercent(100);
		
		// open the assessment tool and check the participant passed the node
		// and the course
		AssessmentToolPage assessmentTool = new CoursePageFragment(browser)
			.assessmentTool();
		assessmentTool
			.users()
			.assertOnUsers(participant)
			.selectUser(participant)
			.assertPassed(participant)
			.assertUserPassedCourseNode(nodeTitle);
	}
	
	/**
	 * An author create a course with a course element
	 * with one check box. It add one participant and he
	 * checks the box.
	 * 
	 * @param loginPage The login page
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void courseWithCheckbox()
	throws IOException, URISyntaxException {
						
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		UserVO participant = new UserRestClient(deploymentUrl).createRandomUser("nezuko");

		LoginPage loginPage = LoginPage.load(browser, deploymentUrl);
		loginPage.loginAs(author.getLogin(), author.getPassword());
		
		//create a course
		String courseTitle = "Checklist" + UUID.randomUUID();
		NavigationPage navBar = NavigationPage.load(browser);
		CoursePageFragment courseRuntime = navBar
			.openAuthoringEnvironment()
			.createCourse(courseTitle, true)
			.clickToolbarBack();
		
		//add participant
		MembersPage members = courseRuntime
			.members();
		members
			.addMember()
			.importList()
			.setMembers(participant)
			.nextUsers()
			.nextOverview()
			.selectRepositoryEntryRole(false, false, true)
			.nextPermissions()
			.finish();
		// back to course
		members
			.clickToolbarBack();
		
		//create a course element of type Test with the test that we create above
		String nodeTitle = "Liste de controle";
		CourseEditorPageFragment courseEditor = CoursePageFragment.getCourse(browser)
			.edit();
		courseEditor
			.createNode("checklist")
			.nodeTitle(nodeTitle);
		
		String checkboxTitle = "Do some stuff";
		
		CheckListConfigPage checkConfig = new CheckListConfigPage(browser);
		checkConfig
			.selectListConfiguration()
			.addCheckbox(checkboxTitle, -1)
			.assertOnCheckboxInList(checkboxTitle);
		
		checkConfig
			.selectAssessmentConfiguration()
			.disableScoring()
			.saveAssessmentConfiguration();
		
		courseEditor
			.selectTabLearnPath()
			.setCompletionCriterion(FullyAssessedTrigger.confirmed)
			.save();
		
		courseEditor
			.autoPublish()
			.publish()
			.settings()
			.accessConfiguration()
			.setAccessToMembersOnly()
			.save()
			.clickToolbarBack();
		
		//log out
		new UserToolsPage(browser)
			.logout();
		
		// participant comes in
		LoginPage.load(browser, deploymentUrl)
			.loginAs(participant.getLogin(), participant.getPassword());

		NavigationPage participantNavBar = NavigationPage.load(browser);
		participantNavBar
			.openMyCourses()
			.select(courseTitle);
		
		CoursePageFragment course = new CoursePageFragment(browser);
		course
			.tree()
			.selectWithTitle(nodeTitle);

		CheckListPage checkPage = new CheckListPage(browser);
		checkPage
			.assertOnCheckbox(checkboxTitle)
			.check(checkboxTitle);
		// check doesn't influence the learn path
		course
			.assertOnLearnPathNodeReady(nodeTitle)
			.assertOnLearnPathPercent(0);
		// student has done the course
		course
			.confirmNode()
			.assertOnLearnPathNodeDone(nodeTitle)
			.assertOnLearnPathPercent(100);
	}
}
