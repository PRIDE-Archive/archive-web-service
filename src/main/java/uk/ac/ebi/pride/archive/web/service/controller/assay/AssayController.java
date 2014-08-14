package uk.ac.ebi.pride.archive.web.service.controller.assay;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.social.InternalServerErrorException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import uk.ac.ebi.pride.archive.repo.assay.service.AssayAccessException;
import uk.ac.ebi.pride.archive.repo.assay.service.AssaySummary;
import uk.ac.ebi.pride.archive.repo.project.service.ProjectSummary;
import uk.ac.ebi.pride.archive.security.assay.AssaySecureService;
import uk.ac.ebi.pride.archive.security.project.ProjectSecureService;
import uk.ac.ebi.pride.archive.web.service.error.exception.ResourceNotFoundException;
import uk.ac.ebi.pride.archive.web.service.model.assay.AssayAccessionComparator;
import uk.ac.ebi.pride.archive.web.service.model.assay.AssayDetail;
import uk.ac.ebi.pride.archive.web.service.model.assay.AssayDetailList;
import uk.ac.ebi.pride.archive.web.service.util.IdMapper;
import uk.ac.ebi.pride.archive.web.service.util.ObjectMapper;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author Rui Wang
 * @author florian@ebi.ac.uk
 * @version 1.0.4
 */
@Api(value = "retrieve information about assays", position = 1)
@Controller
@RequestMapping("/assay")
public class AssayController {

    private static final Logger logger = LoggerFactory.getLogger(AssayController.class);

    @Autowired
    private AssaySecureService assayService;
    @Autowired
    private ProjectSecureService projectService;

    @ApiOperation(value = "retrieve assay information by accession", position = 1)
    @RequestMapping(value = "/{assayAccession}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public
    @ResponseBody
    AssayDetail findAllByAssayAccession(
            @ApiParam(value = "an assay accession number")
            @PathVariable("assayAccession") String assayAccession) {
        logger.info("Assay " + assayAccession + " summary requested");

        AssaySummary assaySummary;
        try {
            assaySummary = assayService.findByAccession(assayAccession);
        } catch (AssayAccessException aae) {
            logger.error("AssayAccessException trying to access assay: " + assayAccession, aae);
            throw new InternalServerErrorException("AssayAccessException for " + assayAccession);
        }
        if (assaySummary == null) {
            throw new ResourceNotFoundException("No assay found for accession: " + assayAccession);
        }
        // update the ID <-> accession caches
        updateAssayAccCache(assaySummary.getId(), assaySummary.getAccession());
        updateProjectAccCache(assaySummary.getProjectId());

        return ObjectMapper.mapAssaySummaryToWSAssayDetail(assaySummary);
    }


    @ApiOperation(value = "list assays for a project", position = 2)
    @RequestMapping(value = "/list/project/{projectAccession}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public
    @ResponseBody
    AssayDetailList findAllByProjectAccession(
            @ApiParam(value = "a project accession number")
            @PathVariable("projectAccession") String accession) {

        logger.info("Assay list required for project " + accession);

        // first check that the project exists, to evaluate if the request for assays is justified
        ProjectSummary projectSummary = projectService.findByAccession(accession);
        if (projectSummary == null) {
            throw new ResourceNotFoundException("Project does not exist: " + accession);
        }
        // update the ID <-> accession cache
        updateProjectAccCache(projectSummary.getId(), projectSummary.getAccession());

        // once the project accession is verified, retrieve the assay information (if there is any)
        Collection<AssaySummary> assaySummaries = assayService.findAllByProjectAccession(accession);
        if (assaySummaries == null || assaySummaries.isEmpty()) {
            return new AssayDetailList();
        }
        // update the ID <-> accession cache
        for (AssaySummary assaySummary : assaySummaries) {
            updateAssayAccCache(assaySummary.getId(), assaySummary.getAccession());
        }

        List<AssayDetail> assayDetails = ObjectMapper.mapAssaySummariesToWSAssayDetails(assaySummaries);
        // provide a default sorting by assay accession
        Collections.sort(assayDetails, new AssayAccessionComparator<AssayDetail>());

        return new AssayDetailList(assayDetails);
    }

    private void updateAssayAccCache(long assayID, String assayAccession) {
        if ( !IdMapper.getInstance().containsAssayId(assayID) ) {
            if (assayAccession == null || assayAccession.trim().isEmpty()) {
                AssaySummary assay = assayService.findById(assayID);
                assayAccession = assay.getAccession();
            }
            IdMapper.getInstance().storeAssayAccession(assayID, assayAccession);
        }
    }
    private void updateProjectAccCache(long projectID) {
        updateProjectAccCache(projectID, null);
    }
    private void updateProjectAccCache(long projectID, String projectAccession) {
        if ( !IdMapper.getInstance().containsProteinId(projectID) ) {
            if (projectAccession == null || projectAccession.trim().isEmpty()) {
                ProjectSummary project = projectService.findById(projectID);
                projectAccession = project.getAccession();
            }
            IdMapper.getInstance().storeProteinAccession(projectID, projectAccession);
        }
    }

}
