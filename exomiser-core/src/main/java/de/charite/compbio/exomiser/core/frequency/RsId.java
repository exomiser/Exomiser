/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.charite.compbio.exomiser.core.frequency;

/**
 * Class representing an NCBI dbSNP rsID.
 * 
 * {@link http://www.ncbi.nlm.nih.gov/projects/SNP/index.html}
 * 
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class RsId {
    
    private final int id;

    public RsId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 17 * hash + this.id;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final RsId other = (RsId) obj;
        if (this.id != other.id) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "rsId" + id;
    }
}
