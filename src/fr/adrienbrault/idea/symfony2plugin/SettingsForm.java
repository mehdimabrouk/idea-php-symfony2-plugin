package fr.adrienbrault.idea.symfony2plugin;

import com.intellij.ide.actions.ShowSettingsUtilImpl;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import fr.adrienbrault.idea.symfony2plugin.stubs.util.IndexUtil;
import fr.adrienbrault.idea.symfony2plugin.util.IdeHelper;
import fr.adrienbrault.idea.symfony2plugin.webDeployment.WebDeploymentUtil;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class SettingsForm implements Configurable {

    private Project project;

    private JPanel panel1;

    private JCheckBox symfonyContainerTypeProvider;
    private JCheckBox objectRepositoryTypeProvider;
    private JCheckBox objectRepositoryResultTypeProvider;
    private JCheckBox objectManagerFindTypeProvider;

    private JLabel typesLabel;
    private JCheckBox twigAnnotateRoute;
    private JCheckBox twigAnnotateTemplate;
    private JCheckBox twigAnnotateAsset;
    private JCheckBox twigAnnotateAssetTags;
    private JCheckBox phpAnnotateTemplate;
    private JCheckBox phpAnnotateRoute;
    private JCheckBox phpAnnotateTemplateAnnotation;
    private JCheckBox pluginEnabled;

    private JButton directoryToWebReset;
    private JLabel directoryToWebLabel;
    private TextFieldWithBrowseButton directoryToWeb;

    private JButton directoryToAppReset;
    private JLabel directoryToAppLabel;
    private TextFieldWithBrowseButton directoryToApp;
    private JButton pathToTranslationRootTextFieldReset;
    private TextFieldWithBrowseButton pathToTranslationRootTextField;
    private JButton buttonHelp;

    private JCheckBox codeFoldingPhpRoute;
    private JCheckBox codeFoldingPhpModel;
    private JCheckBox codeFoldingTwigRoute;
    private JCheckBox codeFoldingPhpTemplate;
    private JCheckBox codeFoldingTwigTemplate;
    private JCheckBox codeFoldingTwigConstant;

    private JButton buttonReindex;
    private JCheckBox enableSchedulerCheckBox;

    public SettingsForm(@NotNull final Project project) {
        this.project = project;
        buttonHelp.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                IdeHelper.openUrl(Symfony2ProjectComponent.HELP_URL);
            }
        });
    }

    @Nls
    @Override
    public String getDisplayName() {
        return "Symfony Plugin";
    }

    @Nullable
    @Override
    public String getHelpTopic() {
        return null;
    }

    public JComponent createComponent() {
        pathToTranslationRootTextField.getButton().addMouseListener(createPathButtonMouseListener(pathToTranslationRootTextField.getTextField(), FileChooserDescriptorFactory.createSingleFolderDescriptor()));
        pathToTranslationRootTextFieldReset.addMouseListener(createResetPathButtonMouseListener(pathToTranslationRootTextField.getTextField(), Settings.DEFAULT_TRANSLATION_PATH));

        directoryToApp.getButton().addMouseListener(createPathButtonMouseListener(directoryToApp.getTextField(), FileChooserDescriptorFactory.createSingleFolderDescriptor()));
        directoryToAppReset.addMouseListener(createResetPathButtonMouseListener(directoryToApp.getTextField(), Settings.DEFAULT_APP_DIRECTORY));

        directoryToWeb.getButton().addMouseListener(createPathButtonMouseListener(directoryToWeb.getTextField(), FileChooserDescriptorFactory.createSingleFolderDescriptor()));
        directoryToWebReset.addMouseListener(createResetPathButtonMouseListener(directoryToWeb.getTextField(), Settings.DEFAULT_WEB_DIRECTORY));

        enableSchedulerCheckBox.setEnabled(WebDeploymentUtil.isEnabled(project));

        buttonReindex.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                IndexUtil.forceReindex();
                super.mouseClicked(e);
            }
        });

        return panel1;
    }

    @Override
    public boolean isModified() {
        return
            !pluginEnabled.isSelected() == getSettings().pluginEnabled
            || !pathToTranslationRootTextField.getText().equals(getSettings().pathToTranslation)
            || !enableSchedulerCheckBox.isSelected() == getSettings().remoteDevFileScheduler

            || !symfonyContainerTypeProvider.isSelected() == getSettings().symfonyContainerTypeProvider
            || !objectRepositoryTypeProvider.isSelected() == getSettings().objectRepositoryTypeProvider
            || !objectRepositoryResultTypeProvider.isSelected() == getSettings().objectRepositoryResultTypeProvider
            || !objectManagerFindTypeProvider.isSelected() == getSettings().objectManagerFindTypeProvider

            || !twigAnnotateRoute.isSelected() == getSettings().twigAnnotateRoute
            || !twigAnnotateTemplate.isSelected() == getSettings().twigAnnotateTemplate
            || !twigAnnotateAsset.isSelected() == getSettings().twigAnnotateAsset
            || !twigAnnotateAssetTags.isSelected() == getSettings().twigAnnotateAssetTags

            || !phpAnnotateTemplate.isSelected() == getSettings().phpAnnotateTemplate
            || !phpAnnotateRoute.isSelected() == getSettings().phpAnnotateRoute
            || !phpAnnotateTemplateAnnotation.isSelected() == getSettings().phpAnnotateTemplateAnnotation

            || !codeFoldingPhpRoute.isSelected() == getSettings().codeFoldingPhpRoute
            || !codeFoldingPhpModel.isSelected() == getSettings().codeFoldingPhpModel
            || !codeFoldingPhpTemplate.isSelected() == getSettings().codeFoldingPhpTemplate
            || !codeFoldingTwigRoute.isSelected() == getSettings().codeFoldingTwigRoute
            || !codeFoldingTwigTemplate.isSelected() == getSettings().codeFoldingTwigTemplate
            || !codeFoldingTwigConstant.isSelected() == getSettings().codeFoldingTwigConstant

            || !directoryToApp.getText().equals(getSettings().directoryToApp)
            || !directoryToWeb.getText().equals(getSettings().directoryToWeb)
        ;
    }

    @Override
    public void apply() throws ConfigurationException {

        getSettings().pluginEnabled = pluginEnabled.isSelected();

        getSettings().pathToTranslation = pathToTranslationRootTextField.getText();
        getSettings().remoteDevFileScheduler = enableSchedulerCheckBox.isSelected();

        getSettings().symfonyContainerTypeProvider = symfonyContainerTypeProvider.isSelected();
        getSettings().objectRepositoryTypeProvider = objectRepositoryTypeProvider.isSelected();
        getSettings().objectRepositoryResultTypeProvider = objectRepositoryResultTypeProvider.isSelected();
        getSettings().objectManagerFindTypeProvider = objectManagerFindTypeProvider.isSelected();

        getSettings().twigAnnotateRoute = twigAnnotateRoute.isSelected();
        getSettings().twigAnnotateTemplate = twigAnnotateTemplate.isSelected();
        getSettings().twigAnnotateAsset = twigAnnotateAsset.isSelected();
        getSettings().twigAnnotateAssetTags = twigAnnotateAssetTags.isSelected();

        getSettings().phpAnnotateTemplate = phpAnnotateTemplate.isSelected();
        getSettings().phpAnnotateRoute = phpAnnotateRoute.isSelected();
        getSettings().phpAnnotateTemplateAnnotation = phpAnnotateTemplateAnnotation.isSelected();

        getSettings().codeFoldingPhpRoute = codeFoldingPhpRoute.isSelected();
        getSettings().codeFoldingPhpModel = codeFoldingPhpModel.isSelected();
        getSettings().codeFoldingPhpTemplate = codeFoldingPhpTemplate.isSelected();
        getSettings().codeFoldingTwigRoute = codeFoldingTwigRoute.isSelected();
        getSettings().codeFoldingTwigTemplate = codeFoldingTwigTemplate.isSelected();
        getSettings().codeFoldingTwigConstant = codeFoldingTwigConstant.isSelected();

        getSettings().directoryToApp = directoryToApp.getText();
        getSettings().directoryToWeb = directoryToWeb.getText();
    }

    @Override
    public void reset() {
        updateUIFromSettings();
    }

    @Override
    public void disposeUIResources() {
    }

    private Settings getSettings() {
        return Settings.getInstance(project);
    }

    private void updateUIFromSettings() {

        pluginEnabled.setSelected(getSettings().pluginEnabled);

        pathToTranslationRootTextField.setText(getSettings().pathToTranslation);
        enableSchedulerCheckBox.setSelected(getSettings().remoteDevFileScheduler);

        symfonyContainerTypeProvider.setSelected(getSettings().symfonyContainerTypeProvider);
        objectRepositoryTypeProvider.setSelected(getSettings().objectRepositoryTypeProvider);
        objectRepositoryResultTypeProvider.setSelected(getSettings().objectRepositoryResultTypeProvider);
        objectManagerFindTypeProvider.setSelected(getSettings().objectManagerFindTypeProvider);

        twigAnnotateRoute.setSelected(getSettings().twigAnnotateRoute);
        twigAnnotateTemplate.setSelected(getSettings().twigAnnotateTemplate);
        twigAnnotateAsset.setSelected(getSettings().twigAnnotateAsset);
        twigAnnotateAssetTags.setSelected(getSettings().twigAnnotateAssetTags);

        phpAnnotateTemplate.setSelected(getSettings().phpAnnotateTemplate);
        phpAnnotateRoute.setSelected(getSettings().phpAnnotateRoute);
        phpAnnotateTemplateAnnotation.setSelected(getSettings().phpAnnotateTemplateAnnotation);

        codeFoldingPhpRoute.setSelected(getSettings().codeFoldingPhpRoute);
        codeFoldingPhpModel.setSelected(getSettings().codeFoldingPhpModel);
        codeFoldingPhpTemplate.setSelected(getSettings().codeFoldingPhpTemplate);
        codeFoldingTwigRoute.setSelected(getSettings().codeFoldingTwigRoute);
        codeFoldingTwigTemplate.setSelected(getSettings().codeFoldingTwigTemplate);
        codeFoldingTwigConstant.setSelected(getSettings().codeFoldingTwigConstant);

        directoryToApp.setText(getSettings().directoryToApp);
        directoryToWeb.setText(getSettings().directoryToWeb);
    }

    private MouseListener createPathButtonMouseListener(final JTextField textField, final FileChooserDescriptor fileChooserDescriptor) {
        return new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
            }

            @Override
            public void mousePressed(MouseEvent mouseEvent) {
                VirtualFile projectDirectory = project.getBaseDir();
                VirtualFile selectedFile = FileChooser.chooseFile(
                    fileChooserDescriptor,
                    project,
                    VfsUtil.findRelativeFile(textField.getText(), projectDirectory)
                );

                if (null == selectedFile) {
                    return; // Ignore but keep the previous path
                }

                String path = VfsUtil.getRelativePath(selectedFile, projectDirectory, '/');
                if (null == path) {
                    path = selectedFile.getPath();
                }

                textField.setText(path);
            }

            @Override
            public void mouseReleased(MouseEvent mouseEvent) {
            }

            @Override
            public void mouseEntered(MouseEvent mouseEvent) {
            }

            @Override
            public void mouseExited(MouseEvent mouseEvent) {
            }
        };
    }

    private MouseListener createResetPathButtonMouseListener(final JTextField textField, final String defaultValue) {
        return new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
            }

            @Override
            public void mousePressed(MouseEvent mouseEvent) {
                textField.setText(defaultValue);
            }

            @Override
            public void mouseReleased(MouseEvent mouseEvent) {
            }

            @Override
            public void mouseEntered(MouseEvent mouseEvent) {
            }

            @Override
            public void mouseExited(MouseEvent mouseEvent) {
            }
        };
    }

    public static void show(@NotNull Project project) {
        ShowSettingsUtilImpl.showSettingsDialog(project, "Symfony2.SettingsForm", null);
    }

}
