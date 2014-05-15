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
import uk.ac.ebi.pride.prider.service.assay.AssayServiceImpl;
import uk.ac.ebi.pride.prider.service.assay.AssaySummary;
import uk.ac.ebi.pride.prider.service.project.ProjectServiceImpl;
import uk.ac.ebi.pride.prider.service.project.ProjectSummary;

import java.util.Collection;
import java.util.HashSet;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration({"classpath:test-context.xml", "classpath:spring/mvc-config.xml"})
public class AssayControllerFunctionalTest {
    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @Autowired
    private ProjectServiceImpl projectServiceImpl;
    @Autowired
    private AssayServiceImpl assayServiceImpl;


    private static final String PROJECT_ACCESSION = "PXTEST1";
    private static final long PROJECT_ID = 100001;
    private static final String ASSAY_ACCESSION = "9876";
    private static final long ASSAY_ID = 200001;
    private static final String ASSAY_TITLE = "Assay title";
    private static final String ASSAY_SHORT_LABEL = "Assay short label";

    @Before
    public void setUp() throws Exception {
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

        // mock assay service
        when(assayServiceImpl.findByAccession(ASSAY_ACCESSION)).thenReturn(assaySummary);
        when(assayServiceImpl.findAllByProjectAccession(PROJECT_ACCESSION)).thenReturn(assays);

        ProjectSummary projectSummary = new ProjectSummary();
        projectSummary.setAccession(PROJECT_ACCESSION);
        projectSummary.setId(PROJECT_ID);
        when(projectServiceImpl.findByAccession(PROJECT_ACCESSION)).thenReturn(projectSummary);
        when(projectServiceImpl.findById(PROJECT_ID)).thenReturn(projectSummary);
    }

    @Test // /assay/project/{projectAccession}
    public void getByProjectAccession() throws Exception {
        mockMvc.perform(get("/assay/list/project/" + PROJECT_ACCESSION))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string(containsString(PROJECT_ACCESSION)))
                .andExpect(content().string(containsString(ASSAY_ACCESSION)))
                .andExpect(content().string(containsString(ASSAY_TITLE)))
                .andExpect(content().string(containsString(ASSAY_SHORT_LABEL)));
    }

    @Test // /assay/{assayAccession}
    public void getByAssayAccession() throws Exception {
        mockMvc.perform(get("/assay/" + ASSAY_ACCESSION))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string(containsString(PROJECT_ACCESSION)))
                .andExpect(content().string(containsString(ASSAY_ACCESSION)))
                .andExpect(content().string(containsString(ASSAY_TITLE)))
                .andExpect(content().string(containsString(ASSAY_SHORT_LABEL)));
    }

}
