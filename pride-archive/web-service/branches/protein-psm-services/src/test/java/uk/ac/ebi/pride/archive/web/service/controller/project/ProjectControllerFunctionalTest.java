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
import uk.ac.ebi.pride.archive.repo.project.service.ProjectServiceImpl;
import uk.ac.ebi.pride.archive.repo.project.service.ProjectSummary;
import uk.ac.ebi.pride.archive.repo.user.service.UserSummary;
import uk.ac.ebi.pride.archive.search.service.ProjectSearchService;
import uk.ac.ebi.pride.archive.search.service.ProjectSearchSummary;

import java.util.ArrayList;
import java.util.Collection;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration({"classpath:test-context.xml", "classpath:spring/mvc-config.xml"})
public class ProjectControllerFunctionalTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @Autowired
    private ProjectServiceImpl projectServiceImpl;

    @Autowired
    private ProjectSearchService projectSearchService;


    private static final String PROJECT_ACCESSION = "PXTEST1";
    private static final String PROJECT_TITLE = "Project test title";
    private static final long NUM_COUNT_RESULTS = 12345;

    @Before
    public void setUp() throws Exception {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.webApplicationContext).build();

        ///// mock results
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

        Collection<ProjectSearchSummary> projectSummaries = new ArrayList<ProjectSearchSummary>();
        projectSummaries.add(projectSearchSummary);


        // mock project service
        when(projectServiceImpl.findByAccession(PROJECT_ACCESSION)).thenReturn(projectSummary);
        when(projectSearchService.searchProjects(anyString(), anyString(), any(String[].class), anyInt(), anyInt(), anyString(), anyString())).thenReturn(projectSummaries);
        when(projectSearchService.numSearchResults(anyString(), anyString(), any(String[].class))).thenReturn(NUM_COUNT_RESULTS);
    }

    @Test // /project/{projectAccession}
    public void getProjectReturnProjectSummary() throws Exception {
        mockMvc.perform(get("/project/" + PROJECT_ACCESSION))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string(containsString(PROJECT_ACCESSION)))
                .andExpect(content().string(containsString("john.smith@ebi.ac.uk")));
    }


    @Test // /project/list
    public void getProjectListReturnProjectSummary() throws Exception {
        mockMvc.perform(get("/project/list"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string(containsString(PROJECT_ACCESSION)))
                .andExpect(content().string(containsString(PROJECT_TITLE)));
    }

    @Test // /project/count
    public void getProjectCount() throws Exception {
        mockMvc.perform(get("/project/count"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string(containsString(""+NUM_COUNT_RESULTS)));
    }

}
