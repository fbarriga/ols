/*
 * OpenBench LogicSniffer / SUMP project
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin St, Fifth Floor, Boston, MA 02110, USA
 *
 *
 * Copyright (C) 2010-2011 - J.W. Janssen, http://www.lxtreme.nl
 * Copyright (C) 2024 - Felipe Barriga Richards, http://github.com/fbarriga/ols
 */
package nl.lxtreme.ols.logging;


import org.apache.felix.dm.*;
import org.osgi.framework.*;
import org.osgi.service.log.*;


/**
 * Bundle activator.
 */
public class Activator extends DependencyActivatorBase
{
  // CONSTANTS

  private static final String PROPERTY_LOG_TO_CONSOLE = "nl.lxtreme.ols.logToConsole";
  private static final String PROPERTY_FILTER_JDKUI_LOGS = "nl.lxtreme.ols.filterJdkUiLogs";

  // METHODS

  /**
   * Returns whether or not we should filter out the UI-logs from the JDK.
   *
   * @return <code>true</code> if UI-logs should be filtered, <code>false</code>
   *         otherwise.
   */
  public static boolean isFilterJdkUILogs()
  {
    return Boolean.parseBoolean( System.getProperty( PROPERTY_FILTER_JDKUI_LOGS, "true" ) );
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void destroy( BundleContext aContext, DependencyManager aManager ) {
    // Nop
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void init( BundleContext aContext, DependencyManager aManager )
  {
    aManager.add( createComponent() //
        .setInterface( LogService.class.getName(), null ) //
        .setImplementation( ConsoleLogger.class ) //
        );

    aManager.add( createComponent()
      .setImplementation( LogHandler.class ));
  }
}
