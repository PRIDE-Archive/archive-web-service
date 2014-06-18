package uk.ac.ebi.pride;

import org.springframework.http.*;
import org.springframework.security.crypto.codec.Base64;
import org.springframework.web.client.RestTemplate;
import uk.ac.ebi.pride.archive.web.service.model.file.FileDetail;
import uk.ac.ebi.pride.archive.web.service.model.file.FileDetailList;
import uk.ac.ebi.pride.archive.web.service.model.project.ProjectDetail;

import java.util.Arrays;

/**
 * @author Florian Reisinger
 */
public class Playground {

    private static final String projectServiceURL = "http://ves-ebi-4d.ebi.ac.uk:8110/pride/ws/archive/project/";
    private static final String projectServiceURL2 = "http://ves-hx-43.ebi.ac.uk:8090/pride/ws/archive/project/";
    private static final String fileServiceURL = "http://localhost:9091/file/list/project/";

    private static final String authStringPXD000010 = "Qy5NY0NhcnRoeUB1Y2QuaWU6a25vY2thbmU=";

    public static void main(String[] args){


//        testRestGetProject("PXD000010", "C.McCarthy@ucd.ie", "knockane");
//        testSimpleGet("PRD000099");

//        testRestGetProjectWithAuthString("PXD000010", authStringPXD000010);

//        testRESTwithAuthentication("PXD000010", "C.McCarthy@ucd.ie", "knockane");
//        testRESTwithAuthentication("PXD000651", "review55630@ebi.ac.uk", "5MKsBgjZ");

//        testSimpleGetFiles("PRD000099");


    }


    static HttpHeaders getHeaders(String auth) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));

        byte[] encodedAuthorisation = Base64.encode(auth.getBytes());
        headers.add("Authorization", "Basic " + new String(encodedAuthorisation));
//        System.out.println("authentication: " + new String(encodedAuthorisation));

        return headers;
      }


    public static void testRestGetProject(String projectAccession, String username, String password) {

        HttpEntity<String> requestEntity = new HttpEntity<String>(getHeaders(username + ":" + password));

        doRequest(projectServiceURL, projectAccession, requestEntity);

      }

    public static void testRestGetProjectWithAuthString(String projectAccession, String authString) {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.add("Authorization", "Basic " + authString);

        HttpEntity<String> requestEntity = new HttpEntity<String>(headers);

        doRequest(projectServiceURL, projectAccession, requestEntity);
        System.out.println("\n\nNow with other service URL....\n\n");
        doRequest(projectServiceURL2, projectAccession, requestEntity);

      }

    private static void doRequest(String projectServiceURL, String projectAccession, HttpEntity<String> requestEntity) {
        RestTemplate template = new RestTemplate();
        ResponseEntity<ProjectDetail> entity;
        entity = template.exchange(projectServiceURL + projectAccession, HttpMethod.GET, requestEntity, ProjectDetail.class);

        if (entity == null) {
            System.out.println("ERROR: null return");
            return;
        }

        if (entity.getHeaders() != null && entity.getHeaders().getLocation() != null) {
            String path = entity.getHeaders().getLocation().getPath();
            System.out.println("Path: " + path);
        }

        if (entity.getStatusCode() != null) {
            System.out.println("Equals? " + HttpStatus.OK + " = " + entity.getStatusCode());
        }
        ProjectDetail project = entity.getBody();

        System.out.println("The Project acc is: " + project.getAccession());
        System.out.println("Project desc: " + project.getProjectDescription());
    }

    private static void testSimpleGet(String projectAccession) {

        RestTemplate template = new RestTemplate();
        ResponseEntity<ProjectDetail> entity;
        entity = template.getForEntity(projectServiceURL + projectAccession, ProjectDetail.class);
        ProjectDetail projectDetail = entity.getBody();

        System.out.println("Project: " + projectDetail.getAccession() );
        System.out.println("Title: " + projectDetail.getTitle());
        System.out.println("Type: " + projectDetail.getSubmissionType());
    }
    private static void testSimpleGetFiles(String projectAccession) {

        RestTemplate template = new RestTemplate();
        ResponseEntity<FileDetailList> entity;
        entity = template.getForEntity(fileServiceURL + projectAccession, FileDetailList.class);
        FileDetailList fileDetailList = entity.getBody();

        for (FileDetail fileDetail : fileDetailList.getList()) {
            System.out.println("Project: " + fileDetail.getProjectAccession() );
            System.out.println("\ttype=" + fileDetail.getFileType().getName() + " name=" +fileDetail.getFileName() );
        }
    }


    private static void testRESTwithAuthentication(String projectAccession, String username, String password) {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        String authString = username + ":" + password;
        byte[] encodedAuthorisation = Base64.encode(authString.getBytes());
        headers.add("Authorization", "Basic " + new String(encodedAuthorisation));

        HttpEntity<String> requestEntity = new HttpEntity<String>(headers);

        RestTemplate template = new RestTemplate();
        ResponseEntity<ProjectDetail> entity = template.exchange("http://wwwdev.ebi.ac.uk/pride/ws/archive/project/" + projectAccession, HttpMethod.GET, requestEntity, ProjectDetail.class);

        if (entity == null) {
            System.out.println("ERROR: null return");
            return;
        }

        if (entity.getHeaders() != null && entity.getHeaders().getLocation() != null) {
            String path = entity.getHeaders().getLocation().getPath();
            System.out.println("Path: " + path);
        }

        if (entity.getStatusCode() != null) {
            System.out.println("Equals? " + HttpStatus.OK + " = " + entity.getStatusCode());
        }
        ProjectDetail project = entity.getBody();

        System.out.println("The Project acc is: " + project.getAccession());
        System.out.println("Project desc: " + project.getProjectDescription());

    }

}
