package com.hm.pharmacy.repository.search;

import co.elastic.clients.elasticsearch._types.query_dsl.QueryStringQuery;
import com.hm.pharmacy.domain.SupplierPayments;
import com.hm.pharmacy.repository.SupplierPaymentsRepository;
import java.util.stream.Stream;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.scheduling.annotation.Async;

/**
 * Spring Data Elasticsearch repository for the {@link SupplierPayments} entity.
 */
public interface SupplierPaymentsSearchRepository
    extends ElasticsearchRepository<SupplierPayments, Long>, SupplierPaymentsSearchRepositoryInternal {}

interface SupplierPaymentsSearchRepositoryInternal {
    Stream<SupplierPayments> search(String query);

    Stream<SupplierPayments> search(Query query);

    @Async
    void index(SupplierPayments entity);

    @Async
    void deleteFromIndexById(Long id);
}

class SupplierPaymentsSearchRepositoryInternalImpl implements SupplierPaymentsSearchRepositoryInternal {

    private final ElasticsearchTemplate elasticsearchTemplate;
    private final SupplierPaymentsRepository repository;

    SupplierPaymentsSearchRepositoryInternalImpl(ElasticsearchTemplate elasticsearchTemplate, SupplierPaymentsRepository repository) {
        this.elasticsearchTemplate = elasticsearchTemplate;
        this.repository = repository;
    }

    @Override
    public Stream<SupplierPayments> search(String query) {
        NativeQuery nativeQuery = new NativeQuery(QueryStringQuery.of(qs -> qs.query(query))._toQuery());
        return search(nativeQuery);
    }

    @Override
    public Stream<SupplierPayments> search(Query query) {
        return elasticsearchTemplate.search(query, SupplierPayments.class).map(SearchHit::getContent).stream();
    }

    @Override
    public void index(SupplierPayments entity) {
        repository.findById(entity.getId()).ifPresent(elasticsearchTemplate::save);
    }

    @Override
    public void deleteFromIndexById(Long id) {
        elasticsearchTemplate.delete(String.valueOf(id), SupplierPayments.class);
    }
}
