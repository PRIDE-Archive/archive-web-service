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
import uk.ac.ebi.pride.archive.security.protein.MongoProteinIdentificationSecureSearchService;
import uk.ac.ebi.pride.archive.security.protein.ProteinIdentificationSecureSearchService;
import uk.ac.ebi.pride.archive.web.service.error.exception.MaxPageSizeReachedException;
import uk.ac.ebi.pride.archive.web.service.model.protein.ProteinDetailList;
import uk.ac.ebi.pride.archive.web.service.util.ObjectMapper;
import uk.ac.ebi.pride.archive.web.service.util.WsUtils;
import uk.ac.ebi.pride.proteinidentificationindex.search.model.ProteinIdentification;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
  ProteinIdentificationSecureSearchService proteinIdService;

  @Autowired
  MongoProteinIdentificationSecureSearchService mongoProteinIdService;

  @ApiOperation(value = "retrieve protein identifications by project accession", position = 1)
  @RequestMapping(value = "/list/project/{projectAccession}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
  @ResponseStatus(HttpStatus.OK) // 200
  public
  @ResponseBody
  ProteinDetailList getProteinsByProject(
      @ApiParam(value = "a project accession (example: PXD000001)")
      @PathVariable("projectAccession") String projectAccession,
      @ApiParam(value = "how many results to return per page. Maximum page size is: " + WsUtils.MAX_PAGE_SIZE)
      @RequestParam(value = "show", required = false, defaultValue = WsUtils.DEFAULT_SHOW+"") int showResults,
      @ApiParam(value = "which page (starting from 0) of the result to return")
      @RequestParam(value = "page", required = false, defaultValue = WsUtils.DEFAULT_PAGE+"") int page
  ) {
    logger.info("Proteins for project " + projectAccession + " requested");
    if(showResults > WsUtils.MAX_PAGE_SIZE){
      logger.error("Maximum size of page reach");
      throw new MaxPageSizeReachedException("The number of items requested exceed the maximum size for the page: "+ WsUtils.MAX_PAGE_SIZE);
    }
    return getPsmDetailList(proteinIdService.findByProjectAccession(projectAccession, new PageRequest(page, showResults)).getContent());
  }

  @ApiOperation(value = "count protein identifications by project accession", position = 4)
  @RequestMapping(value = "/count/project/{projectAccession}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
  @ResponseStatus(HttpStatus.OK) // 200
  public
  @ResponseBody
  Long countProteinsByProject(
      @ApiParam(value = "a project accession (example: PXD000001)")
      @PathVariable("projectAccession") String projectAccession
  ) {
    logger.info("Protein count for project " + projectAccession + " requested");
    return proteinIdService.countByProjectAccession(projectAccession);
  }

  @ApiOperation(value = "retrieve protein identifications by project accession and protein accession", position = 2)
  @RequestMapping(value = "/list/project/{projectAccession}/protein/{accession}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
  @ResponseStatus(HttpStatus.OK) // 200
  public
  @ResponseBody
  ProteinDetailList getProteinsByProjectAndAccession(
      @ApiParam(value = "a project accession (example: PXD001536)")
      @PathVariable("projectAccession") String projectAccession,
      @ApiParam(value = "a protein accession (example: P38398)")
      @PathVariable("accession") String accession
  ) {
    logger.info("Proteins for project " + projectAccession + "and accession " + accession + " requested");
    return getPsmDetailList(proteinIdService.findByProjectAccessionAndAccession(projectAccession, accession));
  }

  @ApiOperation(value = "count protein identifications by project accession and protein accession", position = 3)
  @RequestMapping(value = "/count/project/{projectAccession}/protein/{accession}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
  @ResponseStatus(HttpStatus.OK) // 200
  public
  @ResponseBody
  Long countProteinsByProjectAndAccession(
      @ApiParam(value = "a project accession (example: PXD001536)")
      @PathVariable("projectAccession") String projectAccession,
      @ApiParam(value = "a protein accession (example: P38398)")
      @PathVariable("accession") String accession
  ) {
    logger.info("Protein count for project " + projectAccession + "and accession " + accession + " requested");
    return proteinIdService.countByProjectAccessionAndAccession(projectAccession, accession);
  }

  @ApiOperation(value = "retrieve protein identifications by assay accession", position = 5)
  @RequestMapping(value = "/list/assay/{assayAccession}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
  @ResponseStatus(HttpStatus.OK) // 200
  public
  @ResponseBody
  ProteinDetailList getProteinsByAssay(
      @ApiParam(value = "an assay accession (example: 22134)")
      @PathVariable("assayAccession") String assayAccession,
      @ApiParam(value = "how many results to return per page. Maximum page size is: " + WsUtils.MAX_PAGE_SIZE)
      @RequestParam(value = "show", required = false, defaultValue = WsUtils.DEFAULT_SHOW+"") int showResults,
      @ApiParam(value = "which page (starting from 0) of the result to return")
      @RequestParam(value = "page", required = false, defaultValue = WsUtils.DEFAULT_PAGE+"") int page
  ) {
    logger.info("Proteins for assay " + assayAccession + " requested");
    if(showResults > WsUtils.MAX_PAGE_SIZE){
      logger.error("Maximum size of page reach");
      throw new MaxPageSizeReachedException("The number of items requested exceed the maximum size for the page: "+ WsUtils.MAX_PAGE_SIZE);
    }
    return getPsmDetailList(proteinIdService.findByAssayAccession(assayAccession, new PageRequest(page, showResults)).getContent());
  }

  @ApiOperation(value = "count protein identifications by assay accession", position = 6)
  @RequestMapping(value = "/count/assay/{assayAccession}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
  @ResponseStatus(HttpStatus.OK) // 200
  public
  @ResponseBody
  Long countProteinsByAssay(
      @ApiParam(value = "an assay accession (example: 22134)")
      @PathVariable("assayAccession") String assayAccession
  ) {
    logger.info("Proteins for assay " + assayAccession + " requested");
    return proteinIdService.countByAssayAccession(assayAccession);
  }

  private static boolean isValidAccession(String accession) {
    // ToDo: extend with more cases!
    return !accession.toUpperCase().contains("DECOY") && !accession.toUpperCase().contains("REVERSE");
  }

  private ProteinDetailList getPsmDetailList(List<ProteinIdentification> foundProteins) {
    return new ProteinDetailList(
        ObjectMapper.mapMongoProteinIdentifiedListToWSProteinDetailList(
            mongoProteinIdService.findByIdIn(
                foundProteins.stream().
                    map(ProteinIdentification::getId).
                    collect(Collectors.toCollection(ArrayList<String>::new)))));
  }
}
