/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2021 Queen Mary University of London.
 * Copyright (c) 2012-2016 Charité Universitätsmedizin Berlin and Genome Research Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.monarchinitiative.exomiser.core.prioritisers.service;

import org.monarchinitiative.exomiser.core.phenotype.Organism;
import org.monarchinitiative.exomiser.core.prioritisers.dao.DiseaseTypeCodes;
import org.monarchinitiative.exomiser.core.prioritisers.dao.InheritanceModeCodes;
import org.monarchinitiative.exomiser.core.prioritisers.model.Disease;
import org.monarchinitiative.exomiser.core.prioritisers.model.GeneDiseaseModel;
import org.monarchinitiative.exomiser.core.prioritisers.model.GeneModel;
import org.monarchinitiative.exomiser.core.prioritisers.model.GeneOrthologModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
@Service
public class ModelServiceImpl implements ModelService {

    private static final Logger logger = LoggerFactory.getLogger(ModelService.class);


    private final DataSource phenotypeDataSource;

    public ModelServiceImpl(@Qualifier("phenotypeDataSource") DataSource phenotypeDataSource) {
        this.phenotypeDataSource = phenotypeDataSource;
    }

    @Override
    public List<GeneModel> getHumanGeneDiseaseModels() {
        // TODO: this should use a diseaseDao.getDiseases()
        String modelQuery = "SELECT distinct 'HUMAN' as organism, " +
                "gene_id as entrez_id, " +
                "symbol as human_gene_symbol, " +
                "d.disease_id as disease_id, " +
                "d.diseasename as disease_term, " +
                "d.type as disease_type, " +
                "d.inheritance as inheritance_code, " +
                "hp_id as pheno_ids " +
                "from entrez2sym e, disease_hp M, disease d " +
                "where e.entrezid=d.gene_id " +
                "and M.disease_id=d.disease_id " +
                "and d.TYPE in ('D', 'C', 'S', '?')";
        return runGeneDiseaseModelQuery(modelQuery);
    }

    @Override
    public List<GeneModel> getMouseGeneOrthologModels() {
        String modelQuery = "SELECT 'MOUSE' as organism, entrez_id, human_gene_symbol, mouse_model_id as model_id, M.mgi_gene_id as model_gene_id, M.mgi_gene_symbol as model_gene_symbol, mp_id as pheno_ids FROM mgi_mp M, human2mouse_orthologs H WHERE M.mgi_gene_id=H.mgi_gene_id and human_gene_symbol != 'null'";
        return runGeneOrthologModelQuery(modelQuery);
    }

    @Override
    public List<GeneModel> getFishGeneOrthologModels() {
        String modelQuery = "SELECT 'FISH' as organism, entrez_id, human_gene_symbol, zfin_model_id as model_id, M.zfin_gene_id as model_gene_id, M.zfin_gene_symbol as model_gene_symbol, zp_id as pheno_ids FROM zfin_zp M, human2fish_orthologs H WHERE M.zfin_gene_id=H.zfin_gene_id and human_gene_symbol != 'null'";
        return runGeneOrthologModelQuery(modelQuery);
    }

