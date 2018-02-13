package uk.ac.ebi.pride.archive.web.service.controller.psm;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import uk.ac.ebi.pride.archive.web.service.util.WsUtils;
import uk.ac.ebi.pride.psmindex.mongo.search.model.MongoPsm;
import uk.ac.ebi.pride.psmindex.mongo.search.service.MongoPsmIndexService;
import uk.ac.ebi.pride.psmindex.search.model.Psm;
import uk.ac.ebi.pride.psmindex.search.service.PsmSearchService;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests the retrieving  psm-related information. A mongoPsmIndexService is mocked with test information,
 * which is then queried.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration({
        "classpath:test-context.xml",
        "classpath:mvc-config.xml",
        "classpath:spring-mongo-test-context.xml"})
public class PsmControllerFunctionalTest {

    @Autowired
    private WebApplicationContext webApplicationContext;
    @Autowired
    private PsmSearchService psmSearchService;
    @Autowired
    private MongoPsmIndexService mongoPsmIndexService;

    private MockMvc mockMvc;

    private static final String ID = "PXTEST1_1234";
    private static final String PROJECT_ACCESSION = "PXTEST1";
    private static final String ASSAY_ACCESSION = "1234";
    private static final String PROTEIN_ACCESSION = "P12345";
    private static final String SEQUENCE = "GIANSILIK";
    private static final long NUM_COUNT_RESULTS = 12345L;

