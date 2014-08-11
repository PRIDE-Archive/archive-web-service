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
import uk.ac.ebi.pride.archive.web.service.util.ObjectMapper;
import uk.ac.ebi.pride.archive.utils.config.FilePathBuilder;
import uk.ac.ebi.pride.archive.utils.streaming.FileUtils;

import java.io.File;
import java.util.Collection;

/**
 * Health check for the web service, to be used by nagios
 *
 * @author Rui Wang
 * @author Florian Reisinger
 * @since 1.0.4
 */
@Controller
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
    private FileSecureService fileService;

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
            ObjectMapper.mapProjectSummary2WSProjectDetail(projectSummary);
            // if there was no exception trying to map the search result, then we assume all is OK
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    private boolean testQueryForProjectFile() {
        try {
            // streaming the file to client
            ProjectSummary projectSummary = projectService.findByAccession(TEST_PROJECT_ACCESSION);
            if (projectSummary == null) { return false; }

            Collection<FileSummary> projectFiles = fileService.findAllByProjectAccession(TEST_PROJECT_ACCESSION);
            if (projectFiles == null || projectFiles.isEmpty()) { return false; }

            FileSummary fileSummary = projectFiles.iterator().next();

            String filePath = filePathBuilder.buildPublicationFilePath(fileLocationPrefix, projectSummary, fileSummary);
            if (filePath == null || filePath.trim().isEmpty()) { return false; }

            File fileToStream = fileUtils.findFileToStream(filePath);
            if (fileToStream == null) { return false; }

            // if there is no exception, we assume all is OK
            return true;

        } catch (Exception ex) {
            return false;
        }
    }


}
