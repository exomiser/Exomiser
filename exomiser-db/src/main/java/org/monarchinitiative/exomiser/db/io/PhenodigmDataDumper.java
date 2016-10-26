/*
 * The Exomiser - A tool to annotate and prioritize variants
 *
 * Copyright (C) 2012 - 2016  Charite Universitätsmedizin Berlin and Genome Research Ltd.
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.monarchinitiative.exomiser.db.io;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Connects to Phenodigm and dumps out data in pipe delimited format to the
 * specified path.
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
@Component
public class PhenodigmDataDumper {

    private static final Logger logger = LoggerFactory.getLogger(PhenodigmDataDumper.class);

    @Autowired
    private DataSource phenodigmDataSource;

    /**
     *
     * @param outputPath
     */
    public void dumpPhenodigmData(Path outputPath) {
        logger.info("Starting to dump files");
        if (outputPath.toFile().mkdir()) {
            logger.info("Created new directory {} for Phenodigm datadumps.", outputPath);
        }
        dumpMp(outputPath, "mp.pg");
        dumpMouseGeneOrthologs(outputPath, "human2mouseOrthologs.pg");
        dumpDiseaseHp(outputPath, "diseaseHp.pg");
        dumpMouseMp(outputPath, "mouseMp.pg");
        dumpOmimTerms(outputPath, "omimTerms.pg");
        dumpHpMpMapping(outputPath, "hpMpMapping.pg");
        dumpHpHpMapping(outputPath, "hpHpMapping.pg");
        dumpFishGeneOrthologs(outputPath, "human2fishOrthologs.pg");
        dumpOrphanet(outputPath, "orphanet.pg");
        dumpZp(outputPath, "zp.pg");
        dumpFishZp(outputPath, "zfin_zp.pg");
        dumpHpZpMapping(outputPath, "hpZpMapping.pg");
        
    }

    protected File dumpOrphanet(Path outputPath, String outName) {
        File outfile = new File(outputPath.toFile(), outName);
        logger.info("Dumping Orphanet data to file: {}", outfile);

        String sql = "select distinct d.disease_id, entrezgene, disease_term "
                + "from mouse_disease_gene_summary mdm, disease d, mouse_gene_ortholog mgo "
                + "where d.disease_id=mdm.disease_id and mdm.model_gene_id = mgo.model_gene_id and "
                + "human_curated = 1 and d.disease_id like '%ORPHA%' and entrezgene is not null";
        //no need to close things when using the try-with-resources            
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outfile));
                Connection connection = phenodigmDataSource.getConnection();
                PreparedStatement ps = connection.prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)) {
            ps.setFetchSize(Integer.MIN_VALUE);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String diseaseId = rs.getString("disease_id");
                String entrezId = rs.getString("entrezgene");
                String diseaseTerm = rs.getString("disease_term");

                String outLine = String.format("%s||%s|%s||", diseaseId, diseaseTerm,entrezId);
                writer.write(outLine);
                writer.newLine();
            }

        } catch (IOException | SQLException ex) {
            logger.error(null, ex);
        }
        return outfile;
    }

    protected File dumpMp(Path outputPath, String outName) {
        File outfile = new File(outputPath.toFile(), outName);
        logger.info("Dumping Phenodigm MP data to file: {}", outfile);

        String sql = "select mp_id, term from mp";
        //no need to close things when using the try-with-resources            
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outfile));
                Connection connection = phenodigmDataSource.getConnection();
                PreparedStatement ps = connection.prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)) {
            ps.setFetchSize(Integer.MIN_VALUE);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String mpId = rs.getString("mp_id");
                String mpTerm = rs.getString("term");

                String outLine = String.format("%s|%s", mpId, mpTerm);
                writer.write(outLine);
                writer.newLine();
            }

        } catch (IOException | SQLException ex) {
            logger.error(null, ex);
        }
        return outfile;
    }

    protected File dumpZp(Path outputPath, String outName) {
        File outfile = new File(outputPath.toFile(), outName);
        logger.info("Dumping Phenodigm ZP data to file: {}", outfile);

        String sql = "select zp_id, term from zp";
        //no need to close things when using the try-with-resources            
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outfile));
                Connection connection = phenodigmDataSource.getConnection();
                PreparedStatement ps = connection.prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)) {
            ps.setFetchSize(Integer.MIN_VALUE);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String zpId = rs.getString("zp_id");
                String zpTerm = rs.getString("term");

                String outLine = String.format("%s|%s", zpId, zpTerm);
                writer.write(outLine);
                writer.newLine();
            }

        } catch (IOException | SQLException ex) {
            logger.error(null, ex);
        }
        return outfile;
    }

    
    protected  File dumpMouseGeneOrthologs(Path outputPath, String outName) {
        File outfile = new File(outputPath.toFile(), outName);
        logger.info("Dumping Phenodigm MouseGeneOrtholog data to file: {}", outfile);

        String sql = "select model_gene_id, model_gene_symbol, hgnc_gene_symbol, entrezgene from mouse_gene_ortholog where entrezgene is not NULL";
        //no need to close things when using the try-with-resources            
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outfile));
                Connection connection = phenodigmDataSource.getConnection();
                PreparedStatement ps = connection.prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)) {
            ps.setFetchSize(Integer.MIN_VALUE);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String modelGeneId = rs.getString("model_gene_id");
                String modelGeneSymbol = rs.getString("model_gene_symbol");
                String hgncGeneId = rs.getString("hgnc_gene_symbol");
                String entrez = rs.getString("entrezgene");

                String outLine = String.format("%s|%s|%s|%s", modelGeneId, modelGeneSymbol, hgncGeneId, entrez);
                writer.write(outLine);
                writer.newLine();
            }

        } catch (IOException | SQLException ex) {
            logger.error(null, ex);
        }
        return outfile;
    }

    protected File dumpFishGeneOrthologs(Path outputPath, String outName) {
        File outfile = new File(outputPath.toFile(), outName);
        logger.info("Dumping Phenodigm FishGeneOrtholog data to file: {}", outfile);

        String sql = "select model_gene_id, model_gene_symbol, hgnc_id, entrezgene from fish_gene_ortholog";
        //no need to close things when using the try-with-resources            
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outfile));
                Connection connection = phenodigmDataSource.getConnection();
                PreparedStatement ps = connection.prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)) {
            ps.setFetchSize(Integer.MIN_VALUE);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String modelGeneId = rs.getString("model_gene_id");
                String modelGeneSymbol = rs.getString("model_gene_symbol");
                String hgncGeneId = rs.getString("hgnc_id");
                String entrez = rs.getString("entrezgene");

                String outLine = String.format("%s|%s|%s|%s", modelGeneId, modelGeneSymbol, hgncGeneId, entrez);
                writer.write(outLine);
                writer.newLine();
            }

        } catch (IOException | SQLException ex) {
            logger.error(null, ex);
        }
        return outfile;
    }

    protected File dumpDiseaseHp(Path outputPath, String outName) {
        File outfile = new File(outputPath.toFile(), outName);
        logger.info("Dumping Phenodigm diseaseHp data to file: {}", outfile);
        // ? why I added the join to hp_hp_mapping - results in scores < 1 when run an Exomiser query with full set of HPO IDs defined in the HPO annotation file
        // Going to try reverting and check nothing breaks 
        //String sql = "select distinct disease_id , group_concat(distinct d.hp_id) as hpids from disease_hp d, hp_hp_mapping h where d.hp_id=h.hp_id group by disease_id";
        String sql = "select distinct disease_id , group_concat(distinct d.hp_id) as hpids from disease_hp d group by disease_id";
        //no need to close things when using the try-with-resources            
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outfile));
                Connection connection = phenodigmDataSource.getConnection();
                PreparedStatement ps = connection.prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)) {
            ps.setFetchSize(Integer.MIN_VALUE);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String diseaseId = rs.getString("disease_id");
                String hpIds = rs.getString("hpids");

                String outLine = String.format("%s|%s", diseaseId, hpIds);
                writer.write(outLine);
                writer.newLine();
            }

        } catch (IOException | SQLException ex) {
            logger.error(null, ex);
        }
        return outfile;
    }

    protected File dumpMouseMp(Path outputPath, String outName) {
        File outfile = new File(outputPath.toFile(), outName);
        logger.info("Dumping Phenodigm mouseMp data to file: {}", outfile);

        String sql = "select mgo.model_gene_id, mgo.model_gene_symbol, mmm.model_id, group_concat(distinct mp_id) as mpids "
                + "from mouse_model_mp mmm, mouse_model_gene_ortholog mmgo, mouse_gene_ortholog mgo "
                + "where mgo.model_gene_id = mmgo.model_gene_id and mmgo.model_id = mmm.model_id group by mmm.model_id";
        //no need to close things when using the try-with-resources            
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outfile));
                Connection connection = phenodigmDataSource.getConnection();
                PreparedStatement ps = connection.prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)) {
            ps.setFetchSize(Integer.MIN_VALUE);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String modelGeneId = rs.getString("model_gene_id");
                String modelGeneSymbol = rs.getString("model_gene_symbol");
                String mpIds = rs.getString("mpids");
                String modelId = rs.getString("model_id");

                String outLine = String.format("%s|%s|%s|%s", modelGeneId, modelGeneSymbol, modelId, mpIds);
                writer.write(outLine);
                writer.newLine();
            }

        } catch (IOException | SQLException ex) {
            logger.error(null, ex);
        }
        return outfile;
    }

    protected File dumpFishZp(Path outputPath, String outName) {
        File outfile = new File(outputPath.toFile(), outName);
        logger.info("Dumping Phenodigm fishZp data to file: {}", outfile);

        String sql = "select mgo.model_gene_id, mgo.model_gene_symbol, mmm.model_id, group_concat(distinct zp_id) as zpids "
                + "from fish_model_zp mmm, fish_model_gene_ortholog mmgo, fish_gene_ortholog mgo "
                + "where mgo.model_gene_id = mmgo.model_gene_id and mmgo.model_id = mmm.model_id group by mmm.model_id";
        //no need to close things when using the try-with-resources            
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outfile));
                Connection connection = phenodigmDataSource.getConnection();
                PreparedStatement ps = connection.prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)) {
            ps.setFetchSize(Integer.MIN_VALUE);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String modelGeneId = rs.getString("model_gene_id");
                String modelGeneSymbol = rs.getString("model_gene_symbol");
                String zpIds = rs.getString("zpids");
                String modelId = rs.getString("model_id");

                String outLine = String.format("%s|%s|%s|%s", modelGeneId, modelGeneSymbol, modelId, zpIds);
                writer.write(outLine);
                writer.newLine();
            }

        } catch (IOException | SQLException ex) {
            logger.error(null, ex);
        }
        return outfile;
    }

    protected File dumpOmimTerms(Path outputPath, String outName) {
        File outfile = new File(outputPath.toFile(), outName);
        logger.info("Dumping Phenodigm omimTerms data to file: {}", outfile);

        String sql = "select disease_id, disease_term from disease";
        //no need to close things when using the try-with-resources            
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outfile));
                Connection connection = phenodigmDataSource.getConnection();
                PreparedStatement ps = connection.prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)) {
            ps.setFetchSize(Integer.MIN_VALUE);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String diseaseId = rs.getString("disease_id");
                String diseaseTerm = rs.getString("disease_term");

                String outLine = String.format("%s|%s", diseaseId, diseaseTerm);
                writer.write(outLine);
                writer.newLine();
            }

        } catch (IOException | SQLException ex) {
            logger.error(null, ex);
        }
        return outfile;
    }

    protected File dumpHpMpMapping(Path outputPath, String outName) {
        File outfile = new File(outputPath.toFile(), outName);
        logger.info("Dumping Phenodigm hpMpMapping data to file: {}", outfile);
        //hp_mp_mapping has a mapping_id column 
        int id = 0;

        String sql = "select hp_id, hp_term, mp_id, mp_term, simJ, ic, sqrt(ic*simJ) as score, lcs from hp_mp_mapping";
        //no need to close things when using the try-with-resources            
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outfile));
                Connection connection = phenodigmDataSource.getConnection();
                PreparedStatement ps = connection.prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)) {
            ps.setFetchSize(Integer.MIN_VALUE);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                id++;
                String hpId = rs.getString("hp_id");
                String mpId = rs.getString("mp_id");
                String score = rs.getString("score");
                String hpTerm = rs.getString("hp_term");
                String mpTerm = rs.getString("mp_term");
                String simJ = rs.getString("simJ");
                String ic = rs.getString("ic");
                String lcs = rs.getString("lcs");
                String lcsId = "";
                String lcsTerm = lcs;
                String[] lcsParts = lcs.split("\\(");
                if (lcsParts.length != 1){
                    lcsTerm = lcsParts[0].trim();
                    lcsId = getLcsId(lcsParts);
                }
                String outLine = String.format("%d|%s|%s|%s|%s|%s|%s|%s|%s|%s", id, hpId, hpTerm, mpId, mpTerm, simJ, ic, score, lcsId, lcsTerm);
                writer.write(outLine);
                writer.newLine();
            }

        } catch (IOException | SQLException ex) {
            logger.error(null, ex);
        }
        return outfile;
    }

    protected File dumpHpZpMapping(Path outputPath, String outName) {
        File outfile = new File(outputPath.toFile(), outName);
        logger.info("Dumping Phenodigm hpZpMapping data to file: {}", outfile);
        //hp_mp_mapping has a mapping_id column 
        int id = 0;

        String sql = "select hp_id, hp_term, zp_id, zp_term, simJ, ic, sqrt(ic*simJ) as score, lcs from hp_zp_mapping";
        //no need to close things when using the try-with-resources            
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outfile));
                Connection connection = phenodigmDataSource.getConnection();
                PreparedStatement ps = connection.prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)) {
            ps.setFetchSize(Integer.MIN_VALUE);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                id++;
                String hpId = rs.getString("hp_id");
                String zpId = rs.getString("zp_id");
                String score = rs.getString("score");
                String hpTerm = rs.getString("hp_term");
                String zpTerm = rs.getString("zp_term");
                String simJ = rs.getString("simJ");
                String ic = rs.getString("ic");
                String lcs = rs.getString("lcs");
                String lcsId = "";
                String lcsTerm = lcs;
                String[] lcsParts = lcs.split("\\(");
                if (lcsParts.length != 1){
                    lcsTerm = lcsParts[0].trim();
                    lcsId = getLcsId(lcsParts);
                }
                String outLine = String.format("%d|%s|%s|%s|%s|%s|%s|%s|%s|%s", id, hpId, hpTerm, zpId, zpTerm, simJ, ic, score, lcsId, lcsTerm);
                writer.write(outLine);
                writer.newLine();
            }

        } catch (IOException | SQLException ex) {
            logger.error(null, ex);
        }
        return outfile;
    }
    
    protected File dumpHpHpMapping(Path outputPath, String outName) {
        File outfile = new File(outputPath.toFile(), outName);
        logger.info("Dumping Phenodigm hpHpMapping data to file: {}", outfile);

        //hp_hp_mapping has a mapping_id column 
        int id = 0;

        String sql = "select hp_id, hp_term, hp_id_hit, hp_term_hit, simJ, ic, sqrt(ic*simJ) as score, lcs from hp_hp_mapping";
        //no need to close things when using the try-with-resources            
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outfile));
                Connection connection = phenodigmDataSource.getConnection();
                PreparedStatement ps = connection.prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)) {
            ps.setFetchSize(Integer.MIN_VALUE);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                id++;
                String hpId = rs.getString("hp_id");
                String hpIdHit = rs.getString("hp_id_hit");
                String score = rs.getString("score");
                String hpTerm = rs.getString("hp_term");
                String hpTermHit = rs.getString("hp_term_hit");
                String simJ = rs.getString("simJ");
                String ic = rs.getString("ic");
                String lcs = rs.getString("lcs");
                String lcsId = "";
                String lcsTerm = lcs;
                String[] lcsParts = lcs.split("\\(");
                if (lcsParts.length != 1){
                    lcsTerm = lcsParts[0].trim();
                    lcsId = getLcsId(lcsParts);
                }
                String outLine = String.format("%d|%s|%s|%s|%s|%s|%s|%s|%s|%s", id, hpId, hpTerm, hpIdHit, hpTermHit, simJ, ic, score, lcsId, lcsTerm);
                writer.write(outLine);
                writer.newLine();
            }

        } catch (IOException | SQLException ex) {
            logger.error(null, ex);
        }
        return outfile;
    }

    private String getLcsId(String[] lcsParts) {
        String lcsId;
        lcsId = lcsParts[lcsParts.length - 1];
        lcsId = lcsId.replace(")", "");
        return lcsId;
    }


}
