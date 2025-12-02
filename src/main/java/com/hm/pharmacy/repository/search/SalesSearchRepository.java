package com.hm.pharmacy.repository.search;

import co.elastic.clients.elasticsearch._types.query_dsl.QueryStringQuery;
import com.hm.pharmacy.domain.Sales;
import com.hm.pharmacy.repository.SalesRepository;
import java.util.stream.Stream;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.scheduling.annotation.Async;

/**
 * Spring Data Elasticsearch repository for the {@link Sales} entity.
 */
public interface SalesSearchRepository extends ElasticsearchRepository<Sales, Long>, SalesSearchRepositoryInternal {}

interface SalesSearchRepositoryInternal {
    Stream<Sales> search(String query);

    Stream<Sales> search(Query query);

    @Async
    void index(Sales entity);

    @Async
    void deleteFromIndexById(Long id);
}

class SalesSearchRepositoryInternalImpl implements SalesSearchRepositoryInternal {

    private final ElasticsearchTemplate elasticsearchTemplate;
    private final SalesRepository repository;

    SalesSearchRepositoryInternalImpl(ElasticsearchTemplate elasticsearchTemplate, SalesRepository repository) {
        this.elasticsearchTemplate = elasticsearchTemplate;
        this.repository = repository;
    }

    @Override
    public Stream<Sales> search(String query) {
        NativeQuery nativeQuery = new NativeQuery(QueryStringQuery.of(qs -> qs.query(query))._toQuery());
        return search(nativeQuery);
    }

    @Override
    public Stream<Sales> search(Query query) {
        return elasticsearchTemplate.search(query, Sales.class).map(SearchHit::getContent).stream();
    }

    @Override
    public void index(Sales entity) {
        repository.findById(entity.getId()).ifPresent(elasticsearchTemplate::save);
    }

    @Override
    public void deleteFromIndexById(Long id) {
        elasticsearchTemplate.delete(String.valueOf(id), Sales.class);
    }
}
