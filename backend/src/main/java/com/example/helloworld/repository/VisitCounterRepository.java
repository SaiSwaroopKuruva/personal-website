package com.example.helloworld.repository;

import com.example.helloworld.model.VisitCounter;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VisitCounterRepository extends JpaRepository<VisitCounter, Long> {
}
