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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;

import org.openide.windows.Mode;
import org.openide.windows.TopComponent;
import org.openide.windows.TopComponent.Registry;
import org.openide.windows.WindowManager;

/**
 *
 * @author TTS
 */
public abstract class ContextSupport<T> implements PropertyChangeListener {

    private final Class<T> clazz;
    private WeakReference<TopComponent> tcReference;

    public ContextSupport(Class<T> clazz) {
        this.clazz = clazz;
    }

    protected abstract void contextChanged(T context);

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        String pname = evt.getPropertyName();
        if (Registry.PROP_ACTIVATED.equals(pname)) {
            TopComponent tc = (TopComponent) evt.getNewValue();
            Mode mode = WindowManager.getDefault().findMode(tc);
            if (mode != null && mode.getName().equals("editor")) {
                T ctx = tc.getLookup().lookup(clazz);
                if (ctx != null) {
                    tcReference = new WeakReference<TopComponent>(tc);
                } else {
                    tcReference = null;
                }
                contextChanged(ctx);
            }
        } else if (Registry.PROP_TC_CLOSED.equals(pname)) {
            TopComponent tc = (TopComponent) evt.getNewValue();
            if (tcReference != null && tc == tcReference.get()) {
                tcReference = null;
                contextChanged(null);
            }
        }
    }

    public void activate() {
        WindowManager.getDefault().getRegistry().addPropertyChangeListener(this);
    }

    public void deactivate() {
        WindowManager.getDefault().getRegistry().removePropertyChangeListener(this);
        contextChanged(null);
    }
}