    @Before
    public void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.webApplicationContext).build();

        // create fake psm
        Psm psm = new Psm();
        psm.setId(ID);
        psm.setProteinAccession(PROTEIN_ACCESSION);
        psm.setProjectAccession(PROJECT_ACCESSION);
        psm.setAssayAccession(ASSAY_ACCESSION);
        List<Psm> list = new ArrayList<>(1);
        list.add(psm);
        Page<Psm> page = new PageImpl<>(list);

        // mock the psm search service
        PageRequest pageRequest = new PageRequest(0, 10, Sort.Direction.ASC, "peptide_sequence");
        when(psmSearchService.findByProjectAccession(PROJECT_ACCESSION, pageRequest)).thenReturn(page);
        when(psmSearchService.findByAssayAccession(ASSAY_ACCESSION, pageRequest)).thenReturn(page);

        pageRequest = new PageRequest(0, 2, Sort.Direction.ASC, "peptide_sequence");
        when(psmSearchService.findByProjectAccession(PROJECT_ACCESSION, pageRequest)).thenReturn(page);
        when(psmSearchService.findByAssayAccession(ASSAY_ACCESSION, pageRequest)).thenReturn(page);
        when(psmSearchService.countByProjectAccession(PROJECT_ACCESSION)).thenReturn(NUM_COUNT_RESULTS);
        when(psmSearchService.countByAssayAccession(ASSAY_ACCESSION)).thenReturn(NUM_COUNT_RESULTS);
        when(psmSearchService.countByPeptideSequenceAndAssayAccession(SEQUENCE, ASSAY_ACCESSION)).thenReturn(NUM_COUNT_RESULTS);
        when(psmSearchService.countByPeptideSequenceAndProjectAccession(SEQUENCE, PROJECT_ACCESSION)).thenReturn(NUM_COUNT_RESULTS);
        mongoPsmIndexService.deleteAll();

        MongoPsm mongoPsm = new MongoPsm();
        mongoPsm.setId(ID);
        mongoPsm.setProteinAccession(PROTEIN_ACCESSION);
        mongoPsm.setProjectAccession(PROJECT_ACCESSION);
        mongoPsm.setAssayAccession(ASSAY_ACCESSION);
        mongoPsmIndexService.save(mongoPsm);
    }

    /**
     * Tests retrieving list of peptides by providing project accession
     * from the /peptide/list/project/{projectAccession} path.
     *
     * @throws Exception Failed to retrieve results from the mocked service.
     */
    @Test
    public void getPsmByProjectAccession() throws Exception {
        // test default use case
        mockMvc.perform(get("/peptide/list/project/{projectAccession}", PROJECT_ACCESSION))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string(containsString(PROJECT_ACCESSION)))
                .andExpect(content().string(containsString(ASSAY_ACCESSION)))
                .andExpect(content().string(containsString(PROTEIN_ACCESSION)));

        // test with custom paging configuration
        mockMvc.perform(get("/peptide/list/project/{projectAccession}?show=2&page=0", PROJECT_ACCESSION))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string(containsString(PROJECT_ACCESSION)))
                .andExpect(content().string(containsString(ASSAY_ACCESSION)))
                .andExpect(content().string(containsString(PROTEIN_ACCESSION)));
    }

    /**
     * Tests retrieving list of peptides by providing project accession using pagination
     * from the /peptide/list/project/{projectAccession} path.
     *
     * @throws Exception Failed to retrieve results from the mocked service.
     */
    @Test
    public void getPsmByProjectAccessionMaxPageSizeException() throws Exception {
        mockMvc.perform(get("/peptide/list/project/{projectAccession}?show={pageSize}&page=0",
                PROJECT_ACCESSION, (WsUtils.MAX_PAGE_SIZE + 1)))
                .andExpect(status().isForbidden());
    }

    /**
     * Tests retrieving list of peptides by providing assay accession
     * from the /peptide/list/assay/{assayAccession} path.
     *
     * @throws Exception Failed to retrieve results from the mocked service.
     */
    @Test
    public void getPsmByAssayAccession() throws Exception {
        // test default use case
        mockMvc.perform(get("/peptide/list/assay/{assayAccession}", ASSAY_ACCESSION))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string(containsString(PROJECT_ACCESSION)))
                .andExpect(content().string(containsString(ASSAY_ACCESSION)))
                .andExpect(content().string(containsString(PROTEIN_ACCESSION)));

        // test with custom paging configuration
        mockMvc.perform(get("/peptide/list/assay/{assayAccession}?show=2&page=0", ASSAY_ACCESSION))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string(containsString(PROJECT_ACCESSION)))
                .andExpect(content().string(containsString(ASSAY_ACCESSION)))
                .andExpect(content().string(containsString(PROTEIN_ACCESSION)));
    }

    /**
     * Tests retrieving list of peptides by providing assay accession using pagination
     * from the /peptide/list/assay/{assayAccession} path.
     *
     * @throws Exception Failed to retrieve results from the mocked service.
     */
    @Test
    public void getPsmByAssayAccessionMaxPageSizeException() throws Exception {
        // test with custom paging configuration
        mockMvc.perform(get("/peptide/list/assay/{assayAccession}?show={pageSize}&page=0", ASSAY_ACCESSION, (WsUtils.MAX_PAGE_SIZE + 1)))
                .andExpect(status().isForbidden());
    }

    /**
     * Tests retrieving count of identified peptides by providing project accession
     * from the /peptide/count/project/{projectAccession} path.
     *
     * @throws Exception Failed to retrieve results from the mocked service.
     */
    @Test
    public void countPsmsByProject() throws Exception {
        mockMvc.perform(get("/peptide/count/project/{projectAccession}", PROJECT_ACCESSION))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string(containsString("" + NUM_COUNT_RESULTS)));
    }

    /**
     * Tests retrieving count of identified peptides by providing project accession and peptide sequence
     * from the /peptide/count/project/{projectAccession}/sequence/{sequence} path.
     *
     * @throws Exception Failed to retrieve results from the mocked service.
     */
    @Test
    public void countPsmsByProjectAndSequence() throws Exception {
        mockMvc.perform(get("/peptide/count/project/{projectAccession}/sequence/{sequence}", PROJECT_ACCESSION, SEQUENCE))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string(containsString("" + NUM_COUNT_RESULTS)));
    }

    /**
     * Tests retrieving count of identified peptides by providing assay accession
     * from the /peptide/count/assay/{assayAccession} path.
     *
     * @throws Exception Failed to retrieve results from the mocked service.
     */
    @Test
    public void countPsmsByAssay() throws Exception {
        mockMvc.perform(get("/peptide/count/assay/{assayAccession}", ASSAY_ACCESSION))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string(containsString("" + NUM_COUNT_RESULTS)));
    }

    /**
     * Tests retrieving count of identified peptides by providing assay accession and peptide sequence
     * from the /peptide/count/assay/{assayAccession}/sequence/{sequence} path.
     *
     * @throws Exception Failed to retrieve results from the mocked service.
     */
    @Test
    public void countPsmsByAssayAndSequence() throws Exception {
        mockMvc.perform(get("/peptide/count/assay/{assayAccession}/sequence/{sequence}", ASSAY_ACCESSION, SEQUENCE))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string(containsString("" + NUM_COUNT_RESULTS)));
    }
}