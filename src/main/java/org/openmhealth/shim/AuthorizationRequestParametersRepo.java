package org.openmhealth.shim;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuthorizationRequestParametersRepo
    extends MongoRepository<AuthorizationRequestParameters, String> {

    List<AuthorizationRequestParameters> findByUsername(String username);

    AuthorizationRequestParameters findByStateKey(String stateKey);
}
