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
using System.IO;
using System.Xml;

using AdvanceAPIClient.Core;
using System.Reactive.Concurrency;
using System.Reactive.Disposables;

namespace AdvanceAPIClient.Communication
{
    public class XmlNodeTracker<T> : IObservable<T>
    {
        private Func<AbstractResponse> connect;
        private IScheduler scheduler;

        public XmlNodeTracker(Func<AbstractResponse> connect, IScheduler scheduler)
        {
            this.connect = connect;
            this.scheduler = scheduler;
        }

        public IDisposable Subscribe(IObserver<T> observer)
        {
            try
            {
                AbstractResponse response = this.connect();

                CancellationDisposable cd = new CancellationDisposable();

                IDisposable cancel = this.scheduler.Schedule(() =>
                {
                    try
                    {
                        XmlNode next = response.NextNode();
                        while (next != null && !cd.IsDisposed)
                        {
                            observer.OnNext((T)XmlReadWrite.CreateFromXml(typeof(T), next));
                            next = response.NextNode();
                        }
                        observer.OnCompleted();
                    }
                    catch (Exception e)
                    {
                        Log.LogException(e);
                        observer.OnError(e);
                    }
                });
                return new CompositeDisposable(cd, cancel, response);
            }
            catch (Exception e)
            {
                Log.LogException(e);
                observer.OnError(e);
            }
            return Disposable.Empty;
        }
    }

}
