package uk.ac.ebi.pride.archive.web.service.util;

import uk.ac.ebi.pride.archive.web.service.model.assay.AssayDetail;
import uk.ac.ebi.pride.archive.web.service.model.contact.ContactDetail;
import uk.ac.ebi.pride.archive.web.service.model.file.FileDetail;
import uk.ac.ebi.pride.archive.web.service.model.file.FileType;
import uk.ac.ebi.pride.archive.web.service.model.project.ProjectDetail;
import uk.ac.ebi.pride.prider.dataprovider.person.Title;
import uk.ac.ebi.pride.prider.service.assay.AssaySummary;
import uk.ac.ebi.pride.prider.service.assay.InstrumentSummary;
import uk.ac.ebi.pride.prider.service.file.FileSummary;
import uk.ac.ebi.pride.prider.service.param.CvParamSummary;
import uk.ac.ebi.pride.prider.service.person.ContactSummary;
import uk.ac.ebi.pride.prider.service.person.UserSummary;
import uk.ac.ebi.pride.prider.service.project.ProjectSearchSummary;
import uk.ac.ebi.pride.prider.service.project.ProjectSummary;
import uk.ac.ebi.pride.prider.service.project.ProjectTagSummary;

import java.util.*;


/**
 * @author florian@ebi.ac.uk
 * @since 1.0.4
 */
public final class ObjectMapper {

    private static final String NOT_APPLICABLE = "N/A";


