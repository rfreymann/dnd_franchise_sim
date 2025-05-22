package dnd.franchise.repository;

import dnd.franchise.model.Franchise;
import dnd.franchise.model.UniqueWorker;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class FranchiseRepositoryTest {

    @Autowired
    private FranchiseRepository repository;

    @Test
    void testSaveAndLoadFranchise() {
        Franchise franchise = new Franchise("Test Inc", 1000, 500);
        franchise.setUnskilledWorkers(5);
        franchise.setCostUnskilledWorkers(10);

        UniqueWorker worker = new UniqueWorker("Joe", 5, 3, 2, 200);
        franchise.setUniqueWorkers(List.of(worker));

        Franchise saved = repository.save(franchise);
        Franchise loaded = repository.findById(saved.getId()).orElseThrow();

        assertThat(loaded.getName()).isEqualTo("Test Inc");
        assertThat(loaded.getFunds()).isEqualTo(1000);
        assertThat(loaded.getUniqueWorkers()).hasSize(1);
        assertThat(loaded.getUniqueWorkers().get(0).getName()).isEqualTo("Joe");
    }
}
