package uk.ac.ebi.pride.archive.web.service.controller.check;

import com.mangofactory.swagger.annotations.ApiIgnore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import uk.ac.ebi.pride.archive.repo.file.service.FileSummary;
import uk.ac.ebi.pride.archive.repo.project.service.ProjectSummary;
import uk.ac.ebi.pride.archive.security.file.FileSecureService;
import uk.ac.ebi.pride.archive.security.project.ProjectSecureService;
import uk.ac.ebi.pride.archive.web.service.controller.file.FileController;
import uk.ac.ebi.pride.archive.web.service.model.file.FileDetail;
import uk.ac.ebi.pride.archive.web.service.model.file.FileDetailList;
import uk.ac.ebi.pride.archive.web.service.util.ObjectMapper;
import uk.ac.ebi.pride.archive.utils.config.FilePathBuilder;
import uk.ac.ebi.pride.archive.utils.streaming.FileUtils;

import java.io.File;
import java.net.URL;
import java.util.Collection;

/**
 * Health check for the web service, to be used by nagios
 *
 * @author Rui Wang
 * @author Florian Reisinger
 * @since 1.0.4
 */
@Controller
@ApiIgnore
@RequestMapping("/check")
public class CheckController {

    private static final String TEST_PROJECT_ACCESSION = "PXD000001";

    @Value("#{buildConfig['prider.ws.project.name']}")
    private String projectName;

    @Value("#{buildConfig['prider.ws.project.version']}")
    private String projectVersion;

    @Value("#{buildConfig['prider.ws.build.profiles']}")
    private String buildProfile;

    @Value("#{buildConfig['prider.ws.build.user.name']}")
    private String buildUserName;

    @Value("#{buildConfig['prider.ws.service.layer.version']}")
    private String serviceLayerVersion;

    @Value("#{fileConfig['file.location.prefix']}")
    private String fileLocationPrefix;

    @Autowired
    private ProjectSecureService projectService;

    @Autowired
    private FileController fileController;

    @Autowired
    private FileUtils fileUtils;

    @Autowired
    private FilePathBuilder filePathBuilder;

    @ApiIgnore
    @RequestMapping(method = RequestMethod.GET, produces = MediaType.TEXT_PLAIN_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public String healthCheck() {
        StringBuilder builder = new StringBuilder();

        // check overall health
        boolean projectHealthy = testProjectQuery();
        boolean projectFileDownloadHealthy = testQueryForProjectFile();

        String lineSeparator = System.getProperty("line.separator");

        builder.append("Overall Check: ").append(projectHealthy && projectFileDownloadHealthy).append(lineSeparator);
        builder.append("Project Query Check: ").append(projectHealthy).append(lineSeparator);
        builder.append("Project File Download Check: ").append(projectFileDownloadHealthy).append(lineSeparator);

        // project name
        builder.append("Project Name: ").append(projectName).append(lineSeparator);

        // project version
        builder.append("Project Version: ").append(projectVersion).append(lineSeparator);

        // build profile
        builder.append("Build Profile: ").append(buildProfile).append(lineSeparator);

        // build user name
        builder.append("Build User: ").append(buildUserName).append(lineSeparator);

        // Service layer version
        builder.append("Service Layer Version: ").append(serviceLayerVersion).append(lineSeparator);

        return builder.toString();
    }

    private boolean testProjectQuery() {
        try {
            ProjectSummary projectSummary = projectService.findByAccession(TEST_PROJECT_ACCESSION);
            if (projectSummary == null) { return false; }
            if ( !projectSummary.getAccession().equalsIgnoreCase(TEST_PROJECT_ACCESSION) ) { return false; }
            ObjectMapper.mapProjectSummary2WSProjectDetail(projectSummary, null);
            // if there was no exception trying to map the search result, then we assume all is OK
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    private boolean testQueryForProjectFile() {
        try {
            // retrieve the file records for the test project
            FileDetailList projectFiles = fileController.getFilesByProjectAccession(TEST_PROJECT_ACCESSION);
            if (projectFiles == null || projectFiles.getList() == null || projectFiles.getList().isEmpty()) { return false; }

            // get the first file to run some more checks
            FileDetail fileDetail = projectFiles.getList().iterator().next();

            // there has to be a non-empty file name
            String fileName = fileDetail.getFileName();
            if (fileName == null || fileName.trim().isEmpty()) { return false; }

            // there has to be a download URL (since we are testing a public project)
            URL fileUrl = fileDetail.getDownloadLink();
            if (fileUrl == null) { return false; }

            // the download URL has to end in the file name
            String fileString = fileUrl.getFile();
            if (fileString == null || !fileString.endsWith(fileName)) { return false; }

            // we don't test if the file is actually available at the provided URL

            // if all checks passed and there is no exception, we assume all is OK
            return true;

        } catch (Exception ex) {
            return false;
        }
    }


}
