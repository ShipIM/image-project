package com.example.filter.api.repository;

import com.example.filter.model.entity.Processed;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProcessedRepository extends CrudRepository<Processed, Long> {

    boolean existsByOriginalAndRequest(String original, String request);

}
