package fr.icefeather.exemple.quarkus;

import fr.icefeather.exemple.quarkus.cache.ExempleObjectCacheService;
import fr.icefeather.exemple.quarkus.domain.ExempleObject;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.Collection;

@Path("/exemple")
public class App {

    @Inject
    ExempleObjectCacheService exempleObjectcache;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Collection<ExempleObject> get() {
        return exempleObjectcache.get();
    }

    @Path("/{key}")
    @Produces(MediaType.APPLICATION_JSON)
    public ExempleObject getWithKey(@PathParam("key") Integer key) {
        return exempleObjectcache.get(key);
    }

    @Path("/nom/{nom}")
    @Produces(MediaType.APPLICATION_JSON)
    public ExempleObject getByNom(@PathParam("nom") String nom) {
        return exempleObjectcache.findByNom(nom);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ExempleObject create(ExempleObject exempleObject) {
        return exempleObjectcache.put(exempleObject);
    }

    @Path("/{key}")
    @PATCH
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ExempleObject replace(@PathParam("key") Integer key, ExempleObject exempleObject) {
        return exempleObjectcache.update(key, exempleObject);
    }

    @Path("/{key}")
    @DELETE
    @Produces(MediaType.TEXT_PLAIN)
    public String delete(@PathParam("key") Integer key) {
        exempleObjectcache.delete(key);
        return "exempleObject in cache with key : " + key + " deleted";
    }

}