/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.as.logging;

import static org.jboss.as.controller.parsing.ParseUtils.duplicateNamedElement;
import static org.jboss.as.controller.parsing.ParseUtils.missingRequired;
import static org.jboss.as.controller.parsing.ParseUtils.readStringAttributeElement;
import static org.jboss.as.controller.parsing.ParseUtils.requireNoContent;
import static org.jboss.as.controller.parsing.ParseUtils.requireNoNamespaceAttribute;
import static org.jboss.as.controller.parsing.ParseUtils.requireSingleAttribute;
import static org.jboss.as.controller.parsing.ParseUtils.unexpectedAttribute;
import static org.jboss.as.controller.parsing.ParseUtils.unexpectedElement;
import static org.jboss.as.logging.CommonAttributes.LOGGING_PROFILE;
import static org.jboss.as.logging.handlers.AbstractHandlerDefinition.FORMATTER;
import static org.jboss.as.logging.handlers.AbstractHandlerDefinition.NAMED_FORMATTER;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.xml.stream.XMLStreamException;

import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.operations.common.Util;
import org.jboss.as.controller.parsing.ParseUtils;
import org.jboss.as.logging.formatters.CustomFormatterResourceDefinition;
import org.jboss.as.logging.formatters.PatternFormatterResourceDefinition;
import org.jboss.dmr.ModelNode;
import org.jboss.staxmapper.XMLExtendedStreamReader;

