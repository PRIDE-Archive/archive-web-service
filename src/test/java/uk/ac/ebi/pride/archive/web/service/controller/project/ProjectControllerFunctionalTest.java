package uk.ac.ebi.pride.archive.web.service.controller.project;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import uk.ac.ebi.pride.archive.dataprovider.person.Title;
import uk.ac.ebi.pride.archive.dataprovider.project.SubmissionType;
import uk.ac.ebi.pride.archive.repo.project.service.ProjectSummary;
import uk.ac.ebi.pride.archive.repo.user.service.UserSummary;
import uk.ac.ebi.pride.archive.search.service.ProjectSearchService;
import uk.ac.ebi.pride.archive.search.service.ProjectSearchSummary;
import uk.ac.ebi.pride.archive.security.project.ProjectSecureServiceImpl;
import uk.ac.ebi.pride.archive.web.service.util.WsUtils;

import java.util.ArrayList;
import java.util.Collection;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests the retrieving  project-related information. A ProjectSearchService is mocked with test information, which
 * is then queried.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration({"classpath:test-context.xml", "classpath:mvc-config.xml", "classpath:spring-mongo-test-context.xml"})
public class ProjectControllerFunctionalTest {

    @Autowired
    private WebApplicationContext webApplicationContext;
    @Autowired
    private ProjectSecureServiceImpl projectSecureServiceImpl;
    @Autowired
    private ProjectSearchService projectSearchService;

    private MockMvc mockMvc;

    // mock data values
    private static final String PROJECT_ACCESSION = "PXTEST1";
    private static final String PROJECT_TITLE = "Project test title";
    private static final long NUM_COUNT_RESULTS = 12345;

    /**
     * Sets up the project and project summary test information, used to mock the project service.
     */
    @Before
    public void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.webApplicationContext).build(); // mock results
        // DB ProjectSummary
        ProjectSummary projectSummary = new ProjectSummary();
        projectSummary.setAccession(PROJECT_ACCESSION);
        projectSummary.setSubmissionType(SubmissionType.COMPLETE);
        UserSummary submitter = new UserSummary();
        submitter.setTitle(Title.Mr);
        submitter.setFirstName("John");
        submitter.setLastName("Smith");
        submitter.setAffiliation("EBI");
        submitter.setEmail("john.smith@ebi.ac.uk");
        projectSummary.setSubmitter(submitter);

        // Solr ProjectSearchSummary
        ProjectSearchSummary projectSearchSummary = new ProjectSearchSummary();
        projectSearchSummary.setProjectAccession(PROJECT_ACCESSION);
        projectSearchSummary.setTitle(PROJECT_TITLE);

        Collection<ProjectSearchSummary> projectSummaries = new ArrayList<>();
        projectSummaries.add(projectSearchSummary);

        // mock the project service
        when(projectSecureServiceImpl.findByAccession(PROJECT_ACCESSION)).thenReturn(projectSummary);
        when(projectSearchService.searchProjects(anyString(), anyString(), any(String[].class), anyInt(), anyInt(), anyString(), anyString())).thenReturn(projectSummaries);
        when(projectSearchService.numSearchResults(anyString(), anyString(), any(String[].class))).thenReturn(NUM_COUNT_RESULTS);
    }

    /**
     * Tests retrieving project details from the /project/{projectAccession} path.
     *
     * @throws Exception Failed to retrieve results from the mocked service.
     */
    @Test
    public void getProjectReturnProjectSummary() throws Exception {
        mockMvc.perform(get("/project/{projectAccession}", PROJECT_ACCESSION))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string(containsString(PROJECT_ACCESSION)))
                .andExpect(content().string(containsString("john.smith@ebi.ac.uk")));
    }

    /**
     * Tests retrieving a list of projects from the /project/list path.
     *
     * @throws Exception Failed to retrieve results from the mocked service.
     */
    @Test
    public void getProjectListReturnProjectSummary() throws Exception {
        mockMvc.perform(get("/project/list"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string(containsString(PROJECT_ACCESSION)))
                .andExpect(content().string(containsString(PROJECT_TITLE)));
    }

    /**
     * Tests retrieving a count of projects from the /project/count path.
     *
     * @throws Exception Failed to retrieve results from the mocked service.
     */
    @Test
    public void getProjectCount() throws Exception {
        mockMvc.perform(get("/project/count"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string(containsString("" + NUM_COUNT_RESULTS)));
    }

    /**
     * Tests retrieving a list of projects using pagination from the /project/list path.
     *
     * @throws Exception Failed to retrieve results from the mocked service.
     */
    @Test
    public void projectListMaxPageSizeException() throws Exception {
        mockMvc.perform(get("/project/list?show={pageSize}&page=0", (WsUtils.MAX_PAGE_SIZE)))
                .andExpect(status().isOk());
        mockMvc.perform(get("/project/list?show={pageSize}&page=0", (WsUtils.MAX_PAGE_SIZE + 1)))
                .andExpect(status().isForbidden());
    }
}
