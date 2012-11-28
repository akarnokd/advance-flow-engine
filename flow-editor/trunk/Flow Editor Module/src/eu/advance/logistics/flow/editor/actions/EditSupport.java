package eu.advance.logistics.flow.editor.actions;

import eu.advance.logistics.flow.engine.block.AdvanceData;
import java.awt.BorderLayout;
import java.awt.Dialog;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.explorer.propertysheet.PropertyPanel;
import org.openide.nodes.Node;
import org.openide.nodes.PropertySupport;

/**
 *
 * @author TTS
 */
public class EditSupport {
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    private Class<?> clz;
    private Object value;

    public EditSupport(Class<?> clz) {
        this.clz = clz;
    }

    void setAsText(String text) {
        if (Integer.class.equals(clz)) {
            try {
                value = new Integer(text);
            } catch (NumberFormatException ex) {
                value = new Integer(0);
            }
        } else if (Double.class.equals(clz)) {
            try {
                value = new Double(text);
            } catch (NumberFormatException ex) {
                value = new Double(0);
            }
        } else if (Boolean.class.equals(clz)) {
            value = Boolean.valueOf(text);
        } else if (String.class.equals(clz)) {
            value = text;
        } else if (Date.class.equals(clz)) {
            try {
                value = dateFormat.parse(text);
            } catch (ParseException ex) {
                value = new Date();
            }
        } else {
            value = null;
        }
    }

    String getAsText() {
        if (value instanceof Date) {
            return dateFormat.format((Date)value);
        }
        return value != null ? value.toString() : null;
    }

    Node.Property<?> createProperty() {
        if (Integer.class.equals(clz)) {
            return new PropertySupport.ReadWrite<Integer>("value", Integer.class, "Integer", "advance:integer") {

                @Override
                public Integer getValue() throws IllegalAccessException, InvocationTargetException {
                    return (Integer) value;
                }

                @Override
                public void setValue(Integer val) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
                    value = val;
                }
            };
        } else if (Double.class.equals(clz)) {
            return new PropertySupport.ReadWrite<Double>("value", Double.class, "Real", "advance:real") {

                @Override
                public Double getValue() throws IllegalAccessException, InvocationTargetException {
                    return (Double) value;
                }

                @Override
                public void setValue(Double val) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
                    value = val;
                }
            };
        } else if (Boolean.class.equals(clz)) {
            return new PropertySupport.ReadWrite<Boolean>("value", Boolean.class, "Boolean", "advance:boolean") {

                @Override
                public Boolean getValue() throws IllegalAccessException, InvocationTargetException {
                    return (Boolean) value;
                }

                @Override
                public void setValue(Boolean val) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
                    value = val;
                }
            };
        } else if (String.class.equals(clz)) {
            return new PropertySupport.ReadWrite<String>("value", String.class, "String", "advance:string") {

                @Override
                public String getValue() throws IllegalAccessException, InvocationTargetException {
                    return (String) value;
                }

                @Override
                public void setValue(String val) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
                    value = val;
                }
            };
        } else if (Date.class.equals(clz)) {
            return new PropertySupport.ReadWrite<Date>("value", Date.class, "Date", "advance:timestamp") {

                @Override
                public Date getValue() throws IllegalAccessException, InvocationTargetException {
                    return (Date) value;
                }

                @Override
                public void setValue(Date val) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
                    value = val;
                }
            };
        } else {
            return null;
        }
    }

    static String edit(String content, URI typeURI) {
        String title = typeURI.toString();
        TypeSupport[] types = TypeSupport.create();
        TypeSupport ts = types[TypeSupport.find(types, typeURI)];
        if (ts.clazz != null) {
            EditSupport editSupport = new EditSupport(ts.clazz);
            Node.Property<?> property = editSupport.createProperty();
            if (property != null) {
                editSupport.setAsText(content);
                JPanel panel = new JPanel(new BorderLayout());
                panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
                panel.add(new JLabel(ts.toString()), BorderLayout.NORTH);
                PropertyPanel pp = new PropertyPanel(property);
                panel.add(pp, BorderLayout.CENTER);
                DialogDescriptor dd = new DialogDescriptor(panel, title);
                Dialog dlg = DialogDisplayer.getDefault().createDialog(dd);
                dlg.setVisible(true);
                if (dd.getValue() != NotifyDescriptor.OK_OPTION) {
                    return null;
                }
                return editSupport.getAsText();
            }
        }
        EditDialog dlg = new EditDialog();
        dlg.setTitle(title);
        dlg.setDefaultValue(content);
        dlg.setVisible(true);
        if (dlg.getValue() == null) {
            return null;
        }
        return dlg.getValue();
    }
}
