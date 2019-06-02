package am.ik.handson.expenditure;

import am.ik.handson.App;
import io.r2dbc.spi.ConnectionFactories;
import io.r2dbc.spi.ConnectionFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.r2dbc.connectionfactory.R2dbcTransactionManager;
import org.springframework.data.r2dbc.core.DatabaseClient;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class R2dbcExpenditureRepositoryTest {

    R2dbcExpenditureRepository expenditureRepository;

    DatabaseClient databaseClient;

    TransactionalOperator transactionalOperator;

    private List<Expenditure> fixtures = Arrays.asList(
        new ExpenditureBuilder()
            .withExpenditureName("本")
            .withUnitPrice(2000)
            .withQuantity(1)
            .withExpenditureDate(LocalDate.of(2019, 4, 1))
            .createExpenditure(),
        new ExpenditureBuilder()
            .withExpenditureName("コーヒー")
            .withUnitPrice(300)
            .withQuantity(2)
            .withExpenditureDate(LocalDate.of(2019, 4, 2))
            .createExpenditure());

    @BeforeAll
    void init() {
        final ConnectionFactory connectionFactory = ConnectionFactories.get("r2dbc:h2:file:///./target/test");
        this.databaseClient = DatabaseClient.builder()
            .connectionFactory(connectionFactory)
            .build();
        this.transactionalOperator = TransactionalOperator.create(new R2dbcTransactionManager(connectionFactory));
        this.expenditureRepository = new R2dbcExpenditureRepository(this.databaseClient, transactionalOperator);
        App.initializeDatabase("H2", this.databaseClient).block();
    }

    @BeforeEach
    void each() throws Exception {
        this.databaseClient.execute()
            .sql("TRUNCATE TABLE expenditure")
            .then()
            .thenMany(Flux.fromIterable(this.fixtures)
                .flatMap(expenditure -> this.databaseClient.insert()
                    .into(Expenditure.class)
                    .using(expenditure)
                    .then())
                .as(transactionalOperator::transactional))
            .blockLast();
    }

    @Test
    void findAll() {
        StepVerifier.create(this.expenditureRepository.findAll())
            .consumeNextWith(expenditure -> {
                assertThat(expenditure.getExpenditureId()).isNotNull();
                assertThat(expenditure.getExpenditureName()).isEqualTo("本");
                assertThat(expenditure.getUnitPrice()).isEqualTo(2000);
                assertThat(expenditure.getQuantity()).isEqualTo(1);
                assertThat(expenditure.getExpenditureDate()).isEqualTo(LocalDate.of(2019, 4, 1));
            })
            .consumeNextWith(expenditure -> {
                assertThat(expenditure.getExpenditureId()).isNotNull();
                assertThat(expenditure.getExpenditureName()).isEqualTo("コーヒー");
                assertThat(expenditure.getUnitPrice()).isEqualTo(300);
                assertThat(expenditure.getQuantity()).isEqualTo(2);
                assertThat(expenditure.getExpenditureDate()).isEqualTo(LocalDate.of(2019, 4, 2));
            })
            .verifyComplete();
    }

    @Test
    void findById() {
        Integer expenditureId = this.databaseClient.execute()
            .sql("SELECT expenditure_id FROM expenditure WHERE expenditure_name = :expenditure_name")
            .bind("expenditure_name", "本")
            .map((row, rowMetadata) -> row.get("expenditure_id", Integer.class))
            .one()
            .block();

        StepVerifier.create(this.expenditureRepository.findById(expenditureId))
            .consumeNextWith(expenditure -> {
                assertThat(expenditure.getExpenditureId()).isNotNull();
                assertThat(expenditure.getExpenditureName()).isEqualTo("本");
                assertThat(expenditure.getUnitPrice()).isEqualTo(2000);
                assertThat(expenditure.getQuantity()).isEqualTo(1);
                assertThat(expenditure.getExpenditureDate()).isEqualTo(LocalDate.of(2019, 4, 1));
            })
            .verifyComplete();
    }

    @Test
    void findById_Empty() {
        Integer latestId = this.databaseClient.execute()
            .sql("SELECT MAX(expenditure_id) AS max FROM expenditure")
            .map((row, rowMetadata) -> row.get("max", Integer.class))
            .one()
            .block();

        StepVerifier.create(this.expenditureRepository.findById(latestId + 1))
            .verifyComplete();
    }

    @Test
    void save() {
        Integer latestId = this.databaseClient.execute()
            .sql("SELECT MAX(expenditure_id) AS max FROM expenditure")
            .map((row, rowMetadata) -> row.get("max", Integer.class))
            .one()
            .block();

        Expenditure create = new ExpenditureBuilder()
            .withExpenditureName("ビール")
            .withUnitPrice(250)
            .withQuantity(1)
            .withExpenditureDate(LocalDate.of(2019, 4, 3))
            .createExpenditure();

        StepVerifier.create(this.expenditureRepository.save(create))
            .consumeNextWith(expenditure -> {
                assertThat(expenditure.getExpenditureId()).isGreaterThan(latestId);
                assertThat(expenditure.getExpenditureName()).isEqualTo("ビール");
                assertThat(expenditure.getUnitPrice()).isEqualTo(250);
                assertThat(expenditure.getQuantity()).isEqualTo(1);
                assertThat(expenditure.getExpenditureDate()).isEqualTo(LocalDate.of(2019, 4, 3));
            })
            .verifyComplete();
    }

    @Test
    void deleteById() {
        Integer expenditureId = this.databaseClient.execute()
            .sql("SELECT expenditure_id FROM expenditure WHERE expenditure_name = :expenditure_name")
            .bind("expenditure_name", "本")
            .map((row, rowMetadata) -> row.get("expenditure_id", Integer.class))
            .one()
            .block();

        StepVerifier.create(this.expenditureRepository.deleteById(expenditureId))
            .verifyComplete();

        StepVerifier.create(this.expenditureRepository.findById(expenditureId))
            .verifyComplete();
    }
}