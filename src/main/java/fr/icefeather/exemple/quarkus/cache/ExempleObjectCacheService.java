package fr.icefeather.exemple.quarkus.cache;

import fr.icefeather.exemple.quarkus.domain.ExempleObject;
import io.quarkus.runtime.Startup;
import lombok.extern.slf4j.Slf4j;
import org.infinispan.Cache;
import org.infinispan.configuration.cache.CacheMode;
import org.infinispan.configuration.cache.Configuration;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.manager.EmbeddedCacheManager;
import org.infinispan.remoting.transport.Address;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Collection;
import java.util.List;

@ApplicationScoped
@Startup
@Slf4j
public class ExempleObjectCacheService {

    public static final String EXEMPLE_CACHE = "exemple_cache";

    @Inject
    EmbeddedCacheManager cacheManager;

    private Cache<Integer, ExempleObject> cache;

    @PostConstruct
    public void init() {
        log.info("Initialisation cache exemple...");
        log.info("ECM = {}", cacheManager);
        log.info("cluster name = {}", cacheManager.getCacheManagerInfo().getClusterName());
        Configuration configuration = new ConfigurationBuilder()
                .clustering()
                .cacheMode(CacheMode.REPL_SYNC)
                .build();
        cache = cacheManager.createCache(EXEMPLE_CACHE, configuration);
    }

    public ExempleObject put(ExempleObject exempleObject){
        return cache.put(exempleObject.hashCode(), exempleObject);
    }

    public Collection<ExempleObject> get() {
        return cache.values();
    }

    public ExempleObject get(Integer key){
        return cache.get(key);
    }

    public ExempleObject findByNom(String nom) {
        return cache.values().stream()
                .filter(exempleObject -> nom.equals(exempleObject.getNom()))
                .findFirst()
                .orElseThrow();
    }

    public void delete(Integer key) {
        cache.remove(key);
    }

    public ExempleObject update(Integer key, ExempleObject exempleObject) {
        return cache.replace(key, exempleObject);
    }

    public List<Address> status() {
        return cacheManager.getMembers();
    }

}
