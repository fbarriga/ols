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
 * Copyright (C) 2006-2010 Michael Poppitz, www.sump.org
 * Copyright (C) 2010-2013 J.W. Janssen, www.lxtreme.nl
 */
package nl.lxtreme.ols.client2.views.state;


import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import nl.lxtreme.ols.client2.views.*;
import nl.lxtreme.ols.common.acquisition.*;
import nl.lxtreme.ols.util.swing.component.*;


/**
 * Provides a table-like view of {@link AcquisitionData}, in which data is shown
 * as numeric values in rows and columns.
 */
public class StateView extends BaseView
{
  // CONSTANTS

  private static final long serialVersionUID = 1L;

  // VARIABLES

  private JLxTable table;

  // CONSTRUCTORS

  /**
   * Creates a new {@link StateView} instance.
   * 
   * @param aController
   *          the controller to use, cannot be <code>null</code>;
   * @param aModel
   *          the model to use, cannot be <code>null</code>.
   */
  public StateView( ViewController aController, ViewModel aModel )
  {
    super( aController, aModel );
  }

  // METHODS

  /**
   * {@inheritDoc}
   */
  @Override
  public double getDisplayedInterval()
  {
    Rectangle visibleRect = this.table.getVisibleRect();

    int startRow = this.table.rowAtPoint( new Point( 0, visibleRect.y ) );
    int endRow = this.table.rowAtPoint( new Point( 0, visibleRect.y + visibleRect.height ) );

    return endRow - startRow;
  }

  @Override
  public void initialize()
  {
    final StateTableModel tableModel = new StateTableModel( this.model.getData() );

    this.table = new JLxTable( tableModel );
    this.table.setShowVerticalLines( false );
    this.table.setShowHorizontalLines( true );
    this.table.setAutoCreateRowSorter( true );
    this.table.setAutoCreateColumnsFromModel( false );
    this.table.setDefaultRenderer( Integer.class, new DataCellRenderer() );
    this.table.setAutoResizeMode( JTable.AUTO_RESIZE_OFF );

    this.table.getTableHeader().addMouseListener( new MouseAdapter()
    {
      public void mousePressed( MouseEvent aEvent )
      {
        handlePopup( aEvent );
      }

      public void mouseReleased( MouseEvent aEvent )
      {
        handlePopup( aEvent );
      }

      private void handlePopup( MouseEvent aEvent )
      {
        final int columnIdx = table.columnAtPoint( aEvent.getPoint() );
        if ( aEvent.isPopupTrigger() && columnIdx > 0 )
        {
          final Radix mode = tableModel.getRadix( columnIdx );
          final ButtonGroup group = new ButtonGroup();
          final JPopupMenu menu = new JPopupMenu();

          for ( final Radix vm : Radix.values() )
          {
            JMenuItem item = new JRadioButtonMenuItem( vm.getDisplayName() );
            item.setSelected( vm == mode );
            item.addActionListener( new ActionListener()
            {
              @Override
              public void actionPerformed( ActionEvent aE )
              {
                tableModel.setViewMode( columnIdx, vm );
                tableModel.fireColumnDataChanged( columnIdx );
                // Ensure the column header is updated as well...
                table.resetColumnHeader( columnIdx );
              }
            } );

            menu.add( item );
            group.add( item );
          }

          menu.show( aEvent.getComponent(), aEvent.getX(), aEvent.getY() );
        }
      }
    } );

    add( new JScrollPane( this.table ), BorderLayout.CENTER );
  }
}
