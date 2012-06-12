/*
 * Copyright 2012 McEvoy Software Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.ettrema.examples.db.domain;


import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;


/**
 *
 * @author brad
 */
@Entity
public class Vehicle implements Serializable {
    private Long id;
    private static final long serialVersionUID = 1L;
    private String vehicleMake;

    private String vehicleModel;

    private Integer vehicleYear;

    private Integer ccRating;

    private String redBookReference;

    private String carGroup;

    /**
     * @return the vehicleMake
     */
    @Column(length = 15)
    public String getVehicleMake() {
        return vehicleMake;
    }

    /**
     * @param vehicleMake the vehicleMake to set
     */
    public void setVehicleMake( String vehicleMake ) {
        this.vehicleMake = vehicleMake;
    }

    /**
     * @return the vehicleModel
     */
    @Column(length = 30)
    public String getVehicleModel() {
        return vehicleModel;
    }

    /**
     * @param vehicleModel the vehicleModel to set
     */
    public void setVehicleModel( String vehicleModel ) {
        this.vehicleModel = vehicleModel;
    }

    /**
     * @return the vehicleYear
     */
    @Column
    public Integer getVehicleYear() {
        return vehicleYear;
    }

    /**
     * @param vehicleYear the vehicleYear to set
     */
    public void setVehicleYear( Integer vehicleYear ) {
        this.vehicleYear = vehicleYear;
    }

    /**
     * @return the ccRating
     */
    @Column
    public Integer getCcRating() {
        return ccRating;
    }

    /**
     * @param ccRating the ccRating to set
     */
    public void setCcRating( Integer ccRating ) {
        this.ccRating = ccRating;
    }

    /**
     * @return the redBookReference
     */
    @Column(length = 8)
    public String getRedBookReference() {
        return redBookReference;
    }

    /**
     * @param redBookReference the redBookReference to set
     */
    public void setRedBookReference( String redBookReference ) {
        this.redBookReference = redBookReference;
    }

    /**
     * @return the carGroup
     */
    @Column(length = 2)
    public String getCarGroup() {
        return carGroup;
    }

    /**
     * @param carGroup the carGroup to set
     */
    public void setCarGroup( String carGroup ) {
        this.carGroup = carGroup;
    }

    @Id
    public Long getId() {
        return id;
    }

    public void setId( Long id ) {
        this.id = id;
    }
}
