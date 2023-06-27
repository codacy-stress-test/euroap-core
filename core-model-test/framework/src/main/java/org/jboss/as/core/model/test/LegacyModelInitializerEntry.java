/*
* JBoss, Home of Professional Open Source.
* Copyright 2012, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.as.core.model.test;

import static org.wildfly.common.Assert.checkNotNullParam;

import java.io.Serializable;

import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.PathElement;
import org.jboss.dmr.ModelNode;

/**
 *
 * @author <a href="kabir.khan@jboss.com">Kabir Khan</a>
 */
public class LegacyModelInitializerEntry implements Serializable {

    private static final long serialVersionUID = 1L;

    private final PathAddress parentAddress;
    private final PathElement relativeResourceAddress;
    private final ModelNode model;
    private final String[] capabilities;

    public LegacyModelInitializerEntry(PathAddress parentAddress, PathElement relativeResourceAddress, ModelNode model, String... capabilities) {
        this.parentAddress = parentAddress;
        this.relativeResourceAddress = checkNotNullParam("relativeResourceAddress", relativeResourceAddress);
        this.model = model;
        this.capabilities = capabilities == null || capabilities.length == 0 ? null : capabilities;
    }

    public PathAddress getParentAddress() {
        return parentAddress;
    }

    public PathElement getRelativeResourceAddress() {
        return relativeResourceAddress;
    }

    public ModelNode getModel() {
        return model;
    }

    public String[] getCapabilities() {
        return capabilities;
    }

}
