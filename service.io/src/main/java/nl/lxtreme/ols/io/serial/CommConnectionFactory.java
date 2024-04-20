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
package nl.lxtreme.ols.io.serial;


import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.microedition.io.*;

import com.fazecast.jSerialComm.SerialPort;
import org.osgi.service.io.ConnectionFactory;


/**
 * Provides a connection factory for serial devices.
 */
public class CommConnectionFactory implements ConnectionFactory
{
  // CONSTANTS

  /**
   * The scheme we're exposing through this factory. Serial URIs should be
   * written in the form of:
   * <tt>comm:COM2;baudrate=9600;bitsperchar=8;parity=none;stopbits=1</tt>.
   */
  public static final String SCHEME = "comm";

  /**
   * Number of tries before bailing out on establishing a connection to the
   * serial port...
   */
  private static final int MAX_TRIES = 10;
  /** Name to use when connecting to the port... */

  // VARIABLES

  private static final Logger LOG = Logger.getLogger( CommConnectionFactory.class.getName() );

  // METHODS

  /**
   * {@inheritDoc}
   */
  @Override
  public Connection createConnection( final String aName, final int aMode, final boolean aTimeouts ) throws IOException
  {
    try
    {
      LOG.fine( "Creating connection using %s".formatted( aName ));

      final CommPortOptions options = new CommPortOptions( aName );

      final var port = obtainSerialPort( options );
      port.setBaudRate(options.getBaudrate());
      port.setNumDataBits(options.getDatabits());
      port.setNumStopBits(options.getStopbits());
      port.setParity(options.getParityMode());

      port.setFlowControl(options.getFlowControl());
      if ( aTimeouts )
      {
        // A receive timeout allows us to better control blocking I/O, such as
        // read() from the serial port...
        port.setComPortTimeouts(SerialPort.TIMEOUT_READ_BLOCKING, options.getReceiveTimeout(), port.getWriteTimeout());
      }

      if (options.isDTR()) {
        port.setDTR();
      } else {
        port.clearDTR();
      }

      final CommConnectionImpl connection = new CommConnectionImpl( port );

      // Some devices need some time to initialize after being opened for the
      // first time, see issue #34.
      final int openDelay = options.getOpenDelay();
      if ( openDelay > 0 )
      {
        Thread.sleep( openDelay );
      }

      return connection;
    }
    catch ( IllegalArgumentException exception )
    {
      throw new IOException( "Invalid URI!", exception );
    }
    catch ( InterruptedException exception )
    {
      throw new IOException( "Interrupted while opening port!" );
    }
  }

  /**
   * Returns the serial port instance.
   *
   * @param aOptions
   *          the serial port options, cannot be <code>null</code>.
   * @return the serial port instance, never <code>null</code>.
   * @throws IOException
   *           in case of other I/O problems.
   */
  private SerialPort getSerialPort(final CommPortOptions aOptions ) throws IOException
  {
    var port = SerialPort.getCommPort(aOptions.getPortName());
    if (!port.openPort(100)) {
      throw new IOException( "Cannot open serial port!" );
    }

    return port;
  }

  /**
   * Performs a best effort in trying to get a serial port instance by calling
   * {@link #getSerialPort(CommPortOptions)} a number of times before bailing
   * out.
   * <p>
   * Idea taken from: <a href=
   * "http://mailman.qbang.org/pipermail/rxtx/2010-September/7821768.html">this
   * posting</a> on the RxTx mailing list.
   * </p>
   *
   * @param aOptions
   *          the serial port options, cannot be <code>null</code>.
   * @return the serial port instance, never <code>null</code>.
   * @throws IOException
   *           in case of other I/O problems.
   */
  private SerialPort obtainSerialPort(final CommPortOptions aOptions ) throws IOException
  {
    int tries = 1;
    SerialPort port = null;

    do
    {
      LOG.fine( "obtaining serial port. try %d/%d".formatted( tries, MAX_TRIES ) );
      try
      {
        port = getSerialPort( aOptions );
        LOG.fine( "Got serial port after %d tries".formatted( tries ) );
      }
      catch ( IOException exception )
      {
        LOG.log(Level.WARNING, "Error opening port!", exception );
        if (tries == MAX_TRIES) {
          throw exception;
        }
      }
    } while( ( tries++ <= MAX_TRIES ) && ( port == null ) );

    if ( port == null )
    {
      throw new IOException("Cannot open serial port!");
    }

    return port;
  }
}
