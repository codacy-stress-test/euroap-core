/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.extension.discovery;

import static org.jboss.as.controller.PersistentResourceXMLDescription.builder;

import org.jboss.as.controller.PersistentResourceXMLDescription;
import org.jboss.as.controller.PersistentSubsystemSchema;
import org.jboss.as.controller.SubsystemSchema;
import org.jboss.as.controller.xml.VersionedNamespace;
import org.jboss.staxmapper.IntVersion;

/**
 * Enumeration of discovery subsystem schema versions.
 * @author Paul Ferraro
 */
enum DiscoverySubsystemSchema implements PersistentSubsystemSchema<DiscoverySubsystemSchema> {
    VERSION_1_0(1, 0),
    ;
    static final DiscoverySubsystemSchema CURRENT = VERSION_1_0;

    private final VersionedNamespace<IntVersion, DiscoverySubsystemSchema> namespace;

    DiscoverySubsystemSchema(int major, int minor) {
        this.namespace = SubsystemSchema.createLegacySubsystemURN(DiscoverySubsystemRegistrar.NAME, new IntVersion(major, minor));
    }

    @Override
    public VersionedNamespace<IntVersion, DiscoverySubsystemSchema> getNamespace() {
        return this.namespace;
    }

    @Override
    public PersistentResourceXMLDescription getXMLDescription() {
        return builder(DiscoverySubsystemRegistrar.PATH, this.getNamespace())
                .addChild(builder(StaticDiscoveryProviderRegistrar.PATH).addAttributes(StaticDiscoveryProviderRegistrar.ATTRIBUTES.stream()))
                .addChild(builder(AggregateDiscoveryProviderRegistrar.PATH).addAttributes(AggregateDiscoveryProviderRegistrar.ATTRIBUTES.stream()))
                .build();
    }
}
