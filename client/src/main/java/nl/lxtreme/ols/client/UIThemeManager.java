package nl.lxtreme.ols.client;

import com.formdev.flatlaf.FlatDarculaLaf;
import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatIntelliJLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.themes.FlatMacDarkLaf;
import com.formdev.flatlaf.themes.FlatMacLightLaf;
import nl.lxtreme.ols.client.signaldisplay.laf.UIManagerKeys;
import org.osgi.service.cm.Configuration;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.Set;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class UIThemeManager {

  private final static String DEFAULT_THEME = FlatLightLaf.NAME;
  private static final java.util.logging.Logger LOG = java.util.logging.Logger.getLogger( UIThemeManager.class.getName() );

  private boolean initialized = false;
  private String currentTheme;
  private Set<String> availableThemes;
  private Configuration config;

  public void initialize(Configuration config)
  {
    if ( initialized )
    {
      LOG.fine( "Already initialized" );
      return;
    }

    currentTheme = UIManager.getLookAndFeel().getName();
    this.config = config;
    installAvailableLafs();
    populateInstalledThemes();
    setTheme( getConfiguredTheme() );
    initialized = true;
  }

  public void showPreviewTheme( String uiTheme )
  {
    LOG.fine( "Showing preview theme: '" + uiTheme + "'" );
    setTheme( uiTheme );
  }

  public void revertPreviewTheme()
  {
    LOG.fine( "Reverting to current theme: '" + currentTheme + "' from '" + UIManager.getLookAndFeel().getName() + "'" );
    setTheme( getConfiguredTheme() );
  }

  public void setTheme( final String uiTheme )
  {
    LOG.fine( "Setting theme to '" + uiTheme + "' currentTheme: " + currentTheme );
    if ( currentTheme.equals(uiTheme) ) {
      LOG.fine( "Theme already set to '" + uiTheme + "', returning" );
      return;
    }

    if ( !availableThemes.contains(uiTheme) )
    {
      LOG.warning( "Theme '" + uiTheme + "' not found, not changing lookAndFeel" );
      return;
    }

    Arrays.stream( UIManager.getInstalledLookAndFeels() )
      .peek(theme -> LOG.fine("Available theme: '" + theme.getName() + "' (" + theme.getClassName() + ")"))
      .filter( theme -> theme.getName().equals( uiTheme ) )
      .findFirst()
      .ifPresentOrElse(
        theme -> {
          try
          {
            LOG.fine( "Requested theme found: '" + theme.getClassName() + "', setting it" );
            UIManager.setLookAndFeel(theme.getClassName());
            currentTheme = theme.getClassName();
            for ( Window window : Window.getWindows() )
            {
              SwingUtilities.updateComponentTreeUI( window );
            }
          }
          catch (Exception e)
          {
            LOG.log(Level.SEVERE, "Failed to set system look and feel", e);
          }
        }, () -> LOG.info( "Requested theme '" + uiTheme + "' not found, not changing lookAndFeel" ));
  }

  private void populateInstalledThemes()
  {
    availableThemes = Arrays.stream( UIManager.getInstalledLookAndFeels() )
      .map( UIManager.LookAndFeelInfo::getName )
      .collect( Collectors.toSet() );
  }

  private String getConfiguredTheme()
  {
    var themeObj = config.getProperties().get( UIManagerKeys.USER_SELECTED_UI_THEME );

    if ((themeObj instanceof String) && availableThemes.contains((String) themeObj))
    {
      LOG.fine( "getConfiguredTheme: Configured theme: '" + themeObj + "'" );
      return (String) themeObj;
    }
    else
    {
      LOG.fine( "getConfiguredTheme: Configured theme: '" + themeObj + "' not found or available, using default theme: '" + DEFAULT_THEME + "'" );
      return DEFAULT_THEME;
    }
  }

  private void installAvailableLafs()
  {
    FlatMacLightLaf.installLafInfo();
    FlatMacDarkLaf.installLafInfo();
    FlatLightLaf.installLafInfo();
    FlatDarkLaf.installLafInfo();
    FlatDarculaLaf.installLafInfo();
    FlatIntelliJLaf.installLafInfo();
  }
}
