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
package gov.nasa.arc.mct.data.action;

import gov.nasa.arc.mct.components.AbstractComponent;
import gov.nasa.arc.mct.gui.View;

import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.concurrent.ExecutionException;

import javax.swing.JOptionPane;
import javax.swing.ProgressMonitor;

/**
 * Responsible for managing the import of a Dictionary file. 
 * Serves as an intermediary between the worker which handles 
 * the background import and the progress monitor which updates 
 * the user.
 * 
 * @author jdong2
 *
 */
public class DictionaryImporter {
	
	/** an AWT component (for progress monitor) */
	private Component component;
			
	/** Associated with the currently selected manifestation (the parent component 
	 *  from which the import menu item was selected) **/
	private AbstractComponent parent;
	
	/** A file containing a list of references to TLE data that will be 
	 *  to be parsed into imported into MCT components **/
	private File file;
	
	/**
	 * Create a new DictionaryImporter.
	 * @param manifestation the associated View on which the action is performed
	 * @param parent the parent to form a tree structure
	 * @param files the file to read
	 */
	public DictionaryImporter(View manifestation, AbstractComponent parent, File file) {
		super();
		this.component = manifestation;
		this.parent = parent;
		this.file = file;
	}
	
	/**
	 * Perform the import of dictionary file. 
	 * The parsing of Dictionary and saving to MCT 
	 * occur on a background thread, and a progress 
	 * monitor is provided if necessary.
	 */
	public void importDictionaries() {
		final DictionaryImportWorker worker = new DictionaryImportWorker(parent, file);
		final ProgressMonitor monitor = new ProgressMonitor(component,
				BundleAccess.BUNDLE.getString("import_progress_message"), 
				"", 0, 100);
		
		monitor.setMillisToDecideToPopup(200);
		monitor.setMillisToPopup(750);
		
		worker.addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				if (worker.isDone() && evt.getPropertyName().equals("state")) {
					notifyComplete(worker);
				} else if (monitor.isCanceled()) {
					worker.cancel(true);
				} else {
					monitor.setProgress(worker.getProgress());
				} 
			}			
		});
		
		worker.execute();
	}
	
	private void notifyComplete(DictionaryImportWorker worker) {
		try {
			// Should return true on success; we only want to notify on false.
			// Also, if there is no exception, assume export was cancelled.
			if (!worker.get() && worker.getException() != null) {
				Exception e = worker.getException();
				String message = BundleAccess.BUNDLE.getString("import_dictionary_error_message") +
						(e.getMessage() != null ? "\n" + e.getMessage() : "");
				String title = BundleAccess.BUNDLE.getString("import_dictionary_ error_title");						
				JOptionPane.showMessageDialog(component, message, title, JOptionPane.ERROR_MESSAGE);
			}
		} catch (InterruptedException e) {
			// Should not occur - already checked isDone
		} catch (ExecutionException e) {
			// Should not occur - already checked isDone
		}
	}
}
