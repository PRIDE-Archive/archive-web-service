package uk.ac.ebi.pride.archive.web.service.controller.project;

import com.mangofactory.swagger.annotations.ApiIgnore;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import uk.ac.ebi.pride.archive.web.service.error.exception.ResourceNotFoundException;
import uk.ac.ebi.pride.archive.web.service.model.project.ProjectDetail;
import uk.ac.ebi.pride.archive.web.service.model.project.ProjectSummaryList;
import uk.ac.ebi.pride.archive.web.service.util.ObjectMapper;
import uk.ac.ebi.pride.prider.service.person.UserService;
import uk.ac.ebi.pride.prider.service.person.UserSummary;
import uk.ac.ebi.pride.prider.service.project.ProjectSearchService;
import uk.ac.ebi.pride.prider.service.project.ProjectSearchSummary;
import uk.ac.ebi.pride.prider.service.project.ProjectService;
import uk.ac.ebi.pride.prider.service.project.ProjectSummary;

import javax.servlet.http.HttpServletRequest;
import java.security.Principal;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Rui Wang
 * @author florian@ebi.ac.uk
 * @since 1.0.4
 */
@Api(value = "retrieve information about projects", position = 1)
@Controller
@RequestMapping(value = "/project")
public class ProjectController {

    private static final Logger logger = LoggerFactory.getLogger(ProjectController.class);


    private static final String DATE_SORTING_CRITERIA = "publication_date";
    protected static final String SCORE_SORTING_CRITERIA = "score";
    protected static final String DESCENDING_ORDER = "desc";
    protected static final int DEFAULT_SHOW = 100;
    protected static final int DEFAULT_PAGE = 1;

    @Autowired
    private ProjectSearchService projectSearchService;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private UserService userServiceImpl;


