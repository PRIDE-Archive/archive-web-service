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
import uk.ac.ebi.pride.psmindex.search.model.Psm;
import uk.ac.ebi.pride.psmindex.search.service.PsmSearchService;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author florian@ebi.ac.uk.
 * @since 1.0.8
 */
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration({"classpath:test-context.xml", "classpath:mvc-config.xml"})
public class PsmControllerFunctionalTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private PsmSearchService psmSearchService;


    private MockMvc mockMvc;
    private static final String PROJECT_ACCESSION = "PXTEST1";
    private static final String ASSAY_ACCESSION = "1234";
    private static final String PROTEIN_ACCESSION = "P12345";



    @Before
    public void setUp() throws Exception {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.webApplicationContext).build();

        Psm psm = new Psm();
        psm.setProteinAccession(PROTEIN_ACCESSION);
        psm.setProjectAccession(PROJECT_ACCESSION);
        psm.setAssayAccession(ASSAY_ACCESSION);

        List<Psm> list = new ArrayList<Psm>(1);
        list.add(psm);

        Page<Psm> page = new PageImpl<Psm>(list);

        PageRequest pageRequest = new PageRequest(0, 10, Sort.Direction.ASC, "peptide_sequence");
        when( psmSearchService.findByProjectAccession(PROJECT_ACCESSION, pageRequest) ).thenReturn(page);
        when( psmSearchService.findByAssayAccession(ASSAY_ACCESSION, pageRequest) ).thenReturn(page);
        pageRequest = new PageRequest(0, 2, Sort.Direction.ASC, "peptide_sequence");
        when( psmSearchService.findByProjectAccession(PROJECT_ACCESSION, pageRequest) ).thenReturn(page);
        when( psmSearchService.findByAssayAccession(ASSAY_ACCESSION, pageRequest) ).thenReturn(page);
    }

    @Test // /peptide/list/project/{projectAccession}
    public void getPsmByProjectAccession() throws Exception {
        // test default use case
        mockMvc.perform(get("/peptide/list/project/" + PROJECT_ACCESSION))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string(containsString(PROJECT_ACCESSION)))
                .andExpect(content().string(containsString(ASSAY_ACCESSION)))
                .andExpect(content().string(containsString(PROTEIN_ACCESSION)));
        // test with custom paging configuration
        mockMvc.perform(get("/peptide/list/project/" + PROJECT_ACCESSION + "?show=2&page=0"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string(containsString(PROJECT_ACCESSION)))
                .andExpect(content().string(containsString(ASSAY_ACCESSION)))
                .andExpect(content().string(containsString(PROTEIN_ACCESSION)));
    }

    @Test // /peptide/list/assay/{assayAccession}
    public void getPsmByAssayAccession() throws Exception {
        // test default use case
        mockMvc.perform(get("/peptide/list/assay/" + ASSAY_ACCESSION))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string(containsString(PROJECT_ACCESSION)))
                .andExpect(content().string(containsString(ASSAY_ACCESSION)))
                .andExpect(content().string(containsString(PROTEIN_ACCESSION)));
        // test with custom paging configuration
        mockMvc.perform(get("/peptide/list/assay/" + ASSAY_ACCESSION + "?show=2&page=0"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string(containsString(PROJECT_ACCESSION)))
                .andExpect(content().string(containsString(ASSAY_ACCESSION)))
                .andExpect(content().string(containsString(PROTEIN_ACCESSION)));
    }

}