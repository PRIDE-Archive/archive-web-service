package uk.ac.ebi.pride.archive.web.service.swagger;

import com.fasterxml.classmate.TypeResolver;
import com.mangofactory.swagger.authorization.AuthorizationContext;
import com.mangofactory.swagger.configuration.JacksonScalaSupport;
import com.mangofactory.swagger.configuration.SpringSwaggerConfig;
import com.mangofactory.swagger.configuration.SwaggerGlobalSettings;
import com.mangofactory.swagger.core.SwaggerApiResourceListing;
import com.mangofactory.swagger.core.SwaggerPathProvider;
import com.mangofactory.swagger.models.alternates.AlternateTypeProvider;
import com.mangofactory.swagger.models.alternates.WildcardType;
import com.mangofactory.swagger.scanners.ApiListingReferenceScanner;
import com.wordnik.swagger.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.mangofactory.swagger.models.alternates.Alternates.newRule;

/**
 * @author Florian Reisinger
 * @since 1.0.1
 */
@Configuration
public class SwaggerConfig {

    public static final List<String> DEFAULT_INCLUDE_PATTERNS = Arrays.asList("/.*");

    public static final String SWAGGER_GROUP = "";


    /**
     * Autowire the bundled swagger config
     */
    @Autowired
    private SpringSwaggerConfig springSwaggerConfig;


    /**
     * Adds the jackson scala module to the MappingJackson2HttpMessageConverter registered with spring
     * Swagger core models are scala so we need to be able to convert to JSON
     * Also registers some custom serializers needed to transform swagger models to swagger-ui required json format
     */
    @Bean
    public JacksonScalaSupport jacksonScalaSupport() {
        JacksonScalaSupport jacksonScalaSupport = new JacksonScalaSupport();
        //Set to false to disable
        jacksonScalaSupport.setRegisterScalaModule(true);
        return jacksonScalaSupport;
    }


    /**
     * Global swagger settings
     */
    @Bean
    public SwaggerGlobalSettings swaggerGlobalSettings() {
        SwaggerGlobalSettings swaggerGlobalSettings = new SwaggerGlobalSettings();
        swaggerGlobalSettings.setGlobalResponseMessages(springSwaggerConfig.defaultResponseMessages());
        swaggerGlobalSettings.setIgnorableParameterTypes(springSwaggerConfig.defaultIgnorableParameterTypes());
        AlternateTypeProvider alternateTypeProvider = springSwaggerConfig.defaultAlternateTypeProvider();
        TypeResolver typeResolver = new TypeResolver();
        alternateTypeProvider.addRule(newRule(typeResolver.resolve(ResponseEntity.class),typeResolver.resolve(Void.class)));
        alternateTypeProvider.addRule(newRule(typeResolver.resolve(ResponseEntity.class, WildcardType.class),typeResolver.resolve(WildcardType.class)));
        alternateTypeProvider.addRule(newRule(typeResolver.resolve(HttpEntity.class, WildcardType.class),typeResolver.resolve(WildcardType.class)));
        swaggerGlobalSettings.setAlternateTypeProvider(alternateTypeProvider);
        return swaggerGlobalSettings;
    }


    /**
     * API Info as it appears on the swagger-ui page
     */
    private ApiInfo apiInfo() {
        return new ApiInfo(
                "PRIDE Archive web service API",
                "Programmatic access to the PRIDE Archive data via RESTful Web Services.",
                "http://www.ebi.ac.uk/about/terms-of-use",
                "pride-support@ebi.ac.uk",
                "Apache 2.0",
                "http://www.apache.org/licenses/LICENSE-2.0.html"
        );
    }