    private List<GeneModel> runGeneDiseaseModelQuery(String modelQuery) {
        List<GeneModel> models = new ArrayList<>();
        try (Connection connection = phenotypeDataSource.getConnection();
             PreparedStatement findAnnotationStatement = connection.prepareStatement(modelQuery);
             ResultSet rs = findAnnotationStatement.executeQuery()) {
            //each row is an animal model or disease, its phenotypes and the known causitive gene of these phenotypes. e.g.
            //ORGANISM  ENTREZ_ID   HUMAN_GENE_SYMBOL   DISEASE_ID  DISEASE_TERM    pheno_ids
            //HUMAN     2263        FGFR2               OMIM:101600 Apert syndrome  HP:0000174,HP:0000194,HP:0000218,HP:0000238,HP:0000244,HP:0000272,HP:0000303,HP:0000316,HP:0000322,HP:0000324,HP:0000327,HP:0000348,HP:0000431,HP:0000452,HP:0000453,HP:0000470,HP:0000486,HP:0000494,HP:0000508,HP:0000586,HP:0000678,HP:0001156,HP:0001249,HP:0002308,HP:0002676,HP:0002780,HP:0003041,HP:0003070,HP:0003196,HP:0003272,HP:0003307,HP:0003795,HP:0004209,HP:0004322,HP:0004440,HP:0005048,HP:0005280,HP:0005347,HP:0006101,HP:0006110,HP:0009602,HP:0009773,HP:0010055,HP:0010669,HP:0011304
            //HUMAN     2260        FGFR1               OMIM:101600 Another syn...  HP:0000174,HP:0000194,HP:0000218,HP:0000238,HP:0000244,HP:0000272,HP:0000303,HP:0000316,HP:0000322,HP:0000324,HP:0000327,HP:0000348,HP:0000431,HP:0000452,HP:0000453,HP:0000470,HP:0000486,HP:0000494,HP:0000508,HP:0000586,HP:0000678,HP:0001156,HP:0001249,HP:0002308,HP:0002676,HP:0002780,HP:0003041,HP:0003070,HP:0003196,HP:0003272,HP:0003307,HP:0003795,HP:0004209,HP:0004322,HP:0004440,HP:0005048,HP:0005280,HP:0005347,HP:0006101,HP:0006110,HP:0009602,HP:0009773,HP:0010055,HP:0010669,HP:0011304
            while (rs.next()) {
                List<String> phenotypes = List.of(rs.getString("pheno_ids").split(","));
                Disease disease = Disease.builder()
                        .diseaseId(rs.getString("disease_id"))
                        .diseaseName(rs.getString("disease_term"))
                        .associatedGeneId(rs.getInt("entrez_id"))
                        .associatedGeneSymbol(rs.getString("human_gene_symbol"))
                        .inheritanceMode(InheritanceModeCodes.parseInheritanceModeCode(rs.getString("inheritance_code")))
                        .diseaseType(DiseaseTypeCodes.parseDiseaseTypeCode(rs.getString("disease_type")))
                        .phenotypeIds(phenotypes)
                        .build();

                String modelId = disease.getDiseaseId() + "_" + disease.getAssociatedGeneId();

                GeneDiseaseModel model = new GeneDiseaseModel(modelId, Organism.HUMAN, disease);
                models.add(model);
            }
        } catch (SQLException e) {
            logger.error("Problem setting up model query: {}", modelQuery, e);
        }
        return List.copyOf(models);
    }
        
    private List<GeneModel> runGeneOrthologModelQuery(String modelQuery) {
        List<GeneModel> models = new ArrayList<>();
        try (Connection connection = phenotypeDataSource.getConnection();
             PreparedStatement findAnnotationStatement = connection.prepareStatement(modelQuery);
             ResultSet rs = findAnnotationStatement.executeQuery()) {
            //each row is an animal model or disease, its phenotypes and the known causitive gene of these phenotypes. e.g.
            //ORGANISM  MODEL_ID      ENTREZ_ID	HUMAN_GENE_SYMBOL   MODEL_GENE_ID	MODEL_GENE_SYMBOL   pheno_ids                   
            //MOUSE     115           2263	FGFR2               MGI:95523           Fgfr2               MP:0000031,MP:0000035,MP:0000039,MP:0000081,MP:0000111,MP:0000118,MP:0000440,MP:0000470,MP:0000492,MP:0000551,MP:0000557,MP:0000613,MP:0000629,MP:0000633,MP:0001176,MP:0001181,MP:0001199,MP:0001201,MP:0001218,MP:0001231,MP:0001244,MP:0001265,MP:0001341,MP:0002095,MP:0002428,MP:0002691,MP:0003051,MP:0003124,MP:0003308,MP:0003315,MP:0003703,MP:0003817,MP:0004135,MP:0004310,MP:0004343,MP:0004346,MP:0004507,MP:0004509,MP:0004619,MP:0004691,MP:0005298,MP:0005354,MP:0006011,MP:0006279,MP:0006287,MP:0006288,MP:0008320,MP:0009479,MP:0009509,MP:0009510,MP:0009522,MP:0009524,MP:0011026,MP:0011089,MP:0011158
            //MOUSE     116           2263	FGFR2               MGI:95523           Fgfr2               MP:0009522,MP:0009525	
            while (rs.next()) {
                Organism organism = Organism.valueOf(rs.getString("organism"));
                String modelId = rs.getString("model_id");
                
                int entrezId = rs.getInt("entrez_id");
                String humanGeneSymbol = rs.getString("human_gene_symbol");
                
                String modelGeneId = rs.getString("model_gene_id");
                String modelGeneSymbol = rs.getString("model_gene_symbol");
                        
                modelId = modelGeneId + "_" + modelId;
                
                String phenotypeIdString = toEmptyIfNull(rs.getString("pheno_ids"));

                String[] mpInitial = phenotypeIdString.split(",");
                List<String> phenotypeIds = List.of(mpInitial);
                
                GeneOrthologModel model = new GeneOrthologModel(modelId, organism, entrezId, humanGeneSymbol, modelGeneId, modelGeneSymbol, phenotypeIds);
                models.add(model);
            }
        } catch (SQLException e) {
            logger.error("Problem setting up model query: {}", modelQuery, e);
        }
        return List.copyOf(models);
    }

    private String toEmptyIfNull(String result) {
        if (result == null) {
            return "";
        }
        return result;
    }

}
