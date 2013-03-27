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

package net.sourceforge.aprog.markups;

import static net.sourceforge.aprog.i18n.Messages.setMessagesBase;
import static net.sourceforge.aprog.markups.MarkupsConstants.Variables.APPLICATION_COPYRIGHT;
import static net.sourceforge.aprog.markups.MarkupsConstants.Variables.APPLICATION_NAME;
import static net.sourceforge.aprog.markups.MarkupsConstants.Variables.APPLICATION_VERSION;
import static net.sourceforge.aprog.markups.MarkupsConstants.Variables.DOM;
import static net.sourceforge.aprog.markups.MarkupsConstants.Variables.FILE;
import static net.sourceforge.aprog.markups.MarkupsConstants.Variables.FILE_MODIFIED;
import static net.sourceforge.aprog.markups.MarkupsConstants.Variables.QUASI_XPATH_ERROR;
import static net.sourceforge.aprog.markups.MarkupsConstants.Variables.QUASI_XPATH_EXPRESSION;
import static net.sourceforge.aprog.markups.MarkupsConstants.Variables.SELECTED_NODE;
import static net.sourceforge.aprog.markups.MarkupsConstants.Variables.XPATH_ERROR;
import static net.sourceforge.aprog.markups.MarkupsConstants.Variables.XPATH_EXPRESSION;
import static net.sourceforge.aprog.markups.MarkupsConstants.Variables.XPATH_RESULT;
import static net.sourceforge.aprog.markups.MarkupsTools.addListener;
import static net.sourceforge.aprog.swing.SwingTools.canInvokeLaterThisMethodInAWT;
import static net.sourceforge.aprog.swing.SwingTools.useSystemLookAndFeel;
import static net.sourceforge.aprog.tools.Tools.getThisPackagePath;
import net.sourceforge.aprog.af.MacOSXTools;
import net.sourceforge.aprog.context.Context;
import net.sourceforge.aprog.events.Variable;
import net.sourceforge.aprog.events.Variable.ValueChangedEvent;
import net.sourceforge.aprog.tools.IllegalInstantiationException;
import net.sourceforge.aprog.xml.XMLTools;

import org.w3c.dom.Node;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;

/**
 *
 * @author codistmonk (creation 2010-07-03)
 */
public final class Markups {

    /**
     * @throws IllegalInstantiationException To prevent instantiation
     */
    private Markups() {
        throw new IllegalInstantiationException();
    }

    static {
        MacOSXTools.setApplicationName(MarkupsConstants.APPLICATION_NAME);
        useSystemLookAndFeel();
        setMessagesBase(getThisPackagePath() + "Messages");
    }

    /**
     * @param arguments the command line arguments
     */
    public static final void main(final String[] arguments) {
        if (canInvokeLaterThisMethodInAWT(null, (Object) arguments)) {
            MarkupsComponents.newMainFrame(newContext()).setVisible(true);
        }
    }

    /**
     *
     * @return
     * <br>Not null
     * <br>New
     */
    public static final Context newContext() {
        final Context result = new Context();

        result.set(APPLICATION_NAME, MarkupsConstants.APPLICATION_NAME);
        result.set(APPLICATION_VERSION, MarkupsConstants.APPLICATION_VERSION);
        result.set(APPLICATION_COPYRIGHT, MarkupsConstants.APPLICATION_COPYRIGHT);
        result.set(FILE, null);
        result.set(FILE_MODIFIED, false);
        result.set(DOM, XMLTools.newDocument());
        result.set(SELECTED_NODE, null);
        result.set(XPATH_EXPRESSION, null);
        result.set(XPATH_RESULT, null);
        result.set(XPATH_ERROR, null);
        result.set(QUASI_XPATH_EXPRESSION, null);
        result.set(QUASI_XPATH_ERROR, null);

        final Variable<String> xPathExpressionVariable = result.getVariable(XPATH_EXPRESSION);

        xPathExpressionVariable.addListener(new Variable.Listener<String>() {

            @Override
            public final void valueChanged(final ValueChangedEvent<String, ?> event) {
                MarkupsActions.evaluateXPathExpression(result);
            }

        });

        final EventListener domListener = new EventListener() {

            @Override
            public final void handleEvent(final Event event) {
                result.set(FILE_MODIFIED, true);
                MarkupsActions.evaluateXPathExpression(result);
            }

        };

        addListener(result, DOM, new AbstractDOMListenerReattacher(domListener) {

            @Override
            protected final void afterReattachment(final ValueChangedEvent<Node, ?> event) {
                MarkupsActions.evaluateXPathExpression(result);
            }

        });

        addListener(result, SELECTED_NODE, new Variable.Listener<Node>() {

            @Override
            public final void valueChanged(final ValueChangedEvent<Node, ?> event) {
                MarkupsActions.evaluateXPathExpression(result);
            }

        });

        return result;
    }

}
