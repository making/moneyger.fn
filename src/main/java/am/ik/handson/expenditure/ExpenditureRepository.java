package am.ik.handson.expenditure;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ExpenditureRepository {

    Flux<Expenditure> findAll();

    Mono<Expenditure> findById(int expenditureId);

    Mono<Expenditure> save(Expenditure expenditure);

    Mono<Void> deleteById(int expenditureId);
}
