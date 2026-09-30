package com.netflix.watchspaces.service;

import com.netflix.watchspaces.domain.dto.response.TitleResponse;
import com.netflix.watchspaces.domain.entity.Title;
import com.netflix.watchspaces.repository.TitleRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.stream.Collectors;

@Service
public class TitleService {

    private final TitleRepository titleRepository;

    public TitleService(TitleRepository titleRepository) {
        this.titleRepository = titleRepository;
    }

    public Flux<TitleResponse> getAllTitles() {
        return Mono.fromCallable(() -> titleRepository.findAll().stream()
                        .map(TitleResponse::fromEntity)
                        .collect(Collectors.toList()))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMapMany(Flux::fromIterable);
    }

    public Mono<TitleResponse> getTitleById(Long id) {
        return Mono.fromCallable(() -> {
            Title title = titleRepository.findById(id)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Title not found with ID: " + id));
            return TitleResponse.fromEntity(title);
        }).subscribeOn(Schedulers.boundedElastic());
    }
}
