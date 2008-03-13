/**
 * 
 */
package seg.jUCMNav.views.preferences.staticSemantic;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import seg.jUCMNav.staticSemantic.Rule;
import seg.jUCMNav.staticSemantic.RuleGroup;
import seg.jUCMNav.staticSemantic.StaticSemanticDefMgr;

/**
 * This class provides an Eclipse preference page for the static checking settings. The settings include:
 * <ul>
 * <li>The switch of showing the rule secription on the problem view when reporting the checking result.
 * <il>Enable/disable a rule or rules by individual rule or groups
 * <li>Open a rule group creating/editing dialog
 * <li>Open a rule cretaing/editing dialog 
 * <li>Load default settings
 * <li>Save all setting to the preference store
 * </ul>
 * 
 * @author Byrne Yan
 * 
 */
public class StaticSemanticPreferencePage extends PreferencePage implements IWorkbenchPreferencePage, SelectionListener {
    private static final String APPDATA_UTILITIES_NUMBER = "UtilitiesNumber";
    private static final String APPDATA_CHECKBOX = "CheckBox";
    private static final String APPDATA_UTILITY = "Utility";
    private static final String BUTTON_EDIT_THE_SELECTED_RULE = "Edit";
    private static final String BUTTON_DEFINE_A_NEW_RULE = "New Rule";
    private static final String BUTTON_DEFINE_A_NEW_GROUP = "New Group";
    private static final String BUTTON_DELETE = "Delete";
    private static final String BUTTON_EXPORT = "Export";
    private static final String BUTTON_IMPORT = "Import";

    private static final int BTN_COLUMN_CHECK = 0;
    private static final int TBL_COLUMN_NAME = 1;
    private static final int TBL_COLUMN_DESCRIPTION = 5;
    private static final int TBL_COLUMN_CONTEXT = 3;
    private static final int TBL_COLUMN_CLASSIFIER = 2;
    private static final int TBL_COLUMN_CONSTRAINT = 4;
    private static final int TBL_COLUMN_UTILITY = 6;

    /**
     * The tree represnts rule groups and rules in the crresponding group
     */
    private Tree tree;
    private Button btnNewRule;
    private Button btnNewGroup;
    private Button btnEdit;

    private Button btnExport;
    private Button btnImport;

    private Button btnShowDescription;
    private Button btnDelete;

