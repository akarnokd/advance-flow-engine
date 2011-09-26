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

import eu.advance.logistics.flow.editor.model.BlockParameter;
import eu.advance.logistics.flow.model.AdvanceBlockParameterDescription;
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
public class ParamEditAction extends AbstractAction {

    private BlockParameter parameter;

    public ParamEditAction(BlockParameter param) {
        this.parameter = param;
        putValue(NAME, NbBundle.getBundle(ParamEditAction.class).getString("EDIT"));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String tag = parameter.type.toString().toLowerCase();
        XElement x = new XElement(tag);
        parameter.description.save(x);
        String s = x.toString();
        while (true) {
            EditDialog dlg = new EditDialog();
            dlg.setDefaultValue(s);
            dlg.setVisible(true);

            s = dlg.getValue();
            if (s == null) {
                return;
            }
            try {
                XElement xe = XElement.parseXML(new StringReader(s));
                AdvanceBlockParameterDescription desc = new AdvanceBlockParameterDescription();
                desc.load(xe);
//                if (!parameter.getId().equals(desc.id)) {
//                    parameter.setId(desc.id); // check if ID already exists
//                }
                desc.id = parameter.getId();
                parameter.description = desc;
                return;
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }
}
