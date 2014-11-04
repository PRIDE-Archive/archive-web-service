package uk.ac.ebi.pride.archive.web.service.controller.protein;

import com.mangofactory.swagger.annotations.ApiIgnore;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import uk.ac.ebi.pride.archive.security.protein.ProteinIdentificationSecureSearchService;
import uk.ac.ebi.pride.archive.web.service.model.protein.ProteinDetail;
import uk.ac.ebi.pride.archive.web.service.model.protein.ProteinDetailList;
import uk.ac.ebi.pride.archive.web.service.util.ObjectMapper;
import uk.ac.ebi.pride.archive.web.service.util.WsUtils;
import uk.ac.ebi.pride.proteinidentificationindex.search.model.ProteinIdentification;

import java.util.List;
import java.util.Set;

/**
* @author Florian Reisinger
* @since 1.0.8
*/
@Api(value = "protein", description = "retrieve protein identifications", position = 3)
@Controller
@RequestMapping(value = "/protein")
public class ProteinController {

    private static final Logger logger = LoggerFactory.getLogger(ProteinController.class);


    @Autowired
    ProteinIdentificationSecureSearchService pissService; // ToDo: find a better name ;)

    // ToDo: count methods once available in the service
    // ToDo: performance tests (page sizes, security impact), and perhaps max number of retrievable (page size) results



//    not available as not secured yet
//    @ApiOperation(value = "retrieve protein identifications by protein identifier", position = 1)
//    @RequestMapping(value = "/{proteinId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
//    @ResponseStatus(HttpStatus.OK) // 200
//    public
//    @ResponseBody
//    ProteinDetailList getProteinSummary(
//            @ApiParam(value = "a protein identifier")
//            @PathVariable("proteinId") String id,
//            @ApiParam(value = "whether to include alternative protein ids")
//            @RequestParam(value = "includeSynonyms", required = false, defaultValue = "false") boolean includeSynonyms
//            ) {
//        logger.info("Protein " + id + " summary requested");
//
//        List<ProteinIdentified> foundProteins;
//        if (includeSynonyms) {
//            foundProteins = proteinIdentificationSearchService.findBySynonyms(id);
//        } else {
//            foundProteins = proteinIdentificationSearchService.findByAccession(id);
//        }
//
//        // convert the searches List of ProteinIdentified objects into the WS ProteinDetail objects
//        List<ProteinDetail> resultProteins = ObjectMapper.mapProteinIdentifiedListToWSProteinDetailList(foundProteins);
//
//        return new ProteinDetailList(resultProteins);
//    }


