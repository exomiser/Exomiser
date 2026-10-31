package org.monarchinitiative.exomiser.core.prioritisers;

import org.jgrapht.graph.DefaultDirectedGraph;
import org.monarchinitiative.phenol.graph.IdLabeledEdge;
import org.monarchinitiative.phenol.graph.OntologyGraph;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.monarchinitiative.phenol.ontology.data.Relationship;
import org.monarchinitiative.phenol.ontology.data.Term;
import org.monarchinitiative.phenol.ontology.data.TermId;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class HpoOntologyStub implements Ontology {

    @Override
    public Map<String, String> getMetaInfo() {
        return Map.of();
    }

    @Override
    public DefaultDirectedGraph<TermId, IdLabeledEdge> getGraph() {
        return null;
    }

    @Override
    public OntologyGraph<TermId> graph() {
        return null;
    }

    @Override
    public Map<TermId, Term> getTermMap() {
        return Map.of();
    }

    @Override
    public Optional<Term> termForTermId(TermId termId) {
        return Optional.empty();
    }

    @Override
    public Map<Integer, Relationship> getRelationMap() {
        return Map.of();
    }

    @Override
    public Optional<Relationship> relationshipById(int i) {
        return Optional.empty();
    }

    @Override
    public Iterable<TermId> allTermIds() {
        return null;
    }

    @Override
    public Iterable<TermId> nonObsoleteTermIds() {
        return null;
    }

    @Override
    public Iterable<TermId> obsoleteTermIds() {
        return null;
    }

    @Override
    public Collection<Term> getTerms() {
        return List.of();
    }

    @Override
    public Ontology subOntology(TermId termId) {
        return null;
    }

    @Override
    public Optional<String> version() {
        return Optional.empty();
    }
}
