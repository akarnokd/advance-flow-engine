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

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import org.openide.util.NbBundle;

import com.google.common.eventbus.Subscribe;

//@ActionID(category = "RemoteFlowEngine",
//id = "eu.advance.logistics.flow.engine.controlcenter.WebDataSourcesAction")
//@ActionRegistration(displayName = "#CTL_WebDataSourcesAction")
//@ActionReferences({
//    @ActionReference(path = "Menu/RemoteFlowEngine", position = 1200)
//})
public final class WebDataSourcesAction  extends AbstractAction {

    /** */
	private static final long serialVersionUID = -2451718985440230405L;

	public WebDataSourcesAction() {
        putValue(NAME, NbBundle.getMessage(WebDataSourcesAction.class, "CTL_WebDataSourcesAction"));
        setEnabled(false);
        EngineController.getInstance().getEventBus().register(this);
    }

    @Subscribe
    public void onEngine(EngineController ec) {
        setEnabled(ec.getEngine() != null);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // TODO implement action body
    }
}
