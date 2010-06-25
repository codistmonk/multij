/*
 *  The MIT License
 *
 *  Copyright 2010 The Codist Monk.
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

package net.sourceforge.aprog.i18n;

import static net.sourceforge.aprog.tools.Tools.*;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.logging.Level;
import net.sourceforge.aprog.events.AbstractObservable;

import net.sourceforge.aprog.tools.Tools;

/**
 * Instances of this class can translate messages using locales and resource bundles.
 * <br>The easiest way to add translation to a Swing program with this class is by using the static methods in {@link Translator.Helpers}.
 * <br>To improve performance, call {@code this.setAutoCollectingLocales(false)} after all available locales have been collected.
 * <br>You can manually collect locales with {@link #collectAvailableLocales(String)}.
 * <br>Instances of this class are thread-safe as long as the listeners don't cause synchronization problems.
 *
 * @author codistmonk (2010-05-11)
 *
 */
public class Translator extends AbstractObservable<Translator.Listener> {

    private final Set<Autotranslator> autotranslators;

    private final Set<Locale> availableLocales;

    private Locale locale;

    private boolean autoCollectingLocales;

    public Translator() {
        this.autotranslators = new HashSet<Autotranslator>();
        this.availableLocales = new HashSet<Locale>();
        this.locale = Locale.getDefault();
        this.autoCollectingLocales = true;
    }

    /**
     *
     * @return {@code true} if {@link #collectAvailableLocales(String)} is called automatically each time a translation is performed
     */
    public final boolean isAutoCollectingLocales() {
        return this.autoCollectingLocales;
    }

    /**
     *
     * @param autoCollectingLocales {@code true} if {@link #collectAvailableLocales(String)} should be called automatically each time a translation is performed
     */
    public final void setAutoCollectingLocales(final boolean autoCollectingLocales) {
        this.autoCollectingLocales = autoCollectingLocales;
    }

    /**
     *
     * TODO doc
     * @param <T> the actual type of {@code object}
     * @param object
     * <br>Should not be null
     * <br>Input-output parameter
     * <br>Shared parameter
     * @param textPropertyName
     * <br>Should not be null
     * <br>Shared parameter
     * @param translationKey
     * <br>Should not be null
     * <br>Shared parameter
     * @param messagesBase
     * <br>Should not be null
     * <br>Shared parameter
     * @param parameters
     * <br>Should not be null
     * @return {@code object}
     * <br>A non-null value
     * <br>A shared value
     */
    public final synchronized <T> T translate(final T object, final String textPropertyName, final String translationKey, final String messagesBase, final Object... parameters) {
        this.autoCollectLocales(messagesBase);

        final Autotranslator autotranslator = this.new Autotranslator(object, textPropertyName, translationKey, messagesBase, parameters);

        autotranslator.translate();

        // If there is already another autotranslator with the same object and textPropertyName
        // remove it before adding the new autotranslator
        this.autotranslators.remove(autotranslator);
        this.autotranslators.add(autotranslator);

        return object;
    }

    /**
     * Removes {@code object} from the list of autotranslatables and resets its text property to the translation key.
     * <br>That means that subsequent calls to {@link #setLocale(Locale)} won't update {@code object} anymore.
     *
     * @param object
     * <br>Should not be null
     * <br>Input-output parameter
     * <br>Shared parameter
     * @param textPropertyName
     * <br>Should not be null
     * <br>Shared parameter
     */
    public final synchronized void untranslate(final Object object, final String textPropertyName) {
        for (final Iterator<Autotranslator> iterator = this.autotranslators.iterator(); iterator.hasNext();) {
            final Autotranslator autotranslator = iterator.next();

            if (autotranslator.getObject().equals(object) && autotranslator.getTextPropertyName().equals(textPropertyName)) {
                iterator.remove();

                autotranslator.untranslate();

                return;
            }
        }
    }

    /**
     *
     * @return
     * <br>A non-null value
     * <br>A shared value
     */
    public final synchronized Locale getLocale() {
        return this.locale;
    }

    /**
     * If {@code this.getLocale()} is not equal to {@code locale},
     * then the locale is changed, the autotranslators are updated and the listeners are notified.
     *
     * @param locale
     * <br>Should not be null
     * <br>Shared parameter
     */
    public final synchronized void setLocale(final Locale locale) {
        if (!this.getLocale().equals(locale)) {
            final LocaleChangedEvent event = this.new LocaleChangedEvent(this.getLocale(), locale);

            this.locale = locale;

            for (final Autotranslator autotranslator : this.autotranslators) {
                autotranslator.translate();
            }

            event.fire();
        }
    }

    /**
     * The set of available locales can be augmented with {@link #getAvailableLocales()}.
     * <br>{@link #getAvailableLocales()} is called each time a translation is performed.
     *
     * @return
     * <br>A new value
     * <br>A non-null value
     */
    public final synchronized Locale[] getAvailableLocales() {
        return this.availableLocales.toArray(new Locale[this.availableLocales.size()]);
    }

