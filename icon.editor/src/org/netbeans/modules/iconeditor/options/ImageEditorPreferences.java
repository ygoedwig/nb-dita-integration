/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.iconeditor.options;

import java.util.prefs.Preferences;

/**
 *
 * @author matt
 */
public class ImageEditorPreferences {

    private static final Preferences PREFS = Preferences.userNodeForPackage(ImageEditorPreferences.class);
    private static final String SVG_EDITOR = "svg.editor";
    private static final String OTHER_EDITOR = "other.editor";

    public static String getSvgEditor() {
        return PREFS.get(SVG_EDITOR, null);
    }

    public static void setSvgEditor(String svgEditor) {
        PREFS.put(SVG_EDITOR, svgEditor);
    }

    public static String getOtherEditor() {
        return PREFS.get(OTHER_EDITOR, null);
    }

    public static void setOtherEditor(String otherEditor) {
        PREFS.put(OTHER_EDITOR, otherEditor);
    }
}
