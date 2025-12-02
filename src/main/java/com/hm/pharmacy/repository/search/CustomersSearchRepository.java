package com.hm.pharmacy.repository.search;

import co.elastic.clients.elasticsearch._types.query_dsl.QueryStringQuery;
import com.hm.pharmacy.domain.Customers;
import com.hm.pharmacy.repository.CustomersRepository;
import java.util.stream.Stream;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.scheduling.annotation.Async;

/**
 * Spring Data Elasticsearch repository for the {@link Customers} entity.
 */
public interface CustomersSearchRepository extends ElasticsearchRepository<Customers, Long>, CustomersSearchRepositoryInternal {}

interface CustomersSearchRepositoryInternal {
    Stream<Customers> search(String query);

    Stream<Customers> search(Query query);

    @Async
    void index(Customers entity);

    @Async
    void deleteFromIndexById(Long id);
}

class CustomersSearchRepositoryInternalImpl implements CustomersSearchRepositoryInternal {

    private final ElasticsearchTemplate elasticsearchTemplate;
    private final CustomersRepository repository;

    CustomersSearchRepositoryInternalImpl(ElasticsearchTemplate elasticsearchTemplate, CustomersRepository repository) {
        this.elasticsearchTemplate = elasticsearchTemplate;
        this.repository = repository;
    }

    @Override
    public Stream<Customers> search(String query) {
        NativeQuery nativeQuery = new NativeQuery(QueryStringQuery.of(qs -> qs.query(query))._toQuery());
        return search(nativeQuery);
    }

    @Override
    public Stream<Customers> search(Query query) {
        return elasticsearchTemplate.search(query, Customers.class).map(SearchHit::getContent).stream();
    }

    @Override
    public void index(Customers entity) {
        repository.findById(entity.getId()).ifPresent(elasticsearchTemplate::save);
    }

    @Override
    public void deleteFromIndexById(Long id) {
        elasticsearchTemplate.delete(String.valueOf(id), Customers.class);
    }
}
