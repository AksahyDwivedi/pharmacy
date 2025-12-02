package com.hm.pharmacy.repository.search;

import co.elastic.clients.elasticsearch._types.query_dsl.QueryStringQuery;
import com.hm.pharmacy.domain.SaleItems;
import com.hm.pharmacy.repository.SaleItemsRepository;
import java.util.stream.Stream;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.scheduling.annotation.Async;

/**
 * Spring Data Elasticsearch repository for the {@link SaleItems} entity.
 */
public interface SaleItemsSearchRepository extends ElasticsearchRepository<SaleItems, Long>, SaleItemsSearchRepositoryInternal {}

interface SaleItemsSearchRepositoryInternal {
    Stream<SaleItems> search(String query);

    Stream<SaleItems> search(Query query);

    @Async
    void index(SaleItems entity);

    @Async
    void deleteFromIndexById(Long id);
}

class SaleItemsSearchRepositoryInternalImpl implements SaleItemsSearchRepositoryInternal {

    private final ElasticsearchTemplate elasticsearchTemplate;
    private final SaleItemsRepository repository;

    SaleItemsSearchRepositoryInternalImpl(ElasticsearchTemplate elasticsearchTemplate, SaleItemsRepository repository) {
        this.elasticsearchTemplate = elasticsearchTemplate;
        this.repository = repository;
    }

    @Override
    public Stream<SaleItems> search(String query) {
        NativeQuery nativeQuery = new NativeQuery(QueryStringQuery.of(qs -> qs.query(query))._toQuery());
        return search(nativeQuery);
    }

    @Override
    public Stream<SaleItems> search(Query query) {
        return elasticsearchTemplate.search(query, SaleItems.class).map(SearchHit::getContent).stream();
    }

    @Override
    public void index(SaleItems entity) {
        repository.findById(entity.getId()).ifPresent(elasticsearchTemplate::save);
    }

    @Override
    public void deleteFromIndexById(Long id) {
        elasticsearchTemplate.delete(String.valueOf(id), SaleItems.class);
    }
}
