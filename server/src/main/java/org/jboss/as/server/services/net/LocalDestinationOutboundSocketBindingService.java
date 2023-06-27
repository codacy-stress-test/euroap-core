/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.jboss.as.server.services.net;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.jboss.as.network.NetworkInterfaceBinding;
import org.jboss.as.network.OutboundSocketBinding;
import org.jboss.as.network.SocketBinding;
import org.jboss.as.network.SocketBindingManager;

/**
 * Service that represents a local-destination outbound socket binding
 *
 * @author Jaikiran Pai
 * @author <a href="mailto:ropalka@redhat.com">Richard Opalka</a>
 */
public class LocalDestinationOutboundSocketBindingService extends OutboundSocketBindingService {

    private final Supplier<SocketBinding> localDestinationSocketBindingSupplier;

    public LocalDestinationOutboundSocketBindingService(final Consumer<OutboundSocketBinding> outboundSocketBindingConsumer,
                                                        final Supplier<SocketBindingManager> socketBindingManagerSupplier,
                                                        final Supplier<NetworkInterfaceBinding> sourceInterfaceSupplier,
                                                        final Supplier<SocketBinding> localDestinationSocketBindingSupplier,
                                                        final String name, final Integer sourcePort, final boolean fixedSourcePort) {
        super(outboundSocketBindingConsumer, socketBindingManagerSupplier, sourceInterfaceSupplier, name, sourcePort, fixedSourcePort);
        this.localDestinationSocketBindingSupplier = localDestinationSocketBindingSupplier;
    }

    @Override
    protected OutboundSocketBinding createOutboundSocketBinding() {
        // unlike the RemoteDestinationOutboundSocketBindingService, we resolve the destination address
        // here itself (instead of doing it lazily in the OutboundSocketBinding) is because we already
        // inject a SocketBinding reference which is local to the server instance and is expected to have
        // already resolved the (local) address (via the NetworkInterfaceBinding).
        final InetAddress destinationAddress = this.getDestinationAddress();
        final int destinationPort = this.getDestinationPort();
        return new OutboundSocketBinding(outboundSocketName, socketBindingManagerSupplier.get(),
                destinationAddress, destinationPort, sourceInterfaceSupplier != null ? sourceInterfaceSupplier.get() : null,
                sourcePort, fixedSourcePort);
    }

    private InetAddress getDestinationAddress() {
        final SocketBinding localDestinationSocketBinding = localDestinationSocketBindingSupplier.get();
        return localDestinationSocketBinding.getSocketAddress().getAddress();
    }

    private int getDestinationPort() {
        final SocketBinding localDestinationSocketBinding = localDestinationSocketBindingSupplier.get();
        // instead of directly using SocketBinding.getPort(), we go via the SocketBinding.getSocketAddress()
        // since the getPort() method doesn't take into account whether the port is a fixed port or whether an offset
        // needs to be added. Alternatively, we could introduce a getAbsolutePort() in the SocketBinding class.
        final InetSocketAddress socketAddress = localDestinationSocketBinding.getSocketAddress();
        return socketAddress.getPort();
    }

}
