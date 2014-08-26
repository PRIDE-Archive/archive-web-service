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
import uk.ac.ebi.pride.proteinidentificationindex.search.model.ProteinIdentification;
import uk.ac.ebi.pride.proteinidentificationindex.search.service.ProteinIdentificationSearchService;

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
public class ProteinControllerFunctionalTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ProteinIdentificationSearchService pissService;


    private MockMvc mockMvc;
    private static final String PROJECT_ACCESSION = "PXTEST1";
    private static final String ASSAY_ACCESSION = "1234";
    private static final String PROTEIN_ACCESSION = "P12345";



    @Before
    public void setUp() throws Exception {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.webApplicationContext).build();

        ProteinIdentification protein = new ProteinIdentification();
        protein.setAccession(PROTEIN_ACCESSION);
        protein.setProjectAccession(PROJECT_ACCESSION);
        protein.setAssayAccession(ASSAY_ACCESSION);

        List<ProteinIdentification> list = new ArrayList<ProteinIdentification>(1);
        list.add(protein);

        Page<ProteinIdentification> page = new PageImpl<ProteinIdentification>(list);
        PageRequest pageRequest = new PageRequest(0, 10);
        when( pissService.findByProjectAccession(PROJECT_ACCESSION, pageRequest) ).thenReturn(page);
        when( pissService.findByAssayAccession(ASSAY_ACCESSION, pageRequest) ).thenReturn(page);
        pageRequest = new PageRequest(0, 2);
        when( pissService.findByProjectAccession(PROJECT_ACCESSION, pageRequest) ).thenReturn(page);
        when( pissService.findByAssayAccession(ASSAY_ACCESSION, pageRequest) ).thenReturn(page);
    }

    @Test // /protein/list/project/{projectAccession}
    public void getProteinByProjectAccession() throws Exception {
        // test default use case
        mockMvc.perform(get("/protein/list/project/" + PROJECT_ACCESSION))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string(containsString(PROJECT_ACCESSION)))
                .andExpect(content().string(containsString(ASSAY_ACCESSION)))
                .andExpect(content().string(containsString(PROTEIN_ACCESSION)));
        // test with custom paging configuration
        mockMvc.perform(get("/protein/list/project/" + PROJECT_ACCESSION + "?show=2&page=0"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string(containsString(PROJECT_ACCESSION)))
                .andExpect(content().string(containsString(ASSAY_ACCESSION)))
                .andExpect(content().string(containsString(PROTEIN_ACCESSION)));
    }

    @Test // /protein/list/assay/{assayAccession}
    public void getProteinByAssayAccession() throws Exception {
        // test default use case
        mockMvc.perform(get("/protein/list/assay/" + ASSAY_ACCESSION))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string(containsString(PROJECT_ACCESSION)))
                .andExpect(content().string(containsString(ASSAY_ACCESSION)))
                .andExpect(content().string(containsString(PROTEIN_ACCESSION)));
        // test with custom paging configuration
        mockMvc.perform(get("/protein/list/assay/" + ASSAY_ACCESSION + "?show=2&page=0"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string(containsString(PROJECT_ACCESSION)))
                .andExpect(content().string(containsString(ASSAY_ACCESSION)))
                .andExpect(content().string(containsString(PROTEIN_ACCESSION)));
    }

}
