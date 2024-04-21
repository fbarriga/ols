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

import org.osgi.framework.*;
import org.osgi.service.log.*;

/**
 * An implementation of the OSGi LogService that directly outputs each log
 * message to <code>System.out</code>. It does not implement the LogReader or
 * LogListeners.
 */
public class ConsoleLogger implements LogService
{
  public void log( final int level, final String message )
  {
    log( null, level, message, null );
  }

  public void log( final int level, final String message, final Throwable throwable )
  {
    log( null, level, message, throwable );
  }

  public void log( final ServiceReference reference, final int level, final String message )
  {
    log( reference, level, message, null );
  }

  public void log( final ServiceReference reference, final int level, final String message, final Throwable throwable )
  {
    String bundle = " ";
    if ( reference != null )
    {
      String service;
      bundle = "00" + reference.getBundle().getBundleId();
      bundle = " [" + bundle.substring( bundle.length() - 3 ) + "]";

      Object objectClass = reference.getProperty( Constants.OBJECTCLASS );
      if ( objectClass instanceof String[] objClassArr )
      {
        var serviceStrBuilder = new StringBuilder( " " );
        var buffer = new StringBuilder();
        for ( String svc : objClassArr )
        {
          if (!buffer.isEmpty())
          {
            buffer.append( ';' );
          }
          buffer.append( svc );
          serviceStrBuilder.append( buffer ).append( ": " );
        }
        service = serviceStrBuilder.toString();
      }
      else
      {
        service = objectClass.toString() + ": ";
      }
      bundle += service;
    }

    var levelName = switch(level) {
      case 1000 -> "ERROR";                    // java.util.logging.Level.SEVERE
      case 900 -> "WARN";                     // java.util.logging.Level.WARNING
      case 800 -> "INFO";                     // java.util.logging.Level.INFO
      case 700 -> "CONFIG";                   // java.util.logging.Level.CONFIG
      case 500 -> "FINE";                     // java.util.logging.Level.FINE
      case 400 -> "FINER";                    // java.util.logging.Level.FINER
      case 300 -> "FINEST";                   // java.util.logging.Level.FINEST
      case Integer.MIN_VALUE -> "ALL";        // java.util.logging.Level.ALL

      case LogService.LOG_ERROR -> "ERROR";   // org.osgi.service.log.LogService.LOG_ERROR
      case LogService.LOG_WARNING -> "WARN";  // org.osgi.service.log.LogService.LOG_WARNING
      case LogService.LOG_INFO -> "INFO";     // org.osgi.service.log.LogService.LOG_INFO
      case LogService.LOG_DEBUG -> "DEBUG";   // org.osgi.service.log.LogService.LOG_DEBUG
      default -> "UNKNOWN " + level;
    };

    String msg = levelName + bundle + message;
    System.out.println( msg );

    if ( throwable != null )
    {
      throwable.printStackTrace( System.out );
    }
  }
}
