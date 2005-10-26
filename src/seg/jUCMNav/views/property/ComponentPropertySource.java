package seg.jUCMNav.views.property;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Vector;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.views.properties.ComboBoxPropertyDescriptor;

import seg.jUCMNav.model.util.EObjectClassNameComparator;
import seg.jUCMNav.model.util.URNNamingHelper;
import urn.URNspec;
import urncore.SpecificationComponent;
import urncore.SpecificationComponentRef;
import urncore.SpecificationNode;
import urncore.URNmodelElement;

/**
 * This class is special cased for SpecificationComponentRef's so that we can replace our id/name/description with that of the ComponentElement and add the
 * SpecificationComponent's properties to ours.
 * 
 * SpecificationComponent are listed as a dropdown of all SpecificationComponent in the model.
 * 
 * The colors shown are those of the linked component definition.
 * 
 * @author jkealey, etremblay
 * 
 */
public class ComponentPropertySource extends URNElementPropertySource {

    // if this is a reference to a component, we want it.
    private SpecificationComponent comp = null;
    int i;

    /**
     * @param obj
     */
    public ComponentPropertySource(EObject obj) {
        super(obj);
        if ((object instanceof SpecificationComponentRef) && ((SpecificationComponentRef) object).getCompDef() != null) {
            comp = (SpecificationComponent)((SpecificationComponentRef) object).getCompDef();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see seg.jUCMNav.views.EObjectPropertySource#isAddable(org.eclipse.emf.ecore.EStructuralFeature)
     */
    protected boolean canAddFeature(EStructuralFeature attr) {
        if (comp != null && (attr.getName().equals("name") || attr.getName().equals("id") || attr.getName().equals("description"))) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            // replace with that of ComponentRef with that of ComponentElement
            return false;
        } else
            return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see seg.jUCMNav.views.EObjectPropertySource#addSpecificProperties()
     */
    protected Vector addSpecificProperties() {
        i = 0;
        Iterator it;
        EClass cls = object.eClass();
        Collection descriptors = new Vector();

        if (comp != null) {
            // we are referencing another object; show its properties here.
            it = comp.eClass().getEAllAttributes().iterator();

            // add the new properties
            while (it.hasNext()) {
                EAttribute attr = (EAttribute) it.next();
                addPropertyToDescriptor(descriptors, attr, comp.eClass());
            }
        }
        return (Vector) descriptors;
    }

    /*
     * (non-Javadoc)
     * 
     * @see seg.jUCMNav.views.EObjectPropertySource#addPropertyToDescriptor(java.util.Collection, org.eclipse.emf.ecore.EStructuralFeature,
     *      org.eclipse.emf.ecore.EClass)
     */
    public void addPropertyToDescriptor(Collection descriptors, EStructuralFeature attr, EClass c) {
        EClassifier type = getFeatureType(attr);

        PropertyID propertyid = new PropertyID(c, attr);

        if (type.getInstanceClass() == SpecificationComponent.class) {
            componentElementDescriptor(descriptors, attr, propertyid);
        } else
            super.addPropertyToDescriptor(descriptors, attr, c);
    }

    /**
     * @param descriptors
     * @param attr
     * @param propertyid
     */
    private void componentElementDescriptor(Collection descriptors, EStructuralFeature attr, PropertyID propertyid) {
        if (((SpecificationComponentRef) getEditableValue()).getSpecDiagram().getUrndefinition() == null)
            return;
        URNspec urn = ((SpecificationComponentRef) getEditableValue()).getSpecDiagram().getUrndefinition().getUrnspec();
        Vector list = new Vector(urn.getUrndef().getComponents());
        Collections.sort(list, new EObjectClassNameComparator());

        String[] values = new String[list.size()];
        for (int i = 0; i < list.size(); i++) {

            values[i] = EObjectClassNameComparator.getSortableElementName((SpecificationComponent) list.get(i));
            if (values[i] == null)
                values[i] = "[unnamed]"; //$NON-NLS-1$
        }

        ComboBoxPropertyDescriptor pd = new ComboBoxPropertyDescriptor(propertyid, "definition", values); //$NON-NLS-1$
        pd.setCategory("Reference"); //$NON-NLS-1$
        descriptors.add(pd);

    }

    /*
     * (non-Javadoc)
     * 
     * @see seg.jUCMNav.views.EObjectPropertySource#returnPropertyValue(org.eclipse.emf.ecore.EStructuralFeature, java.lang.Object)
     */
    protected Object returnPropertyValue(EStructuralFeature feature, Object result) {
        if (result instanceof SpecificationComponent) {
            /*
             * if (((ComponentElement) result).getId() != null) result = new Integer(((ComponentElement) result).getId()); else result = new Integer(0);
             */
            URNspec urn = ((SpecificationComponentRef) getEditableValue()).getSpecDiagram().getUrndefinition().getUrnspec();
            Vector list = new Vector(urn.getUrndef().getComponents());
            Collections.sort(list, new EObjectClassNameComparator());
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i).equals(((SpecificationComponentRef) getEditableValue()).getCompDef()))
                    result = new Integer(i);
            }

        } else
            return super.returnPropertyValue(feature, result);

