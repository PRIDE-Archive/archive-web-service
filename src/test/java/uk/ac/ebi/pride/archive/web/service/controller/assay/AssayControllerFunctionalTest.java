package uk.ac.ebi.pride.archive.web.service.controller.assay;

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
import uk.ac.ebi.pride.archive.repo.assay.service.AssaySummary;
import uk.ac.ebi.pride.archive.repo.project.service.ProjectSummary;
import uk.ac.ebi.pride.archive.security.assay.AssaySecureServiceImpl;
import uk.ac.ebi.pride.archive.security.project.ProjectSecureServiceImpl;

import java.util.Collection;
import java.util.HashSet;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests the retrieving  Assay-related information. A AssaySecureService is mocked with test information,
 * which is then queried.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration({
        "classpath:test-context.xml",
        "classpath:mvc-config.xml",
        "classpath:spring-mongo-test-context.xml"})
public class AssayControllerFunctionalTest {

    @Autowired
    private WebApplicationContext webApplicationContext;
    @Autowired
    private ProjectSecureServiceImpl projectSecureServiceImpl;
    @Autowired
    private AssaySecureServiceImpl assaySecureServiceImpl;

    private MockMvc mockMvc;

    // mock data values
    private static final String PROJECT_ACCESSION = "PXTEST1";
    private static final long PROJECT_ID = 100001;
    private static final String ASSAY_ACCESSION = "9876";
    private static final long ASSAY_ID = 200001;
    private static final String ASSAY_TITLE = "Assay title";
    private static final String ASSAY_SHORT_LABEL = "Assay short label";
    private static final long NUM_COUNT_RESULTS = 12345L;

    /**
     * Sets up the assay summary test information, used to mock the assay service.
     */
    @Before
    public void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.webApplicationContext).build();

        //create fake assay
        AssaySummary assaySummary = new AssaySummary();
        assaySummary.setAccession(ASSAY_ACCESSION);
        assaySummary.setId(ASSAY_ID);
        assaySummary.setProjectId(PROJECT_ID);
        assaySummary.setTitle(ASSAY_TITLE);
        assaySummary.setShortLabel(ASSAY_SHORT_LABEL);

        Collection<AssaySummary> assays = new HashSet<AssaySummary>();
        assays.add(assaySummary);

        ProjectSummary projectSummary = new ProjectSummary();
        projectSummary.setAccession(PROJECT_ACCESSION);
        projectSummary.setId(PROJECT_ID);

        // mock assay service
        when(assaySecureServiceImpl.findByAccession(ASSAY_ACCESSION)).thenReturn(assaySummary);
        when(assaySecureServiceImpl.findAllByProjectAccession(PROJECT_ACCESSION)).thenReturn(assays);
        when(assaySecureServiceImpl.countByProjectAccession(PROJECT_ACCESSION)).thenReturn(NUM_COUNT_RESULTS);

        when(projectSecureServiceImpl.findByAccession(PROJECT_ACCESSION)).thenReturn(projectSummary);
        when(projectSecureServiceImpl.findById(PROJECT_ID)).thenReturn(projectSummary);
    }

    /**
     * Tests retrieving assay details by projectAccession from the /assay/project/{projectAccession} path.
     *
     * @throws Exception Failed to retrieve results from the mocked service.
     */
    @Test
    public void getByProjectAccession() throws Exception {
        mockMvc.perform(get("/assay/list/project/{projectAccession}", PROJECT_ACCESSION))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string(containsString(PROJECT_ACCESSION)))
                .andExpect(content().string(containsString(ASSAY_ACCESSION)))
                .andExpect(content().string(containsString(ASSAY_TITLE)))
                .andExpect(content().string(containsString(ASSAY_SHORT_LABEL)));
    }

    /**
     * Tests retrieving assay details by assayAccession from the /assay/{assayAccession} path.
     *
     * @throws Exception Failed to retrieve results from the mocked service.
     */
    @Test
    public void getByAssayAccession() throws Exception {
        mockMvc.perform(get("/assay/{assayAccession}", ASSAY_ACCESSION))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string(containsString(PROJECT_ACCESSION)))
                .andExpect(content().string(containsString(ASSAY_ACCESSION)))
                .andExpect(content().string(containsString(ASSAY_TITLE)))
                .andExpect(content().string(containsString(ASSAY_SHORT_LABEL)));
    }

    /**
     * Tests retrieving count of assays by providing project accession
     * from the /assay/count/project/{projectAccession} path.
     *
     * @throws Exception Failed to retrieve results from the mocked service.
     */
    @Test
    public void countByProjectAccession() throws Exception {
        mockMvc.perform(get("/assay/count/project/{projectAccession}", PROJECT_ACCESSION))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string(containsString("" + NUM_COUNT_RESULTS)));
    }
}
