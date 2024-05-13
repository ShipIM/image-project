package com.example.imageproject.repository;

import com.example.imageproject.model.entity.FilterRequest;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FilterRequestRepository extends CrudRepository<FilterRequest, Long> {

    Optional<FilterRequest> findByRequestId(String requestId);

    boolean existsByRequestId(String requestId);

}
