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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author jj8
 */
public class SelectOptionTest {
    
    private SelectOption instance;
    
    private final String value = "value";
    private final String text = "text";
    
    @Before
    public void setUp() {
        instance = new SelectOption(value, text);
    }

    @Test
    public void testGetText() {
        assertThat(instance.getText(), equalTo(text));
    }

    @Test
    public void testGetValue() {
        assertThat(instance.getValue(), equalTo(value));
    }

    @Test
    public void testNotEqualToNull() {
        assertThat(instance.equals(null), is(false));
    }
    
    @Test
    public void testNotEqualToOtherClass() {
        String other  = "";
        assertThat(instance.equals(other), is(false));
    }
    
    @Test
    public void testNotEqualToOtherOption() {
        SelectOption other  = new SelectOption("wibble", "sass that hoopy frood");
        assertThat(instance.equals(other), is(false));
    }
    
    @Test
    public void testIsEqualToOtherOptionWithSameVariables() {
        SelectOption other  = new SelectOption(value, text);
        assertThat(instance.equals(other), is(true));
    }

    @Test
    public void testToString() {
        assertThat(instance.toString(), equalTo(String.format("{text=%s, value=%s}", text, value)));
    }
    
}
