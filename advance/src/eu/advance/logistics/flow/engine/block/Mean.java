/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.advance.logistics.flow.engine.block;

import eu.advance.logistics.annotations.Block;
import eu.advance.logistics.annotations.Input;
import eu.advance.logistics.annotations.Output;
import eu.advance.logistics.flow.engine.model.fd.AdvanceCompositeBlock;
import eu.advance.logistics.flow.engine.model.rt.AdvanceBlock;
import eu.advance.logistics.flow.engine.model.rt.AdvanceSchedulerPreference;
import eu.advance.logistics.flow.engine.xml.typesystem.XElement;
import java.util.Collections;
import java.util.Map;
import java.util.logging.Logger;

/**
 *
 * @author szmarcell
 */
@Block(description="Block to calculate the running mean of incoming values.")
public class Mean extends AdvanceBlock {
    private static final Logger LOGGER = Logger.getLogger(Mean.class.getName());

    @Input("advance:real")
    private static final String IN = "in";
    @Output("advance:real")
    private static final String OUT = "out";
    
    public Mean(String id, AdvanceCompositeBlock parent, AdvanceSchedulerPreference schedulerPreference) {
        super(id, parent, schedulerPreference);
    }
    private int count;
    private double value;
    @Override
    protected void invoke(Map<String, XElement> map) {
        double val = Double.parseDouble(map.get(IN).content);
        value = (value * count++ + val) / count;
        XElement out = new XElement("double");
        out.content = Double.toString(value);
        dispatchOutput(Collections.singletonMap(OUT, out));
    }
    
}
