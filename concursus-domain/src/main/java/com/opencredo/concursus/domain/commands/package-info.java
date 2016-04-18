/**
 * Defines a {@link com.opencredo.concursus.domain.commands.Command} and the things that can be done with it. A
 * {@link com.opencredo.concursus.domain.commands.Command} is typically dispatched via a
 * {@link com.opencredo.concursus.domain.commands.dispatching.CommandBus} to a
 * {@link com.opencredo.concursus.domain.commands.dispatching.CommandExecutor}, which determines where and how it will
 * be processed by a {@link com.opencredo.concursus.domain.commands.dispatching.CommandProcessor}.
 */
package com.opencredo.concursus.domain.commands;