/**
 * @author Emanuel Muckenhuber
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
class LoggingSubsystemParser_1_4 extends LoggingSubsystemParser_1_3 {

    LoggingSubsystemParser_1_4() {
        //
    }

    @Override
    public void readElement(final XMLExtendedStreamReader reader, final List<ModelNode> operations) throws XMLStreamException {
        // No attributes
        ParseUtils.requireNoAttributes(reader);

        // Subsystem add operation
        final ModelNode subsystemAddOp = Util.createAddOperation(SUBSYSTEM_ADDRESS);
        operations.add(subsystemAddOp);

        final List<ModelNode> loggerOperations = new ArrayList<>();
        final List<ModelNode> asyncHandlerOperations = new ArrayList<>();
        final List<ModelNode> handlerOperations = new ArrayList<>();
        final List<ModelNode> formatterOperations = new ArrayList<>();

        // Elements
        final Set<String> loggerNames = new HashSet<>();
        final Set<String> handlerNames = new HashSet<>();
        final Set<String> formatterNames = new HashSet<>();
        boolean rootDefined = false;
        while (reader.hasNext() && reader.nextTag() != END_ELEMENT) {
            final Element element = Element.forName(reader.getLocalName());
            switch (element) {
                case ADD_LOGGING_API_DEPENDENCIES: {
                    final String value = ParseUtils.readStringAttributeElement(reader, Attribute.VALUE.getLocalName());
                    LoggingResourceDefinition.ADD_LOGGING_API_DEPENDENCIES.parseAndSetParameter(value, subsystemAddOp, reader);
                    break;
                }
                case LOGGER: {
                    parseLoggerElement(reader, SUBSYSTEM_ADDRESS, loggerOperations, loggerNames);
                    break;
                }
                case ROOT_LOGGER: {
                    if (rootDefined) {
                        throw unexpectedElement(reader);
                    }
                    rootDefined = true;
                    parseRootLoggerElement(reader, SUBSYSTEM_ADDRESS, loggerOperations);
                    break;
                }
                case CONSOLE_HANDLER: {
                    parseConsoleHandlerElement(reader, SUBSYSTEM_ADDRESS, handlerOperations, handlerNames);
                    break;
                }
                case FILE_HANDLER: {
                    parseFileHandlerElement(reader, SUBSYSTEM_ADDRESS, handlerOperations, handlerNames);
                    break;
                }
                case CUSTOM_HANDLER: {
                    parseCustomHandlerElement(reader, SUBSYSTEM_ADDRESS, handlerOperations, handlerNames);
                    break;
                }
                case PERIODIC_ROTATING_FILE_HANDLER: {
                    parsePeriodicRotatingFileHandlerElement(reader, SUBSYSTEM_ADDRESS, handlerOperations, handlerNames);
                    break;
                }
                case SIZE_ROTATING_FILE_HANDLER: {
                    parseSizeRotatingHandlerElement(reader, SUBSYSTEM_ADDRESS, handlerOperations, handlerNames);
                    break;
                }
                case ASYNC_HANDLER: {
                    parseAsyncHandlerElement(reader, SUBSYSTEM_ADDRESS, asyncHandlerOperations, handlerNames);
                    break;
                }
                case SYSLOG_HANDLER: {
                    parseSyslogHandler(reader, SUBSYSTEM_ADDRESS, handlerOperations, handlerNames);
                    break;
                }
                case LOGGING_PROFILES: {
                    parseLoggingProfilesElement(reader, operations);
                    break;
                }
                case FORMATTER: {
                    parseFormatter(reader, SUBSYSTEM_ADDRESS, formatterOperations, formatterNames);
                    break;
                }
                default: {
                    reader.handleAny(operations);
                    break;
                }
            }
        }
        operations.addAll(formatterOperations);
        operations.addAll(handlerOperations);
        operations.addAll(asyncHandlerOperations);
        operations.addAll(loggerOperations);
    }

    void parseFormatter(final XMLExtendedStreamReader reader, final PathAddress address, final List<ModelNode> operations, final Set<String> names) throws XMLStreamException {
        // Attributes
        String name = null;
        final EnumSet<Attribute> required = EnumSet.of(Attribute.NAME);
        final int count = reader.getAttributeCount();
        for (int i = 0; i < count; i++) {
            requireNoNamespaceAttribute(reader, i);
            final String value = reader.getAttributeValue(i);
            final Attribute attribute = Attribute.forName(reader.getAttributeLocalName(i));
            required.remove(attribute);
            switch (attribute) {
                case NAME: {
                    name = value;
                    break;
                }
                default:
                    throw unexpectedAttribute(reader, i);
            }
        }
        if (!required.isEmpty()) {
            throw missingRequired(reader, required);
        }
        if (!names.add(name)) {
            throw duplicateNamedElement(reader, name);
        }

        final EnumSet<Element> encountered = EnumSet.noneOf(Element.class);
        while (reader.nextTag() != END_ELEMENT) {
            final Element element = Element.forName(reader.getLocalName());
            if (!encountered.add(element)) {
                throw unexpectedElement(reader);
            }
            switch (element) {
                case PATTERN_FORMATTER: {
                    final ModelNode operation = Util.createAddOperation();
                    // Setup the operation address
                    addOperationAddress(operation, address, PatternFormatterResourceDefinition.NAME, name);
                    parsePatternFormatterElement(reader, operation);
                    operations.add(operation);
                    break;
                }
                case CUSTOM_FORMATTER: {
                    final ModelNode operation = Util.createAddOperation();
                    // Setup the operation address
                    addOperationAddress(operation, address, CustomFormatterResourceDefinition.NAME, name);
                    parseCustomFormatterElement(reader, operation);
                    operations.add(operation);
                    break;
                }
                default: {
                    throw unexpectedElement(reader);
                }
            }
        }
    }

    @Override
    void parseHandlerFormatterElement(final XMLExtendedStreamReader reader, final ModelNode operation) throws XMLStreamException {
        if (reader.getAttributeCount() > 0) {
            throw unexpectedAttribute(reader, 0);
        }
        boolean formatterDefined = false;
        while (reader.nextTag() != END_ELEMENT) {
            final Element element = Element.forName(reader.getLocalName());
            switch (element) {
                case PATTERN_FORMATTER: {
                    if (formatterDefined) {
                        throw unexpectedElement(reader);
                    }
                    requireSingleAttribute(reader, PatternFormatterResourceDefinition.PATTERN.getName());
                    formatterDefined = true;
                    FORMATTER.parseAndSetParameter(readStringAttributeElement(reader, PatternFormatterResourceDefinition.PATTERN.getName()), operation, reader);
                    break;
                }
                case NAMED_FORMATTER: {
                    if (formatterDefined) {
                        throw unexpectedElement(reader);
                    }
                    formatterDefined = true;
                    NAMED_FORMATTER.parseAndSetParameter(readNameAttribute(reader), operation, reader);
                    break;
                }
                default: {
                    throw unexpectedElement(reader);
                }
            }
        }
    }

    void parsePatternFormatterElement(final XMLExtendedStreamReader reader, final ModelNode operation) throws XMLStreamException {
        final EnumSet<Attribute> required = EnumSet.of(Attribute.PATTERN);
        final int count = reader.getAttributeCount();
        for (int i = 0; i < count; i++) {
            requireNoNamespaceAttribute(reader, i);
            final String value = reader.getAttributeValue(i);
            final Attribute attribute = Attribute.forName(reader.getAttributeLocalName(i));
            required.remove(attribute);
            switch (attribute) {
                case PATTERN: {
                    PatternFormatterResourceDefinition.PATTERN.parseAndSetParameter(value, operation, reader);
                    break;
                }
                case COLOR_MAP: {
                    PatternFormatterResourceDefinition.COLOR_MAP.parseAndSetParameter(value, operation, reader);
                    break;
                }
                default:
                    throw unexpectedAttribute(reader, i);
            }
        }
        if (!required.isEmpty()) {
            throw missingRequired(reader, required);
        }
        requireNoContent(reader);
    }

    void parseCustomFormatterElement(final XMLExtendedStreamReader reader, final ModelNode operation) throws XMLStreamException {
        final EnumSet<Attribute> required = EnumSet.of(Attribute.CLASS, Attribute.MODULE);
        final int count = reader.getAttributeCount();
        for (int i = 0; i < count; i++) {
            requireNoNamespaceAttribute(reader, i);
            final String value = reader.getAttributeValue(i);
            final Attribute attribute = Attribute.forName(reader.getAttributeLocalName(i));
            required.remove(attribute);
            switch (attribute) {
                case CLASS: {
                    CommonAttributes.CLASS.parseAndSetParameter(value, operation, reader);
                    break;
                }
                case MODULE: {
                    CommonAttributes.MODULE.parseAndSetParameter(value, operation, reader);
                    break;
                }
                default:
                    throw unexpectedAttribute(reader, i);
            }
        }
        if (!required.isEmpty()) {
            throw missingRequired(reader, required);
        }


        final EnumSet<Element> encountered = EnumSet.noneOf(Element.class);
        while (reader.nextTag() != END_ELEMENT) {
            final Element element = Element.forName(reader.getLocalName());
            if (!encountered.add(element)) {
                throw unexpectedElement(reader);
            }
            switch (element) {
                case PROPERTIES: {
                    parsePropertyElement(operation, reader);
                    break;
                }
                default: {
                    throw unexpectedElement(reader);
                }
            }
        }
    }

    @Override
    void parseLoggingProfileElement(final XMLExtendedStreamReader reader, final List<ModelNode> operations, final Set<String> profileNames) throws XMLStreamException {
        // Attributes
        String name = null;
        final EnumSet<Attribute> required = EnumSet.of(Attribute.NAME);
        final int count = reader.getAttributeCount();
        for (int i = 0; i < count; i++) {
            requireNoNamespaceAttribute(reader, i);
            final String value = reader.getAttributeValue(i);
            final Attribute attribute = Attribute.forName(reader.getAttributeLocalName(i));
            required.remove(attribute);
            switch (attribute) {
                case NAME: {
                    name = value;
                    break;
                }
                default:
                    throw unexpectedAttribute(reader, i);
            }
        }
        if (!required.isEmpty()) {
            throw missingRequired(reader, required);
        }
        if (!profileNames.add(name)) {
            throw duplicateNamedElement(reader, name);
        }
        // Setup the address
        final PathAddress profileAddress = SUBSYSTEM_ADDRESS.append(LOGGING_PROFILE, name);
        operations.add(Util.createAddOperation(profileAddress));

        final List<ModelNode> loggerOperations = new ArrayList<>();
        final List<ModelNode> asyncHandlerOperations = new ArrayList<>();
        final List<ModelNode> handlerOperations = new ArrayList<>();
        final List<ModelNode> formatterOperations = new ArrayList<>();

        final Set<String> loggerNames = new HashSet<>();
        final Set<String> handlerNames = new HashSet<>();
        final Set<String> formatterNames = new HashSet<>();
        boolean gotRoot = false;
        while (reader.hasNext() && reader.nextTag() != END_ELEMENT) {
            final Element element = Element.forName(reader.getLocalName());
            switch (element) {
                case LOGGER: {
                    parseLoggerElement(reader, profileAddress, loggerOperations, loggerNames);
                    break;
                }
                case ROOT_LOGGER: {
                    if (gotRoot) {
                        throw unexpectedElement(reader);
                    }
                    gotRoot = true;
                    parseRootLoggerElement(reader, profileAddress, loggerOperations);
                    break;
                }
                case CONSOLE_HANDLER: {
                    parseConsoleHandlerElement(reader, profileAddress, handlerOperations, handlerNames);
                    break;
                }
                case FILE_HANDLER: {
                    parseFileHandlerElement(reader, profileAddress, handlerOperations, handlerNames);
                    break;
                }
                case CUSTOM_HANDLER: {
                    parseCustomHandlerElement(reader, profileAddress, handlerOperations, handlerNames);
                    break;
                }
                case PERIODIC_ROTATING_FILE_HANDLER: {
                    parsePeriodicRotatingFileHandlerElement(reader, profileAddress, handlerOperations, handlerNames);
                    break;
                }
                case SIZE_ROTATING_FILE_HANDLER: {
                    parseSizeRotatingHandlerElement(reader, profileAddress, handlerOperations, handlerNames);
                    break;
                }
                case ASYNC_HANDLER: {
                    parseAsyncHandlerElement(reader, profileAddress, asyncHandlerOperations, handlerNames);
                    break;
                }
                case SYSLOG_HANDLER: {
                    parseSyslogHandler(reader, profileAddress, handlerOperations, handlerNames);
                    break;
                }
                case FORMATTER: {
                    parseFormatter(reader, profileAddress, formatterOperations, formatterNames);
                    break;
                }
                default: {
                    reader.handleAny(operations);
                    break;
                }
            }
        }
        operations.addAll(formatterOperations);
        operations.addAll(handlerOperations);
        operations.addAll(asyncHandlerOperations);
        operations.addAll(loggerOperations);
    }
}
