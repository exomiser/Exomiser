/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.monarchinitiative.exomiser.cli.options;

import org.apache.commons.cli.Option;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
@Component
public abstract class AbstractOptionMarshaller implements OptionMarshaller {
    
    protected Option option;
    
    @Override
    public String getCommandLineParameter() {
        return option.getLongOpt();
    }

    @Override
    public Option getOption() {
        return option;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + Objects.hashCode(this.option);
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
        final AbstractOptionMarshaller other = (AbstractOptionMarshaller) obj;
        return Objects.equals(this.option, other.option);
    }    

    @Override
    public String toString() {
        return String.format("OptionMarshaller for: '%s'", getCommandLineParameter());
    }
    
}
