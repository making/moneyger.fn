package am.ik.handson.expenditure;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class InMemoryExpenditureRepository implements ExpenditureRepository {

    final List<Expenditure> expenditures = new CopyOnWriteArrayList<>();

    final AtomicInteger counter = new AtomicInteger(1);

    @Override
    public Flux<Expenditure> findAll() {
        return Flux.fromIterable(this.expenditures);
    }

    @Override
    public Mono<Expenditure> findById(Integer expenditureId) {
        return Mono.justOrEmpty(this.expenditures.stream()
            .filter(x -> Objects.equals(x.getExpenditureId(), expenditureId))
            .findFirst());
    }

    @Override
    public Mono<Expenditure> save(Expenditure expenditure) {
        return Mono.fromCallable(() -> {
            expenditure.setExpenditureId(this.counter.getAndIncrement());
            this.expenditures.add(expenditure);
            return expenditure;
        });
    }

    @Override
    public Mono<Void> deleteById(Integer expenditureId) {
        return Mono.defer(() -> {
            this.expenditures.removeIf(x -> Objects.equals(x.getExpenditureId(), expenditureId));
            return Mono.empty();
        });
    }
}
