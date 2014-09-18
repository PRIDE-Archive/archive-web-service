package uk.ac.ebi.pride.archive.web.service.util;

/**
 * @author florian@ebi.ac.uk.
 */
public class WsUtils {

    public static final int DEFAULT_SHOW = 10;
    public static final int DEFAULT_PAGE = 1;

    public static int adjustPage(int page) {
        // externally we want to expose the first page as page 1 (to be in
        // sync with the Archive Web), but internally requests are are 0 based.
        // therefore we adjust the page number
        page -= 1; // ToDo: check if page is in a valid range
        return page;
    }


}
