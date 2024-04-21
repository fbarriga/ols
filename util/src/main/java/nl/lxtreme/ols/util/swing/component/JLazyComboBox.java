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
 */
package nl.lxtreme.ols.util.swing.component;


import nl.lxtreme.ols.util.HostInfo;
import nl.lxtreme.ols.util.HostUtils;

import java.awt.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.plaf.basic.*;


/**
 * Provides a lazy-loaded combobox.
 */
public class JLazyComboBox<T> extends JComboBox<T>
{
  // INNER TYPES

  // HACK: get access to private getPopupHeightForRowCount(int) method...
  private static class BasicComboPopupProxy extends BasicComboPopup {
    public BasicComboPopupProxy(JComboBox<Object> combo) {
      super(combo);
    }

    public int getPopupHeightForRowCount(int maxRowCount) {
      return super.getPopupHeightForRowCount(maxRowCount);
    }
  }

  /**
   * Provides a data provider for lazy loaded comboboxes.
   */
  public interface ItemProvider<E>
  {
    /**
     * Returns the items.
     *
     * @return an array of items, cannot be <code>null</code>.
     */
    E[] getItems();
  }

  /**
   * A popup menu listener that populates the combobox model with items right
   * before it is shown.
   */
  static final class ComboboxPopupListener<E> implements PopupMenuListener
  {
    // CONSTANTS

    // VARIABLES

    private final ItemProvider<E> itemProvider;

    // CONSTRUCTORS

    /**
     * Creates a new ComboboxPopupListener instance.
     * 
     * @param aItemProvider
     *          the item provider to use to provide the individual items to this
     *          combobox listener.
     */
    public ComboboxPopupListener( final ItemProvider<E> aItemProvider )
    {
      this.itemProvider = aItemProvider;
    }

    // METHODS

    /**
     * @see javax.swing.event.PopupMenuListener#popupMenuCanceled(javax.swing.event.PopupMenuEvent)
     */
    @Override
    public void popupMenuCanceled( final PopupMenuEvent aEvent )
    {
      // NO-op
    }

    /**
     * @see javax.swing.event.PopupMenuListener#popupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent)
     */
    @Override
    public void popupMenuWillBecomeInvisible( final PopupMenuEvent aEvent )
    {
      // NO-op
    }

    /**
     * @see javax.swing.event.PopupMenuListener#popupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent)
     */
    @Override
    public void popupMenuWillBecomeVisible( final PopupMenuEvent aEvent )
    {
      final JComboBox<E> combobox = ( JComboBox<E> )aEvent.getSource();
      final Dimension originalPreferredSize = combobox.getPreferredSize();

      // In case the combobox model is empty; we lazy add the ports we currently
      // have to it...
      if ( combobox.getItemCount() <= 0 )
      {
        // By default a mutable combobox model is set;
        final MutableComboBoxModel<E> model = ( MutableComboBoxModel<E> )combobox.getModel();

        final var items = this.itemProvider.getItems();
        for ( E item : items )
        {
          model.addElement( item );
        }

        correctSize( combobox, originalPreferredSize, items.length );
      }
    }

    /**
     * Corrects the dimensions of both the popup of the given combobox as well
     * as the combobox itself to ensure it remains the same after the items were
     * added. Otherwise, the size is changed and causes weird visual behaviour.
     * 
     * @param aComboBox
     *          the combobox to resize;
     * @param aPreferredSize
     *          the original preferred size of the combobox (before the items
     *          were added), cannot be <code>null</code>;
     * @param aAddedItemCount
     *          the number of added items, >= 0.
     */
    private void correctSize( final JComboBox<E> aComboBox, final Dimension aPreferredSize, final int aAddedItemCount )
    {
      aComboBox.setPreferredSize( aPreferredSize );

      if ( aAddedItemCount > 0 )
      {
        // Idea for this taken from:
        // <http://forums.java.net/jive/message.jspa?messageID=61267>
        final Object comp = aComboBox.getUI().getAccessibleChild( aComboBox, 0 );
        if ( !( comp instanceof BasicComboPopup ) )
        {
          return;
        }

        final HostInfo hostInfo = HostUtils.getHostInfo();
        if ( !hostInfo.isMacOS() )
        {
          final BasicComboPopupProxy popup = (BasicComboPopupProxy) comp;
          final JScrollPane scrollPane = (JScrollPane) popup.getComponent(0);

          final int newWidth = Math.max(scrollPane.getPreferredSize().width, aPreferredSize.width);
          final int newHeight = popup.getPopupHeightForRowCount(
                  Math.min(aComboBox.getMaximumRowCount(), aAddedItemCount));

          final Dimension size = new Dimension(newWidth, newHeight);
          scrollPane.setPreferredSize(size);
          scrollPane.setMaximumSize(size);
        }
      }
    }
  }

  // CONSTANTS

  private static final long serialVersionUID = -4065089150844005742L;

  // CONSTRUCTORS

  /**
   * Creates a new JLazyComboBox instance.
   * 
   * @param aItemProvider
   *          the item provider to use for populating this combobox, cannot be
   *          <code>null</code>.
   */
  public JLazyComboBox( final ItemProvider<T> aItemProvider )
  {
    super( new DefaultComboBoxModel<>() );
    addPopupMenuListener( new ComboboxPopupListener<>( aItemProvider ) );
  }
}