    /*
     * Create all GUI components
     * 
     * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
     */
    protected Control createContents(Composite parent) {

        Composite head = new Composite(parent, SWT.NULL);
        GridLayout headLayout = new GridLayout();
        headLayout.numColumns = 1;
        head.setLayout(headLayout);
        head.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL));
        btnShowDescription = new Button(head, SWT.CHECK);
        btnShowDescription.setText("Show rule description in the rule violation reporting");

        Label label1 = new Label(parent, SWT.LEFT);
        label1.setText("Rules defined:");

        tree = new Tree(parent, SWT.BORDER | SWT.CHECK | SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI | SWT.FULL_SELECTION);
        tree.setHeaderVisible(true);
        tree.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
                if (event.detail == SWT.CHECK) {
                    TreeItem item = (TreeItem) event.item;
                    Object data = item.getData();
                    boolean checked = item.getChecked();
                    if (data instanceof Rule) {
                        checkSameRules(item, new Rule[] { (Rule) data }, checked);
                        checkGroups();
                    } else if (data instanceof RuleGroup) {
                        RuleGroup group = (RuleGroup) data;
                        checkRules(item, checked);
                        Rule[] rules = new Rule[group.getRules().size()];
                        for (int i = 0; i < rules.length; ++i) {
                            rules[i] = (Rule) group.getRules().get(i);
                        }
                        checkSameRules(null, rules, checked);
                        checkGroups();
                    }
                }
            }
        });

        String[] titles = { "Name", "Context", "Query Expression", "Constraint Expression", "Descreption", "Utilities" };
        int[] widths = { 150, 50, 50, 50, 50, 50 };
        for (int i = 0; i < titles.length; i++) {
            TreeColumn column = new TreeColumn(tree, SWT.LEFT);
            column.setText(titles[i]);
            column.setWidth(widths[i]);
        }
        tree.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL | GridData.VERTICAL_ALIGN_FILL | GridData.GRAB_VERTICAL));

        Composite c = new Composite(parent, SWT.NULL);
        GridLayout layout = new GridLayout();
        layout.numColumns = 6;
        c.setLayout(layout);

        c.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL));

        btnNewRule = new Button(c, SWT.PUSH);
        btnNewRule.setText(BUTTON_DEFINE_A_NEW_RULE);
        btnNewRule.addSelectionListener(this);

        btnNewRule = new Button(c, SWT.PUSH);
        btnNewRule.setText(BUTTON_DEFINE_A_NEW_GROUP);
        btnNewRule.addSelectionListener(this);

        btnEdit = new Button(c, SWT.PUSH);
        btnEdit.setText(BUTTON_EDIT_THE_SELECTED_RULE);
        btnEdit.addSelectionListener(this);

        btnDelete = new Button(c, SWT.PUSH);
        btnDelete.setText(BUTTON_DELETE);
        btnDelete.addSelectionListener(this);

        btnImport = new Button(c, SWT.PUSH);
        btnImport.setText(BUTTON_IMPORT);
        btnImport.addSelectionListener(this);

        btnExport = new Button(c, SWT.PUSH);
        btnExport.setText(BUTTON_EXPORT);
        btnExport.addSelectionListener(this);

        initializeValues();
        return null;
    }

    /**
     * Put a check/uncheck on all sub rule items of the specified group item
     * @param item  the spcified tree item on which a check/uncheck will be put
     * @param checked   true if a check is  put, false if not.
     */
    protected void checkRules(TreeItem item, boolean checked) {
        TreeItem[] ruleItems = item.getItems();
        for (int i = 0; i < ruleItems.length; ++i) {
            Rule r = (Rule) ruleItems[i].getData();
            r.setEnabled(checked);
            ruleItems[i].setChecked(checked);
            ruleItems[i].setGrayed(false);
        }

    }

    /**
     * Put a check mark on all groups based on their rules.If all rules of a group are checked, the group is put on checked mark. If only some of rules of a group are checked, the group is put on a greyed mark. If no rules of a group is checked, the group is put on an unchecked mark.
     */
    protected void checkGroups() {
        TreeItem[] groupItems = tree.getItems();
        for (int i = 0; i < groupItems.length; ++i) {
            boolean checked = groupItems[i].getChecked();
            boolean grayed = groupItems[i].getGrayed();
            TreeItem[] ruleItems = groupItems[i].getItems();
            if (ruleItems.length == 1) {
                checked = ruleItems[0].getChecked();
                grayed = false;
            } else if (ruleItems.length > 1) {
                checked = ruleItems[0].getChecked();
                grayed = false;
                for (int j = 1; j < ruleItems.length; ++j) {
                    if (checked != ruleItems[j].getChecked()) {
                        checked = grayed = true;
                        break;
                    }
                }
            }
            groupItems[i].setChecked(checked);
            groupItems[i].setGrayed(grayed);
        }

    }

    /**
     * Put an checked or unchecked mark on all rule items that point to the specified same rules. No change to any group items
     * @param item  the tree item that will not be considered during the processing.
     * @param rules the specified rules. Any rule item that point to one of these rules will be put on a specified mark.
     * @param checked true if an checked mark will be put on, otherwise false.
     */
    protected void checkSameRules(TreeItem item, Rule[] rules, boolean checked) {
        TreeItem[] groupItems = tree.getItems();
        for (int i = 0; i < groupItems.length; ++i) {
            TreeItem[] ruleItems = groupItems[i].getItems();
            for (int j = 0; j < ruleItems.length; ++j) {
                Rule rule = (Rule) ruleItems[j].getData();
                for (int k = 0; k < rules.length; ++k) {
                    if (rule == rules[k]) {
                        ruleItems[j].setGrayed(false);
                        ruleItems[j].setChecked(checked);
                        Rule r = (Rule) ruleItems[j].getData();
                        r.setEnabled(checked);
                        break;
                    }
                }
            }
        }

    }

    /**
     * Fill contents of all GUI componets
     */
    private void initializeValues() {
        StaticSemanticDefMgr.instance().load();
        btnShowDescription.setSelection(StaticSemanticDefMgr.instance().isShowDesc());
        populateTree();
        tree.pack();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
     */
    public void init(IWorkbench workbench) {
        // do nothing

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.swt.events.SelectionListener#widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent)
     */
    public void widgetDefaultSelected(SelectionEvent e) {
        // do nothing
    }

    /*
     * Buttons click event dispacher
     * 
     * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
     */
    public void widgetSelected(SelectionEvent e) {
        if (e.getSource() instanceof Button) {
            Button btn = (Button) e.getSource();

            if (btn.getText().compareTo(BUTTON_DEFINE_A_NEW_RULE) == 0) {
                performNewRule();
            } else if (btn.getText().compareTo(BUTTON_DEFINE_A_NEW_GROUP) == 0) {
                performNewGroup();
            } else if (btn.getText().compareTo(BUTTON_EDIT_THE_SELECTED_RULE) == 0) {
                performEdit();
            } else if (btn.getText().compareTo(BUTTON_DELETE) == 0) {
                performDelete();
            } else if (btn.getText().compareTo(BUTTON_IMPORT) == 0) {
                performImport();
            } else if (btn.getText().compareTo(BUTTON_EXPORT) == 0) {
                performExport();
            }
        }
    }

    /**
     * Remove the selected rule group or rule item. Notice: the group of "All" can not be removed.
     */
    private void performDelete() {
        TreeItem[] items = tree.getSelection();
        for (int i = 0; i < items.length; ++i) {
            Object data = items[i].getData();
            if (data instanceof RuleGroup) {
                // Only empty group can be deleted
                // The group ALl can not be deleted
                RuleGroup g = (RuleGroup) data;
                if (g.getRules().size() == 0 && g.getName().compareTo("All") != 0) {
                    StaticSemanticDefMgr.instance().removeGroup(g);
                }
            } else if (data instanceof Rule) {
                Rule r = (Rule) data;
                StaticSemanticDefMgr.instance().removeRule(r);
            }
        }
        updateTree();
    }

    /**
     * Open a dialog to create a new group and put the created group into the tree.
     */
    private void performNewGroup() {
        GroupEditDialog dlg = new GroupEditDialog(getControl().getShell());
        if (Window.OK == dlg.open()) {
            StaticSemanticDefMgr.instance().addGroup(dlg.getGroup());
            updateTree();
        }
    }

    /**
     * Open a dialog ro create a new rule and update the group items that contains this rule(A rule is always in the group of "All").
     */
    private void performNewRule() {
        RuleEditDialog dlg = new RuleEditDialog(getControl().getShell());

        if (Window.OK == dlg.open()) {
            StaticSemanticDefMgr.instance().addRule(dlg.getRule());
            updateTree();
        }
    }

    /**
     * Export selected rules into a file that the user specifies in the file dialog.Notice: only rule items in the selected items are exported. group items selected are ignored.
     */
    private void performExport() {

        TreeItem[] items = tree.getSelection();
        if (items.length > 0) {
            FileDialog dlg = new FileDialog(getControl().getShell(), SWT.SAVE);
            String file = dlg.open();
            if (file != null) {
                List rules = new ArrayList();

                for (int i = 0; i < items.length; ++i) {
                    Object data = items[i].getData();
                    if (data instanceof Rule) {
                        rules.add(data);
                    }
                }
                StaticSemanticDefMgr.exportRules(rules, file);
            }
        }

    }

    /**
     * Import rules in the file that the user specifies in the file dialog.
     */
    private void performImport() {

        FileDialog dlg = new FileDialog(getControl().getShell(), SWT.OPEN);
        String file = dlg.open();
        if (file != null) {
            // save modification before import new rules
            performOk();

            try {
                StaticSemanticDefMgr.instance().importRules(file, getControl().getShell());
                updateTree();
            } catch (FileNotFoundException e) {
                MessageBox msg = new MessageBox(getControl().getShell(), SWT.ICON_ERROR);
                msg.setMessage(e.getMessage());
                msg.setText("Failure to import rules");
                msg.open();
            }
        }
    }

    /**
     * Edit the selected rule or rule group. If more than one item is selected, a warning message dialog is showed.
     */
    private void performEdit() {
        // Check if only one item selected
        if (tree.getSelectionCount() != 1) {
            MessageBox msg = new MessageBox(getControl().getShell(), SWT.ICON_WARNING);
            msg.setMessage("Please select one and only one rule to edit");
            msg.setText("Rule selection");
            msg.open();
            return;
        }
        TreeItem item = tree.getSelection()[0];

        Object o = item.getData();
        if (o instanceof Rule) {
            editRule((Rule) o);
        } else if (o instanceof RuleGroup) {
            editRuleGroup((RuleGroup) o);
            updateGroupNode(item);
            checkGroups();
        }

    }

    /**
     * Update a rule group items due to the change of members of the group. The enable/disabled information will not be changed even it is not saved.
     * @param item
     */
    private void updateGroupNode(TreeItem item) {
        RuleGroup group = (RuleGroup) item.getData();

        // Save check information before remove items
        HashMap checkInfo = saveCheckInfo(item);

        item.removeAll();
        populateGroupNode(item, group);
        // Restore check states
        restoreCheckState(item, checkInfo);

    }

    /**
     * Resore all enabled/dsiabled information on the subitems of the specified item.
     * @param item  the specified item
     * @param checkInfo the HashMap object that holds all enabled/disabled information
     */
    private void restoreCheckState(TreeItem item, HashMap checkInfo) {
        TreeItem[] items = item.getItems();
        for (int i = 0; i < items.length; ++i) {
            Boolean checked = (Boolean) checkInfo.get(items[i].getData());
            if (checked != null) {
                items[i].setChecked(checked.booleanValue());
            }
        }

    }

    /**
     * Save enabled/disabled information of all subitems of the specified item to the specied HashMap.
     * @param item  the specified tree item
     * @return  HashMap the HashMap object that holds all enabled/disabled information
     */
    private HashMap saveCheckInfo(TreeItem item) {
        HashMap info = new HashMap();
        TreeItem[] items = item.getItems();
        for (int i = 0; i < items.length; ++i) {
            info.put(items[i].getData(), new Boolean(items[i].getChecked()));
        }
        return info;
    }

    /**
     * Open a dialog to edit a rule.
     * @param r the rule object to be edited
     */
    private void editRule(Rule r) {
        RuleEditDialog dlg = new RuleEditDialog(getControl().getShell());

        dlg.setRule(r);

        dlg.open();
    }

    /**
     * Update the entire tree based on the system groups and rules.
     */
    private void updateTree() {
        tree.removeAll();
        populateTree();

    }

    /**
     * Open a dialog to edit a specified rule group. Notice: the group of "All" can not be edited.
     * @param g the rule group to be edited
     */
    private void editRuleGroup(RuleGroup g) {
        if (g.getName().compareTo("All") == 0) {
            MessageBox msg = new MessageBox(getControl().getShell(), SWT.ICON_WARNING);
            msg.setMessage("The special group [All] can not be edited.");
            msg.setText("Warning");
            msg.open();
            return;
        }
        GroupEditDialog dlg = new GroupEditDialog(getControl().getShell());
        dlg.setGroup(g);
        dlg.open();

    }

    /**
     * Set all dafault values of settings
     * 
     */
    protected void performDefaults() {
        super.performDefaults();

        StaticSemanticDefMgr.instance().loadDefault();

        btnShowDescription.setSelection(StaticSemanticDefMgr.instance().isShowDesc());
        updateTree();

    }

    /**
     * Construct the entire tree based on the system groups and rules.
     */
    private void populateTree() {
        List groups = StaticSemanticDefMgr.instance().getGroups();
        for (int i = 0; i < groups.size(); ++i) {
            RuleGroup g = (RuleGroup) groups.get(i);
            TreeItem item = new TreeItem(tree, SWT.NONE);
            populateGroupNode(item, g);
        }
        checkGroups();
    }

    /**
     * Construc subitens of the specified rule group tree item based on the specified rule group object.
     * @param item  the specified rule group tree item
     * @param g the specified rule group object
     */
    private void populateGroupNode(TreeItem item, RuleGroup g) {
        item.setText(new String[] { g.getName(), "", "", "", "" });
        item.setData(g);
        item.setExpanded(true);
        List rules = g.getRules();
        for (int j = 0; j < rules.size(); j++) {
            Rule r = (Rule) rules.get(j);
            TreeItem subItem = new TreeItem(item, SWT.NONE);
            subItem.setText(new String[] { r.getName(), r.getClassifier(), r.getContext(), r.getQuery(), r.getDescription() });
            subItem.setData(r);
            subItem.setChecked(r.isEnabled());
        }
    }

    /**
     * Close the preference page and save all settings
     */
    public boolean performOk() {
        StaticSemanticDefMgr.instance().save();
        return true;
    }

    /**
     * Close the preference page and discard all changes of settings
     */
    public boolean performCancel() {
        StaticSemanticDefMgr.instance().load();
        return super.performCancel();
    }

}
