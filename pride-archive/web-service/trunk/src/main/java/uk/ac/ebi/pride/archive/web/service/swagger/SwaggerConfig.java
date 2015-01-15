package uk.ac.ebi.pride.archive.web.service.swagger;

import com.google.common.collect.Ordering;
import com.mangofactory.swagger.configuration.SpringSwaggerConfig;
import com.mangofactory.swagger.ordering.ResourceListingPositionalOrdering;
import com.mangofactory.swagger.plugin.EnableSwagger;
import com.mangofactory.swagger.plugin.SwaggerSpringMvcPlugin;
import com.wordnik.swagger.model.ApiDescription;
import com.wordnik.swagger.model.ApiInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import uk.ac.ebi.pride.archive.dataprovider.file.ProjectFileSource;

import java.net.URL;

/**
 * @author florian@ebi.ac.uk.
 * @since 1.0.8
 */
@Configuration
@EnableSwagger
@ComponentScan("uk.ac.ebi.pride.archive.web.service.controller")
public class SwaggerConfig {

    private SpringSwaggerConfig springSwaggerConfig;

    @Autowired
    public void setSpringSwaggerConfig(SpringSwaggerConfig springSwaggerConfig) {
        this.springSwaggerConfig = springSwaggerConfig;
    }

    @Bean
    public SwaggerSpringMvcPlugin customImplementation(){
        return new SwaggerSpringMvcPlugin(this.springSwaggerConfig)
                .apiInfo(apiInfo())
                // PRIDE Archive RESTful API version
                .apiVersion("1.1")
                // try the default RelativeSwaggerPathProvider
                .apiListingReferenceOrdering(new ResourceListingPositionalOrdering())
                // direct overwrites of model classes
                .directModelSubstitute(URL.class, String.class) // don't document URL as complex object, but as simple string
                .directModelSubstitute(ProjectFileSource.class, String.class) // don't list all enum values (as some will never make it to the client), just use string and use API description annotation to show possible values
                // overwrite the default ordering of description annotations
                .apiDescriptionOrdering(new ApiDescriptionPositionOrdering())
                .includePatterns("/.*");
    }

    private ApiInfo apiInfo() {
        return new ApiInfo(
                "PRIDE Archive RESTful web service API",
                "For more details and examples please see the <a href=\"/pride/help/archive/access/webservice\">additional documentation pages</a>",
                "http://www.ebi.ac.uk/about/terms-of-use",
                "pride-support@ebi.ac.uk",
                "Apache 2.0",
                "http://www.apache.org/licenses/LICENSE-2.0.html"
        );
    }

    private class ApiDescriptionPositionOrdering extends Ordering<ApiDescription> {
        @Override
        public int compare(ApiDescription apiDescription, ApiDescription other) {
            int pos1 = apiDescription.operations().iterator().next().position();
            int pos2 = other.operations().iterator().next().position();
            return Integer.compare(pos1, pos2);
        }
    }

}