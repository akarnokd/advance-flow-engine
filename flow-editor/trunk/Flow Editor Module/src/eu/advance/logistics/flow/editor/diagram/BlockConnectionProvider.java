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

import java.awt.Point;
import java.util.List;

import org.netbeans.api.visual.action.ConnectProvider;
import org.netbeans.api.visual.action.ConnectorState;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;

import eu.advance.logistics.flow.editor.model.BlockBind;
import eu.advance.logistics.flow.editor.model.BlockParameter;
import eu.advance.logistics.flow.editor.model.CompositeBlock;
import eu.advance.logistics.flow.editor.model.ConstantBlock;
import eu.advance.logistics.flow.editor.model.FlowDescription;
import eu.advance.logistics.flow.editor.undo.BindCreated;
import eu.advance.logistics.flow.editor.undo.UndoRedoSupport;

/**
 * <b>BlockConnectionProvider</b>
 * 
 * @author TTS
 */
class BlockConnectionProvider implements ConnectProvider {

    private final FlowScene scene;
    private FlowDescription flowDescription;

    BlockConnectionProvider(FlowScene scene, FlowDescription flowDescription) {
        this.scene = scene;
        this.flowDescription = flowDescription;
    }

    @Override
    public boolean isSourceWidget(Widget widget) {
        if (widget instanceof PinWidget) {
            final Object obj = scene.findObject(widget);
            if (obj instanceof BlockParameter) {
                final BlockParameter param = (BlockParameter) obj;
                return true; // getParamType((PinWidget) widget, param) == BlockParameter.Type.OUTPUT;
            }
        }

        return false;
    }

    @Override
    public ConnectorState isTargetWidget(Widget src, Widget dest) {
        /*
        if (dest instanceof PinWidget) {
            final Object destObj = scene.findObject(dest);
            if (destObj instanceof BlockParameter) {
                final BlockParameter destParam = (BlockParameter) destObj;
                if (getParamType((PinWidget) dest, destParam) == BlockParameter.Type.INPUT) {
                    //controllo il source
                    if (src instanceof PinWidget) {
                        final Object srcObj = scene.findObject(src);
                        if (srcObj instanceof BlockParameter) {
                            final BlockParameter srcParam = (BlockParameter) srcObj;
                            if (getParamType((PinWidget) src, srcParam) == BlockParameter.Type.OUTPUT) {
                                //vedo se non c'è duplicato                                
                                if (flowDescription.getActiveBlock().find(srcParam, destParam) == null) {
                                    return ConnectorState.ACCEPT;
                                }
                            }
                        }
                    }
                }
            }
        }
         */
        if (dest instanceof PinWidget) {
            final Object destObj = scene.findObject(dest);
            if (destObj instanceof BlockParameter) {
                final BlockParameter destParam = (BlockParameter) destObj;
                if (src instanceof PinWidget) {
                    final Object srcObj = scene.findObject(src);
                    if (srcObj instanceof BlockParameter) {
                        final BlockParameter srcParam = (BlockParameter) srcObj;
                        if (flowDescription.getActiveBlock().find(srcParam, destParam) == null) {

                            Boolean cc = canConnect(srcParam, destParam);
                            if (cc != null) {
                                return ConnectorState.ACCEPT;
                            }
                        }
                    }
                }
            }
        }
        if (dest instanceof ConstantBlockWidget) {
            ConstantBlock cb = ((ConstantBlockWidget)dest).getBlock();
            BlockParameter cp = cb.getParameter();
            if (src instanceof PinWidget) {
                final Object srcObj = scene.findObject(src);
                if (srcObj instanceof BlockParameter) {
                    final BlockParameter srcParam = (BlockParameter) srcObj;
                    Boolean cc = canConnect(srcParam, cp);
                    if (cc != null) {
                        return ConnectorState.ACCEPT;
                    }
                }
            }
        }
        return ConnectorState.REJECT;
    }

    static BlockParameter.Type getParamType(PinWidget w, BlockParameter p) {
        if (w.isInverted()) {
            return p.type == BlockParameter.Type.INPUT ? BlockParameter.Type.OUTPUT : BlockParameter.Type.INPUT;
        }
        return p.type;
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
    /**
     * Check if two block parameters may be connected together.
     * @param src the source block parameter
     * @param dst the destination block parameter
     * @return non-null if the parameters may be connected together, and the
     * value indicates that the source and destinations should be swapped
     */
    protected Boolean canConnect(BlockParameter src, BlockParameter dst) {
        boolean srcIn = src.type == BlockParameter.Type.INPUT;
        boolean dstIn = dst.type == BlockParameter.Type.INPUT;

        CompositeBlock target = flowDescription.getActiveBlock();
                
        if (src.owner == dst.owner && srcIn != dstIn) {
            if (target == src.owner) {
                return !srcIn;
            }
            return srcIn;
        } 
        if (src.owner != dst.owner) {
            // context to block
            if (src.owner == target && dst.owner != target && srcIn == dstIn) {
                return !srcIn;
            }
            // block to context
            if (src.owner != target && dst.owner == target && srcIn == dstIn) {
                return srcIn;
            }
            
            // block to block
            if (srcIn != dstIn) {
                return srcIn;
            }
        }
        
        return null;
    }
    @Override
    public void createConnection(Widget src, Widget dest) {
        BlockParameter input = (BlockParameter) scene.findObject(src);
        Object destObj = scene.findObject(dest);
        BlockParameter output = null;
        if (destObj instanceof BlockParameter) {
            output = (BlockParameter)destObj;
        } else
        if (dest instanceof ConstantBlockWidget) {
            output = ((ConstantBlockWidget)dest).getBlock().getParameter();
        } else {
            throw new IllegalArgumentException("Destination error: " + dest + " " + dest.getClass());
        }
        
        Boolean cc = canConnect(input, output);
        if (cc == null) {
            throw new IllegalStateException("canConnect returned null for " + input + " and " + output);
        }
        if (cc) {
            BlockParameter temp = input;
            input = output;
            output = temp;
        }
        
        UndoRedoSupport urs = scene.getUndoRedoSupport();
        urs.start();
        CompositeBlock target = flowDescription.getActiveBlock();
        BlockBind bind = target.createBind(input, output);
        urs.commit(new BindCreated(target, bind));

    }
}
