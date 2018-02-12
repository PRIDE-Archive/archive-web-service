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
import uk.ac.ebi.pride.proteinidentificationindex.search.model.ProteinIdentification;
import uk.ac.ebi.pride.proteinidentificationindex.search.service.ProteinIdentificationSearchService;

import java.util.ArrayList;
import java.util.List;

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
    private ProteinIdentificationSearchService pissService;

    private MockMvc mockMvc;

    private static final String PROJECT_ACCESSION = "PXTEST1";
    private static final String ASSAY_ACCESSION = "1234";
    private static final String PROTEIN_ACCESSION = "P12345";
    private static final long NUM_COUNT_RESULTS = 12345L;

    /**
     * Sets up the protein test information, used to mock the protein identification service.
     */
    @Before
    public void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.webApplicationContext).build();

        // Create fake Protein
        ProteinIdentification protein = new ProteinIdentification();
        protein.setAccession(PROTEIN_ACCESSION);
        protein.setProjectAccession(PROJECT_ACCESSION);
        protein.setAssayAccession(ASSAY_ACCESSION);

        List<ProteinIdentification> list = new ArrayList<ProteinIdentification>(1);
        list.add(protein);
        Page<ProteinIdentification> page = new PageImpl<ProteinIdentification>(list);

        // mock the protein identification service
        PageRequest pageRequest = new PageRequest(0, 10);
        when(pissService.findByProjectAccession(PROJECT_ACCESSION, pageRequest)).thenReturn(page);
        when(pissService.findByAssayAccession(ASSAY_ACCESSION, pageRequest)).thenReturn(page);

        pageRequest = new PageRequest(0, 2);
        when(pissService.findByProjectAccession(PROJECT_ACCESSION, pageRequest)).thenReturn(page);
        when(pissService.findByAssayAccession(ASSAY_ACCESSION, pageRequest)).thenReturn(page);

        when(pissService.countByProjectAccession(PROJECT_ACCESSION)).thenReturn(NUM_COUNT_RESULTS);
        when(pissService.countByProjectAccessionAndAccession(PROJECT_ACCESSION, PROTEIN_ACCESSION)).thenReturn(NUM_COUNT_RESULTS);
        when(pissService.countByAssayAccession(ASSAY_ACCESSION)).thenReturn(NUM_COUNT_RESULTS);
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
//        mockMvc.perform(get("/protein/list/project/{projectAccession}", PROJECT_ACCESSION))
//                .andExpect(status().isOk())
//                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
//                .andExpect(content().string(containsString(PROJECT_ACCESSION)))
//                .andExpect(content().string(containsString(ASSAY_ACCESSION)))
//                .andExpect(content().string(containsString(PROTEIN_ACCESSION)));
//
//        // test with custom paging configuration
//        mockMvc.perform(get("/protein/list/project/{projectAccession}?show=2&page=0", PROJECT_ACCESSION))
//                .andExpect(status().isOk())
//                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
//                .andExpect(content().string(containsString(PROJECT_ACCESSION)))
//                .andExpect(content().string(containsString(ASSAY_ACCESSION)))
//                .andExpect(content().string(containsString(PROTEIN_ACCESSION)));
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
//        mockMvc.perform(get("/protein/list/assay/{assayAccession}", ASSAY_ACCESSION))
//                .andExpect(status().isOk())
//                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
//                .andExpect(content().string(containsString(PROJECT_ACCESSION)))
//                .andExpect(content().string(containsString(ASSAY_ACCESSION)))
//                .andExpect(content().string(containsString(PROTEIN_ACCESSION)));
//
//        // test with custom paging configuration
//        mockMvc.perform(get("/protein/list/assay/{assayAccession}?show=2&page=0", ASSAY_ACCESSION))
//                .andExpect(status().isOk())
//                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
//                .andExpect(content().string(containsString(PROJECT_ACCESSION)))
//                .andExpect(content().string(containsString(ASSAY_ACCESSION)))
//                .andExpect(content().string(containsString(PROTEIN_ACCESSION)));
    }

    /**
     * Tests retrieving list of proteins by providing assay accession using pagination
     * from the /protein/list/assay/{assayAccession} path.
     *
     * @throws Exception Failed to retrieve results from the mocked service.
     */
    @Test
    public void ProteinByAssayAccessionMaxPageSizeException() throws Exception {
        // test with custom paging configuration
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
}
