package uk.ac.ebi.pride.archive.web.service.util;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Florian Reisinger
 * @since 1.0.4
 */
public class IdMapper {

    private static IdMapper instance = new IdMapper();

    Map<Long, String> proteinId2ProteinAcc;
    Map<Long, String> assayId2AssayAcc;

    private IdMapper() {
        this.proteinId2ProteinAcc = new HashMap<Long, String>();
        this.assayId2AssayAcc = new HashMap<Long, String>();
    }

    public static IdMapper getInstance() {
        return instance;
    }

    public String storeProteinAccession(long id, String accession) {
        return this.proteinId2ProteinAcc.put(id, accession);
    }
    public String getProteinAccession(long id) {
        return this.proteinId2ProteinAcc.get(id);
    }
    public boolean containsProteinId(long id) {
        return this.proteinId2ProteinAcc.containsKey(id);
    }

    public String storeAssayAccession(long id, String accession) {
        return this.assayId2AssayAcc.put(id, accession);
    }
    public String getAssayAccession(long id) {
        return this.assayId2AssayAcc.get(id);
    }
    public boolean containsAssayId(long id) {
        return this.assayId2AssayAcc.containsKey(id);
    }
}
