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
package org.monarchinitiative.exomiser.web.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.lessThan;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class SelectOptionTest {

    private SelectOption instance;

    private final String value = "value";
    private final String text = "text";

    @BeforeEach
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
        String other = "";
        assertThat(instance.equals(other), is(false));
    }

    @Test
    public void testNotEqualToOtherOption() {
        SelectOption other = new SelectOption("wibble", "sass that hoopy frood");
        assertThat(instance.equals(other), is(false));
    }

    @Test
    public void testIsEqualToOtherOptionWithSameVariables() {
        SelectOption other = new SelectOption(value, text);
        assertThat(instance.equals(other), is(true));
    }

    @Test
    public void selectOptionsAreSortedByTextValue() {
        SelectOption firstOption = new SelectOption("99999", "AAAA");
        SelectOption secondOption = new SelectOption("00000", "ZZZZZ");

        assertThat(firstOption.compareTo(secondOption), lessThan(0));

    }

    @Test
    public void selectOptionsAreSortedByTextValueInLists() {
        SelectOption firstOption = new SelectOption("99999", "AAAA");
        SelectOption secondOption = new SelectOption("111111", "BBBBB");
        SelectOption lastOption = new SelectOption("00000", "ZZZZZ");

        List<SelectOption> expected = new ArrayList<>();
        expected.add(firstOption);
        expected.add(secondOption);
        expected.add(lastOption);


        List<SelectOption> sortedList = new ArrayList<>();
        sortedList.add(lastOption);
        sortedList.add(firstOption);
        sortedList.add(secondOption);

        Collections.sort(sortedList);

        assertThat(sortedList, equalTo(expected));

    }

    @Test
    public void testToString() {
        assertThat(instance.toString(), equalTo(String.format("{text=%s, value=%s}", text, value)));
    }

}
