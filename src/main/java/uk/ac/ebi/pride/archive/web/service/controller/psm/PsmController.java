package uk.ac.ebi.pride.archive.web.service.controller.psm;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import uk.ac.ebi.pride.archive.security.psm.PsmSecureSearchService;
import uk.ac.ebi.pride.archive.web.service.model.peptide.PsmDetail;
import uk.ac.ebi.pride.archive.web.service.model.peptide.PsmDetailList;
import uk.ac.ebi.pride.archive.web.service.util.ObjectMapper;
import uk.ac.ebi.pride.archive.web.service.util.WsUtils;
import uk.ac.ebi.pride.psmindex.search.model.Psm;

import java.util.List;

/**
* @author Florian Reisinger
* @since 1.0.8
*/
@Api(value = "peptide", description = "retrieve peptide identifications test", position = 4)
@Controller
@RequestMapping(value = "/peptide")
public class PsmController {

    private static final Logger logger = LoggerFactory.getLogger(PsmController.class);


    @Autowired
    PsmSecureSearchService psmSecureSearchService;


    // ToDo: performance tests (page sizes, security impact), and perhaps max number of retrievable (page size) results

    // ToDo: method not available, as not secured
//    @ApiOperation(value = "retrieve peptide identifications by sequence and project accession", position = 1)
//    @RequestMapping(value = "/list/{sequence}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
//    @ResponseStatus(HttpStatus.OK) // 200
//    public
//    @ResponseBody
//    PsmDetailList getPsmsBySequence(
//            @ApiParam(value = "a peptide sequence")
//            @PathVariable("sequence") String sequence
//    ) {
//        logger.info("Query for PSMs with sequence: " + sequence);
//
//        List<Psm> foundPsms;
//            foundPsms = psmSecureSearchService.findByPeptideSequence(sequence);
//
//        // convert the searches List of Psm objects into the WS PsmDetail objects
//        List<PsmDetail> resultPsms = ObjectMapper.mapPsmListToWSPsmDetailList(foundPsms);
//
//        return new PsmDetailList(resultPsms);
//    }

    // ToDo: method not available, as not secured
//    @ApiOperation(value = "retrieve peptide identifications by protein accession", position = 4)
//    @RequestMapping(value = "/list/protein/{proteinAccession}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
//    @ResponseStatus(HttpStatus.OK) // 200
//    public
//    @ResponseBody
//    PsmDetailList getPsmsByProtein(
//            @ApiParam(value = "an assay accession")
//            @PathVariable("proteinAccession") String proteinAccession
//    ) {
//        logger.info("Peptides for protein " + proteinAccession + " requested");
//
//        // ToDo: this should probably allow a search expansion using the proteins synonyms
//        List<Psm> foundPsms = psmSecureSearchService.findByProteinAccession(proteinAccession);
//
//        // convert the searches List of Psm objects into the WS PsmDetail objects
//        List<PsmDetail> resultPsms = ObjectMapper.mapPsmListToWSPsmDetailList(foundPsms);
//
//        return new PsmDetailList(resultPsms);
//    }


