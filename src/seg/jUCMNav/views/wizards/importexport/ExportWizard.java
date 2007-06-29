package seg.jUCMNav.views.wizards.importexport;

import grl.GRLGraph;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.draw2d.LayeredPane;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IExportWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.FileEditorInput;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import seg.jUCMNav.Messages;
import seg.jUCMNav.editors.UCMNavMultiPageEditor;
import seg.jUCMNav.editors.UrnEditor;
import seg.jUCMNav.editparts.URNRootEditPart;
import seg.jUCMNav.extensionpoints.IURNExport;
import seg.jUCMNav.extensionpoints.IURNExportPrePostHooks;
import seg.jUCMNav.extensionpoints.IUseCaseMapExport;
import seg.jUCMNav.importexport.UCMExportExtensionPointHelper;
import seg.jUCMNav.importexport.URNExportExtensionPointHelper;
import ucm.map.UCMmap;
import urn.URNspec;
import urncore.IURNDiagram;
import urncore.URNmodelElement;

/**
 * Assumption: the default editor for .jucm files is UCMNavMultiPageEditor. Otherwise, a bunch of stuff won't work properly.
 * 
 * Will look into its IStructuredSelection and find all applicable maps.
 * 
 * @author jkealey
 * 
 */
public class ExportWizard extends Wizard implements IExportWizard {

	protected static final String PAGE0 = Messages.getString("ExportWizard.ExportURNorUCM"); //$NON-NLS-1$
	protected static final String PAGE1 = Messages.getString("ExportImageWizard.exportImage"); //$NON-NLS-1$

	/**
	 * Removes illegal characters from filenames. Not sure if the complete list is here.
	 * 
	 * @param name
	 * @return cleaned up filename
	 */
	public static String cleanFileName(String name) {
		name = name.replace('\\', '_');
		name = name.replace('/', '_');
		name = name.replace(':', '_');
		name = name.replace('*', '_');
		name = name.replace('?', '_');
		name = name.replace('"', '_');
		name = name.replace('<', '_');
		name = name.replace('>', '_');
		name = name.replace('|', '_');
		return name;
	}

	/**
	 * mapsToEditor: f(map) |-> UCMNavMultiPageEditor
	 * 
	 * mapsToSpecificEditor: f(map) |-> UcmEditor
	 */
	protected HashMap mapsToEditor, mapsToSpecificEditor;

	/**
	 * List of maps to export, first determined by selection when invoking wizard, narrowed down by page 1.
	 */
	protected Vector mapsToExport;

	/**
	 * List of editors opened by the wizard, which must be closed when done.
	 */
	protected Collection openedEditors;

	/**
	 * The workbench page in which we are working
	 */
	protected IWorkbenchPage page;

	/**
	 * The selection we have in this workbench page (might be resources, might be URNspec/maps in outline).
	 */
	protected IStructuredSelection selection;

	/**
	 * The diagrams that should be selected by default.
	 */
	protected Vector selectedDiagrams;

	protected Vector postHooks;

	/**
	 * Initialize preferences.
	 */
	public ExportWizard() {
		ExportPreferenceHelper.createPreferences();

	}

	/**
	 * Add both pages
	 */
	public void addPages() {
		addPage(new ExportWizardTypeSelectionPage(PAGE0));
		addPage(new ExportWizardMapSelectionPage(PAGE1, mapsToExport, mapsToEditor));
	}

	/**
	 * Closes the editors opened during the wizard's work.
	 */
	private void closedOpenedEditors() {
		for (Iterator iter = openedEditors.iterator(); iter.hasNext();) {
			IEditorPart element = (IEditorPart) iter.next();
			page.closeEditor(element, false);
		}
	}

	/**
	 * Adds an entry in both mapsToEditor and mapsToSpecificEditor
	 * 
	 * @param editor
	 * @param diagram
	 */
	private void defineMapping(UCMNavMultiPageEditor editor, IURNDiagram diagram) {
		mapsToEditor.put(diagram, editor);
		mapsToSpecificEditor.put(diagram, editor.getEditor(editor.getModel().getUrndef().getSpecDiagrams().indexOf(diagram)));
	}

	/**
	 * Saves all images and closes opened editors.
	 */
	private boolean doFinish(IProgressMonitor monitor) throws Exception {
		boolean b = ((ExportWizardMapSelectionPage) getPage(PAGE1)).finish();
		postHooks = new Vector();

		if (b) {
			// vector which will be filled with already exported URNspecs
			Vector v = new Vector();

			for (Iterator iter = mapsToExport.iterator(); iter.hasNext();) {
				if (ExportPreferenceHelper.getExportType() == ExportPreferenceHelper.URN_DIAGRAM) {
					ExportDiagram((IURNDiagram) iter.next());
				}
				else
					ExportURN((IURNDiagram) iter.next(), v);

				monitor.worked(1);
			}
		}
		return b;
	}

