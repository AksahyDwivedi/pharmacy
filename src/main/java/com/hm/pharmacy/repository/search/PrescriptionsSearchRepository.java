package com.hm.pharmacy.repository.search;

import co.elastic.clients.elasticsearch._types.query_dsl.QueryStringQuery;
import com.hm.pharmacy.domain.Prescriptions;
import com.hm.pharmacy.repository.PrescriptionsRepository;
import java.util.stream.Stream;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.scheduling.annotation.Async;

/**
 * Spring Data Elasticsearch repository for the {@link Prescriptions} entity.
 */
public interface PrescriptionsSearchRepository
    extends ElasticsearchRepository<Prescriptions, Long>, PrescriptionsSearchRepositoryInternal {}

interface PrescriptionsSearchRepositoryInternal {
    Stream<Prescriptions> search(String query);

    Stream<Prescriptions> search(Query query);

    @Async
    void index(Prescriptions entity);

    @Async
    void deleteFromIndexById(Long id);
}

class PrescriptionsSearchRepositoryInternalImpl implements PrescriptionsSearchRepositoryInternal {

    private final ElasticsearchTemplate elasticsearchTemplate;
    private final PrescriptionsRepository repository;

    PrescriptionsSearchRepositoryInternalImpl(ElasticsearchTemplate elasticsearchTemplate, PrescriptionsRepository repository) {
        this.elasticsearchTemplate = elasticsearchTemplate;
        this.repository = repository;
    }

    @Override
    public Stream<Prescriptions> search(String query) {
        NativeQuery nativeQuery = new NativeQuery(QueryStringQuery.of(qs -> qs.query(query))._toQuery());
        return search(nativeQuery);
    }

    @Override
    public Stream<Prescriptions> search(Query query) {
        return elasticsearchTemplate.search(query, Prescriptions.class).map(SearchHit::getContent).stream();
    }

    @Override
    public void index(Prescriptions entity) {
        repository.findById(entity.getId()).ifPresent(elasticsearchTemplate::save);
    }

    @Override
    public void deleteFromIndexById(Long id) {
        elasticsearchTemplate.delete(String.valueOf(id), Prescriptions.class);
    }
}
