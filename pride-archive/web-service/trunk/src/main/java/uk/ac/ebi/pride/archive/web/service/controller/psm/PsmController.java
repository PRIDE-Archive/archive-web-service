package uk.ac.ebi.pride.archive.web.service.controller.psm;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
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
import uk.ac.ebi.pride.psmindex.search.model.Psm;

import java.util.List;

/**
* @author Florian Reisinger
* @since 1.0.8
*/
@Api(value = "retrieve peptide identifications", position = 4)
@Controller
@RequestMapping(value = "/peptide")
public class PsmController {

    private static final Logger logger = LoggerFactory.getLogger(PsmController.class);

    protected static final int DEFAULT_SHOW = 10;
    protected static final int DEFAULT_PAGE = 1;


    @Autowired
    PsmSecureSearchService psmSecureSearchService;


    // ToDo: count methods once available in the service
    // ToDo: performance tests (page sizes, security impact), and perhaps max number of retrievable (page size) results

//    method not available, as not secured
//    @ApiOperation(value = "retrieve peptide identifications by sequence", position = 1)
//    @RequestMapping(value = "/{sequence}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
//    @ResponseStatus(HttpStatus.OK) // 200
//    public
//    @ResponseBody
//    PsmDetailList getPeptideBySequence(
//            @ApiParam(value = "a peptide sequence")
//            @PathVariable("sequence") String sequence,
//            @ApiParam(value = "how many results to show per page")
//            @RequestParam(value = "show", required = false, defaultValue = DEFAULT_SHOW+"") int showResults,
//            @ApiParam(value = "which page of the result to return")
//            @RequestParam(value = "page", required = false, defaultValue = DEFAULT_PAGE+"") int page
//    ) {
//        logger.info("Query for peptide with sequence: " + sequence);
//
//        List<Psm> foundPsms = psmSecureSearchService.findByPeptideSequence(sequence, new PageRequest(page, showResults));
//
//        // convert the searches List of ProteinIdentified objects into the WS ProteinDetail objects
//        List<PsmDetail> resultPsms = ObjectMapper.mapPsmListToWSPsmDetailList(foundPsms);
//
//        return new PsmDetailList(resultPsms);
//    }

      // ToDo: method not available, as not secured
//    @ApiOperation(value = "retrieve peptide identifications by extended sequence search", position = 2)
//    @RequestMapping(value = "/list/ext/{sequence}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
//    @ResponseStatus(HttpStatus.OK) // 200
//    public
//    @ResponseBody
//    PsmDetailList getPeptideByExtendedSequenceSearch(
//            @ApiParam(value = "a peptide sequence")
//            @PathVariable("sequence") String sequence,
//            @ApiParam(value = "how many results to show per page")
//            @RequestParam(value = "show", required = false, defaultValue = DEFAULT_SHOW+"") int showResults,
//            @ApiParam(value = "which page of the result to return")
//            @RequestParam(value = "page", required = false, defaultValue = DEFAULT_PAGE+"") int page
//    ) {
//        logger.info("Extended query for peptide with sequence: " + sequence);
//
//        List<Psm> foundPsms = psmSecureSearchService.findByPeptideSubSequence(sequence, new PageRequest(page, showResults)).getContent();
//
//        // convert the searches List of ProteinIdentified objects into the WS ProteinDetail objects
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
//    PsmDetailList getProteinsByProtein(
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


    @ApiOperation(value = "retrieve peptide identifications by project accession", position = 3)
    @RequestMapping(value = "/list/project/{projectAccession}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK) // 200
    public
    @ResponseBody
    PsmDetailList getPeptidesByProject(
            @ApiParam(value = "a project accession")
            @PathVariable("projectAccession") String projectAccession,
            @ApiParam(value = "how many results to show per page")
            @RequestParam(value = "show", required = false, defaultValue = DEFAULT_SHOW+"") int showResults,
            @ApiParam(value = "which page of the result to return")
            @RequestParam(value = "page", required = false, defaultValue = DEFAULT_PAGE+"") int page
    ) {
        logger.info("Peptides for project " + projectAccession + " requested");

        List<Psm> foundPsms = psmSecureSearchService.findByProjectAccession(projectAccession, new PageRequest(page, showResults, Sort.Direction.ASC, "peptide_sequence")).getContent();

        // convert the searches List of Psm objects into the WS PsmDetail objects
        List<PsmDetail> resultPsms = ObjectMapper.mapPsmListToWSPsmDetailList(foundPsms);

        return new PsmDetailList(resultPsms);
    }

    @ApiOperation(value = "retrieve peptide identifications by assay accession", position = 4)
    @RequestMapping(value = "/list/assay/{assayAccession}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK) // 200
    public
    @ResponseBody
    PsmDetailList getProteinsByAssay(
            @ApiParam(value = "an assay accession")
            @PathVariable("assayAccession") String assayAccession,
            @ApiParam(value = "how many results to show per page")
            @RequestParam(value = "show", required = false, defaultValue = DEFAULT_SHOW+"") int showResults,
            @ApiParam(value = "which page of the result to return")
            @RequestParam(value = "page", required = false, defaultValue = DEFAULT_PAGE+"") int page
    ) {
        logger.info("Proteins for assay " + assayAccession + " requested");

        List<Psm> foundPsms = psmSecureSearchService.findByAssayAccession(assayAccession, new PageRequest(page, showResults, Sort.Direction.ASC, "peptide_sequence")).getContent();

        // convert the searches List of Psm objects into the WS PsmDetail objects
        List<PsmDetail> resultPsms = ObjectMapper.mapPsmListToWSPsmDetailList(foundPsms);

        return new PsmDetailList(resultPsms);
    }

}
