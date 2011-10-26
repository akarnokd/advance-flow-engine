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

import eu.advance.logistics.flow.editor.BlockRegistry;
import eu.advance.logistics.flow.editor.model.BlockParameter;
import eu.advance.logistics.flow.editor.model.CompositeBlock;
import eu.advance.logistics.flow.engine.model.fd.AdvanceBlockParameterDescription;
import eu.advance.logistics.flow.engine.xml.typesystem.XElement;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.io.StringReader;
import javax.swing.AbstractAction;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author TTS
 */
public class ParamAddAction extends AbstractAction {

    private CompositeBlock block;
    private BlockParameter.Type type;
    private Point location;

    public ParamAddAction(CompositeBlock block, BlockParameter.Type type) {
        this.block = block;
        this.type = type;
        putValue(NAME, NbBundle.getBundle(ParamAddAction.class).getString("ADD") + type + "...");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        NotifyDescriptor.InputLine nd = new NotifyDescriptor.InputLine("id", type + " name");
        if (DialogDisplayer.getDefault().notify(nd) != NotifyDescriptor.OK_OPTION) {
            return;
        }
        String paramId = nd.getInputText();
        if (paramId == null || paramId.isEmpty()) {
            return;
        }
        String s = getParamDescAsText(paramId);
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
                if (type == BlockParameter.Type.INPUT) {
                    block.createInput(desc);
                } else {
                    block.createOutput(desc);
                }
                return;
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    private String getParamDescAsText(String paramId) {
        String tag = type.toString().toLowerCase();
        AdvanceBlockParameterDescription desc = new AdvanceBlockParameterDescription();
        desc.id = paramId;
        desc.displayName = paramId;
        //desc.documentation = "";
        desc.type = BlockRegistry.getInstance().getDefaultAdvanceType();
        XElement x = new XElement(tag);
        desc.save(x);
        return x.toString();
    }

    public Point getLocation() {
        return location;
    }

    public void setLocation(Point location) {
        this.location = location;
    }
}
