/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2014 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */

package org.sonatype.nexus.plugins.ruby;

import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.sonatype.nexus.events.EventSubscriber;
import org.sonatype.nexus.proxy.events.NexusStoppedEvent;
import org.sonatype.nexus.ruby.DefaultRubygemsGateway;
import org.sonatype.sisu.goodies.common.ComponentSupport;

import com.google.common.eventbus.Subscribe;
import org.jruby.embed.ScriptingContainer;

/**
 * A provider for JRuby scripting container that creates it lazily (ie. if NX instance does not have rubygems
 * repository, the container should not be created either. Also, this provider is an event subscriber, that will
 * terminate the container to have it properly clean up the resources.
 *
 * @since 2.11
 */
@Singleton
@Named
public class ScriptingContainerProvider
    extends ComponentSupport
    implements Provider<ScriptingContainer>, EventSubscriber
{
  private ScriptingContainer scriptingContainer;

  @Override
  public synchronized ScriptingContainer get() {
    if (scriptingContainer == null) {
      log.debug("Creating JRuby ScriptingContainer");
      scriptingContainer = new ScriptingContainer();
      scriptingContainer.setClassLoader(DefaultRubygemsGateway.class.getClassLoader());
    }
    return scriptingContainer;
  }

  @Subscribe
  public void on(final NexusStoppedEvent event) {
    if (scriptingContainer != null) {
      log.debug("Terminating JRuby ScriptingContainer");
      scriptingContainer.terminate();
    }
  }
}