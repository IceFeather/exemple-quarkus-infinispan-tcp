package fr.icefeather.exemple.quarkus;

import fr.icefeather.exemple.quarkus.cache.ExempleObjectContextInitializerImpl;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.infinispan.configuration.global.GlobalConfiguration;
import org.infinispan.configuration.global.GlobalConfigurationBuilder;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.manager.EmbeddedCacheManager;
import org.infinispan.remoting.transport.jgroups.JGroupsTransport;
import org.jgroups.JChannel;
import org.jgroups.protocols.*;
import org.jgroups.protocols.pbcast.GMS;
import org.jgroups.protocols.pbcast.NAKACK2;
import org.jgroups.protocols.pbcast.STABLE;
import org.jgroups.stack.Protocol;

import javax.enterprise.context.ApplicationScoped;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class InfinispanConfig {

    @ConfigProperty(name = "infinispan.adresse")
    public String adresse;

    @ConfigProperty(name = "infinispan.port")
    public Integer port;

    @ConfigProperty(name = "infinispan.members")
    public List<String> members;

    @ConfigProperty(name = "infinispan.machine")
    public String machine;

    /**
     * Creation du cache manager Infinispan
     *
     * @return {@link EmbeddedCacheManager}
     * @throws Exception
     */
    @ApplicationScoped
    EmbeddedCacheManager cacheManager() throws Exception {
        log.info("Début configuration infinispan avec\n" +
                "adresse : {}\n" +
                "port: {}\n" +
                "members: {}\n" +
                "machine: {}",
                adresse,
                port,
                members,
                machine
        );

        // On transforme la propriété issue de la configuration infinispan.adresse en InetAddress pour la conf TCP
        // We tranform the property from the infinispan.adresse configuration in InetAddress for the use in TCP
        InetAddress bindAddr = InetAddress.getByName(adresse);

        // On transforme la liste issue de la configuration infinispan.members en list d'inetSocketAddress pour la conf TCPPING
        // We transform the list from the infinispan.members configuration property in list of InetSocketAddress for the use in TCPPING
        List<InetSocketAddress> infinispanMembers = members.stream()
                .map(h -> {
                    String host = h.substring(0, h.indexOf("["));
                    int port = Integer.parseInt(h.substring(h.indexOf("[") + 1, h.indexOf("]")));
                    return new InetSocketAddress(host, port);
                }).collect(Collectors.toList())
        ;

        // On déclare une nouvelle configuration globale
        // We declare a new infinispan global configuration
        GlobalConfigurationBuilder globalConfigurationBuilder = new GlobalConfigurationBuilder();

        // On déclare notre stack de protocoles
        // We declare our protocols stack
        Protocol[] protocols = {
                new TCP_NIO2()
                        .setBindAddress(bindAddr)
                        .setBindPort(port)
                        .setPortRange(0)
                        .setValue("external_addr", bindAddr)
                        .setValue("external_port", port),
                new TCPPING()
                        .setInitialHosts(infinispanMembers),
                new MERGE3(),
                new FD_SOCK(),
                new FD_ALL(),
                new VERIFY_SUSPECT(),
                new NAKACK2(),
                new UNICAST3(),
                new STABLE(),
                new GMS(),
                new UFC(),
                new MFC(),
                new FRAG3()
        };

        // On crée la configuration JChannel avec notre stack
        // We create the JChannel with our stack
        JChannel jChannel = new JChannel(protocols).name("EXEMPLE");

        /// On crée la configuration JGroups avec notre configuration JChannel
        // We create the JGroups with our JChannel configuration
        JGroupsTransport transport = new JGroupsTransport(jChannel);

        // On défini la configuration du cache clusterisé
        // We define the cluster cache configuration
        globalConfigurationBuilder.cacheContainer()
                .name("exemple-cache")
                .transport()
                    .clusterName("exemple-cluster")
                    .machineId(machine)
                    .transport(transport)
                .serialization()
                    // ExempleObjectContextInitializerImpl exists after mvn install of project
                    .addContextInitializer(new ExempleObjectContextInitializerImpl());


        // On build la configuration
        // We build the infinispan configuration
        GlobalConfiguration globalConfiguration = globalConfigurationBuilder.build();

        log.info("Démarrage cache manager");

        // On retourne le CacheManager issu de la configuration globale créée dans un nouveau bean EmbeddedCacheManager
        // We return the CacheManager build from the global configuration in a new bean EmbeddedCacheManager
        return new DefaultCacheManager(globalConfiguration);

    }

}
