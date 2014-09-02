package org.openmhealth.schema.pojos;

public enum TemporalRelationshipToPhysicalActivity implements LabeledEnum {

    at_rest("at rest"),
    active("active"),
    before_exercise("before exercise"),
    after_exercise("after exercise"),
    during_exercise("during exercise");

    private String label;

    TemporalRelationshipToPhysicalActivity(String label) {
        this.label = label;
    }

    @Override
    public String getLabel() {
        return label;
    }

    public static TemporalRelationshipToPhysicalActivity valueForLabel(String label) {
        for (
            TemporalRelationshipToPhysicalActivity val :
            TemporalRelationshipToPhysicalActivity.values()
            ) {
            if (val.getLabel().equals(label)) {
                return val;
            }
        }
        return null;
    }

}
