package seg.jUCMNav.editparts.treeEditparts;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.gef.editparts.AbstractTreeEditPart;

import seg.jUCMNav.editors.UCMNavMultiPageEditor;

/**
 * Created on 20-May-2005
 * 
 * This class is simply here because the root of our tree (URNspec) doesn't appear in the outline.
 * 
 * @author jkealey
 *  
 */
public class OutlineRootEditPart extends AbstractTreeEditPart {

    public OutlineRootEditPart(UCMNavMultiPageEditor editor) {
        super(editor);
    }

    /**
     * Return the root URNSpec
     */
    protected List getModelChildren() {
        ArrayList l = new ArrayList();
        l.add(((UCMNavMultiPageEditor) getModel()).getModel());
        return l;
    }

    
}