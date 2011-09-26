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
package eu.advance.logistics.flow.editor.actions;

import eu.advance.logistics.flow.editor.model.ConstantBlock;
import eu.advance.logistics.flow.model.AdvanceConstantBlock;
import eu.advance.logistics.xml.typesystem.XElement;
import java.awt.event.ActionEvent;
import java.io.StringReader;
import javax.swing.AbstractAction;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author TTS
 */
public class ConstEditAction extends AbstractAction {

    private ConstantBlock target;

    public ConstEditAction(ConstantBlock target) {
        this.target = target;
        putValue(NAME, NbBundle.getBundle(ConstEditAction.class).getString("EDIT"));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        XElement temp = new XElement("constant");        
        for (String s : target.getConstant().keywords) {
            if (s.startsWith("location(")) {
                target.getConstant().keywords.remove(s);
                break;
            }
        }
        target.getConstant().save(temp);
        while (true) {
            EditDialog dlg = new EditDialog();
            dlg.setDefaultValue(temp.toString());
            dlg.setVisible(true);
            String value = dlg.getValue();
            if (value == null) {
                return;
            }
            try {
                XElement xe = XElement.parseXML(new StringReader(value));
                AdvanceConstantBlock c = new AdvanceConstantBlock();
                c.load(xe);
                target.setConstant(c);
                return;
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }
}