    @ApiOperation(value = "retrieve peptide identifications by project accession", position = 0)
    @RequestMapping(value = "/list/project/{projectAccession}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK) // 200
    public
    @ResponseBody
    PsmDetailList getPsmsByProject(
            @ApiParam(value = "a project accession")
            @PathVariable("projectAccession") String projectAccession,
            @ApiParam(value = "how many results to return per page")
            @RequestParam(value = "show", required = false, defaultValue = WsUtils.DEFAULT_SHOW+"") int showResults,
            @ApiParam(value = "which page (starting from 0) of the result to return")
            @RequestParam(value = "page", required = false, defaultValue = WsUtils.DEFAULT_PAGE+"") int page
    ) {
        logger.info("Peptides for project " + projectAccession + " requested");

        List<Psm> foundPsms = psmSecureSearchService.findByProjectAccession(projectAccession, new PageRequest(page, showResults, Sort.Direction.ASC, "peptide_sequence")).getContent();

        // convert the searches List of Psm objects into the WS PsmDetail objects
        List<PsmDetail> resultPsms = ObjectMapper.mapPsmListToWSPsmDetailList(foundPsms);

        return new PsmDetailList(resultPsms);
    }

    @ApiOperation(value = "count peptide identifications by project accession", position = 1)
    @RequestMapping(value = "/count/project/{projectAccession}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK) // 200
    public
    @ResponseBody
    Long countPsmsByProject(
            @ApiParam(value = "a project accession")
            @PathVariable("projectAccession") String projectAccession
    ) {
        logger.info("PSM count for assay " + projectAccession + " requested");

        Long foundPsms = psmSecureSearchService.countByProjectAccession(projectAccession);

        logger.debug( foundPsms + " PSMs for assay " + projectAccession);
        return foundPsms;
    }

    @ApiOperation(value = "retrieve peptide identifications by project accession and peptide sequence", position = 2)
    @RequestMapping(value = "/list/project/{projectAccession}/sequence/{sequence}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK) // 200
    public
    @ResponseBody
    PsmDetailList getPsmsByProjectAndSequence(
            @ApiParam(value = "a project accession")
            @PathVariable("projectAccession") String projectAccession,
            @ApiParam(value = "the peptide sequence to limit the query on")
            @PathVariable("sequence") String sequence
    ) {
        logger.info("Request for peptides for project " + projectAccession + " with sequence: " + sequence);

        List<Psm> foundPsms = psmSecureSearchService.findByPeptideSequenceAndProjectAccession(sequence, projectAccession);

        // convert the searches List of Psm objects into the WS PsmDetail objects
        List<PsmDetail> resultPsms = ObjectMapper.mapPsmListToWSPsmDetailList(foundPsms);

        return new PsmDetailList(resultPsms);
    }

    @ApiOperation(value = "count peptide identifications by project accession and peptide sequence", position = 3)
    @RequestMapping(value = "/count/project/{projectAccession}/sequence/{sequence}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK) // 200
    public
    @ResponseBody
    Long countPsmsByProjectAndSequence(
            @ApiParam(value = "a project accession")
            @PathVariable("projectAccession") String projectAccession,
            @ApiParam(value = "the peptide sequence to limit the query on")
            @PathVariable("sequence") String sequence
    ) {
        logger.info("PSM count for assay " + projectAccession + " requested");

        Long foundPsms = psmSecureSearchService.countByPeptideSequenceAndProjectAccession(sequence, projectAccession);

        logger.debug( foundPsms + " PSMs for assay " + projectAccession);
        return foundPsms;
    }

    @ApiOperation(value = "retrieve peptide identifications by assay accession", position = 4)
    @RequestMapping(value = "/list/assay/{assayAccession}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK) // 200
    public
    @ResponseBody
    PsmDetailList getPsmsByAssay(
            @ApiParam(value = "an assay accession")
            @PathVariable("assayAccession") String assayAccession,
            @ApiParam(value = "how many results to return per page")
            @RequestParam(value = "show", required = false, defaultValue = WsUtils.DEFAULT_SHOW+"") int showResults,
            @ApiParam(value = "which page (starting from 0) of the result to return")
            @RequestParam(value = "page", required = false, defaultValue = WsUtils.DEFAULT_PAGE+"") int page
    ) {
        logger.info("PSMs for assay " + assayAccession + " requested");

        List<Psm> foundPsms = psmSecureSearchService.findByAssayAccession(assayAccession, new PageRequest(page, showResults, Sort.Direction.ASC, "peptide_sequence")).getContent();

        // convert the searches List of Psm objects into the WS PsmDetail objects
        List<PsmDetail> resultPsms = ObjectMapper.mapPsmListToWSPsmDetailList(foundPsms);

        return new PsmDetailList(resultPsms);
    }


    @ApiOperation(value = "count peptide identifications by assay accession", position = 5)
    @RequestMapping(value = "/count/assay/{assayAccession}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK) // 200
    public
    @ResponseBody
    Long countPsmsByAssay(
            @ApiParam(value = "an assay accession")
            @PathVariable("assayAccession") String assayAccession
            ) {
        logger.info("PSM count for assay " + assayAccession + " requested");

        Long foundPsms = psmSecureSearchService.countByAssayAccession(assayAccession);

        logger.debug( foundPsms + " PSMs for assay " + assayAccession);
        return foundPsms;
    }

    @ApiOperation(value = "retrieve peptide identifications by assay accession and peptide sequence", position = 6)
    @RequestMapping(value = "/list/assay/{assayAccession}/sequence/{sequence}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK) // 200
    public
    @ResponseBody
    PsmDetailList getPsmsByAssayAndSequence(
            @ApiParam(value = "a assay accession")
            @PathVariable("assayAccession") String assayAccession,
            @ApiParam(value = "the peptide sequence to limit the query on")
            @PathVariable("sequence") String sequence
    ) {
        logger.info("Request for peptides for assay " + assayAccession + " with sequence: " + sequence);

        List<Psm> foundPsms = psmSecureSearchService.findByPeptideSequenceAndAssayAccession(sequence, assayAccession);

        // convert the searches List of Psm objects into the WS PsmDetail objects
        List<PsmDetail> resultPsms = ObjectMapper.mapPsmListToWSPsmDetailList(foundPsms);

        return new PsmDetailList(resultPsms);
    }

    @ApiOperation(value = "count peptide identifications by assay accession and peptide sequence", position = 7)
    @RequestMapping(value = "/count/assay/{assayAccession}/sequence/{sequence}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK) // 200
    public
    @ResponseBody
    Long countPsmsByAssayAndSequence(
            @ApiParam(value = "a assay accession")
            @PathVariable("assayAccession") String assayAccession,
            @ApiParam(value = "the peptide sequence to limit the query on")
            @PathVariable("sequence") String sequence
    ) {
        logger.info("PSM count for assay " + assayAccession + " requested");

        Long foundPsms = psmSecureSearchService.countByPeptideSequenceAndAssayAccession(sequence, assayAccession);

        logger.debug( foundPsms + " PSMs for assay " + assayAccession);
        return foundPsms;
    }


}
