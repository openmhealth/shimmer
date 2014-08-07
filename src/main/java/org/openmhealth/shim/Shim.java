package org.openmhealth.shim;

import org.joda.time.DateTime;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * High level abstraction contract for shims. This version
 * does not take into account any details (oauth, oauth2, custom, etc)
 */
public interface Shim {

    /**
     * returns a unique string identifier for the shim.
     *
     * @return - value of identifier ('jawbone', 'fitbit', etc).
     */
    public String getShimKey();

    /**
     * Retrieve authorization parameter object so that an external
     * endpoint can take control of the flow.
     *
     * @param addlParameters - any parameters that should be added
     *                       to the authorization request.
     * @return AuthorizationParameters needed to iniate oauth flow.
     */
    AuthorizationRequestParameters getAuthorizationRequestParameters(
        final Map<String, String> addlParameters);

    /**
     * Handles the authorization response from the external data provider.
     * In most cases this handler's details will depend on the type of oauth
     * or any other custom parameters.
     * <p/>
     * AuthorizationResponse is agnostic to the type of authentication.
     *
     * @param servletRequest - HTTP request to be handled externally
     * @return Authorization response.
     */
    AuthorizationResponse handleAuthorizationResponse(
        final HttpServletRequest servletRequest);

    /**
     * Obtain data from the external data provider using access parameters
     *
     * @param dataTypeKey      - Identifier for the type of data being retrieved
     * @param accessParameters - parameters required for acessing data, this
     *                         will likely be oauth token + any extras or some
     *                         kind of trusted access.
     * @param startDate        - The start date for the data being retrieved
     * @param endDate          - The end date for the data being retrieved
     * @param columnList       - List of columns required
     * @param numToSkip        - The starting row for the data (for pagination purposes)
     * @param numToReturn      - Number of rows to return
     * @return Generic object wrapper including timestamp, shim, and results
     */
    ShimDataResponse getData(final String dataTypeKey,
                             final AccessParameters accessParameters,
                             final DateTime startDate,
                             final DateTime endDate,
                             final List<String> columnList,
                             final Long numToSkip,
                             final Long numToReturn);
}