	/**
	 * Exports a diagram to a file. Uses mapsToExport to find the editor and the preference store to build the file name.
	 * 
	 * @param diagram
	 */
	private void ExportDiagram(IURNDiagram diagram) throws Exception  {
		FileOutputStream fos = null;

		try {

			// given the export type, get the exporter id
			int imgtype = ExportPreferenceHelper.getPreferenceStore().getInt(ExportPreferenceHelper.PREF_IMAGETYPE);
			String id = UCMExportExtensionPointHelper.getExporterFromLabelIndex(imgtype);

			// generate the path.
			String diagramName = getDiagramName(diagram);
			Path genericPath = new Path(ExportPreferenceHelper.getPreferenceStore().getString(ExportPreferenceHelper.PREF_PATH));
			genericPath = (Path) genericPath.append("/" + diagramName); //$NON-NLS-1$
			genericPath = (Path) genericPath.addFileExtension(UCMExportExtensionPointHelper.getFilenameExtension(id));

			// get the simple editor
			UrnEditor editor = (UrnEditor) mapsToSpecificEditor.get(diagram);

			if (UCMExportExtensionPointHelper.isUseStream(id)) {
				// prepare file stream
				fos = new FileOutputStream(genericPath.toOSString());
			}

			// get exporter
			IUseCaseMapExport exporter = UCMExportExtensionPointHelper.getExporter(id);

			// save it
			if (UCMExportExtensionPointHelper.getMode(id).equals("image")) { //$NON-NLS-1$
				// get the high level IFigure to be saved.
				LayeredPane pane = ((URNRootEditPart) (editor.getGraphicalViewer().getRootEditPart())).getScaledLayers();
				if (UCMExportExtensionPointHelper.isUseStream(id)) {
					exporter.export(pane, fos);
				} else {
					exporter.export(pane, genericPath.toOSString());
				}
			} else {
				// model instance
				if (UCMExportExtensionPointHelper.isUseStream(id)) {
					exporter.export(editor.getModel(), fos);
				} else {
					exporter.export(editor.getModel(), genericPath.toOSString());
				}
			}

//			} catch (InvocationTargetException e) {
//			Throwable realException = e.getTargetException();
//			IStatus error = new Status(IStatus.ERROR, JUCMNavPlugin.PLUGIN_ID, 1, realException.toString(), realException);
//			ErrorDialog.openError(getShell(), Messages.getString("ExportWizard.ErrorOccurred"), e.getMessage(), error); //$NON-NLS-1$
//			return;
//			} catch (Exception e) {
//			IStatus error = new Status(IStatus.ERROR, JUCMNavPlugin.PLUGIN_ID, 1, e.toString(), e);
//			ErrorDialog.openError(getShell(), Messages.getString("ExportWizard.ErrorOccurred"), e.getMessage(), error); //$NON-NLS-1$
//			return;
		} finally {
			// close the stream
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		}

	}





	/**
	 * Exports a URNSpec to a file. Uses mapsToExport to find the editor and the preference store to build the file name.
	 * 
	 * Won't export the URNSpec which contains map if it is already contained in v
	 * 
	 * @param diagram
	 * @param v
	 */

