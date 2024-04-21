/**
 * 
 */
package nl.lxtreme.ols.client.project.impl;


import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.*;

import nl.lxtreme.ols.api.*;
import nl.lxtreme.ols.api.data.project.*;
import nl.lxtreme.ols.util.*;


/**
 * Provides a manager for loading/storing implicit user settings.
 */
public final class UserSettingsManagerImpl implements UserSettingsManager
{
  // CONSTANTS

  private static final String SETTINGS_ID_FILENAME = "settings.";

  // VARIABLES
  private static final Logger LOG = Logger.getLogger( UserSettingsManagerImpl.class.getName() );

  // METHODS

  /**
   * {@inheritDoc}
   */
  @Override
  public void loadUserSettings( final File aUserSettingsFile, final Project aProject )
  {
    if ( aUserSettingsFile == null )
    {
      throw new IllegalArgumentException( "User settings file cannot be null!" );
    }
    if ( aProject == null )
    {
      throw new IllegalArgumentException( "Project cannot be null!" );
    }

    if ( !aUserSettingsFile.exists() )
    {
      LOG.info( "Ignoring user settings from " + aUserSettingsFile + "; file does not exist..." );
      return;
    }

    LOG.info( "Loading user settings from " + aUserSettingsFile );

    InputStream is = null;
    ZipInputStream zipIS = null;

    try
    {
      is = new BufferedInputStream( new FileInputStream( aUserSettingsFile ) );
      zipIS = new ZipInputStream( is );

      ZipEntry ze = null;
      while ( ( ze = zipIS.getNextEntry() ) != null )
      {
        final String userSettingsName = ze.getName();
        // Ignore settings ID marker file...
        if ( userSettingsName.startsWith( SETTINGS_ID_FILENAME ) )
        {
          continue;
        }

        final Properties settings = new Properties();
        settings.load( zipIS );

        final UserSettings userSettings = aProject.getSettings( userSettingsName );
        userSettings.putAll( settings );

        zipIS.closeEntry();
      }
    }
    catch ( IOException exception )
    {
      LOG.log(Level.WARNING, "Failed to load implicit user settings...", exception );
    }
    finally
    {
      HostUtils.closeResource( zipIS );
      HostUtils.closeResource( is );
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void saveUserSettings( final File aUserSettingsFile, final Project aProject )
  {
    if ( aUserSettingsFile == null )
    {
      throw new IllegalArgumentException( "User settings file cannot be null!" );
    }
    if ( aProject == null )
    {
      throw new IllegalArgumentException( "Project cannot be null!" );
    }

    LOG.info( "Saving user settings to " + aUserSettingsFile );

    OutputStream os = null;

    try
    {
      os = new BufferedOutputStream( new FileOutputStream( aUserSettingsFile ) );
      final ZipOutputStream zipOS = new ZipOutputStream( os );

      // Provide a "special" marker to ensure the ZIP file has at least one
      // entry and can be detected as settings file...
      final ZipEntry zipEntry = new ZipEntry( SETTINGS_ID_FILENAME + System.currentTimeMillis() );
      zipOS.putNextEntry( zipEntry );

      aProject.visit( new ProjectVisitor()
      {
        @Override
        public void visit( final UserSettings aSettings ) throws IOException
        {
          final ZipEntry zipEntry = new ZipEntry( aSettings.getName() );
          zipOS.putNextEntry( zipEntry );

          // Convert to a properties object...
          final Properties props = new Properties();
          for ( Map.Entry<String, Object> userSetting : aSettings )
          {
            props.put( userSetting.getKey(), userSetting.getValue() );
          }

          // Write the project settings
          props.store( zipOS, aSettings.getName().concat( " settings" ) );
        }
      } );

      zipOS.flush();
      zipOS.close();
    }
    catch ( IOException exception )
    {
      LOG.log( Level.WARNING, "Failed to save implicit user settings...", exception );
      throw new RuntimeException( "Failed to save implicit user settings.", exception );
    }
    finally
    {
      HostUtils.closeResource( os );
    }
  }
}
