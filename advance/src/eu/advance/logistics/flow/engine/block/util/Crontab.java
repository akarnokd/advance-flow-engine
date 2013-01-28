/*
 * Copyright 2010-2013 The Advance EU 7th Framework project consortium
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
package eu.advance.logistics.flow.engine.block.util;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;

import org.jcrontab.data.CalendarBuilder;
import org.jcrontab.data.CrontabEntryBean;
import org.jcrontab.data.CrontabEntryException;
import org.jcrontab.data.CrontabParser;

import eu.advance.logistics.annotations.Block;
import eu.advance.logistics.annotations.Input;
import eu.advance.logistics.annotations.Output;
import eu.advance.logistics.flow.engine.block.AdvanceBlock;

/**
 * A crontab block which signals the identifier of a task to be executed, based
 * on the crontab-definition. Signature: Crontab(string) -> string
 *
 * @author TTS
 */
@Block(id = "Crontab", category = "string", scheduler = "IO", description = "A crontab block which signals the identifier of a task to be executed, based on the crontab-definition.")
public class Crontab extends AdvanceBlock {

    /**
     * The logger.
     */
    protected static final Logger LOGGER = Logger.getLogger(Crontab.class.getName());
    /**
     * In.
     */
    @Input("advance:string")
    protected static final String IN = "in";
    /**
     * Out.
     */
    @Output("advance:string")
    protected static final String OUT = "out";

    @Override
    protected void invoke() {
        try {
            final CrontabParser parser = new CrontabParser();
            final CrontabEntryBean bean = parser.marshall(resolver().getString(get(IN)));
            final CalendarBuilder builder = new CalendarBuilder();
            final Date zeroDate = new Date();

            final Timer timer = new Timer(true);
            timer.schedule(new TimerTask() {

                @Override
                public void run() {
                    // verify the contab
                    final Date now = new Date();
                    final long unixNow = now.getTime();
                    final Date date = builder.buildCalendar(bean, zeroDate);
                    final long unixDate = date.getTime();

                    final long delta = unixNow - unixDate;
                    // if that CrontabEntryBean is to be executed in this minute 
                    if ((delta > 0) && (delta < 1000)) {
                        //dispatch the taks
                        dispatch(OUT, resolver().create(bean.getMethodName()));
                    }

                    // blocca il timer ??
                    timer.cancel();
                }
            }, 60 * 1000); //ogni minuto


        } catch (CrontabEntryException ex) {
            log(ex);
        }
    }
}
