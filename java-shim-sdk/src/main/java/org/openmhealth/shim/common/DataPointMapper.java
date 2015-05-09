package org.openmhealth.shim.common;


import org.openmhealth.schema.domain.omh.DataPoint;

import java.util.ArrayList;
import java.util.List;


/**
 * A mapper that takes a generic input and creates data points.
 *
 * @param <DP> the body type of the data points to create
 * @param <I> the input type
 * @author Emerson Farrugia
 */
public interface DataPointMapper<DP extends DataPoint, I> {

    List<DP> asDataPoints(I input);

    /**
     * By default, handle each input individually.
     */
    default List<DP> asDataPoints(List<I> input) {
        List<DP> dataPoints = new ArrayList<>();

        for (I i : input) {
            dataPoints.addAll(asDataPoints(i));
        }

        return dataPoints;
    }
}
