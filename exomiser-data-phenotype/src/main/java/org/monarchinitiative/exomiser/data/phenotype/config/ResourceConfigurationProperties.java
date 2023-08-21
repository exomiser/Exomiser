/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2020 Queen Mary University of London.
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

package org.monarchinitiative.exomiser.data.phenotype.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
@ConfigurationProperties("phenotype.resource")
public class ResourceConfigurationProperties {

    // Disease Gene-Pheno
    @NestedConfigurationProperty
    private ResourceProperties mim2gene = ResourceProperties.ofRemote("https://omim.org/static/omim/data/", "mim2gene.txt");

    @NestedConfigurationProperty
    private ResourceProperties genemap2 = ResourceProperties.ofRemote("https://data.omim.org/downloads/nLKYV3qGSpe-HOV8OfliKA/", "genemap2.txt");

    @NestedConfigurationProperty
    private ResourceProperties hpoAnnotations = ResourceProperties.ofRemote("https://purl.obolibrary.org/obo/hp/hpoa/", "phenotype.hpoa");

    @NestedConfigurationProperty
    private ResourceProperties orphaProduct1 = ResourceProperties.ofRemote("http://www.orphadata.org/data/xml/", "en_product1.xml");

    @NestedConfigurationProperty
    private ResourceProperties orphaProduct6 = ResourceProperties.ofRemote("http://www.orphadata.org/data/xml/", "en_product6.xml");

    @NestedConfigurationProperty
    private ResourceProperties orphaProduct9Ages = ResourceProperties.ofRemote("http://www.orphadata.org/data/xml/", "en_product9_ages.xml");

    @NestedConfigurationProperty
    private ResourceProperties hgncCompleteSet = ResourceProperties.ofRemote("ftp://ftp.ebi.ac.uk/pub/databases/genenames/new/tsv/", "hgnc_complete_set.txt");


    // Genes-Phenotypes
    //Mouse
    @NestedConfigurationProperty
    private ResourceProperties mgiGenePheno = ResourceProperties.ofRemote("http://www.informatics.jax.org/downloads/reports/", "MGI_GenePheno.rpt");

    @NestedConfigurationProperty
    private ResourceProperties impcAllGenotypePhenotype = ResourceProperties.ofRemote("http://ftp.ebi.ac.uk/pub/databases/impc/all-data-releases/latest/results/", "genotype-phenotype-assertions-ALL.csv.gz");

    @NestedConfigurationProperty
    private ResourceProperties mgiMouseHumanOrthologs = ResourceProperties.ofRemote("http://www.informatics.jax.org/downloads/reports/", "HOM_MouseHumanSequence.rpt");

    @NestedConfigurationProperty
    private ResourceProperties ensemblMouseHumanOrthologs = new ResourceProperties("http://www.ensembl.org/biomart/martservice?query=%3C?xml%20version=%221.0%22%20encoding=%22UTF-8%22?%3E%20%3C!DOCTYPE%20Query%3E%20%3CQuery%20%20virtualSchemaName%20=%20%22default%22%20formatter%20=%20%22TSV%22%20header%20=%20%220%22%20uniqueRows%20=%20%220%22%20count%20=%20%22%22%20datasetConfigVersion%20=%20%220.6%22%20%3E%20%20%3CDataset%20name%20=%20%22hsapiens_gene_ensembl%22%20interface%20=%20%22default%22%20%3E%20%3CAttribute%20name%20=%20%22entrezgene_id%22%20/%3E%20%3CAttribute%20name%20=%20%22hgnc_symbol%22%20/%3E%20%3C/Dataset%3E%20%3CDataset%20name%20=%20%22mmusculus_gene_ensembl%22%20interface%20=%20%22default%22%20%3E%20%3CAttribute%20name%20=%20%22mgi_id%22%20/%3E%20%3CAttribute%20name%20=%20%22mgi_symbol%22%20/%3E%20%3C/Dataset%3E%20%3C/", "Query%3E", "human_mouse_ensembl_orthologs.txt");

    // Fish
    @NestedConfigurationProperty
    private ResourceProperties monarchFishPhenotypes = ResourceProperties.ofRemote("https://archive.monarchinitiative.org/latest/owlsim/data/Danio_rerio/", "Dr_gene_phenotype.txt");

    @NestedConfigurationProperty
    private ResourceProperties monarchFishGeneLabels = ResourceProperties.ofRemote("https://archive.monarchinitiative.org/latest/owlsim/data/Danio_rerio/", "Dr_gene_labels.txt");

    @NestedConfigurationProperty
    private ResourceProperties zfinFishHumanOrthologs = ResourceProperties.ofRemote("https://zfin.org/downloads/", "human_orthos.txt");

    // Ontologies
    /**
     * The HPO
     */
    @NestedConfigurationProperty
    private ResourceProperties hp = ResourceProperties.ofRemote("https://purl.obolibrary.org/obo/hp/", "hp.obo");

    @NestedConfigurationProperty
    private ResourceProperties mp = ResourceProperties.ofRemote("https://github.com/obophenotype/mammalian-phenotype-ontology/releases/download/current/", "mp.obo");

    @NestedConfigurationProperty
    private ResourceProperties zp = ResourceProperties.ofRemote("https://archive.monarchinitiative.org/latest/owlsim/data/Danio_rerio/", "Dr_phenotype_labels.txt");

