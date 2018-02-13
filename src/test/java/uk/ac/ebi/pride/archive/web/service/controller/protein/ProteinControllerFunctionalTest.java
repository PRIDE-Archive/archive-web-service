package uk.ac.ebi.pride.archive.web.service.controller.protein;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import uk.ac.ebi.pride.archive.web.service.util.WsUtils;
import uk.ac.ebi.pride.proteinidentificationindex.mongo.search.model.MongoProteinIdentification;
import uk.ac.ebi.pride.proteinidentificationindex.mongo.search.service.MongoProteinIdentificationIndexService;
import uk.ac.ebi.pride.proteinidentificationindex.search.model.ProteinIdentification;
import uk.ac.ebi.pride.proteinidentificationindex.search.service.ProteinIdentificationSearchService;

import java.util.*;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests the retrieving  protein-related information. A ProteinIdentificationSearchService is mocked with test information, which
 * is then queried.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration({
        "classpath:test-context.xml",
        "classpath:mvc-config.xml",
        "classpath:spring-mongo-test-context.xml"})
public class ProteinControllerFunctionalTest {

    @Autowired
    private WebApplicationContext webApplicationContext;
    @Autowired
    private ProteinIdentificationSearchService proteinIdentificationSearchService;
    @Autowired
    private MongoProteinIdentificationIndexService mongoProteinIdentificationIndexService;

    private MockMvc mockMvc;

    private static final String PROJECT_ACCESSION = "PXTEST1";
    private static final String ASSAY_ACCESSION = "1234";
    private static final String PROTEIN_ACCESSION = "P12345";
    private static final long NUM_COUNT_RESULTS = 12345L;
    private static final String ID = "PXTEST1_1234";

