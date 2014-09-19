/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.dita.options;

import java.io.File;
import java.util.prefs.Preferences;

/**
 *
 * @author u415714
 */
public class DitaPreferences {

    private static final Preferences PREFS = Preferences.userNodeForPackage(DitaPreferences.class);
    private static final String DITA_HOME = "dita.home";

    public static File getDitaHome() {
        File ditaHome = null;
        String ditaHomeDir = PREFS.get(DITA_HOME, null);
        if (ditaHomeDir != null) {
            ditaHome = new File(ditaHomeDir);
        }
        return ditaHome;
    }

    public static void setDitaHome(File ditaHome) {
        if (ditaHome.exists()) {
            PREFS.put(DITA_HOME, ditaHome.getAbsolutePath());
        }
    }
}
