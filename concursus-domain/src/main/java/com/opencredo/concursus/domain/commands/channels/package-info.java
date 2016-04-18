/**
 * Command Channels are how {@link com.opencredo.concursus.domain.commands.Command}s enter and leave the system.
 * <p>
 *   They provide an integration point for adapters to message queues, ESBs etc: to integrate a 3rd-party transport for
 *   commands, create suitable {@link com.opencredo.concursus.domain.commands.channels.CommandInChannel} and
 *   {@link com.opencredo.concursus.domain.commands.channels.CommandOutChannel} implementations for it.
 * </p>
 * <p>
 *   Commands received through a {@link com.opencredo.concursus.domain.commands.channels.CommandInChannel} will
 *   typically be passed on to a {@link com.opencredo.concursus.domain.commands.dispatching.CommandBus} for execution.
 *   Where a client would normally use a {@link com.opencredo.concursus.domain.commands.dispatching.CommandBus} to
 *   dispatch commands, a {@link com.opencredo.concursus.domain.commands.channels.CommandOutChannel} can be used instead
 *   to send commands out through the 3rd-party transport.
 * </p>
 */
package com.opencredo.concursus.domain.commands.channels;