    /**
     * Configure a SwaggerApiResourceListing for each swagger instance within your app. e.g. 1. private 2. external apis
     * Required to be a spring bean as spring will call the postConstruct method to bootstrap swagger scanning.
     *
     * @return the SwaggerApiResourceListing to use.
     */
    @Bean
    public SwaggerApiResourceListing swaggerApiResourceListing() {
        //The group name is important and should match the group set on ApiListingReferenceScanner
        //Note that swaggerCache() is by DefaultSwaggerController to serve the swagger json
        SwaggerApiResourceListing swaggerApiResourceListing = new SwaggerApiResourceListing(springSwaggerConfig.swaggerCache(), SWAGGER_GROUP);

        //Set the required swagger settings
        swaggerApiResourceListing.setSwaggerGlobalSettings(swaggerGlobalSettings());

        //Use a custom path provider or springSwaggerConfig.defaultSwaggerPathProvider()
        swaggerApiResourceListing.setSwaggerPathProvider(demoPathProvider());

        //Supply the API Info as it should appear on swagger-ui web page
        swaggerApiResourceListing.setApiInfo(apiInfo());

        //Global authorization - see the swagger documentation
        swaggerApiResourceListing.setAuthorizationTypes(authorizationTypes());
        //Sets up an auth context - i.e. which controller request paths to apply global auth to
        swaggerApiResourceListing.setAuthorizationContext(authorizationContext());

        //Every SwaggerApiResourceListing needs an ApiListingReferenceScanner to scan the spring request mappings
        swaggerApiResourceListing.setApiListingReferenceScanner(apiListingReferenceScanner());
        return swaggerApiResourceListing;
    }


    /**
     * The ApiListingReferenceScanner does most of the work.
     * Scans the appropriate spring RequestMappingHandlerMappings
     * Applies the correct absolute paths to the generated swagger resources
     */
    @Bean
    public ApiListingReferenceScanner apiListingReferenceScanner() {
        ApiListingReferenceScanner apiListingReferenceScanner = new ApiListingReferenceScanner();

        //Picks up all of the registered spring RequestMappingHandlerMappings for scanning
        apiListingReferenceScanner.setRequestMappingHandlerMapping(springSwaggerConfig.swaggerRequestMappingHandlerMappings());

        //Excludes any controllers with the supplied annotations
        apiListingReferenceScanner.setExcludeAnnotations(springSwaggerConfig.defaultExcludeAnnotations());

        //How to group request mappings to ApiResource's typically by spring controller clesses or @Api.value()
        apiListingReferenceScanner.setResourceGroupingStrategy(springSwaggerConfig.defaultResourceGroupingStrategy());

        //Path provider used to generate the appropriate uri's
        apiListingReferenceScanner.setSwaggerPathProvider(demoPathProvider());

        //Must match the swagger group set on the SwaggerApiResourceListing
        apiListingReferenceScanner.setSwaggerGroup(SWAGGER_GROUP);

        //Only include paths that match the supplied regular expressions
        apiListingReferenceScanner.setIncludePatterns(DEFAULT_INCLUDE_PATTERNS);

        return apiListingReferenceScanner;
    }


    /**
     * Example of a custom path provider
     */
    @Bean
    public SwaggerPathProvider demoPathProvider() {
        DocuPathProvider demoPathProvider = new DocuPathProvider();
        demoPathProvider.setDefaultSwaggerPathProvider(springSwaggerConfig.defaultSwaggerPathProvider());
        return demoPathProvider;
    }



    private List<AuthorizationType> authorizationTypes() {
        final List<AuthorizationType> authorizationTypes = new ArrayList<AuthorizationType>();
        authorizationTypes.add(new BasicAuth());
        return authorizationTypes;
    }

    @Bean
    public AuthorizationContext authorizationContext() {
        final List<Authorization> authorizations = new ArrayList<Authorization>();
        authorizations.add(new Authorization("basicAuth", new AuthorizationScope[]{}));

        return new AuthorizationContext.AuthorizationContextBuilder(
                authorizations).withIncludePatterns(DEFAULT_INCLUDE_PATTERNS)
                .build();
    }


}
