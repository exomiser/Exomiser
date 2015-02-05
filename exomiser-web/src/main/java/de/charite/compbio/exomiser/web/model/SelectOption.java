/*
 * Copyright (C) 2014 jj8
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.charite.compbio.exomiser.web.model;

import java.util.Objects;

/**
 * Bean class to represent an HTML select option.
 * 
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class SelectOption implements Comparable<SelectOption>{

    private final String text;
    private final String value;

    public SelectOption(String value, String text) {
        this.value = value;
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public String getValue() {
        return value;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 97 * hash + Objects.hashCode(this.text);
        hash = 97 * hash + Objects.hashCode(this.value);
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
        final SelectOption other = (SelectOption) obj;
        if (!Objects.equals(this.text, other.text)) {
            return false;
        }
        return Objects.equals(this.value, other.value);
    }

    @Override
    public int compareTo(SelectOption other) {
        return this.text.compareTo(other.text);
    }

    @Override
    public String toString() {
        return "{text=" + text + ", value=" + value + "}";
    }
}