    // Project map methods
    @SuppressWarnings("UnusedDeclaration")
    public static List<ProjectDetail> mapProjectSummaries2WSProjectDetails(Collection<ProjectSummary> projectSummaries) {
        if (projectSummaries == null) { return null; }
        if (projectSummaries.isEmpty()) { return new ArrayList<ProjectDetail>(0); }

        List<ProjectDetail> list = new ArrayList<ProjectDetail>(projectSummaries.size());
        for (ProjectSummary projectSummary : projectSummaries) {
            list.add(mapProjectSummary2WSProjectDetail(projectSummary));
        }
        return list;
    }
    public static List<uk.ac.ebi.pride.archive.web.service.model.project.ProjectSummary> mapProjectSummaries2WSProjectSummaries(Collection<ProjectSummary> projectSummaries) {
        if (projectSummaries == null) { return null; }
        if (projectSummaries.isEmpty()) { return new ArrayList<uk.ac.ebi.pride.archive.web.service.model.project.ProjectSummary>(0); }

        List<uk.ac.ebi.pride.archive.web.service.model.project.ProjectSummary> list = new ArrayList<uk.ac.ebi.pride.archive.web.service.model.project.ProjectSummary>(projectSummaries.size());
        for (ProjectSummary projectSummary : projectSummaries) {
            list.add(mapProjectSummary2WSProjectSummary(projectSummary, uk.ac.ebi.pride.archive.web.service.model.project.ProjectSummary.class));
        }
        return list;
    }
    public static <T extends uk.ac.ebi.pride.archive.web.service.model.project.ProjectSummary> T mapProjectSummary2WSProjectSummary(ProjectSummary object, Class<T> clazz) {
        if (object == null) { return null; }

        T mappedObject;
        try {
            mappedObject = clazz.newInstance();
        } catch (Exception e) {
            mappedObject = null;
        }
        if (mappedObject == null) {
            throw new IllegalStateException("Could not instantiate object for class: " + clazz.getName());
        }

        mappedObject.setAccession( object.getAccession() );
        mappedObject.setTitle( object.getTitle() );
        mappedObject.setProjectDescription( object.getProjectDescription() );
        mappedObject.setPublicationDate( object.getPublicationDate() );
        mappedObject.setSubmissionType( object.getSubmissionType().name() );
        mappedObject.setNumAssays( object.getNumAssays() );
        // ToDo: define the mapping of cvparams to strings
        mappedObject.setSpecies( getCvParamNames(object.getSpecies()) );
        mappedObject.setTissues( getCvParamNames(object.getTissues()) );
        mappedObject.setPtmNames( getCvParamNames(object.getPtms()) );
        mappedObject.setInstrumentNames( getCvParamNames(object.getInstruments()) );

        return mappedObject;
    }
    public static ProjectDetail mapProjectSummary2WSProjectDetail(ProjectSummary object) {
        if (object == null) { return null; }
        ProjectDetail mappedObject = mapProjectSummary2WSProjectSummary(object, ProjectDetail.class);

        mappedObject.setDoi( object.getDoi() );
        mappedObject.setProjectTags( mapProjectTags(object.getProjectTags()) );
        mappedObject.setSubmitter( mapUserSummaryToWSContactDetail(object.getSubmitter()) );
        if (object.getLabHeads() == null || object.getLabHeads().size() > 0) {
            Set<ContactDetail> labHeads = new HashSet<ContactDetail>();
            labHeads.addAll( mapContactSummariesToWSContactDetails(object.getLabHeads()) );
            mappedObject.setLapHeads(labHeads);
        }
        mappedObject.setSubmissionDate( object.getSubmissionDate( ));
        mappedObject.setReanalysis( object.getReanalysis() );
        mappedObject.setExperimentTypes( getCvParamNames(object.getExperimentTypes()) );
        mappedObject.setQuantificationMethods( getCvParamNames(object.getQuantificationMethods()) );
        mappedObject.setKeywords( object.getKeywords() );
        mappedObject.setSampleProcessingProtocol( object.getSampleProcessingProtocol() );
        mappedObject.setDataProcessingProtocol( object.getDataProcessingProtocol() );
        mappedObject.setOtherOmicsLink( object.getOtherOmicsLink() );
        mappedObject.setNumProteins(-1);
        mappedObject.setNumPeptides(-1);
        mappedObject.setNumSpectra(-1);
        mappedObject.setNumUniquePeptides(-1);
        mappedObject.setNumIdentifiedSpectra(-1);

        return mappedObject;
    }
    public static uk.ac.ebi.pride.archive.web.service.model.project.ProjectSummary mapProjectSearchSummary2WSProjectSummary(ProjectSearchSummary projectSearchSummary) {
        if (projectSearchSummary == null) { return null; }

        uk.ac.ebi.pride.archive.web.service.model.project.ProjectSummary mappedProject = new uk.ac.ebi.pride.archive.web.service.model.project.ProjectSummary();

        mappedProject.setAccession( projectSearchSummary.getProjectAccession() );
        mappedProject.setTitle( projectSearchSummary.getTitle() );
        mappedProject.setProjectDescription( projectSearchSummary.getProjectDescription() );
        mappedProject.setPublicationDate( projectSearchSummary.getPublicationDate() );
        mappedProject.setSubmissionType(projectSearchSummary.getSubmissionType());
        mappedProject.setNumAssays( projectSearchSummary.getNumExperiments() );
        if (projectSearchSummary.getSpeciesNames() != null) {
            mappedProject.getSpecies().addAll( projectSearchSummary.getSpeciesNames() );
        }
        if (projectSearchSummary.getTissueNames() != null) {
            mappedProject.getTissues().addAll( projectSearchSummary.getTissueNames() );
        }
        if (projectSearchSummary.getPtmNames() != null) {
            mappedProject.getPtmNames().addAll( projectSearchSummary.getPtmNames() );
        }
        if (projectSearchSummary.getInstrumentModels() != null) {
            mappedProject.getInstrumentNames().addAll( projectSearchSummary.getInstrumentModels() );
        }

        // build keywords from some other data we have
//        StringBuilder sb = new StringBuilder();
//        if (projectSearchSummary.getDiseaseNames() != null) {
//            for (String s : projectSearchSummary.getDiseaseNames()) {
//                sb.append(s).append(", ");
//            }
//        }
//        if (projectSearchSummary.getQuantificationMethods() != null) {
//            for (String s : projectSearchSummary.getQuantificationMethods()) {
//                sb.append(s).append(", ");
//            }
//        }
//        String keywords = sb.toString();
//        // remove the last ", "
//        if (keywords.length() > 3) {
//            keywords = keywords.substring(0, keywords.length() - 2);
//        }
//        mappedProject.setKeywords(keywords);
        return mappedProject;
    }
    public static List<uk.ac.ebi.pride.archive.web.service.model.project.ProjectSummary> mapProjectSearchSummarys2WSProjectSummaries(Collection<ProjectSearchSummary> projects) {
        if (projects == null) { return null; }
        if (projects.isEmpty()) { return new ArrayList<uk.ac.ebi.pride.archive.web.service.model.project.ProjectSummary>(); }

        List<uk.ac.ebi.pride.archive.web.service.model.project.ProjectSummary> list = new ArrayList<uk.ac.ebi.pride.archive.web.service.model.project.ProjectSummary>(projects.size());
        for (ProjectSearchSummary project : projects) {
            list.add( mapProjectSearchSummary2WSProjectSummary(project) );
        }
        return list;
    }


