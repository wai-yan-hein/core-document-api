package core.document.api.service;

import reactor.core.publisher.Mono;

public interface SeqService {
    Mono<Integer> getSequence(String period, String option);
}
