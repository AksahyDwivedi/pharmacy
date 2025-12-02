package com.hm.pharmacy.repository.search;

import co.elastic.clients.elasticsearch._types.query_dsl.QueryStringQuery;
import com.hm.pharmacy.domain.Payments;
import com.hm.pharmacy.repository.PaymentsRepository;
import java.util.stream.Stream;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.scheduling.annotation.Async;

/**
 * Spring Data Elasticsearch repository for the {@link Payments} entity.
 */
public interface PaymentsSearchRepository extends ElasticsearchRepository<Payments, Long>, PaymentsSearchRepositoryInternal {}

interface PaymentsSearchRepositoryInternal {
    Stream<Payments> search(String query);

    Stream<Payments> search(Query query);

    @Async
    void index(Payments entity);

    @Async
    void deleteFromIndexById(Long id);
}

class PaymentsSearchRepositoryInternalImpl implements PaymentsSearchRepositoryInternal {

    private final ElasticsearchTemplate elasticsearchTemplate;
    private final PaymentsRepository repository;

    PaymentsSearchRepositoryInternalImpl(ElasticsearchTemplate elasticsearchTemplate, PaymentsRepository repository) {
        this.elasticsearchTemplate = elasticsearchTemplate;
        this.repository = repository;
    }

    @Override
    public Stream<Payments> search(String query) {
        NativeQuery nativeQuery = new NativeQuery(QueryStringQuery.of(qs -> qs.query(query))._toQuery());
        return search(nativeQuery);
    }

    @Override
    public Stream<Payments> search(Query query) {
        return elasticsearchTemplate.search(query, Payments.class).map(SearchHit::getContent).stream();
    }

    @Override
    public void index(Payments entity) {
        repository.findById(entity.getId()).ifPresent(elasticsearchTemplate::save);
    }

    @Override
    public void deleteFromIndexById(Long id) {
        elasticsearchTemplate.delete(String.valueOf(id), Payments.class);
    }
}
