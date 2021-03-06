/*******************************************************************************
 * Mission Control Technologies, Copyright (c) 2009-2012, United States Government
 * as represented by the Administrator of the National Aeronautics and Space 
 * Administration. All rights reserved.
 *
 * The MCT platform is licensed under the Apache License, Version 2.0 (the 
 * "License"); you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT 
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the 
 * License for the specific language governing permissions and limitations under 
 * the License.
 *
 * MCT includes source code licensed under additional open source licenses. See 
 * the MCT Open Source Licenses file included with this distribution or the About 
 * MCT Licenses dialog available at runtime from the MCT Help menu for additional 
 * information. 
 *******************************************************************************/
package gov.nasa.arc.mct.scenario.component;

import gov.nasa.arc.mct.gui.CustomVisualControl;
import gov.nasa.arc.mct.util.MCTIcons;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.UIManager;

/**
 * Provides the custom user interface needed for displaying  
 * Activity Types in the Info View.
 * 
 * Specifically, provides a text field with a link button 
 * next to it, which triggers the opening of a browser 
 * window.
 */
public class LinkVisualControl extends CustomVisualControl {
	private static final long serialVersionUID = 3538827305917390749L;	
	private static final ResourceBundle BUNDLE = ResourceBundle.getBundle("Bundle");
	private static final ImageIcon LINK_ICON = new ImageIcon(LinkVisualControl.class.getResource("/icons/mct_icon_link.png"));	
	
	private JLabel label = new JLabel();
	private JTextField field = new JTextField();
	private JButton button = new JButton();
	private Color border = UIManager.getColor("border");
	private boolean mutable = false;
	
	public LinkVisualControl(String title) {
		this();
		add(new JLabel(title), BorderLayout.WEST);
	}
	
	public LinkVisualControl() {
		setLayout(new BorderLayout());
		
		add(label, BorderLayout.CENTER);
		
		button.setIcon(MCTIcons.processIcon(LINK_ICON, label.getForeground(), false));
		button.setBorder(BorderFactory.createEmptyBorder());
		button.setContentAreaFilled(false);

		if (Desktop.isDesktopSupported()) {
			add(button, BorderLayout.LINE_END);
			button.addActionListener(buttonListener);
			button.setToolTipText(BUNDLE.getString("url_tooltip"));
		}
		
		FieldChangeListener fieldListener = new FieldChangeListener();
		field.addActionListener(fieldListener);
		field.addFocusListener(fieldListener);
		field.setBorder(BorderFactory.createLineBorder(border != null ? border : Color.GRAY));
		
		setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));		
	}
	
	@Override
	public void setValue(Object value) {
		field.setText(value.toString());
		label.setText(value.toString());
	}

	@Override
	public Object getValue() {
		return mutable ? field.getText() : label.getText();
	}

	@Override
	public void setMutable(boolean mutable) {
		remove(mutable ? field : label);
		this.mutable = mutable;
		add(mutable ? field : label, BorderLayout.CENTER);		
	}
	
	private void error(String message) {
		JOptionPane.showMessageDialog(
				this, 
				String.format(BUNDLE.getString("url_error_message"), getValue().toString()), 
				BUNDLE.getString("url_error_title"), 
				JOptionPane.ERROR_MESSAGE);
	}
	
	private ActionListener buttonListener = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			try {
				Desktop.getDesktop().browse(new URI(getValue().toString()));
			} catch (IOException e) {
				error(e.getLocalizedMessage());
			} catch (URISyntaxException e) {
				error(e.getLocalizedMessage());
			}			
		}			
	};
	
	private class FieldChangeListener implements ActionListener, FocusListener {		
		@Override
		public void focusGained(FocusEvent e) {
			fireChange();
		}

		@Override
		public void focusLost(FocusEvent e) {
			fireChange();
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			fireChange();
		}		
	}
}
