/*
 *  The MIT License
 * 
 *  Copyright 2010 Codist Monk.
 * 
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 * 
 *  The above copyright notice and this permission notice shall be included in
 *  all copies or substantial portions of the Software.
 * 
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *  THE SOFTWARE.
 */

package net.sourceforge.aprog.events;

import java.util.ArrayList;
import java.util.Collection;

/**
 *
 * @param <L> the event listener type
 * @author codistmonk (creation 2010-06-14)
 */
public abstract class AbstractObservable<L extends Observable.Listener> implements Observable<L> {

    private final Collection<L> listeners;

    public AbstractObservable() {
        this.listeners = new ArrayList<L>();
    }

    /**
     *
     * @param listener
     * <br>Not null
     * <br>Shared
     */
    public final void addListener(final L listener) {
        this.listeners.add(listener);
    }

    /**
     *
     * @param listener
     * <br>Maybe null
     * <br>Shared
     */
    public final void removeListener(final L listener) {
        this.listeners.remove(listener);
    }

    /**
     *
     * @return
     * <br>Not null
     * <br>New
     */
    public final Iterable<L> getListeners() {
        return new ArrayList<L>(this.listeners);
    }

    /**
     *
     * @param <S> the event source type
     * @param <L> the event listener type
     * @author codistmonk (creation 2010-06-15)
     */
    public abstract class AbstractEvent<S extends AbstractObservable<L>, L extends Observable.Listener> extends Observable.AbstractEvent<S, L> {

        /**
         *
         * @param time in milliseconds
         * <br>Range: {@code [0 .. Long.MAX_VALUE]}
         */
        @SuppressWarnings("unchecked")
        protected AbstractEvent(final long time) {
            super((S) AbstractObservable.this, time);
        }

        @SuppressWarnings("unchecked")
        protected AbstractEvent() {
            super((S) AbstractObservable.this);
        }

    }

}