    @NestedConfigurationProperty
    private ResourceProperties hpHpMappings = ResourceProperties.ofLocal("hp-hp-phenodigm-cache.txt.gz");

    @NestedConfigurationProperty
    private ResourceProperties hpMpMappings = ResourceProperties.ofLocal("hp-mp-phenodigm-cache.txt.gz");

    @NestedConfigurationProperty
    private ResourceProperties hpZpMappings = ResourceProperties.ofLocal("hp-zp-phenodigm-cache.txt.gz");


    public ResourceProperties getMim2gene() {
        return mim2gene;
    }

    public void setMim2gene(ResourceProperties mim2gene) {
        this.mim2gene = mim2gene;
    }

    public ResourceProperties getGenemap2() {
        return genemap2;
    }

    public void setGenemap2(ResourceProperties genemap2) {
        this.genemap2 = genemap2;
    }

    public ResourceProperties getHpoAnnotations() {
        return hpoAnnotations;
    }

    public void setHpoAnnotations(ResourceProperties hpoAnnotations) {
        this.hpoAnnotations = hpoAnnotations;
    }

    public ResourceProperties getOrphaProduct1() {
        return orphaProduct1;
    }

    public void setOrphaProduct1(ResourceProperties orphaProduct1) {
        this.orphaProduct1 = orphaProduct1;
    }

    public ResourceProperties getOrphaProduct6() {
        return orphaProduct6;
    }

    public void setOrphaProduct6(ResourceProperties orphaProduct6) {
        this.orphaProduct6 = orphaProduct6;
    }

    public ResourceProperties getOrphaProduct9Ages() {
        return orphaProduct9Ages;
    }

    public void setOrphaProduct9Ages(ResourceProperties orphaProduct9Ages) {
        this.orphaProduct9Ages = orphaProduct9Ages;
    }

    public ResourceProperties getHgncCompleteSet() {
        return hgncCompleteSet;
    }

    public void setHgncCompleteSet(ResourceProperties hgncCompleteSet) {
        this.hgncCompleteSet = hgncCompleteSet;
    }

    public ResourceProperties getMgiGenePheno() {
        return mgiGenePheno;
    }

    public void setMgiGenePheno(ResourceProperties mgiGenePheno) {
        this.mgiGenePheno = mgiGenePheno;
    }

    public ResourceProperties getImpcAllGenotypePhenotype() {
        return impcAllGenotypePhenotype;
    }

    public void setImpcAllGenotypePhenotype(ResourceProperties impcAllGenotypePhenotype) {
        this.impcAllGenotypePhenotype = impcAllGenotypePhenotype;
    }

    public ResourceProperties getMgiMouseHumanOrthologs() {
        return mgiMouseHumanOrthologs;
    }

    public void setMgiMouseHumanOrthologs(ResourceProperties mgiMouseHumanOrthologs) {
        this.mgiMouseHumanOrthologs = mgiMouseHumanOrthologs;
    }

    public ResourceProperties getEnsemblMouseHumanOrthologs() {
        return ensemblMouseHumanOrthologs;
    }

    public void setEnsemblMouseHumanOrthologs(ResourceProperties ensemblMouseHumanOrthologs) {
        this.ensemblMouseHumanOrthologs = ensemblMouseHumanOrthologs;
    }

    public ResourceProperties getMonarchFishPhenotypes() {
        return monarchFishPhenotypes;
    }

    public void setMonarchFishPhenotypes(ResourceProperties monarchFishPhenotypes) {
        this.monarchFishPhenotypes = monarchFishPhenotypes;
    }

    public ResourceProperties getMonarchFishGeneLabels() {
        return monarchFishGeneLabels;
    }

    public void setMonarchFishGeneLabels(ResourceProperties monarchFishGeneLabels) {
        this.monarchFishGeneLabels = monarchFishGeneLabels;
    }

    public ResourceProperties getZfinFishHumanOrthologs() {
        return zfinFishHumanOrthologs;
    }

    public void setZfinFishHumanOrthologs(ResourceProperties zfinFishHumanOrthologs) {
        this.zfinFishHumanOrthologs = zfinFishHumanOrthologs;
    }

    public ResourceProperties getHp() {
        return hp;
    }

    public void setHp(ResourceProperties hp) {
        this.hp = hp;
    }

    public ResourceProperties getMp() {
        return mp;
    }

    public void setMp(ResourceProperties mp) {
        this.mp = mp;
    }

    public ResourceProperties getZp() {
        return zp;
    }

    public void setZp(ResourceProperties zp) {
        this.zp = zp;
    }

    public ResourceProperties getHpHpMappings() {
        return hpHpMappings;
    }

    public void setHpHpMappings(ResourceProperties hpHpMappings) {
        this.hpHpMappings = hpHpMappings;
    }

    public ResourceProperties getHpMpMappings() {
        return hpMpMappings;
    }

    public void setHpMpMappings(ResourceProperties hpMpMappings) {
        this.hpMpMappings = hpMpMappings;
    }

    public ResourceProperties getHpZpMappings() {
        return hpZpMappings;
    }

    public void setHpZpMappings(ResourceProperties hpZpMappings) {
        this.hpZpMappings = hpZpMappings;
    }
}
