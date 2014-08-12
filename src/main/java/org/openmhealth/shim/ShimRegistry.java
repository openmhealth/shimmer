package org.openmhealth.shim;

/**
 * Basic contract for a shim registry.
 */
public interface ShimRegistry {

    Shim getShim(String shimKey);

}
