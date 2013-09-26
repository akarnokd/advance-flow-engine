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
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Xml;

using AdvanceAPIClient.Core;
using System.IO;

namespace AdvanceClient
{
    public delegate void Messagedelegate(string msg, Exception e);

    public class AdvanceTrackingFinished : Exception 
    {
        public AdvanceTrackingFinished(string msg) : base(msg) { }
        public AdvanceTrackingFinished(string msg, Exception e) : base(msg, e) { }
    }

    public class XmlObjectReporter<T> : IObserver<T>, IDisposable
    {
        public bool IsStarted { get { return this.started; } }

        private bool started;
        private int count;
        private IDisposable unsubscriber;
        private Messagedelegate messagedelegate;
        private Stream outStream;

        public XmlObjectReporter(Messagedelegate messagedelegate, string outFileName)
        {
            this.messagedelegate = messagedelegate;
            this.count = 0;
            this.started = false;
            if (outFileName != null)
                try
                {
                    this.outStream = File.Create(outFileName);
                }
                catch { }
        }

        public virtual void Subscribe(IObservable<T> provider)
        {
            if (provider != null)
            {
                this.started = true;
                this.unsubscriber = provider.Subscribe(this);
            }
        }

        public virtual void OnCompleted()
        {
            this.Report(null, null, new AdvanceTrackingFinished("Tracker completed"));
        }

        public virtual void OnError(Exception e)
        {
            this.Report(null, null, new AdvanceTrackingFinished("Error during observation", e));
        }

        public virtual void OnNext(T value)
        {
             this.Report(value, null, null);
        }

        private void Report(object obj, string msg, Exception e)
        {
            if (obj != null)
                msg = obj.ToString();
            this.messagedelegate(msg, e);
            if (this.outStream != null)
            {
                if (obj != null)
                    XmlReadWrite.AddToStream(this.outStream, obj);
                if (msg != null)
                    XmlReadWrite.AddToStream(this.outStream, msg);
                if (e != null)
                    XmlReadWrite.AddToStream(this.outStream, e);
                this.outStream.Flush();
            }
        }

        public void Dispose()
        {
            this.messagedelegate(this.count + "object received", null); 
            if (this.outStream != null)
                this.outStream.Close(); 
            if (this.unsubscriber != null)
                this.unsubscriber.Dispose();
        }

    }

}
