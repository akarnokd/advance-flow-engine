/*
 * Copyright 2010-2012 The Advance EU 7th Framework project consortium
 *
 * This file is part of Advance.
 *
 * Advance is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 *
 * Advance is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Advance.  If not, see
 * <http://www.gnu.org/licenses/>.
 *
 */
package eu.advance.logistics.flow.engine.controlcenter;

import com.google.common.collect.Lists;
import com.google.common.eventbus.EventBus;
import com.google.common.io.Files;
import eu.advance.logistics.flow.engine.api.AdvanceEngineControl;
import eu.advance.logistics.flow.engine.api.AdvanceHttpAuthentication;
import eu.advance.logistics.flow.engine.api.AdvanceLoginType;
import eu.advance.logistics.flow.engine.api.impl.HttpRemoteEngineControl;
import eu.advance.logistics.flow.engine.test.BasicLocalEngine;
import eu.advance.logistics.flow.engine.util.KeystoreManager;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.List;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.Exceptions;

/**
 *
 * @author TTS
 */
public class EngineController {

    private List<LoginInfo> loginData = Lists.newArrayList();
    private AdvanceEngineControl engine;
    private String engineAddress;
    private String engineVersion;
    private EventBus eventBus = new EventBus();

    private EngineController() {
    }

    public EventBus getEventBus() {
        return eventBus;
    }

    private void reset() {
        engine = null;
        engineAddress = "(none)";
        engineVersion = "N/A";
    }

    /**
     * Gets a <b>copy</b> of login info.
     * @return a copy of the list of login info
     */
    public List<LoginInfo> getLoginInfo() {
        return Lists.newArrayList(loginData);
    }

    public void setLoginInfo(Collection<LoginInfo> data) {
        loginData = Lists.newArrayList(data);
    }

    public void loadLoginInfo() {
        loginData.clear();

        try {
            File file = getLoginInfoFile();
            if (file.exists()) {
                LoginInfo.read(file, loginData);
            }
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }

        if (loginData.isEmpty()) {
            try {
                LoginInfo info = new LoginInfo();
                info.address = getLocalEngineDir().toURI().toString();
                info.lastLogin = null;
                info.username = "admin";
                info.password = "admin";
                loginData.add(info);
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    public void saveLoginInfo() {
        try {
            LoginInfo.save(getLoginInfoFile(), loginData);
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    public AdvanceEngineControl getEngine() {
        return engine;
    }

    public String getEngineAddress() {
        return engineAddress;
    }

    public String getEngineVersion() {
        return engineVersion;
    }

    public boolean login(String address, String username, char[] password, String serverCert) {
        reset();
        try {
            if (address.startsWith("file:")) {
                return loginLocal(username);
            } else {
                return login(new URL(address), username, password, serverCert);
            }
        } catch (MalformedURLException ex) {
            Exceptions.printStackTrace(ex);
        }
        return false;
    }

    private boolean loginLocal(String username) {
        try {
            File dir = checkLocalEngine();
            engine = BasicLocalEngine.create(username, dir.getCanonicalPath());
            engineAddress = dir.toString();
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
        return (engine != null);
    }

    public boolean login(URL address, String username, char[] password, String serverCert) {
        reset();
        try {
            if (address.getProtocol().equals("file")) {
                return loginLocal(username);
            } else {
 		AdvanceHttpAuthentication auth = new AdvanceHttpAuthentication();

 		auth.name = username;
		auth.password(password);
		auth.loginType = AdvanceLoginType.BASIC;
		
                if (serverCert != null) {
                    KeystoreManager mgr = new KeystoreManager();
                    mgr.create();
                    FileInputStream in = new FileInputStream(serverCert);
                    try {
                            mgr.importCertificate(address.getHost(), in);
                    } finally {
                            in.close();
                    }

                    auth.certStore = mgr.getKeyStore();
                }
		
		engine = new HttpRemoteEngineControl(address, auth);
                
                engineAddress = address.toString();
                engineVersion = engine.queryVersion().toString();
            }
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
        return (engine != null);
    }

    static File checkLocalEngine() throws IOException {
        File dir = getLocalEngineDir();
        if (!dir.exists()) {
            File source = InstalledFileLocator.getDefault().locate("LocalEngine", "eu.advance.logistics.core", false);  // NOI18N
            if (source != null) {
                copy(source, dir);
            } else {
                // something wrong!
            }
        }
        return dir;
    }

    static File getLocalEngineDir() throws IOException {
        return new File(getWorkspaceDir(), "LocalEngine");
    }

    private static void copy(File source, File dest) throws IOException {
        if (source.isDirectory()) {
            dest.mkdir();
            for (File f : source.listFiles()) {
                copy(f, new File(dest, f.getName()));
            }
        } else if (source.isFile()) {
            Files.copy(source, dest);
        }
    }

    static File getLoginInfoFile() {
        return new File(getWorkspaceDir(), "login-info.xml");
    }

    static File getWorkspaceDir() {
        final String userHome = System.getProperty("user.home");
        final File workspace = new File(userHome, ".advance-flow-editor-ws");

        if (!workspace.exists()) {
            workspace.mkdir();
        }

        return workspace;
    }
    private static EngineController INSTANCE = new EngineController();

    public static EngineController getInstance() {
        return INSTANCE;
    }
}
