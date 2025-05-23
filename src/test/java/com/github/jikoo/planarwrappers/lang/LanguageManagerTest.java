package com.github.jikoo.planarwrappers.lang;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.List;
import java.util.logging.Logger;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class LanguageManagerTest {

  private final String parentLocale = "en";
  private final String childLocale = parentLocale + "_us";

  private final Message override = new Message("sample.override", "Message default");
  private final Message newKey = new Message("sample.newKey", "Message default");
  private final Message inheritedParent = new Message("sample.inherited.parent", "Message default");
  private final Message inheritedParentDefault = new Message("sample.inherited.parentDefault", "Message default");
  private final Message notPresent = new Message("notpresent", "Not present");

  private final List<Message> messages = List.of(
      override,
      newKey,
      inheritedParent,
      inheritedParentDefault,
      notPresent
  );

  private LocaleProvider provider;

  @BeforeEach
  void beforeEach() {
    provider = createLocaleProvider();
  }

  @Test
  void parentLocale() {
    LanguageManager manager = new LanguageManager(provider);

    String value = manager.getValue(parentLocale, override);
    assertThat("Value set by locale", value, is("Overridden"));

    value = manager.getValue(parentLocale, newKey);
    assertThat("Value is from bundle if not present", value, is("New Value"));

    value = manager.getValue(parentLocale, inheritedParent);
    assertThat("Value set by locale", value, is("From parent"));

    value = manager.getValue(parentLocale, inheritedParentDefault);
    assertThat("Value is from bundle if not present", value, is("From parent default"));
  }

  @Test
  void childLocale() {
    LanguageManager manager = new LanguageManager(provider);

    String value = manager.getValue(childLocale, override);
    assertThat("Value set by locale", value, is("Child override"));

    value = manager.getValue(childLocale, newKey);
    assertThat("Value is from bundle if not present", value, is("Child bundled value"));

    value = manager.getValue(childLocale, inheritedParent);
    assertThat("Value set by parent", value, is("From parent"));

    value = manager.getValue(childLocale, inheritedParentDefault);
    assertThat("Value is from bundled parent if not present", value, is("From parent default"));
  }

  @Test
  void childFullFallthrough() {
    String nonexistentChild = parentLocale + "_ca";

    assertThat("Child does not exist", provider.getLocaleFile(nonexistentChild).exists(), is(false));
    assertThat("Child bundle does not exist", provider.getLocaleBundle(nonexistentChild), is(nullValue()));

    LanguageManager manager = new LanguageManager(provider);

    for (Message message : messages) {
      assertThat(
          "Message is inherited",
          manager.getValue(nonexistentChild, message),
          is(manager.getValue(parentLocale, message))
      );
    }
  }

  @Test
  void nullIsDefaultLocale() {
    LanguageManager manager = new LanguageManager(provider);

    for (Message message : messages) {
      assertThat(
          "Default locale is used",
          manager.getValue((String) null, message),
          is(manager.getValue(parentLocale, message))
      );
    }
  }

  @Test
  void playerLocale() {
    LanguageManager manager = new LanguageManager(provider);
    Player sender = mock();
    doReturn(childLocale).when(sender).getLocale();

    for (Message message : messages) {
      assertThat(
          "Player locale is used",
          manager.getValue(sender, message),
          is(manager.getValue(childLocale, message))
      );
    }
  }

  @Test
  void otherSenderIsDefaultLocale() {
    LanguageManager manager = new LanguageManager(provider);
    CommandSender sender = mock();

    for (Message message : messages) {
      assertThat(
          "Default locale is used",
          manager.getValue(sender, message),
          is(manager.getValue(parentLocale, message))
      );
    }
  }

  @Test
  void blankIsNull() {
    LanguageManager manager = new LanguageManager(provider);
    Message message = new Message(notPresent.key(), "");

    assertThat("Blank message is null", manager.getValue(parentLocale, message), is(nullValue()));
  }

  @Test
  void replacementBlankIsNull() {
    LanguageManager manager = new LanguageManager(provider);
    Message message = new Message(notPresent.key(), "");

    assertThat("Blank message is null", manager.getValue(parentLocale, message, message), is(nullValue()));
  }

  @Test
  void replacementMessage() {
    LanguageManager manager = new LanguageManager(provider);
    Replacement replacement = new Message("placeholder", "Replacement");
    Message message = new Message(notPresent.key(), replacement.getPlaceholder());

    assertThat(
        "Replacement is performed",
        manager.getValue(parentLocale, message, replacement), is(replacement.getValue()));
  }

  @Test
  void replacementMessageBlank() {
    LanguageManager manager = new LanguageManager(provider);
    Replacement replacement = new Message("placeholder", "");
    Message message = new Message(notPresent.key(), replacement.getPlaceholder());

    assertThat(
        "Replacement is performed",
        manager.getValue(parentLocale, message, replacement), is(replacement.getValue()));
  }

  @Test
  void replacementOther() {
    LanguageManager manager = new LanguageManager(provider);
    Replacement replacement = new Replacement() {
      @Override
      public String getPlaceholder() {
        return "{placeholder}";
      }

      @Override
      public String getValue() {
        return "Replacement";
      }
    };
    Message message = new Message(notPresent.key(), replacement.getPlaceholder());

    assertThat(
        "Replacement is performed",
        manager.getValue(parentLocale, message, replacement), is(replacement.getValue()));
  }

  @Test
  void replacementSenderLocale() {
    LanguageManager manager = new LanguageManager(provider);
    CommandSender sender = mock();
    Replacement replacement = new Message("placeholder", "Replacement");
    Message message = new Message(notPresent.key(), replacement.getPlaceholder());

    assertThat(
        "Replacement is performed",
        manager.getValue(sender, message, replacement), is(replacement.getValue()));
  }

  private LocaleProvider createLocaleProvider() {
    return new LocaleProvider() {

      private final Logger logger = mock();

      @Override
      public @NotNull String getDefaultLocale() {
        return parentLocale;
      }

      @Override
      public @NotNull Iterable<@NotNull Message> getMessages() {
        return messages;
      }

      @Override
      public @NotNull File getLocaleFile(@NotNull String locale) {
        return new File("src/test/resources/lang/good/locale/" + locale + ".yml");
      }

      @Override
      public @Nullable InputStream getLocaleBundle(@NotNull String locale) {
        try {
          return Files.newInputStream(Path.of("src/test/resources/lang/good/bundle/locale/" + locale + ".yml"));
        } catch (IOException e) {
          if (e instanceof NoSuchFileException) {
            return null;
          }
          throw new RuntimeException(e);
        }
      }

      @Override
      public @NotNull Logger getLogger() {
        return logger;
      }

    };
  }

}
