/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package grl;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.emf.common.util.AbstractEnumerator;

/**
 * <!-- begin-user-doc -->
 * A representation of the literals of the enumeration '<em><b>Intentional Element Type</b></em>',
 * and utility methods for working with them.
 * <!-- end-user-doc -->
 * @see grl.GrlPackage#getIntentionalElementType()
 * @model
 * @generated
 */
public final class IntentionalElementType extends AbstractEnumerator {
    /**
     * The '<em><b>Goal</b></em>' literal value.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #GOAL_LITERAL
     * @model name="Goal"
     * @generated
     * @ordered
     */
    public static final int GOAL = 0;

    /**
     * The '<em><b>Softgoal</b></em>' literal value.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #SOFTGOAL_LITERAL
     * @model name="Softgoal"
     * @generated
     * @ordered
     */
    public static final int SOFTGOAL = 1;

    /**
     * The '<em><b>Task</b></em>' literal value.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #TASK_LITERAL
     * @model name="Task"
     * @generated
     * @ordered
     */
    public static final int TASK = 2;

    /**
     * The '<em><b>Ressource</b></em>' literal value.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #RESSOURCE_LITERAL
     * @model name="Ressource"
     * @generated
     * @ordered
     */
    public static final int RESSOURCE = 3;

    /**
     * The '<em><b>Goal</b></em>' literal object.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of '<em><b>Goal</b></em>' literal object isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @see #GOAL
     * @generated
     * @ordered
     */
    public static final IntentionalElementType GOAL_LITERAL = new IntentionalElementType(GOAL, "Goal");

    /**
     * The '<em><b>Softgoal</b></em>' literal object.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of '<em><b>Softgoal</b></em>' literal object isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @see #SOFTGOAL
     * @generated
     * @ordered
     */
    public static final IntentionalElementType SOFTGOAL_LITERAL = new IntentionalElementType(SOFTGOAL, "Softgoal");

    /**
     * The '<em><b>Task</b></em>' literal object.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of '<em><b>Task</b></em>' literal object isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @see #TASK
     * @generated
     * @ordered
     */
    public static final IntentionalElementType TASK_LITERAL = new IntentionalElementType(TASK, "Task");

    /**
     * The '<em><b>Ressource</b></em>' literal object.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of '<em><b>Ressource</b></em>' literal object isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @see #RESSOURCE
     * @generated
     * @ordered
     */
    public static final IntentionalElementType RESSOURCE_LITERAL = new IntentionalElementType(RESSOURCE, "Ressource");

    /**
     * An array of all the '<em><b>Intentional Element Type</b></em>' enumerators.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    private static final IntentionalElementType[] VALUES_ARRAY =
        new IntentionalElementType[] {
            GOAL_LITERAL,
            SOFTGOAL_LITERAL,
            TASK_LITERAL,
            RESSOURCE_LITERAL,
        };

    /**
     * A public read-only list of all the '<em><b>Intentional Element Type</b></em>' enumerators.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public static final List VALUES = Collections.unmodifiableList(Arrays.asList(VALUES_ARRAY));

    /**
     * Returns the '<em><b>Intentional Element Type</b></em>' literal with the specified name.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public static IntentionalElementType get(String name) {
        for (int i = 0; i < VALUES_ARRAY.length; ++i) {
            IntentionalElementType result = VALUES_ARRAY[i];
            if (result.toString().equals(name)) {
                return result;
            }
        }
        return null;
    }

    /**
     * Returns the '<em><b>Intentional Element Type</b></em>' literal with the specified value.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public static IntentionalElementType get(int value) {
        switch (value) {
            case GOAL: return GOAL_LITERAL;
            case SOFTGOAL: return SOFTGOAL_LITERAL;
            case TASK: return TASK_LITERAL;
            case RESSOURCE: return RESSOURCE_LITERAL;
        }
        return null;	
    }

    /**
     * Only this class can construct instances.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    private IntentionalElementType(int value, String name) {
        super(value, name);
    }

} //IntentionalElementType