    // Assay map methods
    public static <T extends uk.ac.ebi.pride.archive.web.service.model.assay.AssaySummary> T mapAssaySummaryToWSAssaySummary(AssaySummary object, Class<T> clazz) {
        if (object == null) { return null; }

        T mappedObject;
        try {
            mappedObject = clazz.newInstance();
        } catch (Exception e) {
            mappedObject = null;
        }
        if (mappedObject == null) {
            throw new IllegalStateException("Could not instantiate object for class: " + clazz.getName());
        }
        IdMapper idMapper = IdMapper.getInstance();
        String projectAccession = idMapper.getProteinAccession(object.getProjectId());

        mappedObject.setAssayAccession( object.getAccession() );
        mappedObject.setProjectAccession(projectAccession);
        mappedObject.setTitle( object.getTitle() );
        mappedObject.setShortLabel( object.getShortLabel() );
//        mappedObject.setKeywords(""); // no keywords on AssaySummary

        mappedObject.setSpecies( getCvParamNames(object.getSpecies()) );
        // add a list of sample details (using Tissue, Disease, GoTerms and ExperimentalFactor)
        mappedObject.setSampleDetails( getCvParamNames(object.getTissues()) );
        mappedObject.getSampleDetails().addAll(getCvParamNames(object.getCellTypes()));
        mappedObject.getSampleDetails().addAll( getCvParamNames(object.getDiseases()) );
        mappedObject.getSampleDetails().addAll( getCvParamNames(object.getGoTerms()) );
        // experimental factor not supported on AssaySummary...
//        mappedObject.getSampleDetails().add( object.getExperimentalFactor() );

        mappedObject.setInstrumentNames( getInstrumentDefinitions(object.getInstruments()) );

        mappedObject.setPtmNames( getCvParamNames(object.getPtms()) );

        return mappedObject;
    }
    public static AssayDetail mapAssaySummaryToWSAssayDetail(AssaySummary assaySummary) {
        if (assaySummary == null) { return null; }

        AssayDetail mappedObject = mapAssaySummaryToWSAssaySummary(assaySummary, AssayDetail.class);

        mappedObject.setChromatogram( assaySummary.hasChromatogram() );
        mappedObject.setMs2Annotation( assaySummary.hasMs2Annotation() );
        mappedObject.setContacts( mapContactSummariesToWSContactDetails(assaySummary.getContacts()) );
        mappedObject.setDiseases( getCvParamNames(assaySummary.getDiseases()) );
        mappedObject.setExperimentalFactor( assaySummary.getExperimentalFactor() );
        mappedObject.setQuantMethods( getCvParamNames(assaySummary.getQuantificationMethods()) );
        mappedObject.setIdentifiedSpectrumCount( assaySummary.getIdentifiedSpectrumCount() );
        mappedObject.setPeptideCount( assaySummary.getPeptideCount() );
        mappedObject.setProteinCount( assaySummary.getProteinCount() );
        mappedObject.setTotalSpectrumCount( assaySummary.getTotalSpectrumCount() );
        mappedObject.setUniquePeptideCount( assaySummary.getUniquePeptideCount() );

        return mappedObject;
    }
    public static List<AssayDetail> mapAssaySummariesToWSAssayDetails(Collection<AssaySummary> assaySummaries) {
        if (assaySummaries == null) { return null; }
        if (assaySummaries.isEmpty()) { return new ArrayList<AssayDetail>(0); }

        List<AssayDetail> list = new ArrayList<AssayDetail>(assaySummaries.size());
        for (AssaySummary assaySummary : assaySummaries) {
            list.add( mapAssaySummaryToWSAssayDetail(assaySummary) );
        }
        return list;
    }