    /**
     * Sets up the protein test information, used to mock the protein identification service.
     */
    @Before
    public void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.webApplicationContext).build();

        // Create fake Protein
        ProteinIdentification protein = new ProteinIdentification();
        protein.setId(ID);
        protein.setAccession(PROTEIN_ACCESSION);
        protein.setProjectAccession(PROJECT_ACCESSION);
        protein.setAssayAccession(ASSAY_ACCESSION);

        List<ProteinIdentification> list = new ArrayList<>(1);
        list.add(protein);
        Page<ProteinIdentification> page = new PageImpl<>(list);

        // mock the protein identification service
        PageRequest pageRequest = new PageRequest(0, 10);
        when(proteinIdentificationSearchService.findByProjectAccession(PROJECT_ACCESSION, pageRequest)).thenReturn(page);
        when(proteinIdentificationSearchService.findByAssayAccession(ASSAY_ACCESSION, pageRequest)).thenReturn(page);

        pageRequest = new PageRequest(0, 2);
        when(proteinIdentificationSearchService.findByProjectAccession(PROJECT_ACCESSION, pageRequest)).thenReturn(page);
        when(proteinIdentificationSearchService.findByAssayAccession(ASSAY_ACCESSION, pageRequest)).thenReturn(page);

        when(proteinIdentificationSearchService.countByProjectAccession(PROJECT_ACCESSION)).thenReturn(NUM_COUNT_RESULTS);
        when(proteinIdentificationSearchService.countByProjectAccessionAndAccession(PROJECT_ACCESSION, PROTEIN_ACCESSION)).thenReturn(NUM_COUNT_RESULTS);
        when(proteinIdentificationSearchService.countByAssayAccession(ASSAY_ACCESSION)).thenReturn(NUM_COUNT_RESULTS);
        when(proteinIdentificationSearchService.findByProjectAccessionAndAccession(PROJECT_ACCESSION, PROTEIN_ACCESSION)).thenReturn(list);

        Set<String> assayAccessions = new HashSet<>(Collections.singletonList("22134"));
        Set<String> projectAccessions = new HashSet<>(Collections.singletonList("PXD000001"));
        when(proteinIdentificationSearchService.getUniqueProteinAccessionsByAssayAccession(ASSAY_ACCESSION)).thenReturn(assayAccessions);
        when(proteinIdentificationSearchService.getUniqueProteinAccessionsByProjectAccession(PROJECT_ACCESSION)).thenReturn(projectAccessions);

        MongoProteinIdentification mongoProteinIdentification = new MongoProteinIdentification();
        mongoProteinIdentification.setId(ID);
        mongoProteinIdentification.setAccession(PROTEIN_ACCESSION);
        mongoProteinIdentification.setProjectAccession(PROJECT_ACCESSION);
        mongoProteinIdentification.setAssayAccession(ASSAY_ACCESSION);
        mongoProteinIdentificationIndexService.save(mongoProteinIdentification);
    }

    /**
     * Tests retrieving list of proteins by providing project accession
     * from the /protein/list/project/{projectAccession} path.
     *
     * @throws Exception Failed to retrieve results from the mocked service.
     */
    @Test
    public void getProteinByProjectAccession() throws Exception {
        // test default use case
        mockMvc.perform(get("/protein/list/project/{projectAccession}", PROJECT_ACCESSION))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string(containsString(PROJECT_ACCESSION)))
                .andExpect(content().string(containsString(ASSAY_ACCESSION)))
                .andExpect(content().string(containsString(PROTEIN_ACCESSION)));

        // test with custom paging configuration
        mockMvc.perform(get("/protein/list/project/{projectAccession}?show=2&page=0", PROJECT_ACCESSION))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string(containsString(PROJECT_ACCESSION)))
                .andExpect(content().string(containsString(ASSAY_ACCESSION)))
                .andExpect(content().string(containsString(PROTEIN_ACCESSION)));
    }

    /**
     * Tests retrieving list of proteins by providing project accession using pagination
     * from the /protein/list/project/{projectAccession} path.
     *
     * @throws Exception Failed to retrieve results from the mocked service.
     */
    @Test
    public void ProteinByProjectAccessionMaxPageSizeException() throws Exception {
        // test with custom paging configuration
        mockMvc.perform(get("/protein/list/project/{projectAccession}?show={pageSize}&page=0", PROJECT_ACCESSION, (WsUtils.MAX_PAGE_SIZE + 1)))
                .andExpect(status().isForbidden());
    }

    /**
     * Tests retrieving list of proteins by providing assay accession
     * from the /protein/list/assay/{assayAccession} path.
     *
     * @throws Exception Failed to retrieve results from the mocked service.
     */
    @Test
    public void getProteinByAssayAccession() throws Exception {
        // test default use case
        mockMvc.perform(get("/protein/list/assay/{assayAccession}", ASSAY_ACCESSION))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string(containsString(PROJECT_ACCESSION)))
                .andExpect(content().string(containsString(ASSAY_ACCESSION)))
                .andExpect(content().string(containsString(PROTEIN_ACCESSION)));

        // test with custom paging configuration
        mockMvc.perform(get("/protein/list/assay/{assayAccession}?show=2&page=0", ASSAY_ACCESSION))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string(containsString(PROJECT_ACCESSION)))
                .andExpect(content().string(containsString(ASSAY_ACCESSION)))
                .andExpect(content().string(containsString(PROTEIN_ACCESSION)));
    }

    /**
     * Tests retrieving list of proteins by providing assay accession using pagination
     * from the /protein/list/assay/{assayAccession} path.
     *
     * @throws Exception Failed to retrieve results from the mocked service.
     */
    @Test
    public void ProteinByAssayAccessionMaxPageSizeException() throws Exception {
        mockMvc.perform(get("/protein/list/assay/{assayAccession}?show={pageSize}&page=0",
                ASSAY_ACCESSION, (WsUtils.MAX_PAGE_SIZE + 1)))
                .andExpect(status().isForbidden());
    }

    /**
     * Tests retrieving count of proteins by providing project accession
     * from the /protein/count/project/{projectAccession} path.
     *
     * @throws Exception Failed to retrieve results from the mocked service.
     */
    @Test
    public void countPsmsByProject() throws Exception {
        mockMvc.perform(get("/protein/count/project/{projectAccession}", PROJECT_ACCESSION))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string(containsString("" + NUM_COUNT_RESULTS)));
    }

    /**
     * Tests retrieving count of proteins by providing project accession and protein accession
     * from the /protein/count/project/{projectAccession}/protein/{proteinAccession} path.
     *
     * @throws Exception Failed to retrieve results from the mocked service.
     */
    @Test
    public void countProteinsByProjectAndAccession() throws Exception {
        mockMvc.perform(get("/protein/count/project/{projectAccession}/protein/{proteinAccession}",
                PROJECT_ACCESSION, PROTEIN_ACCESSION))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string(containsString("" + NUM_COUNT_RESULTS)));
    }

    /**
     * Tests retrieving count of proteins by providing assay accession
     * from the /protein/count/assay/{assayAccession} path.
     *
     * @throws Exception Failed to retrieve results from the mocked service.
     */
    @Test
    public void countProteinsByAssay() throws Exception {
        mockMvc.perform(get("/protein//count/assay/{assayAccession}", ASSAY_ACCESSION))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string(containsString("" + NUM_COUNT_RESULTS)));
    }

    /**
     * Tests retrieving list of proteins by providing project accession and protein accession
     * from the /protein/list/project/{projectAccession}/protein/{proteinAccession} path.
     *
     * @throws Exception Failed to retrieve results from the mocked service.
     */
    @Test
    public void getProteinsByProjectAndAccession() throws Exception {
        mockMvc.perform(get("/protein/list/project/{projectAccession}/protein/{proteinAccession}", PROJECT_ACCESSION, PROTEIN_ACCESSION))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string(containsString(PROJECT_ACCESSION)))
                .andExpect(content().string(containsString(PROTEIN_ACCESSION)));
    }

    /**
     * Tests retrieving list of proteins for assay by providing assay accession
     * from the /protein/list/assay/{assayAccession}.acc path.
     *
     * @throws Exception Failed to retrieve results from the mocked service.
     */
    @Test
    public void getProteinListForAssay() throws Exception {
        mockMvc.perform(get("/protein/list/assay/{assayAccession}.acc", ASSAY_ACCESSION))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.TEXT_PLAIN_VALUE))
                .andExpect(content().string(containsString(ASSAY_ACCESSION)));
    }

    /**
     * Tests retrieving list of proteins for project by providing project accession
     * from the /protein/list/project/{projectAccession}.acc path.
     *
     * @throws Exception Failed to retrieve results from the mocked service.
     */
    @Test
    public void getProteinListForProject() throws Exception {
        mockMvc.perform(get("/protein/list/project/{projectAccession}.acc", PROJECT_ACCESSION))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.TEXT_PLAIN_VALUE))
                .andExpect(content().string(containsString(PROJECT_ACCESSION)));
    }
}