    /**
     *
     * TODO doc
     * @param translationKey
     * <br>Should not be null
     * @param messagesBase
     * <br>Should not be null
     * @param parameters
     * <br>Should not be null
     * @return
     * <br>A non-null value
     */
    public final synchronized String translate(final String translationKey, final String messagesBase, final Object... parameters) {
        this.autoCollectLocales(messagesBase);

        String translatedMessage = translationKey;

        try {
            final ResourceBundle messages = ResourceBundle.getBundle(messagesBase, this.getLocale());

            translatedMessage = iso88591ToUTF8(messages.getString(translationKey));
        } catch (final MissingResourceException exception) {
            System.err.println(Tools.debug(2, exception.getMessage()));
            getLoggerForThisMethod().log(Level.WARNING, "Missing translation for locale (" + Translator.this.getLocale() + ") of " + translationKey);
        }

        final Object[] localizedParameters = parameters.clone();

        for (int i = 0; i < localizedParameters.length; ++i) {
            if (localizedParameters[i] instanceof Throwable) {
                localizedParameters[i] = ((Throwable) localizedParameters[i]).getLocalizedMessage();
            }
        }

        return MessageFormat.format(translatedMessage, localizedParameters);
    }

    /**
     * Scans {@code messagesBase} using {@link Locale#getAvailableLocales()} and adds the available locales to {@code this}.
     * <br>A locale is "available" to the translator if an appropriate resource bundle is found.
     *
     * @param messagesBase
     * <br>Should not be null
     */
    public final synchronized void collectAvailableLocales(final String messagesBase) {
        // TODO don't rely on Locale.getAvailableLocales(), use only messagesBase if possible
        for (final Locale predefinedLocale : Locale.getAvailableLocales()) {
            try {
                if (predefinedLocale.equals(ResourceBundle.getBundle(messagesBase, predefinedLocale).getLocale())) {
                    this.availableLocales.add(predefinedLocale);
                }
            } catch (final Exception exception) {
                // Do nothing
            }
        }
    }

    /**
     * Calls {@link #collectAvailableLocales(String)} if {@code this.isAutoCollectingLocales()}.
     *
     * @param messagesBase
     * <br>Should not be null
     */
    private final void autoCollectLocales(final String messagesBase) {
        if (this.isAutoCollectingLocales()) {
            this.collectAvailableLocales(messagesBase);
        }
    }

    /**
     *
     * This class defines a property translation operation.
     *
     * @author codistmonk (creation 2010-05-11)
     *
     */
    private final class Autotranslator {

        private final Object object;

        private final String textPropertyName;

        private final String translationKey;

        private final String messagesBase;

        private final Object[] parameters;

        private final Method setter;

        /**
         *
         * @param object
         * <br>Should not be null
         * <br>Shared parameter
         * @param textPropertyName
         * <br>Should not be null
         * <br>Shared parameter
         * @param translationKey
         * <br>Should not be null
         * <br>Shared parameter
         * @param messagesBase
         * <br>Should not be null
         * <br>Shared parameter
         * @param parameters
         * <br>Should not be null
         * <br>Shared parameter
         * @throws RuntimeException if a setter cannot be retrieved for the property.
         */
        public Autotranslator(final Object object, final String textPropertyName,
                final String translationKey, final String messagesBase, final Object... parameters) {
            this.object = object;
            this.textPropertyName = textPropertyName;
            this.translationKey = translationKey;
            this.messagesBase = messagesBase;
            this.parameters = parameters;
            this.setter = getSetter(object, textPropertyName, String.class);
        }

        /**
         *
         * @return
         * <br>A non-null value
         * <br>A shared value
         */
        public final Object getObject() {
            return this.object;
        }

        /**
         *
         * @return
         * <br>A non-null value
         * <br>A shared value
         */
        public final String getTextPropertyName() {
            return this.textPropertyName;
        }

        public final void translate() {
            this.set(Translator.this.translate(this.translationKey, this.messagesBase, this.parameters));
        }

        /**
         * Sets the property with the translation key.
         */
        public final void untranslate() {
            this.set(this.translationKey);
        }

        @Override
        public final boolean equals(final Object object) {
            final Autotranslator that = castToCurrentClass(object);

            return this == that ||
                    that != null &&
                    this.getObject().equals(that.getObject()) &&
                    this.getTextPropertyName().equals(that.getTextPropertyName());
        }

        @Override
        public final int hashCode() {
            return this.object.hashCode() + this.textPropertyName.hashCode();
        }

        /**
         * Calls {@code this.setter} with parameter {@code text}.
         *
         * @param text
         * <br>Should not be null
         * <br>Shared parameter
         */
        private final void set(final String text) {
            try {
                this.setter.invoke(this.getObject(), text);
            } catch (final Exception exception) {
                getLoggerForThisMethod().log(Level.WARNING, "", exception);
            }
        }

    }

