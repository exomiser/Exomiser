package de.charite.compbio.exomiser.core.prioritisers;

import java.util.Objects;

/**
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class BasePriorityResult implements PriorityResult {

    private final PriorityType priorityType;
    private final float priorityScore;

    public BasePriorityResult(PriorityType PriorityType, float priorityScore) {
        this.priorityType = PriorityType;
        this.priorityScore = priorityScore;
    }

    @Override
    public PriorityType getPriorityType() {
        return priorityType;
    }

    @Override
    public String getHTMLCode() {
        return "Not implemented here";
    }

    @Override
    public float getScore() {
        return priorityScore;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 47 * hash + Objects.hashCode(this.priorityType);
        hash = 47 * hash + Float.floatToIntBits(this.priorityScore);
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
        final BasePriorityResult other = (BasePriorityResult) obj;
        if (this.priorityType != other.priorityType) {
            return false;
        }
        if (Float.floatToIntBits(this.priorityScore) != Float.floatToIntBits(other.priorityScore)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "BasePriorityResult{" + "priorityType=" + priorityType + ", priorityScore=" + priorityScore + '}';
    }

}
