/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.advance.logistics.flow.editor.actions;

import com.google.common.collect.Lists;
import hu.akarnokd.utils.xml.XNElement;
import java.awt.BorderLayout;
import java.awt.Dialog;
import java.util.List;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;

/**
 * Display a basic GUI for editing a type constructor.
 * @author karnokd
 */
public class EditType {
    /**
     * Display the type editor dialog.
     * @param value the initial type value.
     * @return the advance:type element
     */
    public static XNElement edit(XNElement value) {
        String title = "Type<T>";
        
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        
        panel.add(new JLabel("Type declaration of T:"), BorderLayout.NORTH);
        
        JTextField tf = new JTextField(30);
        panel.add(tf, BorderLayout.CENTER);
        
        if (value != null) {
            tf.setText(fromXML(value));
        }
        
        DialogDescriptor dd = new DialogDescriptor(panel, title);
        Dialog dlg = DialogDisplayer.getDefault().createDialog(dd);
        dlg.setVisible(true);
        if (dd.getValue() != NotifyDescriptor.OK_OPTION) {
            return null;
        }
        return toXML(tf.getText());
    }
    /**
     * Convert the string representation of the type into an XElement of advance:type structure.
     * @param s the string to convert, if empty, null is returned
     * @return the XElement representing the type
     */
    public static XNElement toXML(String s) {
        if (s.isEmpty()) {
            return null;
        }
        List<String> tokens = Lists.newArrayList();
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == ',' || c == '<' || c == '>') {
                tokens.add(b.toString().trim());
                b.setLength(0);
                tokens.add(String.valueOf(c));
            } else {
                b.append(c);
            }
        }
        if (b.length() > 0) {
            tokens.add(b.toString());
        }
        // uri[<uri[<...>][,uri[<...>]]>]
        XNElement result = new XNElement("type");
        result.set("type", tokens.get(0));
        
        if (tokens.size() > 3 && tokens.get(1).equals("<")) {
            parseURIList(result, tokens.subList(2, tokens.size()));
        }
        return result;
    }        
    static void parseURIList(XNElement argsFor, List<String> tokens) {
        int i = 0;
        do {
            XNElement out = argsFor.add("type-argument");
            out.set(tokens.get(i));
            if (tokens.size() > i + 1) {
                final String tok = tokens.get(i + 1);
                if (tok.equals("<")) {
                    parseURIList(out, tokens.subList(i + 2, tokens.size()));
                } else
                if (tok.equals(">")) {
                    break;
                } else {
                    i++;
                }
                   
            } 
        } while (i < tokens.size());
    }
    /**
     * Convert the XElement of an advance:type into string representation.
     * @param type the type to convert
     * @return  the string representation
     */
    public static String fromXML(XNElement type) {
        StringBuilder b = new StringBuilder();
        
        b.append(type.get("type"));
        if (type.hasChildren()) {
            b.append("<");
            int i = 0;
            for (XNElement c : type.childrenWithName("type-argument")) {
                if (i > 0) {
                    b.append(", ");
                }
                b.append(fromXML(c));
                i++;
            }
            b.append(">");
        }
        
        return b.toString();
    }
    /**
     * Create a type constructor from the given type definition.
     * @param value the type.xsd definition of a type structure
     * @return the associated type, e.g., if value is advance:string, the result is advance:type&lt;advance:string&gt;
     */
    public static String createTypeConstructor(XNElement value) {
        return "advance:type<" + EditType.fromXML(value) + ">";
    }
}
