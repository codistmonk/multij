/*
 *  The MIT License
 * 
 *  Copyright 2010 Codist Monk.
 * 
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 * 
 *  The above copyright notice and this permission notice shall be included in
 *  all copies or substantial portions of the Software.
 * 
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *  THE SOFTWARE.
 */

package net.sourceforge.aprog.subtitlesadjuster;

import static net.sourceforge.aprog.i18n.Messages.*;
import static net.sourceforge.aprog.subtitlesadjuster.Components.*;
import static net.sourceforge.aprog.subtitlesadjuster.Constants.*;
import static net.sourceforge.aprog.subtitlesadjuster.Constants.Variables.*;

import java.awt.Component;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

import net.sourceforge.aprog.context.Context;
import net.sourceforge.aprog.tools.Tools;

/**
 *
 * @author codistmonk (creation 2010-06-27)
 */
public final class Actions {

    /**
     * Private default constructor to prevent instantiation.
     */
    private Actions() {
        // Do nothing
    }

    /**
     *
     * @param context
     * <br>Not null
     */
    public static final void showAboutDialog(final Context context) {
        JOptionPane.showMessageDialog(
                (Component) context.get(MAIN_FRAME),
                APPLICATION_NAME + "\n" + APPLICATION_VERSION + "\n" + APPLICATION_COPYRIGHT,
                translate("About $0", APPLICATION_NAME),
                JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     *
     * @param context
     * <br>Not null
     */
    public static final void showPreferencesDialog(final Context context) {
        createPreferencesDialog(context).setVisible(true);
    }

    /**
     *
     * @param context
     * <br>Not null
     */
    public static final void quit(final Context context) {
        System.exit(0);
    }

    /**
     *
     * @param context
     * <br>Not null
     * <br>Input-output
     */
    public static final void open(final Context context) {
        final JFileChooser fileChooser = new JFileChooser();

        fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        fileChooser.setMultiSelectionEnabled(false);
        fileChooser.setFileFilter(new FileFilter() {

            @Override
            public final boolean accept(final File file) {
                return file.getName().endsWith(".srt");
            }

            @Override
            public final String getDescription() {
                return translate("Subtitles file $0", "(*.srt)");
            }

        });

        if (JFileChooser.APPROVE_OPTION == fileChooser.showOpenDialog((Component) context.get(MAIN_FRAME)) &&
                fileChooser.getSelectedFile() != null) {
            context.set(FILE, fileChooser.getSelectedFile());
        }
    }

    /**
     *
     * @param context
     * <br>Not null
     * <br>Input-output
     */
    public static final void save(final Context context) {
        showTODOMessage(context);
    }

    /**
     *
     * @param context
     * <br>Not null
     */
    public static final void showManual(final Context context) {
        showTODOMessage(context);
    }

    /**
     *
     * @param context
     * <br>Not null
     */
    public static final void showTODOMessage(final Context context) {
        System.out.println(Tools.debug(3, "TODO"));
        JOptionPane.showMessageDialog(
                (Component) context.get(MAIN_FRAME),
                "Not implemented",
                APPLICATION_NAME,
                JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     *
     * @param context
     * <br>Not null
     */
    public static final void updateMainFrameTitle(final Context context) {
        ((JFrame) context.get(MAIN_FRAME)).setTitle(createMainFrameTitle(context));
    }

    /**
     *
     * @param context
     * <br>Not null
     * @return
     * <br>Not null
     */
    private static final String createMainFrameTitle(final Context context) {
        final File file = context.get(FILE);
        final Boolean fileModified = context.get(FILE_MODIFIED);

        return file == null ? APPLICATION_NAME : file.getName() + (fileModified ? "*" : "");
    }

}