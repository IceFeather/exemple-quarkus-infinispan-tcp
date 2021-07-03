package fr.icefeather.exemple.quarkus.cache;

import fr.icefeather.exemple.quarkus.domain.ExempleObject;
import org.infinispan.protostream.SerializationContextInitializer;
import org.infinispan.protostream.annotations.AutoProtoSchemaBuilder;

@AutoProtoSchemaBuilder(
        includeClasses = {ExempleObject.class},
        schemaPackageName = "fr.icefeather.exemple.quarkus.cache"
)
public interface ExempleObjectContextInitializer extends SerializationContextInitializer {
}
