package uk.ac.ebi.pride.archive.web.service.controller;

import com.mangofactory.swagger.annotations.ApiIgnore;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * @author Florian Reisinger
 *         Date: 01/04/14
 * @since $version
 */
@Controller
@RequestMapping(value = {"", "/"})
public class WelcomeController {

    @ApiIgnore
    @RequestMapping(method= RequestMethod.GET)
    protected String gotoIndex() throws Exception {
        return "forward:/index.html";
    }


}
