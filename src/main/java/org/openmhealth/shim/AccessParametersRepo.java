package org.openmhealth.shim;

import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AccessParametersRepo extends MongoRepository<AccessParameters, String> {

    AccessParameters findByUsernameAndShimKey(String username, String shimKey, Sort sort);

    List<AccessParameters> findAllByUsernameAndShimKey(String username, String shimKey);

}
