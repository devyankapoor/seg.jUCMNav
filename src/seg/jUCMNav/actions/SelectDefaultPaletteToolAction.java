package seg.jUCMNav.actions;

import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.ui.IWorkbenchPart;

import seg.jUCMNav.editors.UCMNavMultiPageEditor;

/**
 * Reverts the palette to the default tool. To be used with keybindings.
 * 
 * @author jkealey
 */
public class SelectDefaultPaletteToolAction extends URNSelectionAction {

    public static final String SETDEFAULTPALETTETOOL = "seg.jUCMNav.SelectDefaultPaletteTool"; //$NON-NLS-1$

    public SelectDefaultPaletteToolAction(IWorkbenchPart part) {
        super(part);
        setId(SETDEFAULTPALETTETOOL);
        setAccelerator(SWT.ESC);
    }
    
    /* (non-Javadoc)
     * @see seg.jUCMNav.actions.UCMSelectionAction#calculateEnabled()
     */
    protected boolean calculateEnabled() {
        return true;
    }
    
    /* (non-Javadoc)
     * @see seg.jUCMNav.actions.UCMSelectionAction#run()
     */
    public void run() {
     // this one is already done by the framework. 
     ((UCMNavMultiPageEditor)getWorkbenchPart()).getCurrentPage().getEditDomain().getPaletteViewer().setActiveTool(null);
     ((UCMNavMultiPageEditor)getWorkbenchPart()).getCurrentPage().getGraphicalViewer().setSelection(StructuredSelection.EMPTY);
    }
}