package com.hellocaldav;

import java.util.Date;

/**
 *
 * @author brad
 */
public class Meeting {
    private long id;
    private String name;
    private Date modifiedDate;
    private byte[] icalData;

    /**
     * The filename within the user
     * 
     * @return 
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }    

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Date getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(Date modifiedDate) {
        this.modifiedDate = modifiedDate;
    }
               
    public byte[] getIcalData() {
        return icalData;
    }

    public void setIcalData(byte[] icalData) {
        this.icalData = icalData;
    }
    
}
