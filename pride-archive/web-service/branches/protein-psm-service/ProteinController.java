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
import uk.ac.ebi.pride.archive.web.service.model.protein.ProteinDetail;
import uk.ac.ebi.pride.archive.web.service.model.protein.ProteinDetailList;
import uk.ac.ebi.pride.archive.web.service.util.ObjectMapper;
import uk.ac.ebi.pride.proteinindex.search.model.ProteinIdentified;
import uk.ac.ebi.pride.proteinindex.search.search.service.ProteinIdentificationSearchService;

import java.util.List;

/**
* @author Florian Reisinger
* @since 1.0.8
*/
@Api(value = "retrieve protein identifications", position = 4)
@Controller
@RequestMapping(value = "/protein")
public class ProteinController {

    private static final Logger logger = LoggerFactory.getLogger(ProteinController.class);

    protected static final int DEFAULT_SHOW = 10;
    protected static final int DEFAULT_PAGE = 1;


    @Autowired
    ProteinIdentificationSearchService proteinIdentificationSearchService;

    // ToDo: count methods once available in the service
    // ToDo: performance tests (page sizes, security impact), and perhaps max number of retrievable (page size) results



    @ApiOperation(value = "retrieve protein identifications by protein identifier", position = 1)
    @RequestMapping(value = "/{proteinId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK) // 200
    public
    @ResponseBody
    ProteinDetailList getProteinSummary(
            @ApiParam(value = "a protein identifier")
            @PathVariable("proteinId") String id,
            @ApiParam(value = "whether to include alternative protein ids")
            @RequestParam(value = "includeSynonyms", required = false, defaultValue = "false") boolean includeSynonyms
            ) {
        logger.info("Protein " + id + " summary requested");

        List<ProteinIdentified> foundProteins;
        if (includeSynonyms) {
            foundProteins = proteinIdentificationSearchService.findBySynonyms(id);
        } else {
            foundProteins = proteinIdentificationSearchService.findByAccession(id);
        }

        // convert the searches List of ProteinIdentified objects into the WS ProteinDetail objects
        List<ProteinDetail> resultProteins = ObjectMapper.mapProteinIdentifiedListToWSProteinDetailList(foundProteins);

        return new ProteinDetailList(resultProteins);
    }


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

        List<ProteinIdentified> foundProteins = proteinIdentificationSearchService.findByProjectAccessions(projectAccession, new PageRequest(page, showResults));

        // convert the searches List of ProteinIdentified objects into the WS ProteinDetail objects
        List<ProteinDetail> resultProteins = ObjectMapper.mapProteinIdentifiedListToWSProteinDetailList(foundProteins);

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

        List<ProteinIdentified> foundProteins = proteinIdentificationSearchService.findByAssayAccessions(assayAccession, new PageRequest(page, showResults));

        // convert the searches List of ProteinIdentified objects into the WS ProteinDetail objects
        List<ProteinDetail> resultProteins = ObjectMapper.mapProteinIdentifiedListToWSProteinDetailList(foundProteins);

        return new ProteinDetailList(resultProteins);
    }
}