    @ApiOperation(value = "retrieve protein identifications by project accession", position = 2)
    @RequestMapping(value = "/list/project/{projectAccession}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK) // 200
    public
    @ResponseBody
    ProteinDetailList getProteinsByProject(
            @ApiParam(value = "a project accession")
            @PathVariable("projectAccession") String projectAccession,
            @ApiParam(value = "how many results to return per page")
            @RequestParam(value = "show", required = false, defaultValue = WsUtils.DEFAULT_SHOW+"") int showResults,
            @ApiParam(value = "which page (starting from 1) of the result to return")
            @RequestParam(value = "page", required = false, defaultValue = WsUtils.DEFAULT_PAGE+"") int page
            ) {
        logger.info("Proteins for project " + projectAccession + " requested");

        List<ProteinIdentification> foundProteins = pissService.findByProjectAccession(projectAccession, new PageRequest(page, showResults)).getContent();

        // convert the searches List of ProteinIdentified objects into the WS ProteinDetail objects
        List<ProteinDetail>resultProteins = ObjectMapper.mapProteinIdentifiedListToWSProteinDetailList(foundProteins);

        return new ProteinDetailList(resultProteins);
    }

    @ApiOperation(value = "count protein identifications by project accession", position = 3)
    @RequestMapping(value = "/count/project/{projectAccession}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK) // 200
    public
    @ResponseBody
    Long countProteinsByProject(
            @ApiParam(value = "a project accession")
            @PathVariable("projectAccession") String projectAccession
    ) {
        logger.info("Protein count for project " + projectAccession + " requested");
        return pissService.countByProjectAccession(projectAccession);
    }

    @ApiOperation(value = "retrieve protein identifications by assay accession", position = 4)
    @RequestMapping(value = "/list/assay/{assayAccession}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK) // 200
    public
    @ResponseBody
    ProteinDetailList getProteinsByAssay(
            @ApiParam(value = "an assay accession")
            @PathVariable("assayAccession") String assayAccession,
            @ApiParam(value = "how many results to return per page")
            @RequestParam(value = "show", required = false, defaultValue = WsUtils.DEFAULT_SHOW+"") int showResults,
            @ApiParam(value = "which page (starting from 1) of the result to return")
            @RequestParam(value = "page", required = false, defaultValue = WsUtils.DEFAULT_PAGE+"") int page
            ) {
        logger.info("Proteins for assay " + assayAccession + " requested");

        List<ProteinIdentification> foundProteins = pissService.findByAssayAccession(assayAccession, new PageRequest(page, showResults)).getContent();
        // convert the searches List of ProteinIdentified objects into the WS ProteinDetail objects
        List<ProteinDetail> resultProteins = ObjectMapper.mapProteinIdentifiedListToWSProteinDetailList(foundProteins);

        return new ProteinDetailList(resultProteins);
    }

    @ApiOperation(value = "count protein identifications by assay accession", position = 4)
    @RequestMapping(value = "/count/assay/{assayAccession}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK) // 200
    public
    @ResponseBody
    Long countProteinsByAssay(
            @ApiParam(value = "an assay accession")
            @PathVariable("assayAccession") String assayAccession
    ) {
        logger.info("Proteins for assay " + assayAccession + " requested");
        return pissService.countByAssayAccession(assayAccession);
    }


    @ApiIgnore
    @RequestMapping(value = "/list/assay/{assayAccession}.acc", method = RequestMethod.GET, produces = MediaType.TEXT_PLAIN_VALUE)
    @ResponseStatus(HttpStatus.OK) // 200
    public
    @ResponseBody
    String getProteinListForAssay(
            @ApiParam(value = "an assay accession")
            @PathVariable("assayAccession") String assayAccession,
            @ApiParam(value = "filter accessions (to remove decoy, reverse, etc accessions)")
            @RequestParam(value = "filter", required = false, defaultValue = "true") boolean filter
    ) {
        logger.info("Protein accessions for assay " + assayAccession + " requested");
        StringBuilder sb = new StringBuilder();
        sb.append("#PRIDE assay:").append(assayAccession).append("\n");
        Set<String> accessions = pissService.getUniqueProteinAccessionsByAssayAccession(assayAccession);
        for (String accession : accessions) {
            if (filter && !isValidAccession(accession)) {
                // if filtering is enabled, we apply accession filtering to remove decoy, etc accessions
                continue;
            }
            sb.append(accession).append("\n");
        }
        return sb.toString();
    }
    @ApiIgnore
    @RequestMapping(value = "/list/project/{projectAccession}.acc", method = RequestMethod.GET, produces = MediaType.TEXT_PLAIN_VALUE)
    @ResponseStatus(HttpStatus.OK) // 200
    public
    @ResponseBody
    String getProteinListForProject(
            @ApiParam(value = "an project accession")
            @PathVariable("projectAccession") String projectAccession,
            @ApiParam(value = "filter accessions (to remove decoy, reverse, etc accessions)")
            @RequestParam(value = "filter", required = false, defaultValue = "true") boolean filter
    ) {
        logger.info("Protein accessions for project " + projectAccession + " requested");
        StringBuilder sb = new StringBuilder();
        sb.append("#PRIDE project:").append(projectAccession).append("\n");
        Set<String> accessions = pissService.getUniqueProteinAccessionsByProjectAccession(projectAccession);
        for (String accession : accessions) {
            if (filter && !isValidAccession(accession)) {
                // if filtering is enabled, we apply accession filtering to remove decoy, etc accessions
                continue;
            }
            sb.append(accession).append("\n");
        }
        return sb.toString();
    }

    private static boolean isValidAccession(String accession) {
        // ToDo: this should probably be a general utility for use in different projects
        // ToDo: extend with more cases!
        return !accession.toUpperCase().contains("DECOY") && !accession.toUpperCase().contains("REVERSE");

    }


}
