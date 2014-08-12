package uk.ac.ebi.pride.archive.web.service.controller.protein;

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
import uk.ac.ebi.pride.proteinidentificationindex.search.model.ProteinIdentification;

import java.util.List;

/**
* @author Florian Reisinger
* @since 1.0.8
*/
@Api(value = "retrieve protein identifications", position = 4)
@Controller
@RequestMapping(value = "/protein")
public class ProteinController {


    // ToDo: before we can use this service for a public interface, the methods have to be secured, which currently is not easily possible


    private static final Logger logger = LoggerFactory.getLogger(ProteinController.class);

    protected static final int DEFAULT_SHOW = 10;
    protected static final int DEFAULT_PAGE = 1;


    @Autowired
    ProteinIdentificationSecureSearchService pissService; // ToDo: find a better name ;)

    // ToDo: count methods once available in the service
    // ToDo: performance tests (page sizes, security impact), and perhaps max number of retrievable (page size) results



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
            @ApiParam(value = "how many results to show per page")
            @RequestParam(value = "show", required = false, defaultValue = DEFAULT_SHOW+"") int showResults,
            @ApiParam(value = "which page of the result to return")
            @RequestParam(value = "page", required = false, defaultValue = DEFAULT_PAGE+"") int page
            ) {
        logger.info("Proteins for project " + projectAccession + " requested");

        List<ProteinDetail> resultProteins = null;
        try {
            List<ProteinIdentification> foundProteins = pissService.findByProjectAccession(projectAccession, new PageRequest(page, showResults)).getContent();
            // convert the searches List of ProteinIdentified objects into the WS ProteinDetail objects
            resultProteins = ObjectMapper.mapProteinIdentifiedListToWSProteinDetailList(foundProteins);
        } catch (Exception e) {
            // ToDo: handle properly!
            System.out.println("Exception during secure protein identification search: " + e.getStackTrace());
        }

        return new ProteinDetailList(resultProteins);
    }

    @ApiOperation(value = "retrieve protein identifications by assay accession", position = 3)
    @RequestMapping(value = "/list/assay/{assayAccession}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK) // 200
    public
    @ResponseBody
    ProteinDetailList getProteinsByAssay(
            @ApiParam(value = "an assay accession")
            @PathVariable("assayAccession") String assayAccession,
            @ApiParam(value = "how many results to show per page")
            @RequestParam(value = "show", required = false, defaultValue = DEFAULT_SHOW+"") int showResults,
            @ApiParam(value = "which page of the result to return")
            @RequestParam(value = "page", required = false, defaultValue = DEFAULT_PAGE+"") int page
            ) {
        logger.info("Proteins for assay " + assayAccession + " requested");

        List<ProteinDetail> resultProteins = null;
        try {
            List<ProteinIdentification> foundProteins = pissService.findByAssayAccession(assayAccession, new PageRequest(page, showResults)).getContent();
            // convert the searches List of ProteinIdentified objects into the WS ProteinDetail objects
            resultProteins = ObjectMapper.mapProteinIdentifiedListToWSProteinDetailList(foundProteins);
        } catch (Exception e) {
            // ToDo: handle properly!
            System.out.println("Exception during secure protein identification search service call " + e.getStackTrace());
        }

        return new ProteinDetailList(resultProteins);
    }

}
