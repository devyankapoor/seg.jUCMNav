/**
 * 
 */
package seg.jUCMNav.editparts.strategyTreeEditparts;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;

import seg.jUCMNav.JUCMNavPlugin;
import seg.jUCMNav.model.util.EObjectClassNameComparator;
import ucm.UCMspec;

/**
 * This class is the root edit part for the scenarios view. 
 * 
 * @author jkealey
 *
 */
public class UCMspecScenarioTreeEditPart extends StrategyUrnModelElementTreeEditPart {
    
    /**
     * @param model
     *          The UCMspec model
     */
    public UCMspecScenarioTreeEditPart(UCMspec model) {
        super(model);
    }

    /**
     * Listens to UCMspec
     * 
     * @see org.eclipse.gef.EditPart#activate()
     */
    public void activate() {
        if (!isActive()) {
            getUCMspec().eAdapters().add(this);
        }
        super.activate();
    }

    /**
     * Stops listening to both the URNspec and the UCMspec.
     * 
     * @see org.eclipse.gef.EditPart#deactivate()
     */
    public void deactivate() {
        if (isActive()) {
        	getUCMspec().eAdapters().remove(this);
        }
        super.deactivate();
    }
    
    /**
     * @return the icon associated with URNspec
     */
    protected Image getImage() {
        if (super.getImage() == null)
            setImage((ImageDescriptor.createFromFile(JUCMNavPlugin.class, "icons/folder16.gif")).createImage()); //$NON-NLS-1$
        return super.getImage();
    }
    
    /**
     * @return the sorted list of Scenario Group
     */
    protected List getModelChildren() {
        ArrayList list = new ArrayList();
        list.addAll(getUCMspec().getScenarioGroups());
        Collections.sort(list, new EObjectClassNameComparator());
        return list;
    }

    private UCMspec getUCMspec(){
        return (UCMspec)getModel();
    }
    
    /**
     * @return the URNspec name.
     */
    protected String getText() {
        return "UCM Scenarios";
    }
}
