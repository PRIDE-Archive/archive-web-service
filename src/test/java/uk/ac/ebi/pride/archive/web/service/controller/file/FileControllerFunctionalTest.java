package uk.ac.ebi.pride.archive.web.service.controller.file;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import uk.ac.ebi.pride.archive.dataprovider.file.ProjectFileType;
import uk.ac.ebi.pride.archive.repo.assay.service.AssaySummary;
import uk.ac.ebi.pride.archive.repo.file.service.FileSummary;
import uk.ac.ebi.pride.archive.repo.project.service.ProjectSummary;
import uk.ac.ebi.pride.archive.security.assay.AssaySecureServiceImpl;
import uk.ac.ebi.pride.archive.security.file.FileSecureServiceImpl;
import uk.ac.ebi.pride.archive.security.project.ProjectSecureServiceImpl;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration({"classpath:test-context.xml", "classpath:mvc-config.xml", "classpath:spring-mongo-test-context.xml"})
public class FileControllerFunctionalTest {
    @Autowired
    private WebApplicationContext webApplicationContext;

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    private MockMvc mockMvc;

    @Autowired
    private FileSecureServiceImpl fileSecureServiceImpl;
    @Autowired
    private ProjectSecureServiceImpl projectSecureServiceImpl;
    @Autowired
    private AssaySecureServiceImpl assaySecureServiceImpl;

    // mock data values
    private static final String PROJECT_ACCESSION = "PXTEST1";
    private static final long PROJECT_ID = 100001;
    private static final String ASSAY_ACCESSION = "9876";
    private static final long ASSAY_ID = 200001;
    private static final int FILE_SIZE = 1000;
    private static final String FILE_NAME = "aFileName";
    private static final String FTP_PATH_FRAGMENT = "ftp://ftp.pride.ebi.ac.uk/pride/data/archive/2010/01/" + PROJECT_ACCESSION + "/" + FILE_NAME;

    @Before
    public void setUp() throws Exception {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.webApplicationContext).build();

        ///// mock results
        FileSummary fileSummary = new FileSummary();
        fileSummary.setFileName(FILE_NAME);
        fileSummary.setFileSize(FILE_SIZE);
        fileSummary.setFileType(ProjectFileType.RESULT);
        fileSummary.setAssayId(ASSAY_ID);
        fileSummary.setProjectId(PROJECT_ID);

        Collection<FileSummary> files = new HashSet<FileSummary>();
        files.add(fileSummary);


        ///// mock services
        // mock file service
        when(fileSecureServiceImpl.findAllByProjectAccession(PROJECT_ACCESSION)).thenReturn(files);
        when(fileSecureServiceImpl.findAllByAssayAccession(ASSAY_ACCESSION)).thenReturn(files);
//        when(fileServiceImpl.findById(fileId)).thenReturn(fileSummary); // not tested since deprecated internal functionality


        // mock project service (used for mapping of project IDs to project accessions)
        ProjectSummary projectSummary = new ProjectSummary();
        projectSummary.setId(PROJECT_ID);
        projectSummary.setAccession(PROJECT_ACCESSION);
        projectSummary.setPublicProject(true);
        // set a date, so the FTP download path can be created (needs to match the FTP path defined above)
        Date date = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).parse("2010-01-30");
        projectSummary.setPublicationDate(date);

        when(projectSecureServiceImpl.findByAccession(PROJECT_ACCESSION)).thenReturn(projectSummary);
        when(projectSecureServiceImpl.findById(PROJECT_ID)).thenReturn(projectSummary);


        // mock assay service (used for mapping of assay IDs to assay accessions)
        AssaySummary assaySummary = new AssaySummary();
        assaySummary.setId(ASSAY_ID);
        assaySummary.setAccession(ASSAY_ACCESSION);

        when(assaySecureServiceImpl.findById(ASSAY_ID)).thenReturn(assaySummary);

    }

    @Test // /file/list/project/{projectAccession}
    public void getFilesByProjectReturnsFileSummary() throws Exception {
        mockMvc.perform(get("/file/list/project/" + PROJECT_ACCESSION))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(content().string(containsString(FILE_NAME)))
                .andExpect(content().string(containsString(ASSAY_ACCESSION)))
                .andExpect(content().string(containsString(PROJECT_ACCESSION)))
                .andExpect(content().string(containsString(FTP_PATH_FRAGMENT)))
                .andExpect(content().string(containsString(""+ FILE_SIZE)));
    }

    @Test // /file/list/assay/{assayAccession}
    public void getFilesByAssayReturnsFileSummary() throws Exception {
        mockMvc.perform(get("/file/list/assay/" + ASSAY_ACCESSION))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(content().string(containsString(FILE_NAME)))
                .andExpect(content().string(containsString(ASSAY_ACCESSION)))
                .andExpect(content().string(containsString(PROJECT_ACCESSION)))
                .andExpect(content().string(containsString(FTP_PATH_FRAGMENT)))
                .andExpect(content().string(containsString(""+ FILE_SIZE)));
    }

}
