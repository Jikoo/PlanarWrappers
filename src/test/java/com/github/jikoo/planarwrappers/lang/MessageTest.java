package com.github.jikoo.planarwrappers.lang;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class MessageTest {

  @Test
  void getPlaceholder() {
    Message message = new Message("path", "Message");
    assertThat("Placeholder is wrapped key", message.getPlaceholder(), is('{' + message.key() + '}'));
  }

  @Test
  void getValue() {
    Message message = new Message("path", "Message");
    assertThat("Value is default message", message.getValue(), is(message.defaultValue()));
  }

}