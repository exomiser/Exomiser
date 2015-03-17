/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.prioritisers.util;

import de.charite.compbio.exomiser.core.model.GeneModel;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
@Service
public class ModelServiceImpl implements ModelService {

    private static final Logger logger = LoggerFactory.getLogger(ModelService.class);

    @Autowired
    private DataSource dataSource;

    @Override
    public List<GeneModel> getHumanDiseaseModels() {
        String modelQuery = "SELECT d.disease_id as model_id, hp_id as pheno_ids, gene_id as entrez_id, human_gene_symbol, d.diseasename as model_symbol FROM human2mouse_orthologs hm, disease_hp M, disease d WHERE hm.entrez_id=d.gene_id AND M.disease_id=d.disease_id";
        return runModelQuery(modelQuery);
    }

    @Override
    public List<GeneModel> getMouseGeneModels() {
        String modelQuery = "SELECT mouse_model_id as model_id, mp_id as pheno_ids, entrez_id, human_gene_symbol, M.mgi_gene_id, M.mgi_gene_symbol as model_symbol FROM mgi_mp M, human2mouse_orthologs H WHERE M.mgi_gene_id=H.mgi_gene_id and human_gene_symbol != 'null'";
        return runModelQuery(modelQuery);
    }

    @Override
    public List<GeneModel> getFishGeneModels() {
        String modelQuery = "SELECT zfin_model_id as model_id, zp_id as pheno_ids, entrez_id, human_gene_symbol, M.zfin_gene_id, M.zfin_gene_symbol as model_symbol FROM zfin_zp M, human2fish_orthologs H WHERE M.zfin_gene_id=H.zfin_gene_id and human_gene_symbol != 'null'";
        return runModelQuery(modelQuery);
    }

    private List<GeneModel> runModelQuery(String modelQuery) {
        List<GeneModel> models = new ArrayList<>();
        try (Connection connection = dataSource.getConnection();
                PreparedStatement findAnnotationStatement = connection.prepareStatement(modelQuery);
                ResultSet rs = findAnnotationStatement.executeQuery()) {;
            //each row is an animal model or disease, its phenotypes and the known causitive gene of these phenotypes. e.g.
            //MODEL_ID      ENTREZ_ID	HUMAN_GENE_SYMBOL   pheno_ids
            //OMIM:101600   2263	FGFR2               HP:0000174,HP:0000194,HP:0000218,HP:0000238,HP:0000244,HP:0000272,HP:0000303,HP:0000316,HP:0000322,HP:0000324,HP:0000327,HP:0000348,HP:0000431,HP:0000452,HP:0000453,HP:0000470,HP:0000486,HP:0000494,HP:0000508,HP:0000586,HP:0000678,HP:0001156,HP:0001249,HP:0002308,HP:0002676,HP:0002780,HP:0003041,HP:0003070,HP:0003196,HP:0003272,HP:0003307,HP:0003795,HP:0004209,HP:0004322,HP:0004440,HP:0005048,HP:0005280,HP:0005347,HP:0006101,HP:0006110,HP:0009602,HP:0009773,HP:0010055,HP:0010669,HP:0011304
            //OMIM:101600   2260	FGFR1               HP:0000174,HP:0000194,HP:0000218,HP:0000238,HP:0000244,HP:0000272,HP:0000303,HP:0000316,HP:0000322,HP:0000324,HP:0000327,HP:0000348,HP:0000431,HP:0000452,HP:0000453,HP:0000470,HP:0000486,HP:0000494,HP:0000508,HP:0000586,HP:0000678,HP:0001156,HP:0001249,HP:0002308,HP:0002676,HP:0002780,HP:0003041,HP:0003070,HP:0003196,HP:0003272,HP:0003307,HP:0003795,HP:0004209,HP:0004322,HP:0004440,HP:0005048,HP:0005280,HP:0005347,HP:0006101,HP:0006110,HP:0009602,HP:0009773,HP:0010055,HP:0010669,HP:0011304
            //MODEL_ID      ENTREZ_ID	HUMAN_GENE_SYMBOL   pheno_ids                   MGI_GENE_ID	MGI_GENE_SYMBOL
            //115           2263	FGFR2               MP:0000031,MP:0000035,MP:0000039,MP:0000081,MP:0000111,MP:0000118,MP:0000440,MP:0000470,MP:0000492,MP:0000551,MP:0000557,MP:0000613,MP:0000629,MP:0000633,MP:0001176,MP:0001181,MP:0001199,MP:0001201,MP:0001218,MP:0001231,MP:0001244,MP:0001265,MP:0001341,MP:0002095,MP:0002428,MP:0002691,MP:0003051,MP:0003124,MP:0003308,MP:0003315,MP:0003703,MP:0003817,MP:0004135,MP:0004310,MP:0004343,MP:0004346,MP:0004507,MP:0004509,MP:0004619,MP:0004691,MP:0005298,MP:0005354,MP:0006011,MP:0006279,MP:0006287,MP:0006288,MP:0008320,MP:0009479,MP:0009509,MP:0009510,MP:0009522,MP:0009524,MP:0011026,MP:0011089,MP:0011158	2263	FGFR2	MGI:95523	Fgfr2
            //116           2263	FGFR2               MP:0009522,MP:0009525	MGI:95523	Fgfr2
            while (rs.next()) {
                String modelId = rs.getString("model_id");
                String phenotypeIdString = rs.getString("pheno_ids");
                int entrezId = rs.getInt("entrez_id");
                String humanGeneSymbol = rs.getString("human_gene_symbol");
                String modelSymbol = rs.getString("model_symbol");
                        
                String[] mpInitial = phenotypeIdString.split(",");
                List<String> phenotypeIds = Arrays.asList(mpInitial);
                GeneModel model = new GeneModel(entrezId, humanGeneSymbol, modelId, modelSymbol, phenotypeIds);
                models.add(model);
            }
        } catch (SQLException e) {
            logger.error("Problem setting up model query: {}", modelQuery, e);
        }
        return models;
    }

}
