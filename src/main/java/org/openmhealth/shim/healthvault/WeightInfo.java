package org.openmhealth.shim.healthvault;

import java.io.Serializable;

public class WeightInfo implements Serializable {
    public static final String WeightType = "3d34d87e-7fc1-4153-800f-f56592cb0d17";

    private String id;
    private String value;

    public WeightInfo() {
    }

    public WeightInfo(String id, String value) {
        this.id = id;
        this.value = value;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
