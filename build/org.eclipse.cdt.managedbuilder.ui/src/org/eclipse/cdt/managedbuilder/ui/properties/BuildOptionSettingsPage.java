package org.eclipse.cdt.managedbuilder.ui.properties;

import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.IOptionCategory;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.graphics.Point;

public class BuildOptionSettingsPage extends BuildSettingsPage {

	private IOptionCategory category;
	
	BuildOptionSettingsPage(IConfiguration configuration, IOptionCategory category) {
		// Cache the configuration and option category this page is created for
		super(configuration);
		this.category = category;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#computeSize()
	 */
	public Point computeSize() {
		return super.computeSize();
	}
	
	/**
	 * @see org.eclipse.jface.preference.FieldEditorPreferencePage#createFieldEditors()
	 */
	protected void createFieldEditors() {
		// Get the preference store for the build settings
		super.createFieldEditors();

		// Iterate over the options in the category and create a field editor for each
		IOption[] options = category.getOptions(configuration);
		for (int index = 0; index < options.length; ++index) {
			// Get the option
			IOption opt = options[index];
			// Figure out which type the option is and add a proper field editor for it
			switch (opt.getValueType()) {
				case IOption.STRING :
					StringFieldEditor stringField = new StringFieldEditor(opt.getId(), opt.getName(), getFieldEditorParent());
					addField(stringField);
					break;
				case IOption.BOOLEAN :
					BooleanFieldEditor booleanField = new BooleanFieldEditor(opt.getId(), opt.getName(), getFieldEditorParent());
					addField(booleanField);
					break;
				case IOption.ENUMERATED :
					String sel;
					try {
						sel = opt.getSelectedEnum();
					} catch (BuildException e) {
						// If we get this exception, then the option type is wrong
						break;
					}
					BuildOptionComboFieldEditor comboField = new BuildOptionComboFieldEditor(opt.getId(), opt.getName(), opt.getApplicableValues(), sel, getFieldEditorParent());
					addField(comboField); 
					break;
				case IOption.STRING_LIST :
				case IOption.INCLUDE_PATH :
				case IOption.PREPROCESSOR_SYMBOLS :
				case IOption.LIBRARIES :
				case IOption.OBJECTS:
					BuildOptionListFieldEditor listField = new BuildOptionListFieldEditor(opt.getId(), opt.getName(), getFieldEditorParent());
					addField(listField); 
					break;
				default :
					SummaryFieldEditor summaryField = new SummaryFieldEditor(opt.getId(), opt.getName(), category.getTool(), getFieldEditorParent());
					addField(summaryField);
					break;
//				default :
//					break;
			}
		}
	}

	/**
	 * @param category
	 * @return
	 */
	public boolean isForCategory(IOptionCategory category) {
		if (category != null) {
			return category.equals(this.category);
		}
		return false;
	}

	/**
	 * @see IPreferencePage#performOk()
	 */
	public boolean performOk() {
		// Write the field editor contents out to the preference store
		boolean ok = super.performOk();

		// Write the preference store values back to the build model
		IOption[] options = category.getOptions(configuration);
		for (int i = 0; i < options.length; i++) {
			IOption option = options[i];

			// Transfer value from preference store to options
			switch (option.getValueType()) {
				case IOption.BOOLEAN :
					boolean boolVal = getPreferenceStore().getBoolean(option.getId());
					ManagedBuildManager.setOption(configuration, option, boolVal);
					break;
				case IOption.ENUMERATED :
					String enumVal = getPreferenceStore().getString(option.getId());
					ManagedBuildManager.setOption(configuration, option, enumVal);
					break;
				case IOption.STRING :
					String strVal = getPreferenceStore().getString(option.getId());
					ManagedBuildManager.setOption(configuration, option, strVal);
					break;
				case IOption.STRING_LIST :
				case IOption.INCLUDE_PATH :
				case IOption.PREPROCESSOR_SYMBOLS :
				case IOption.LIBRARIES :
				case IOption.OBJECTS:
					String listStr = getPreferenceStore().getString(option.getId());
					String[] listVal = BuildToolsSettingsStore.parseString(listStr);
					ManagedBuildManager.setOption(configuration, option, listVal);
					break;
				default :
					break;
			}			
		}
		
		return ok;
	}

}
