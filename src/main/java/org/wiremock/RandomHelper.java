package org.wiremock;

import com.github.jknack.handlebars.Options;
import com.github.tomakehurst.wiremock.extension.responsetemplating.helpers.HandlebarsHelper;
import java.util.Locale;
import net.datafaker.Faker;

/*
 * Author: Shreya Agarwal
 */
public class RandomHelper extends HandlebarsHelper<Object> {
  private static final String LOCALE_PROP = "faker.locale";

  private final Faker faker;

  public RandomHelper() {
    this.faker = createFaker();
  }

  @Override
  public Object apply(Object context, Options options) {
    try {
      String expr = String.valueOf(context);

      expr = expr.replace("\\'", "'");

      return faker.expression("#{" + expr + "}");
    } catch (RuntimeException e) {
      return handleError("Unable to evaluate the expression " + context, e);
    }
  }

  private static Faker createFaker() {
    String raw = System.getProperty(LOCALE_PROP);

    if (raw == null || raw.isBlank()) {
      // Полная совместимость: как раньше
      return new Faker();
    }

    Locale locale = Locale.forLanguageTag(raw);

    if (locale.getLanguage().isEmpty()) {
      // Некорректная locale → fallback
      return new Faker();
    }

    return new Faker(locale);
  }
}