    private static Translator defaultTranslator;

    /**
     * This method creates the default translator if necessary, and then always returns the same value.
     *
     * @return
     * <br>A non-null value
     * <br>A shared value
     */
    public static final synchronized Translator getDefaultTranslator() {
        if (defaultTranslator == null) {
            defaultTranslator = new Translator();
        }

        return defaultTranslator;
    }

    /**
     * This method gets or creates a {@link Locale} corresponding to {@code languageCountryVariant}.
     * <br>{@code languageCountryVariant} is a String made of 1 to 3 elements separated by "_":
     * <br>language ("" or ISO 639 2-letter code) ["_" country ("" or ISO 3166 2-letter code) ["_" variant (can be "")]]
     * @param languageCountryVariant
     * <br>Should not be null
     * @return
     * <br>A possibly new value
     * <br>A non-null value
     */
    public static final Locale createLocale(final String languageCountryVariant) {
        final String[] tmp = languageCountryVariant.split("_");
        final String language = tmp[0];
        final String country = tmp.length > 1 ? tmp[1] : "";
        final String variant = tmp.length > 2 ? tmp[2] : "";

        for (final Locale locale : Locale.getAvailableLocales()) {
            if (locale.getLanguage().equals(language) && locale.getCountry().equals(country) && locale.getVariant().equals(variant)) {
                return locale;
            }
        }

        return new Locale(language, country, variant);
    }

    /**
     * This method does the opposite of {@link #createLocale(String)}.
     *
     * @param locale
     * <br>Should not be null
     * @return
     * <br>A new value
     * <br>A non-null value
     */
    public static final String getLanguageCountryVariant(final Locale locale) {
        String result = locale.getLanguage();

        if (locale.getCountry().length() > 0) {
            result += "_" + locale.getCountry();
        }

        if (locale.getVariant().length() > 0) {
            result += "_" + locale.getVariant();
        }

        return result;
    }

    /**
     * This method reinterprets strings read from property files using UTF-8.
     * <br>{@link ResourceBundle} interprets the contents of .properties files as if they used ISO-8859-1 encoding.
     * <br>If UTF-8 is used to encode these files, the retrieved messages will present bad characters.
     * <br>For instance, the character 'Ω' is encoded as {@code 0xCEA9} in UTF-8 but cannot be directly encoded in ISO-8859-1.
     * <br>Instead, the code \u03A9 would have to be used so that {@link ResourceBundle} retrieves the character 'Ω'.
     * <br>If a file contains 'Ω' in UTF-8, {@link ResourceBundle} will interpret it using ISO-8859-1 as "Î©".
     * because {@code 0xCE} is 'Î' and {@code 0xA9} is '©' in this encoding.
     * <br>If {@code s = "Î©"} is the string retrieved from a file containing 'Ω' in UTF-8,
     * then {@code !s.equals("Ω")} but {@code iso88591ToUTF8(s).equals("Ω")}.
     *
     * @param translatedMessage
     * <br>Should not be null
     * <br>Shared parameter
     * @return a new string or {@code translatedMessage} if the conversion fails
     * <br>A non-null value
     * <br>Shared value
     */
    public static final String iso88591ToUTF8(final String translatedMessage) {
        try {
            return new String(translatedMessage.getBytes("ISO-8859-1"), "UTF-8");
        } catch (final UnsupportedEncodingException exception) {
            getLoggerForThisMethod().log(Level.WARNING, "", exception);

            return translatedMessage;
        }
    }

    /**
     *
     * Listener interface for translator events.
     *
     * @author codistmonk (creation 2009-09-28)
     *
     */
    public static interface Listener {

        /**
         * Called whenever the translator's locale has been changed, and after the registered
         * objects have been translated.
         *
         * @param event
         * <br>Not null
         */
        public abstract void localeChanged(LocaleChangedEvent event);

    }

    public final class LocaleChangedEvent extends AbstractEvent<Translator, Listener> {

        private final Locale oldLocale;

        private final Locale newLocale;

        /**
         *
         * @param oldLocale
         * <br>Not null
         * <br>Shared
         * @param newLocale
         * <br>Not null
         * <br>Shared
         */
        public LocaleChangedEvent(final Locale oldLocale, final Locale newLocale) {
            this.oldLocale = oldLocale;
            this.newLocale = newLocale;
        }

        /**
         *
         * @return
         * <br>Not null
         * <br>Shared
         */
        public final Locale getNewLocale() {
            return this.newLocale;
        }

        /**
         *
         * @return
         * <br>Not null
         * <br>Shared
         */
        public final Locale getOldLocale() {
            return this.oldLocale;
        }

        @Override
        protected final void notifyListener(final Listener listener) {
            listener.localeChanged(this);
        }
        
    }

}
