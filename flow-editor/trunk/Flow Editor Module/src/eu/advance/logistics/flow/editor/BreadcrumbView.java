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
package eu.advance.logistics.flow.editor;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;

import com.google.common.collect.Lists;

import eu.advance.logistics.flow.editor.model.CompositeBlock;
import eu.advance.logistics.flow.editor.model.FlowDescriptionChange;
import eu.advance.logistics.flow.editor.model.FlowDescriptionListener;

/**
 *
 * @author TTS
 */
class BreadcrumbView implements FlowDescriptionListener {

    private JPanel control;
    private JPanel content;

    BreadcrumbView() {
        content = new JPanel(new GridBagLayout());
        control = new JPanel(new BorderLayout());
        control.add(content, BorderLayout.WEST);
    }

    @Override
    public void flowDescriptionChanged(FlowDescriptionChange event, Object... params) {
        switch (event) {
            case ACTIVE_COMPOSITE_BLOCK_CHANGED:
                populate((CompositeBlock) params[1]);
                break;
            case CLOSED:
                content.removeAll();
                break;
        }
    }

    void populate(CompositeBlock active) {
        List<JToggleButton> buttons = Lists.newArrayList();
        CompositeBlock current = active;
        do {
            JToggleButton btn = new JToggleButton(new ActiveBlockAction(current));
            buttons.add(btn);
            current = current.getParent();
        } while (current != null);

        content.removeAll();
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = c.gridy = 0;
        c.anchor = GridBagConstraints.BASELINE_LEADING;
        for (int i = buttons.size() - 1; i > -1; i--) {
            content.add(buttons.get(i), c);
            c.gridx++;
            if (i != 0) {
                content.add(new JLabel(">"), c);
                c.gridx++;
            } else {
                buttons.get(i).setEnabled(false);
            }
        }
        control.revalidate();
        control.repaint();
    }

    JComponent getControl() {
        return control;
    }

    private static class ActiveBlockAction extends AbstractAction {
        /** */
		private static final long serialVersionUID = -7241249810446550214L;
		private CompositeBlock compositeBlock;

        private ActiveBlockAction(CompositeBlock compositeBlock) {
            this.compositeBlock = compositeBlock;
            putValue(NAME, compositeBlock.getId());
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            compositeBlock.getFlowDiagram().setActiveBlock(compositeBlock);
        }
    }
}
