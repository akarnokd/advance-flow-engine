/*
 * Copyright 2010-2013 The Advance EU 7th Framework project consortium
 *
 * This file is part of Advance.
 *
 * Advance is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 *
 * Advance is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Advance.  If not, see
 * <http://www.gnu.org/licenses/>.
 *
 */

package eu.advance.logistics.live.reporter.ws;

import javax.xml.bind.annotation.XmlRegistry;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the eu.advance.logistics.live.reporter.importdata package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {


    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: eu.advance.logistics.live.reporter.importdata.
     * 
     */
    public ObjectFactory() {
    }

    /**
     * @return Create an instance of {@link ImportConsignment }.
     */
    public ImportConsignment createImportConsignments() {
        return new ImportConsignment();
    }

    /**
     * @return Create an instance of {@link ImportVehicleDeclared }.
     */
    public ImportVehicleDeclared createImportVehicleDeclared() {
        return new ImportVehicleDeclared();
    }

    /**
     * @return Create an instance of {@link ImportItem }.
     */
    public ImportItem createImportBarcodes() {
        return new ImportItem();
    }

    /**
     * @return Create an instance of {@link ImportVehicleHeader }.
     */
    public ImportVehicleHeader createImportVehicleHeaders() {
        return new ImportVehicleHeader();
    }

    /**
     * @return Create an instance of {@link ImportEvent }.
     */
    public ImportEvent createImportNotes() {
        return new ImportEvent();
    }

    /**
     * @return Create an instance of {@link ImportVehicleScan }.
     */
    public ImportVehicleScan createImportVehicleScans() {
        return new ImportVehicleScan();
    }

    /**
     * @return Create an instance of {@link DataPack }.
     */
    public DataPack createDataPack() {
        return new DataPack();
    }

    /**
     * @return Create an instance of {@link ImportVehicleItem }.
     */
    public ImportVehicleItem createImportVehicleBarcodes() {
        return new ImportVehicleItem();
    }

    /**
     * @return Create an instance of {@link ImportTerritory }
     */
    public ImportTerritory createImportTerritories() {
        return new ImportTerritory();
    }

}
