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

package eu.advance.logistics.flow.editor.diagram;

import org.netbeans.api.visual.model.ObjectState;
import org.netbeans.api.visual.widget.ConnectionWidget;
import org.netbeans.api.visual.widget.Scene;

/**
 *
 * @author TTS
 */
class BlockConnectionWidget extends ConnectionWidget {

    private ColorScheme scheme;

    /**
     * Creates a connection widget with a specific color scheme.
     * @param scene the scene
     * @param scheme the color scheme
     */
    BlockConnectionWidget(Scene scene, ColorScheme scheme) {
        super(scene);
        assert scheme != null;
        this.scheme = scheme;
        scheme.installUI(this);
        setState(ObjectState.createNormal());
    }

    /**
     * Implements the widget-state specific look of the widget.
     * @param previousState the previous state
     * @param state the new state
     */
    @Override
    public void notifyStateChanged(ObjectState previousState, ObjectState state) {
        scheme.updateUI(this, previousState, state);
    }
}