    // User/Contact map methods
    public static ContactDetail mapContactSummaryToWSContactDetail(ContactSummary contactSummary) {
        if (contactSummary == null) { return null; }

        ContactDetail mappedObject = new ContactDetail();

        mappedObject.setTitle( contactSummary.getTitle().getTitle() );
        mappedObject.setFirstName( contactSummary.getFirstName() );
        mappedObject.setLastName( contactSummary.getLastName() );
        mappedObject.setEmail( contactSummary.getEmail() );
        mappedObject.setAffiliation( contactSummary.getAffiliation() );

        return mappedObject;
    }
    public static Set<ContactDetail> mapContactSummariesToWSContactDetails(Collection<ContactSummary> contactSummaries) {
        if (contactSummaries == null) { return null; }
        if (contactSummaries.isEmpty()) { return new HashSet<ContactDetail>(0); }

        Set<ContactDetail> mappedObjects = new HashSet<ContactDetail>();
        for (ContactSummary userSummary : contactSummaries) {
            mappedObjects.add( mapContactSummaryToWSContactDetail(userSummary) );
        }
        return mappedObjects;
    }
    public static ContactDetail mapUserSummaryToWSContactDetail(UserSummary userSummary) {
        if (userSummary == null) { return null; }

        ContactDetail mappedObject = new ContactDetail();
        Title title = userSummary.getTitle() != null ? userSummary.getTitle() : Title.UNKNOWN;
        mappedObject.setTitle( title.getTitle() );
        mappedObject.setFirstName( userSummary.getFirstName() );
        mappedObject.setLastName( userSummary.getLastName() );
        mappedObject.setEmail( userSummary.getEmail() );
        mappedObject.setAffiliation( userSummary.getAffiliation() );

        return mappedObject;
    }
    // may be needed in the future
//    public static Collection<ContactDetail> mapUserSummariesToWSContactDetails(Collection<UserSummary> userSummaries) {
//        if (userSummaries == null) { return null; }
//        if (userSummaries.isEmpty()) { return new ArrayList<ContactDetail>(0); }
//
//        Collection<ContactDetail> mappedObjects = new ArrayList<ContactDetail>();
//        for (UserSummary userSummary : userSummaries) {
//            mappedObjects.add( mapUserSummaryToWSContactDetail(userSummary) );
//        }
//        return mappedObjects;
//    }


    // File map methods
    public static FileDetail mapFileSummaryToWSFileDetail(FileSummary fileSummary) {
        if (fileSummary == null) { return null; }

        IdMapper idMapper = IdMapper.getInstance();
        String projectAccession = idMapper.getProteinAccession(fileSummary.getProjectId());

        FileDetail mappedObject = new FileDetail();
        mappedObject.setProjectAccession(projectAccession);
        if (fileSummary.getAssayId() != null) {
            String assayAccession = idMapper.getAssayAccession(fileSummary.getAssayId());
            mappedObject.setAssayAccession(assayAccession);
        } else {
            mappedObject.setAssayAccession(NOT_APPLICABLE);
        }
        mappedObject.setFileName( fileSummary.getFileName() );
        mappedObject.setFileSize( fileSummary.getFileSize() );
        mappedObject.setFileType( FileType.findForName(fileSummary.getFileType().name()) );

        return mappedObject;
    }
    public static List<FileDetail> mapFileSummariesToWSFileDetails(Collection<FileSummary> fileSummaries) {
        if (fileSummaries == null) { return null; }
        if (fileSummaries.isEmpty()) { return new ArrayList<FileDetail>(0); }

        List<FileDetail> mappedObjects = new ArrayList<FileDetail>(fileSummaries.size());
        for (FileSummary fileSummary : fileSummaries) {
            mappedObjects.add( mapFileSummaryToWSFileDetail(fileSummary) );
        }
        return mappedObjects;
    }



    public static Set<String> mapProjectTags(Collection<ProjectTagSummary> projectTags) {
        if (projectTags == null) { return null; }
        if (projectTags.isEmpty()) { return new HashSet<String>(0); }

        Set<String> tags = new HashSet<String>(projectTags.size());
        for (ProjectTagSummary tag : projectTags) {
            tags.add( tag.getTag() );
        }
        return tags;
    }

    private static Set<String> getInstrumentDefinitions(Collection<InstrumentSummary> instrumentSummaries) {
        if (instrumentSummaries == null) { return null; }

        Set<String> instrumentSet = new HashSet<String>();
        for (InstrumentSummary instrument : instrumentSummaries) {
            // the model may not always be present, in which case we may have to generate a definition from the components
            if (instrument.getModel() != null) {
                CvParamSummary cv = instrument.getModel();
                // check whether we have a generic instrument model annotation
                if (cv.getAccession().equalsIgnoreCase("MS:1000031")) {
                    instrumentSet.add(cv.getValue());
                } else {
                    instrumentSet.add(cv.getName());
                }
            } else {
//                ToDo: generate definition from components?
                instrumentSet.add("Unknown instrument model.");
            }
        }
        return instrumentSet;
    }

    private static Set<String> getCvParamNames(Collection<CvParamSummary> objects) {
        if (objects == null) { return null; }
        if (objects.isEmpty()) { return new HashSet<String>(0); }

        Set<String> nameSet = new HashSet<String>();
        for (CvParamSummary cvParamSummary : objects) {
            nameSet.add( cvParamSummary.getName() );
        }
        return nameSet;
    }

}