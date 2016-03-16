package org.openmhealth.shim.common.mapper;


import org.openmhealth.schema.domain.omh.DataPoint;
import org.openmhealth.schema.domain.omh.SchemaId;
import org.openmhealth.schema.domain.omh.SchemaSupport;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.util.List;

import static java.util.Collections.singletonList;


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

    /**
     * @see #asDataPoints(List)
     */
    default List<DataPoint<B>> asDataPoints(I input) {

        return asDataPoints(singletonList(input));
    }

    /**
     * Gets the schema identifier of the data point body that this mapper creates. This default implementation assumes
     * that body classes have a default constructor used for serialization, and must be overridden if they don't.
     *
     * @return the schema identifier of the body type
     */
    @SuppressWarnings("unchecked")
    default SchemaId getBodySchemaId() {

        try {
            Class<B> bodyClass = (Class<B>)
                    ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];

            Constructor<B> bodyClassConstructor = bodyClass.getDeclaredConstructor();
            bodyClassConstructor.setAccessible(true);

            return bodyClassConstructor.newInstance().getSchemaId();
        }
        catch (NoSuchMethodException | InstantiationException | InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
