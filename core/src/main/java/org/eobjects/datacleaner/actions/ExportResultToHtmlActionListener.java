/**
 * eobjects.org DataCleaner
 * Copyright (C) 2010 eobjects.org
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, write to:
 * Free Software Foundation, Inc.
 * 51 Franklin Street, Fifth Floor
 * Boston, MA  02110-1301  USA
 */
package org.eobjects.datacleaner.actions;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.io.Writer;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.analyzer.result.AnalysisResult;
import org.eobjects.analyzer.result.html.HtmlAnalysisResultWriter;
import org.eobjects.datacleaner.user.UserPreferences;
import org.eobjects.datacleaner.util.FileFilters;
import org.eobjects.datacleaner.util.WidgetUtils;
import org.eobjects.datacleaner.widgets.DCFileChooser;
import org.eobjects.metamodel.util.FileHelper;
import org.eobjects.metamodel.util.Ref;

/**
 * Action listener used to fire an export of an analysis result
 */
public class ExportResultToHtmlActionListener implements ActionListener {

    private final Ref<AnalysisResult> _result;
    private final UserPreferences _userPreferences;
    private final AnalyzerBeansConfiguration _configuration;

    public ExportResultToHtmlActionListener(Ref<AnalysisResult> result, AnalyzerBeansConfiguration configuration,
            UserPreferences userPreferences) {
        _result = result;
        _configuration = configuration;
        _userPreferences = userPreferences;
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        final AnalysisResult analysisResult = _result.get();
        if (analysisResult == null) {
            WidgetUtils.showErrorMessage("Result not ready",
                    "Please wait for the job to finish before saving the result", null);
            return;
        }

        final DCFileChooser fileChooser = new DCFileChooser(_userPreferences.getAnalysisJobDirectory());
        fileChooser.setFileFilter(FileFilters.HTML);

        final Component parent;
        if (event.getSource() instanceof Component) {
            parent = (Component) event.getSource();
        } else {
            parent = null;
        }

        final int result = fileChooser.showSaveDialog(parent);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();

            if (!file.getName().endsWith(FileFilters.HTML.getExtension())) {
                file = new File(file.getParentFile(), file.getName() + FileFilters.HTML.getExtension());
            }

            if (file.exists()) {
                int overwrite = JOptionPane.showConfirmDialog(parent, "Are you sure you want to overwrite the file '"
                        + file.getName() + "'?", "Overwrite existing file?", JOptionPane.YES_NO_OPTION);
                if (overwrite != JOptionPane.YES_OPTION) {
                    return;
                }
            }

            HtmlAnalysisResultWriter resultWriter = new HtmlAnalysisResultWriter();
            Writer writer = FileHelper.getBufferedWriter(file);
            try {
                resultWriter.write(analysisResult, _configuration, writer);
            } catch (IOException e) {
                WidgetUtils.showErrorMessage("Error writing result to HTML page", e);
            } finally {
                FileHelper.safeClose(writer);
            }
        }
    }

}
