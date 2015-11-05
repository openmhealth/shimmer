package org.openmhealth.shim.common.mapper;


import org.openmhealth.schema.domain.omh.DataPoint;
import org.openmhealth.schema.domain.omh.SchemaSupport;

import java.util.List;


/**
 * A mapper that creates data points from one or more inputs.
 *
 * @param <B> the body type of the data points to create
 * @param <I> the input type
 * @author Emerson Farrugia
 */
public interface DataPointMapper<B extends SchemaSupport, I> {

    /**
     * Maps one or more inputs into one or more data points. The parameter cardinality allows a mapper to use different
     * inputs to assemble a data point, e.g. combining a user profile API response and a blood pressure API response to
     * build an identified blood pressure data point.
     *
     * @param inputs the list of inputs
     * @return the list of data points mapped from those inputs
     */
    List<DataPoint<B>> asDataPoints(List<I> inputs);
}
