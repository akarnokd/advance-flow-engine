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

import eu.advance.logistics.flow.editor.model.BlockParameter;
import java.awt.Point;
import java.util.List;
import org.netbeans.api.visual.action.ConnectProvider;
import org.netbeans.api.visual.action.ConnectorState;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;

/**
 * <b>BlockConnectionProvider</b>
 * 
 * @author TTS
 */
class BlockConnectionProvider implements ConnectProvider {

    private final FlowDiagramScene scene;

    BlockConnectionProvider(FlowDiagramScene scene) {
        this.scene = scene;
    }

    @Override
    public boolean isSourceWidget(Widget widget) {
        if (widget instanceof PinWidget) {
            final Object obj = scene.findObject((PinWidget) widget);
            if (obj instanceof BlockParameter) {
                final BlockParameter param = (BlockParameter) obj;
                return param.type == BlockParameter.Type.OUTPUT;
            }
        }

        return false;
    }

    @Override
    public ConnectorState isTargetWidget(Widget src, Widget dest) {
        if (dest instanceof PinWidget) {
            final Object destObj = scene.findObject((PinWidget) dest);
            if (destObj instanceof BlockParameter) {
                final BlockParameter destParam = (BlockParameter) destObj;
                if (destParam.type == BlockParameter.Type.INPUT) {
                    //controllo il source
                    if (src instanceof PinWidget) {
                        final Object srcObj = scene.findObject((PinWidget) src);
                        if (srcObj instanceof BlockParameter) {
                            final BlockParameter srcParam = (BlockParameter) srcObj;
                            if (srcParam.type == BlockParameter.Type.OUTPUT) {
                                //vedo se non c'è duplicato
                                if (scene.getController().find(srcParam, destParam) == null) {
                                    return ConnectorState.ACCEPT;
                                }
                            }
                        }
                    }
                }
            }
        }

        return ConnectorState.REJECT;
    }

    @Override
    public boolean hasCustomTargetWidgetResolver(Scene scene) {
        return false;
    }

    @Override
    public Widget resolveTargetWidget(Scene scene, Point point) {
        final List<Widget> children = scene.getChildren();
        for (Widget ch : children) {
            if (ch.isHitAt(point)) {
                return ch;
            }
        }

        return null;
    }

    @Override
    public void createConnection(Widget src, Widget dest) {
        final BlockParameter input = (BlockParameter) scene.findObject(src);
        final BlockParameter output = (BlockParameter) scene.findObject(dest);

        scene.getController().createBind(null, input, output);
    }
}
