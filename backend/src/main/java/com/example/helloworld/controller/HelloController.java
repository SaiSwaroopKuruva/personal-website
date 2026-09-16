package com.example.helloworld.controller;

import com.example.helloworld.model.VisitCounter;
import com.example.helloworld.repository.VisitCounterRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@CrossOrigin
public class HelloController {

    private static final Long COUNTER_ID = 1L;

    private final VisitCounterRepository visitCounterRepository;

    public HelloController(VisitCounterRepository visitCounterRepository) {
        this.visitCounterRepository = visitCounterRepository;
    }

    @GetMapping("/api/hello")
    @Transactional
    public Map<String, Object> hello() {
        VisitCounter counter = visitCounterRepository.findById(COUNTER_ID)
                .orElseGet(() -> {
                    VisitCounter created = new VisitCounter();
                    created.setId(COUNTER_ID);
                    created.setCount(0);
                    return created;
                });
        counter.setCount(counter.getCount() + 1);
        visitCounterRepository.save(counter);

        return Map.of(
                "message", "Hello, World! from Spring Boot",
                "visits", counter.getCount()
        );
    }
}
