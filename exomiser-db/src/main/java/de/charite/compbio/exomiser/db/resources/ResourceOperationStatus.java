/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.charite.compbio.exomiser.db.resources;

/**
 * Constants for describing the status of a file operation.
 * 
 * @author Jule Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public enum ResourceOperationStatus {
    FAILURE, SUCCESS, UNTRIED, FILE_NOT_FOUND, PARSER_NOT_FOUND;
}
