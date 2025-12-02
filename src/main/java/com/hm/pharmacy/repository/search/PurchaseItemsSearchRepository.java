package com.hm.pharmacy.repository.search;

import co.elastic.clients.elasticsearch._types.query_dsl.QueryStringQuery;
import com.hm.pharmacy.domain.PurchaseItems;
import com.hm.pharmacy.repository.PurchaseItemsRepository;
import java.util.stream.Stream;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.scheduling.annotation.Async;

/**
 * Spring Data Elasticsearch repository for the {@link PurchaseItems} entity.
 */
public interface PurchaseItemsSearchRepository
    extends ElasticsearchRepository<PurchaseItems, Long>, PurchaseItemsSearchRepositoryInternal {}

interface PurchaseItemsSearchRepositoryInternal {
    Stream<PurchaseItems> search(String query);

    Stream<PurchaseItems> search(Query query);

    @Async
    void index(PurchaseItems entity);

    @Async
    void deleteFromIndexById(Long id);
}

class PurchaseItemsSearchRepositoryInternalImpl implements PurchaseItemsSearchRepositoryInternal {

    private final ElasticsearchTemplate elasticsearchTemplate;
    private final PurchaseItemsRepository repository;

    PurchaseItemsSearchRepositoryInternalImpl(ElasticsearchTemplate elasticsearchTemplate, PurchaseItemsRepository repository) {
        this.elasticsearchTemplate = elasticsearchTemplate;
        this.repository = repository;
    }

    @Override
    public Stream<PurchaseItems> search(String query) {
        NativeQuery nativeQuery = new NativeQuery(QueryStringQuery.of(qs -> qs.query(query))._toQuery());
        return search(nativeQuery);
    }

    @Override
    public Stream<PurchaseItems> search(Query query) {
        return elasticsearchTemplate.search(query, PurchaseItems.class).map(SearchHit::getContent).stream();
    }

    @Override
    public void index(PurchaseItems entity) {
        repository.findById(entity.getId()).ifPresent(elasticsearchTemplate::save);
    }

    @Override
    public void deleteFromIndexById(Long id) {
        elasticsearchTemplate.delete(String.valueOf(id), PurchaseItems.class);
    }
}