    @ApiOperation(value = "retrieve project information by accession", position = 1)
    @RequestMapping(value = "/{projectAccession}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK) // 200
    public
    @ResponseBody
    ProjectDetail getProjectSummary(
            @ApiParam(value = "a project accession number")
            @PathVariable("projectAccession") String accession, HttpServletRequest request) {
        logger.info("Project " + accession + " summary requested");

        // retrieve the authorization header value (if there is any)
//        System.out.println("header value: " + request.getHeader("Authorization"));
        uk.ac.ebi.pride.prider.service.project.ProjectSummary projectSummary = projectService.findByAccession(accession);
        if (projectSummary == null) {
            throw new ResourceNotFoundException("No project found for accession: " + accession);
        }

        // ToDo: retrieve assay accessions for project!
        return ObjectMapper.mapProjectSummary2WSProjectDetail(projectSummary);
    }


    @ApiOperation(value = "list projects for given criteria", position = 2)
    @RequestMapping(value = "/list", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK) // 200
    public
    @ResponseBody
    ProjectSummaryList simpleSearchProjects(
            @ApiParam(value = "keyword to query for")
            @RequestParam(value = "q", required = false) String term,
            @ApiParam(value = "how many results to show per page")
            @RequestParam(value = "show", defaultValue = DEFAULT_SHOW+"") int showResults,
            @ApiParam(value = "which page of the result to return")
            @RequestParam(value = "page", defaultValue = DEFAULT_PAGE+"") int page,
            @ApiParam(value = "the field to sort on")
            @RequestParam(value = "sort", defaultValue = "") String sortBy,
            @ApiParam(value = "the sorting order (asc or desc)")
            @RequestParam(value = "order", defaultValue = DESCENDING_ORDER) String order,
            @ApiParam(value = "filter by species (NCBI taxon ID or name)")
            @RequestParam(value = "speciesFilter", required = false, defaultValue = "") String[] speciesFilter,
            @ApiParam(value = "filter by PTM annotation")
            @RequestParam(value = "ptmsFilter", required = false, defaultValue = "") String[] ptmsFilter,
            @ApiParam(value = "filter by tissue annotation")
            @RequestParam(value = "tissueFilter", required = false, defaultValue = "") String[] tissueFilter,
            @ApiParam(value = "filter by disease annotation")
            @RequestParam(value = "diseaseFilter", required = false, defaultValue = "") String[] diseaseFilter,
            @ApiParam(value = "filter the title for keywords")
            @RequestParam(value = "titleFilter", required = false, defaultValue = "") String[] titleFilter,
            @ApiParam(value = "filter for instrument names or keywords")
            @RequestParam(value = "instrumentFilter", required = false, defaultValue = "") String[] instrumentFilter,
            @ApiParam(value = "filter by experiment type")
            @RequestParam(value = "experimentTypeFilter", required = false, defaultValue = "") String[] experimentTypeFilter,
            @ApiParam(value = "")
            @RequestParam(value = "quantificationFilter", required = false, defaultValue = "") String[] quantificationFilter
            ) throws org.apache.solr.common.SolrException {

        // nonsense request, but can happen...
        if (showResults < 1) {
            // ToDo: perhaps handle with 'wrong input' exception
            return null;
        }

        // if no search term is provided, and therefore score is not very relevant, date has to be the sorting criteria
        if ("".equals(term) && "".equals(sortBy)) {
            sortBy = DATE_SORTING_CRITERIA; // this is the default sorting criteria when no one is specified and there is no search term
        } else if (!"".equals(term) && "".equals(sortBy)) {
            sortBy = SCORE_SORTING_CRITERIA; // this is the default sorting criteria when no one specified and there is a search term
        }


        String queryTerm = SolrQueryBuilder.buildQueryTerm(term);
        String queryFields = SolrQueryBuilder.buildQueryFields();
        String queryFilters[] = SolrQueryBuilder.buildQueryFilters(
                Arrays.asList(ptmsFilter),
                Arrays.asList(speciesFilter),
                Arrays.asList(tissueFilter),
                Arrays.asList(diseaseFilter),
                Arrays.asList(titleFilter),
                Arrays.asList(instrumentFilter),
                Arrays.asList(quantificationFilter),
                Arrays.asList(experimentTypeFilter)
        );

        int start = showResults * (page - 1);

        Collection<ProjectSearchSummary> projects = projectSearchService.searchProjects(
                queryTerm,
                queryFields,
                queryFilters,
                start, showResults, sortBy, order);

        if (projects == null || projects.isEmpty()) {
            logger.info("No projects found for query: " + queryTerm + " + " + Arrays.toString(queryFilters));
            return new ProjectSummaryList();
        }


        ProjectSummaryList list = new ProjectSummaryList(ObjectMapper.mapProjectSearchSummarys2WSProjectSummaries(projects));

        logger.debug("Fetched " + list.size() + " project records.");
        return list;
    }


//    @ApiOperation(value = "count projects for given criteria")
    @RequestMapping(value = "/count", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK) // 200
    public
    @ResponseBody
    long countSearchProjects(
            @ApiParam(value = "keyword to query for")
            @RequestParam(value = "q", required = false) String term,
            @ApiParam(value = "filter by species (NCBI taxon ID or name)")
            @RequestParam(value = "speciesFilter", required = false, defaultValue = "") String[] speciesFilter,
            @ApiParam(value = "filter by PTM annotation")
            @RequestParam(value = "ptmsFilter", required = false, defaultValue = "") String[] ptmsFilter,
            @ApiParam(value = "filter by tissue annotation")
            @RequestParam(value = "tissueFilter", required = false, defaultValue = "") String[] tissueFilter,
            @ApiParam(value = "filter by disease annotation")
            @RequestParam(value = "diseaseFilter", required = false, defaultValue = "") String[] diseaseFilter,
            @ApiParam(value = "filter the title for keywords")
            @RequestParam(value = "titleFilter", required = false, defaultValue = "") String[] titleFilter,
            @ApiParam(value = "filter for instrument names or keywords")
            @RequestParam(value = "instrumentFilter", required = false, defaultValue = "") String[] instrumentFilter,
            @ApiParam(value = "filter by experiment type")
            @RequestParam(value = "experimentTypeFilter", required = false, defaultValue = "") String[] experimentTypeFilter,
            @ApiParam(value = "")
            @RequestParam(value = "quantificationFilter", required = false, defaultValue = "") String[] quantificationFilter
            ) throws org.apache.solr.common.SolrException {

        String queryTerm = SolrQueryBuilder.buildQueryTerm(term);
        String queryFields = SolrQueryBuilder.buildQueryFields();
        String queryFilters[] = SolrQueryBuilder.buildQueryFilters(
                Arrays.asList(ptmsFilter),
                Arrays.asList(speciesFilter),
                Arrays.asList(tissueFilter),
                Arrays.asList(diseaseFilter),
                Arrays.asList(titleFilter),
                Arrays.asList(instrumentFilter),
                Arrays.asList(quantificationFilter),
                Arrays.asList(experimentTypeFilter)
        );

        long count = projectSearchService.numSearchResults(
                queryTerm,
                queryFields,
                queryFilters);

        logger.debug("Results for query:" + queryTerm + " with filters: " + Arrays.toString(queryFilters) + " = " + count);

        return count;
    }



    @ApiIgnore
    @RequestMapping(value = "/list/user", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK) // 200
    public
    @ResponseBody
    ProjectSummaryList getAccessibleProjects(Principal principal) {

        if (principal == null ) {
            throw new AccessDeniedException("Authentication required to list projects for user!");
        }
        if (principal.getName() != null) {
            logger.info("Projects owned by " + principal.getName() + " have been requested");
        }

        UserSummary user = userServiceImpl.findByEmail(principal.getName());

        Collection<ProjectSummary> projectSummaries = userServiceImpl.findAllProjectsById(user.getId());

        if (projectSummaries == null || projectSummaries.isEmpty()) {
            throw new ResourceNotFoundException("No projects found for user: " + user.getEmail());
        }

        Collection<uk.ac.ebi.pride.archive.web.service.model.project.ProjectSummary> summaries = ObjectMapper.mapProjectSummaries2WSProjectSummaries(projectSummaries);

        return new ProjectSummaryList(summaries);
    }




    // ToDo: this class should not be here! It should be part of the solr package (or service package, depends what we want to expose)

    /**
     * @author Jose A. Dianes
     * @version $Id$
     */
    protected static class SolrQueryBuilder {

        private static final int PX_RELEVANCE = 4;

        protected static String buildQueryFields() {
            return ""
                    + SearchFields.TITLE.getIndexName() + "^" + SearchFields.TITLE.getFieldRelevance() + " "
                    + SearchFields.DESCRIPTION.getIndexName() + "^" + SearchFields.DESCRIPTION.getFieldRelevance() + " "
                    + SearchFields.ACCESSION.getIndexName() + "^" + SearchFields.ACCESSION.getFieldRelevance() + " "
                    + SearchFields.PUBMED.getIndexName() + "^" + SearchFields.PUBMED.getFieldRelevance() + " "
                    + SearchFields.SPECIES_NAMES.getIndexName() + "^" + SearchFields.SPECIES_NAMES.getFieldRelevance() + " "
                    + SearchFields.SPECIES_ACCESSIONS.getIndexName() + "^" + SearchFields.SPECIES_ACCESSIONS.getFieldRelevance() + " "
                    + SearchFields.SPECIES_ASCENDANTS_AS_TEXT.getIndexName() + "^" + SearchFields.SPECIES_ASCENDANTS_AS_TEXT.getFieldRelevance() + " "
                    + SearchFields.TISSUE_NAMES.getIndexName() + "^" + SearchFields.TISSUE_NAMES.getFieldRelevance() + " "
                    + SearchFields.TISSUE_ACCESSIONS.getIndexName() + "^" + SearchFields.TISSUE_ACCESSIONS.getFieldRelevance() + " "
                    + SearchFields.TISSUE_ASCENDANTS_AS_TEXT.getIndexName() + "^" + SearchFields.TISSUE_ASCENDANTS_AS_TEXT.getFieldRelevance() + " "
                    + SearchFields.DISEASE_NAMES.getIndexName() + "^" + SearchFields.DISEASE_NAMES.getFieldRelevance() + " "
                    + SearchFields.DISEASE_ACCESSIONS.getIndexName() + "^" + SearchFields.DISEASE_ACCESSIONS.getFieldRelevance() + " "
                    + SearchFields.DISEASE_ASCENDANTS_AS_TEXT.getIndexName() + "^" + SearchFields.DISEASE_ASCENDANTS_AS_TEXT.getFieldRelevance() + " "
                    + SearchFields.CELLTYPE_NAMES.getIndexName() + "^" + SearchFields.CELLTYPE_NAMES.getFieldRelevance() + " "
                    + SearchFields.CELLTYPE_ACCESSIONS.getIndexName() + "^" + SearchFields.CELLTYPE_ACCESSIONS.getFieldRelevance() + " "
                    + SearchFields.CELLTYPE_ASCENDANTS.getIndexName() + "^" + SearchFields.CELLTYPE_ASCENDANTS.getFieldRelevance() + " "
                    + SearchFields.SAMPLE_NAMES.getIndexName() + "^" + SearchFields.SAMPLE_NAMES.getFieldRelevance() + " "
                    + SearchFields.PTM_NAMES.getIndexName() + "^" + SearchFields.PTM_NAMES.getFieldRelevance() + " "
                    + SearchFields.PTM_ACCESSIONS.getIndexName() + "^" + SearchFields.PTM_ACCESSIONS.getFieldRelevance() + " "
                    + SearchFields.PTM_FACET_NAMES.getIndexName() + "^" + SearchFields.PTM_FACET_NAMES.getFieldRelevance() + " "
                    + SearchFields.INSTRUMENT_MODELS.getIndexName() + "^" + SearchFields.INSTRUMENT_MODELS.getFieldRelevance() + " "
                    + SearchFields.INSTRUMENT_FACETS_NAMES.getIndexName() + "^" + SearchFields.INSTRUMENT_FACETS_NAMES.getFieldRelevance() + " "
                    + SearchFields.QUANTIFICATION_METHODS_NAMES.getIndexName() + "^" + SearchFields.QUANTIFICATION_METHODS_NAMES.getFieldRelevance() + " "
                    + SearchFields.QUANTIFICATION_METHODS_ACCESSIONS.getIndexName() + "^" + SearchFields.QUANTIFICATION_METHODS_ACCESSIONS.getFieldRelevance() + " "
                    + SearchFields.EXPERIMENT_TYPES_NAMES.getIndexName() + "^" + SearchFields.EXPERIMENT_TYPES_NAMES.getFieldRelevance() + " "
                    + SearchFields.EXPERIMENT_TYPES_ACCESSIONS.getIndexName() + "^" + SearchFields.EXPERIMENT_TYPES_ACCESSIONS.getFieldRelevance() + " "
                    + SearchFields.ASSAY_ACCESSIONS.getIndexName() + "^" + SearchFields.ASSAY_ACCESSIONS.getFieldRelevance();
        }

        protected static String buildQueryTerm(String term) {
    //        if ("".equals(term))
    //            return "*";
    //        else
    //            return term;
            if (term == null || term.trim().isEmpty())
                return "id:PR* id:PX*^"+PX_RELEVANCE; // PX submissions are more relevant
            else
    //            return term;
                return "(id:PR* id:PX*^"+PX_RELEVANCE + ") AND " + term; // PX submissions are more relevant
        }

        protected static String[] buildQueryFilters(List<String> ptmsFilterList, List<String> speciesFilterList, List<String> tissueFilterList, List<String> diseaseFilterList,
                                           List<String> titleFilterList, List<String> instrumentFilterList,
                                           List<String> quantificationFilterList, List<String> experimentTypeFilterList) {

            LinkedList<String> queryFilterList = new LinkedList<String>();

            if (ptmsFilterList!=null) {
                for (String filter : ptmsFilterList) {
                    queryFilterList.add(SearchFields.PTM_FACET_NAMES.getIndexName() + ":\"" + filter
                            //                        +"\" + OR "+ SearchFields.PTM_ACCESSIONS.getIndexName()+":\""+filter
                            //                        +"\" + OR "+ SearchFields.PTM_NAMES.getIndexName()+":\""+filter
                            + "\"");
                }

            }

            if (speciesFilterList!=null) {
                for (String filter : speciesFilterList) {
                    queryFilterList.add(
                            SearchFields.SPECIES_NAMES.getIndexName() + ":\"" + filter + "\" OR "
                                    + SearchFields.SPECIES_ACCESSIONS.getIndexName() + ":\"" + filter + "\" OR "
                                    + SearchFields.SPECIES_ASCENDANTS_NAMES.getIndexName() + ":\"" + filter + "\""
                    );
                }

            }

            if (tissueFilterList!=null) {
                for (String filter : tissueFilterList) {
                    queryFilterList.add(
                            SearchFields.TISSUE_NAMES.getIndexName() + ":\"" + filter + "\" OR "
                                    + SearchFields.TISSUE_ACCESSIONS.getIndexName() + ":\"" + filter + "\" OR "
                                    + SearchFields.TISSUE_ASCENDANTS_NAMES.getIndexName() + ":\"" + filter + "\""
                    );
                }

            }

            if (diseaseFilterList!=null) {
                for (String filter : diseaseFilterList) {
                    queryFilterList.add(
                            SearchFields.DISEASE_NAMES.getIndexName() + ":\"" + filter + "\" OR "
                                    + SearchFields.DISEASE_ACCESSIONS.getIndexName() + ":\"" + filter + "\" OR "
                                    + SearchFields.DISEASE_ASCENDANTS_NAMES.getIndexName() + ":\"" + filter + "\""
                    );
                }

            }

            if (titleFilterList!=null) {
                for (String filter : titleFilterList) {
                    queryFilterList.add(SearchFields.TITLE.getIndexName() + ":\"" + filter + "\"");
                }

            }

            if (instrumentFilterList!=null) {
                for (String filter : instrumentFilterList) {
                    queryFilterList.add(SearchFields.INSTRUMENT_FACETS_NAMES.getIndexName() + ":\"" + filter
                            //                        +"\" + OR "+ SearchFields.INSTRUMENT_MODELS.getIndexName()+":\""+filter
                            + "\"");
                }

            }

            if (quantificationFilterList!=null) {
                for (String filter : quantificationFilterList) {
                    queryFilterList.add(SearchFields.QUANTIFICATION_METHODS_NAMES.getIndexName() + ":\"" + filter + "\" OR " + SearchFields.QUANTIFICATION_METHODS_ACCESSIONS.getIndexName() + ":\"" + filter + "\"");
                }

            }

            if (experimentTypeFilterList!=null) {
                for (String filter : experimentTypeFilterList) {
                    queryFilterList.add(SearchFields.EXPERIMENT_TYPES_NAMES.getIndexName() + ":\"" + filter + "\" OR " + SearchFields.EXPERIMENT_TYPES_ACCESSIONS.getIndexName() + ":\"" + filter + "\"");
                }

            }

            return queryFilterList.toArray(new String[queryFilterList.size()]);
        }
    }


    // ToDo: this class should not be here! It should be part of the solr package (or service package, depends what we want to expose)
    /**
     * @author Jose A. Dianes
     * @version $Id$
     */
    private enum SearchFields {
        ACCESSION("id", 3),
        TITLE("project_title", 1),
        DESCRIPTION("project_description", 1),
        PUBMED("pubmed_ids", 3),
        PTM_NAMES("ptm_as_text", 2),
        PTM_ACCESSIONS("ptm_accessions", 3),
        PTM_FACET_NAMES("ptm_facet_names", 4),
        INSTRUMENT_MODELS("instrument_models_as_text", 2),
        INSTRUMENT_FACETS_NAMES("instruments_facet_names", 4),
        QUANTIFICATION_METHODS_NAMES("quantification_methods_as_text", 2),
        QUANTIFICATION_METHODS_ACCESSIONS("quantification_methods_accessions", 3),
        EXPERIMENT_TYPES_NAMES("experiment_types_as_text", 2),
        EXPERIMENT_TYPES_ACCESSIONS("experiment_types_accession", 3),
        SPECIES_ACCESSIONS("species_accessions", 3),
        SPECIES_NAMES("species_as_text", 2),
        SPECIES_ASCENDANTS_AS_TEXT("species_descendants_as_text", 1),
        SPECIES_ASCENDANTS_NAMES("species_descendants_names", 1),
        TISSUE_ACCESSIONS("tissue_accessions", 3),
        TISSUE_NAMES("tissue_as_text", 2),
        TISSUE_ASCENDANTS_AS_TEXT("tissue_descendants_as_text", 1),
        TISSUE_ASCENDANTS_NAMES("tissue_descendants_names", 1),
        DISEASE_ACCESSIONS("disease_accessions", 3),
        DISEASE_NAMES("disease_as_text", 2),
        DISEASE_ASCENDANTS_AS_TEXT("disease_descendants_as_text", 1),
        DISEASE_ASCENDANTS_NAMES("disease_descendants_names", 1),
        CELLTYPE_ACCESSIONS("cell_type_accessions", 3),
        CELLTYPE_NAMES("cell_type_as_text", 2),
        CELLTYPE_ASCENDANTS("cell_type_descendants_as_text", 1),
        SAMPLE_NAMES("sample_as_text", 2),
        SAMPLE_ACCESSIONS("sample_accessions", 3),
        ASSAY_ACCESSIONS("assays_accession", 5)
        ;

        private String indexName;
        private int fieldRelevance;

        private SearchFields(String indexName, int fieldRelevance) {
            this.indexName = indexName;
            this.fieldRelevance = fieldRelevance;
        }

        public String getIndexName() {
            return indexName;
        }

        public int getFieldRelevance() {
            return fieldRelevance;
        }

        public static String buildIndexOrQueryString(String term) {
            String res = "(";

            for (SearchFields searchField: SearchFields.values()) {
                res = res + searchField.getIndexName() + ":*"+term+" OR ";
            }

            // remove last OR
            res = res.substring(0, res.length()-3);
            res = res + ")";

            return res;
        }
    }


}