	private void ExportURN(IURNDiagram diagram, Vector v) throws Exception {
		// dont output same URN multiple times.
		if (v.contains(diagram.getUrndefinition().getUrnspec()))
			return; // we've already exported this URNSpec
		v.add(diagram.getUrndefinition().getUrnspec());

		FileOutputStream fos = null;
		try {

			// given the export type, get the exporter id
			int imgtype = ExportPreferenceHelper.getPreferenceStore().getInt(ExportPreferenceHelper.PREF_IMAGETYPE);
			String id = URNExportExtensionPointHelper.getExporterFromLabelIndex(imgtype);

			// generate the path.
			Path genericPath = new Path(ExportPreferenceHelper.getPreferenceStore().getString(ExportPreferenceHelper.PREF_PATH));
			genericPath = (Path) genericPath.append("/" + ExportPreferenceHelper.getFilenamePrefix());  //$NON-NLS-1$
			genericPath = (Path) genericPath.addFileExtension(URNExportExtensionPointHelper.getFilenameExtension(id));

			UCMNavMultiPageEditor editor = (UCMNavMultiPageEditor) mapsToEditor.get(diagram);

			// get exporter
			IURNExport exporter = URNExportExtensionPointHelper.getExporter(id);

			// TODO: this should be before we get to this point, in the same thread as the UI. 
			if (exporter instanceof IURNExportPrePostHooks) {
				IURNExportPrePostHooks hooks = (IURNExportPrePostHooks) exporter;
				hooks.preHook(editor);
			}

			HashMap mapDiagrams = new HashMap();
			for (Iterator iter = editor.getModel().getUrndef().getSpecDiagrams().iterator(); iter.hasNext();) {
				IURNDiagram diag = (IURNDiagram) iter.next();
				UrnEditor editor2 = ((UrnEditor)mapsToSpecificEditor.get(diag));
				LayeredPane pane = ((URNRootEditPart) (editor2.getGraphicalViewer().getRootEditPart())).getScaledLayers();
				mapDiagrams.put(diag, pane);
			}
			if (URNExportExtensionPointHelper.isUseStream(id)) {
				// prepare file stream
				fos = new FileOutputStream(genericPath.toOSString());

				// save it
				exporter.export(editor.getModel(), mapDiagrams, fos);
			} else {
				// save it
				exporter.export(editor.getModel(), mapDiagrams, genericPath.toOSString());
			}

			// export individual maps if desired
			if (URNExportExtensionPointHelper.getPostExporter(id).length() > 0) {
				int oldSelection = ExportPreferenceHelper.getImageType();

				// go through the individual diagram exporters; find the index of the required one
				for (int i = 0; i < UCMExportExtensionPointHelper.getExportLabels().length; i++) {
					if (UCMExportExtensionPointHelper.getExporterFromLabelIndex(i).equals(URNExportExtensionPointHelper.getPostExporter(id))) {
						// set it in our preferences.
						ExportPreferenceHelper.setImageType(i);
						break;
					}
				}
				// export the individual diagrams
				for (Iterator iter = editor.getModel().getUrndef().getSpecDiagrams().iterator(); iter.hasNext();) {
					IURNDiagram element = (IURNDiagram) iter.next();
					ExportDiagram(element);
				}

				// set back the original preference so that the URN export is the same next time.
				ExportPreferenceHelper.setImageType(oldSelection);

			}


			if (fos != null) {
				try {
					fos.close();
					if (id.compareTo("seg.jUCMNav.UCM2CSM") == 0) {
						validateXml(genericPath.toOSString());
					}
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				fos=null;
			}
			if (exporter instanceof IURNExportPrePostHooks) {
				postHooks.add(exporter);
			}

//			} catch (InvocationTargetException e) {
//			Throwable realException = e.getTargetException();
//			IStatus error = new Status(IStatus.ERROR, JUCMNavPlugin.PLUGIN_ID, 1, realException.toString(), realException);
//			ErrorDialog.openError(getShell(), Messages.getString("ExportWizard.ErrorOccurred"), e.getMessage(), error); //$NON-NLS-1$
//			} catch (Exception e) {
//			IStatus error = new Status(IStatus.ERROR, JUCMNavPlugin.PLUGIN_ID, 1, e.toString(), e);
//			ErrorDialog.openError(getShell(), Messages.getString("ExportWizard.ErrorOccurred"), e.getMessage(), error); //$NON-NLS-1$
		} finally {
			// close the stream
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		}




	}

	/**
	 * 
	 * @param diagram
	 * @return the prefix of the file containing the diagram; assumes .jucm extension
	 */
	public static String getFilePrefix(IURNDiagram diagram) {

		String filename = ExportPreferenceHelper.getFilenamePrefix();
		//((FileEditorInput) editor.getEditorInput()).getName();
		// remove the .jucm extension
		if (filename.length()>5 && filename.substring(filename.length() - 5).equals(".jucm")){ //$NON-NLS-1$
			filename = filename.substring(0, filename.length() - 5);
		}
		return filename;
	}

	/**
	 * Returns a name for the diagram, build from the filename it is contained, its id and its map's name, without extension; uses mapsToEditor
	 * 
	 * To be used to save to disk.
	 * 
	 * @param diagram
	 * @return map name
	 */
	public static String getDiagramName(IURNDiagram diagram) {
		String filename = getFilePrefix(diagram);

		String name = ((URNmodelElement) diagram).getName();
		name = cleanFileName(name);

		String result = ""; //$NON-NLS-1$
		if (diagram instanceof UCMmap) {
			result = filename + "-" + "Map" + ((UCMmap) diagram).getId() + "-" + name; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		} else if (diagram instanceof GRLGraph) {
			result = filename + "-" + "GRLGraph" + ((GRLGraph) diagram).getId() + "-" + name; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}
		return result;
	}

	/**
	 * Initializes the pages using the selection
	 * 
	 * @param workbench
	 * @param currentSelection
	 *            a collection of .jucm IFiles, Maps or URNspecs
	 */
	public void init(IWorkbench workbench, IStructuredSelection currentSelection) {
		this.page = workbench.getActiveWorkbenchWindow().getActivePage();
		this.mapsToExport = new Vector();
		this.openedEditors = new Vector();
		this.mapsToEditor = new HashMap();
		this.mapsToSpecificEditor = new HashMap();

		setWindowTitle(Messages.getString("ExportImageWizard.exportImageWizard")); //$NON-NLS-1$

		// filter ifile resources
		this.selection = currentSelection;
		List selectedResources = IDE.computeSelectedResources(currentSelection);
		if (!selectedResources.isEmpty()) {
			this.selection = new StructuredSelection(selectedResources);
		}

		IWorkbenchPage page = workbench.getActiveWorkbenchWindow().getActivePage();
		for (Iterator iter = this.selection.iterator(); iter.hasNext();) {
			Object obj = iter.next();

			// if is an IFile, extract all contained maps.
			if (obj instanceof IFile) {
				IFile element = (IFile) obj;
				IEditorDescriptor desc = PlatformUI.getWorkbench().getEditorRegistry().getDefaultEditor(element.getName());
				UCMNavMultiPageEditor editor = null;
				FileEditorInput input = new FileEditorInput(element);
				try {
					editor = (UCMNavMultiPageEditor) page.findEditor(input);
					// if editor isn't opened, open it.
					if (editor == null) {
						editor = (UCMNavMultiPageEditor) page.openEditor(input, desc.getId(), false);
						// to be able to close them later.
						openedEditors.add(editor);
					}
					//Set the filename prefix
					ExportPreferenceHelper.setFilenamePrefix(editor.getEditorInput().getName());
				} catch (ClassCastException e) {
					// if default editor isn't UCMNavMultiPageEditor
					e.printStackTrace();
				} catch (PartInitException e) {
					e.printStackTrace();
				}

				if (editor==null)return;
				// add all maps.
				this.mapsToExport.addAll(editor.getModel().getUrndef().getSpecDiagrams());
				for (Iterator iterator = editor.getModel().getUrndef().getSpecDiagrams().iterator(); iterator.hasNext();) {
					IURNDiagram diagram = (IURNDiagram) iterator.next();
					defineMapping(editor, diagram);
				}
			} else {
				// is a Diagram or URNSpec; extract diagram from those.
				IEditorPart editorpart = workbench.getActiveWorkbenchWindow().getActivePage().getActiveEditor();
				if (editorpart instanceof UCMNavMultiPageEditor) {
					UCMNavMultiPageEditor editor = (UCMNavMultiPageEditor) editorpart;
					// Set the filename prefix
					ExportPreferenceHelper.setFilenamePrefix(editor.getEditorInput().getName());

					if (obj instanceof IURNDiagram) {
						IURNDiagram diagram = (IURNDiagram) obj;
						addSelectedDiagram(diagram);
						// this.mapsToExport.add(obj);
						// defineMapping(editor, diagram);
						// define all mappings because they are used by some exports
						for (Iterator iterator = diagram.getUrndefinition().getSpecDiagrams().iterator(); iterator.hasNext();) {
							IURNDiagram diagram2 = (IURNDiagram) iterator.next();
							this.mapsToExport.add(diagram2);
							defineMapping(editor, diagram2);
						}
					} else if (obj instanceof URNspec) {
						this.mapsToExport.addAll(((URNspec) obj).getUrndef().getSpecDiagrams());
						for (Iterator iterator = ((URNspec) obj).getUrndef().getSpecDiagrams().iterator(); iterator.hasNext();) {
							IURNDiagram diagram = (IURNDiagram) iterator.next();
							defineMapping(editor, diagram);
						}
					}
				}
			}
		}

	}

	/**
	 * Closes opened editors.
	 */
	public boolean performCancel() {
		closedOpenedEditors();
		return true;
	}

	/**
	 * This method is called when 'Finish' button is pressed in the wizard. We will create an operation and run it using wizard as execution context.
	 */
	public boolean performFinish() {
		IRunnableWithProgress op = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException {
				try {
					monitor.beginTask(Messages.getString("ExportWizard.Exporting"), IProgressMonitor.UNKNOWN); //$NON-NLS-1$
					doFinish(monitor);
				} catch (Exception ex) {
					if (ex instanceof InvocationTargetException)
						throw (InvocationTargetException)ex;
					else 
						throw new InvocationTargetException(ex);
				}finally {
					monitor.done();
				}
			}

		};
		try {
			((ExportWizardMapSelectionPage) getPage(PAGE1)).preFinish();
			getContainer().run(true, false, op);
			for (Iterator iter = postHooks.iterator(); iter.hasNext();) {
				IURNExportPrePostHooks hook = (IURNExportPrePostHooks) iter.next();
				hook.postHook(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage());

			}            
			closedOpenedEditors();

		} catch (InterruptedException e) {
			return false;
		} catch (InvocationTargetException e) {
			Throwable realException = e.getTargetException();
			MessageDialog.openError(getShell(), Messages.getString("ExportImageWizard.error"), realException.getMessage()); //$NON-NLS-1$
			return false;
		}
		return true;
	}

