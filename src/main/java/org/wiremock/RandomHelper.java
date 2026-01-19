package org.wiremock;

import com.github.jknack.handlebars.Options;
import com.github.tomakehurst.wiremock.extension.responsetemplating.helpers.HandlebarsHelper;
import java.util.Locale;
import java.util.Random;
import net.datafaker.Faker;

/*
 * Author: Kirill Tarasenko
 * Based on: Shreya Agarwal
 *
 * Behavior:
 * - No cache
 * - BCP-47 locale everywhere via Locale.forLanguageTag
 * - Locale can be provided via helper hash `locale` or system property -Dfaker.locale=...
 * - Seed supported via hash `seed` (seed.hashCode())
 * - Default Faker stored once (like original)
 * - Escaped quote fix: \\' -> '
 */
public class RandomHelper extends HandlebarsHelper<Object> {

  private static final String SEED_HASH = "seed";
  private static final String LOCALE_HASH = "locale";
  private static final String LOCALE_PROP = "faker.locale";

  private final Random random;
  private final Faker faker;

  public RandomHelper() {
    this(new Random());
  }

  RandomHelper(Random random) {
    this.random = random;
    this.faker = createDefaultFaker(random);
  }

  @Override
  public Object apply(Object context, Options options) {
    // Use a seeded/localized/default Faker if `locale` or `seed` hash exist
    final Faker faker = this.getFaker(options.hash(SEED_HASH), options.hash(LOCALE_HASH));

    try {
      String expr = String.valueOf(context).replace("\\'", "'");
      return faker.expression("#{" + expr + "}");
    } catch (RuntimeException e) {
      return handleError("Unable to evaluate the expression " + context, e);
    }
  }

  private Faker getFaker(Object seed, String localeValue) {
    Locale locale = resolveLocale(localeValue);

    if (seed != null && locale != null) {
      return new Faker(locale, new Random(seed.hashCode()));
    } else if (seed != null) {
      return new Faker(new Random(seed.hashCode()));
    } else if (locale != null) {
      return new Faker(locale, this.random);
    } else {
      return this.faker; // default cached faker (like original)
    }
  }

  private static Faker createDefaultFaker(Random random) {
    Locale locale = resolveLocale(null); // system property fallback only
    return locale != null ? new Faker(locale, random) : new Faker(random);
  }

  /**
   * Resolve locale using BCP-47 tags only.
   *
   * <p>Priority: 1) explicit helper hash argument: locale="en-US" 2) system property:
   * -Dfaker.locale=en-US 3) null -> default Faker locale
   */
  private static Locale resolveLocale(String localeValue) {
    String value =
        (localeValue != null && !localeValue.isBlank())
            ? localeValue
            : System.getProperty(LOCALE_PROP);

    if (value == null || value.isBlank()) {
      return null;
    }

    Locale locale = Locale.forLanguageTag(value);
    return locale.getLanguage().isEmpty() ? null : locale;
  }
}