        return result;
    }

    /**
     * @param propertyid
     * @param feature
     * @return a feature
     */
    protected Object getFeature(PropertyID propertyid, EStructuralFeature feature) {
        Object result = null;

        // if this attribute comes from the referenced object
        if (propertyid.getEClass() != object.eClass())
            result = comp.eGet(feature);
        else
            result = object.eGet(feature);
        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.views.properties.IPropertySource#resetPropertyValue(java.lang.Object)
     */
    public void resetPropertyValue(Object id) {
        PropertyID propertyid = (PropertyID) id;
        EStructuralFeature feature = propertyid.getFeature();

        if (feature.getName().toLowerCase().indexOf("color") >= 0 //$NON-NLS-1$
                || (feature instanceof EReference && ((EReference) feature).getEReferenceType().getInstanceClass() == SpecificationComponentRef.class && (getEditableValue() instanceof SpecificationNode || getEditableValue() instanceof SpecificationComponentRef))) {
            if (propertyid.getEClass() != object.eClass())
                comp.eSet(feature, null);
            else
                object.eSet(feature, null);
        } else
            super.resetPropertyValue(id);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.views.properties.IPropertySource#setPropertyValue(java.lang.Object, java.lang.Object)
     */
    public void setPropertyValue(Object id, Object value) {
        PropertyID propertyid = (PropertyID) id;
        EStructuralFeature feature = propertyid.getFeature();

        Object result = getPropertyValue(id);
        URNspec urn = ((SpecificationComponentRef) getEditableValue()).getSpecDiagram().getUrndefinition().getUrnspec();
        if (feature.getEType().getInstanceClass() == SpecificationComponent.class) {

            Vector list = new Vector(urn.getUrndef().getComponents());
            Collections.sort(list, new EObjectClassNameComparator());
            result = list.get(((Integer) value).intValue());
            setReferencedObject(propertyid, feature, result);
            comp = (SpecificationComponent)((SpecificationComponentRef) object).getCompDef();
        } else if (feature.getName() == "name") { //$NON-NLS-1$
            String message = URNNamingHelper.isNameValid(urn, (URNmodelElement) object, value.toString());

            if (message.length() == 0) {
                super.setPropertyValue(id, value);

            } else if (++i % 2 == 1) { // because refreshed twice.
                MessageDialog.openError(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), "Error", message); //$NON-NLS-1$
            }

        } else
            super.setPropertyValue(id, value);

    }

    protected void setReferencedObject(PropertyID propertyid, EStructuralFeature feature, Object result) {
        if (propertyid.getEClass() != object.eClass()) {
            comp.eSet(feature, result);
            if (feature.getName().equalsIgnoreCase("fillColor")) { //$NON-NLS-1$
                comp.setFilled(true);
            }

        } else
            object.eSet(feature, result);
    }

}