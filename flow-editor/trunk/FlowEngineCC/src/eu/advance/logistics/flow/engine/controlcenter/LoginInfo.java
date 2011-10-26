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

import java.io.File;
import java.util.Date;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 *
 * @author TTS
 */
public class LoginInfo {

    public String address;
    public Date lastLogin;
    public String username;
    public String password;

    static void read(File file, List<LoginInfo> data) throws Exception {
        final DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        final Document doc = docBuilder.parse(file);
        final Element root = doc.getDocumentElement();
        if ("1.0".equals(root.getAttribute("version"))) {
            String str;
            NodeList itemNodeList = root.getElementsByTagName("item");
            for (int i = 0, n = itemNodeList.getLength(); i < n; i++) {
                Element e = (Element) itemNodeList.item(i);
                LoginInfo info = new LoginInfo();
                info.address = e.getAttribute("address");
                info.username = e.getAttribute("username");
                info.password = e.getAttribute("password");
                if ((str = e.getAttribute("lastLogin")) != null) {
                    long time = Long.parseLong(str);
                    if (time != 0) {
                        info.lastLogin = new Date(time);
                    }
                }
                data.add(info);
            }
        }
    }

    static void save(File file, List<LoginInfo> data) throws Exception {
        final DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        final Document doc = docBuilder.newDocument();
        final Element root = doc.createElement("login-info");
        root.setAttribute("version", "1.0");
        for (LoginInfo info : data) {
            Element e = doc.createElement("item");
            e.setAttribute("address", info.address);
            if (info.username != null) {
                e.setAttribute("username", info.username);
            }
            if (info.password != null) {
                e.setAttribute("password", info.password);
            }
            if (info.lastLogin != null) {
                e.setAttribute("lastLogin", Long.toString(info.lastLogin.getTime()));
            }
            root.appendChild(e);
        }
        doc.appendChild(root);
        final Transformer xformer = TransformerFactory.newInstance().newTransformer();
        xformer.transform(new DOMSource(doc), new StreamResult(file));
    }
}