	/**
	 * When the export type changes, one should refresh page 1 as its contents must change.
	 */
	public void refreshPages() {
		((ExportWizardMapSelectionPage) getPage(PAGE1)).refresh();
	}

	/**
	 * 
	 * @return the list of selected diagrams
	 */
	public Vector getSelectedDiagrams() {
		if (selectedDiagrams == null)
			selectedDiagrams = new Vector();
		return selectedDiagrams;
	}

	/**
	 * Add a diagram to the list of selected diagrams
	 * 
	 * @param d
	 *            the diagram to add.
	 */
	public void addSelectedDiagram(IURNDiagram d) {
		getSelectedDiagrams().add(d);
	}

	/**
	 * validateXml verifies if the generated XML document conforms to the CSM schema.
	 * Note that tools that use those XML document do not accept a strictly correct
	 * syntax, hence the generate XML document is first "corrected" (as a temporary
	 * file) before being validated.
	 * The CSM schama need to reside in the same directory as the CSM file has been
	 * generated.
	 * 
	 * @author jack
	 * 
	 * @param fiss
	 * 		the CSM XML generated filename (String, including full path)
	 */
	private void validateXml(String fiss) {

		// NB: the schema MUST reside along with the document
		String workingDirectory = fiss.substring(0, fiss.lastIndexOf(File.separator));
		File csmSchemaFile = new File (workingDirectory + File.separator + "CSM.xsd");
		if (! csmSchemaFile.exists()) {
			System.err.println("NOTE:  XML correctness of the document was not validated since file CSM.xsd was not found in " + workingDirectory);
			return;
		}

		// Generate a parser that will load an XML document into a DOM tree
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
		documentBuilderFactory.setNamespaceAware(true);
		DocumentBuilder documentParser = null;
		try {
			documentParser = documentBuilderFactory.newDocumentBuilder();
		} catch (ParserConfigurationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		// Command to "fix" the CSM XML by replacing CSM:CSMType with CSM
		// Then parses into "document" for validation 
		Document document = null;
		try {
			// Transform file into a String
			FileInputStream CSMfile = new FileInputStream (fiss);
			byte[] filebytes = new byte[CSMfile.available ()];
			CSMfile.read(filebytes);
			CSMfile.close ();
			String CSMdocInMemory = new String (filebytes);
			
			// Remove violating :CSMType required by CSM Viewer
			// parse requires an InputStream (or a URI String)
			ByteArrayInputStream FixedCSMdoc = new ByteArrayInputStream(CSMdocInMemory.replaceAll("CSM:CSMType", "CSM").getBytes());

			// Parse the XML file into document
			document = documentParser.parse(FixedCSMdoc);

		} catch (SAXException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}

		// Create a SchemaFactory for WXS schemas
		SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

		// load a WXS schema, represented by a Schema instance
		Source csmSchemaSource = new StreamSource(new File(workingDirectory + File.separator + "CSM.xsd"));
		Schema csmSchema = null;
		try {
			csmSchema = factory.newSchema(csmSchemaSource);
		} catch (SAXException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		// Create a Validator instance, which can be used to validate an instance document
		Validator csmValidator = csmSchema.newValidator();

		// Validate the DOM tree
		try {
			try {
				csmValidator.validate(new DOMSource(document));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (SAXException e) {
			// instance document is invalid!
			e.printStackTrace();
		}
	}

}