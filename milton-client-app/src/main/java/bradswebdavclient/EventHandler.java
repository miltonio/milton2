/*
 * Copyright 2012 McEvoy Software Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package bradswebdavclient;

import javax.swing.JPanel;

/**
 * Defines an event handler for a BuildDialog events.
 * @author hollomg
 */
public interface EventHandler {
 
  /**   Return the panel with the UI for this dialog. Usually, just return this
   *
   *    Should register any drop target components with the transfer handler
   */
  JPanel getPanel();
  
  /**   return the text of the title for a dialog box which will show these details
   */
  public String getTitle();
}
