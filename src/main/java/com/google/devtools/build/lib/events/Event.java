// Copyright 2014 Google Inc. All rights reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//    http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package com.google.devtools.build.lib.events;

import static java.nio.charset.StandardCharsets.ISO_8859_1;

import com.google.common.base.Preconditions;

import java.io.Serializable;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

/**
 * An event is a situation encountered by the build system that's worth
 * reporting: A 3-tuple of ({@link EventKind}, {@link Location}, message).
 */
@Immutable
public final class Event implements Serializable {

  private final EventKind kind;
  private final Location location;
  private final String message;
  /**
   * An alternative representation for message.
   * Exactly one of message or messageBytes will be non-null.
   * If messageBytes is non-null, then it contains the bytes
   * of the message, encoded using the platform's default charset.
   * We do this to avoid converting back and forth between Strings
   * and bytes.
   */
  private final byte[] messageBytes;

  @Nullable
  private final String tag;

  public Event withTag(String tag) {
    if (this.message != null) {
      return new Event(this.kind, this.location, this.message, tag);
    } else {
      return new Event(this.kind, this.location, this.messageBytes, tag);
    }
  }

  public Event(EventKind kind, @Nullable Location location, String message) {
    this(kind, location, message, null);
  }

  public Event(EventKind kind, @Nullable Location location, String message, @Nullable String tag) {
    this.kind = kind;
    this.location = location;
    this.message = Preconditions.checkNotNull(message);
    this.messageBytes = null;
    this.tag = tag;
  }

  public Event(EventKind kind, @Nullable Location location, byte[] messageBytes) {
    this(kind, location, messageBytes, null);
  }

  public Event(
      EventKind kind, @Nullable Location location, byte[] messageBytes, @Nullable String tag) {
    this.kind = kind;
    this.location = location;
    this.message = null;
    this.messageBytes = Preconditions.checkNotNull(messageBytes);
    this.tag = tag;
  }

  public String getMessage() {
    return message != null ? message : new String(messageBytes);
  }

  public byte[] getMessageBytes() {
    return messageBytes != null ? messageBytes : message.getBytes(ISO_8859_1);
  }

  public EventKind getKind() {
    return kind;
  }

  /**
   * the tag is typically the action that generated the event.
   */
  @Nullable
  public String getTag() {
    return tag;
  }

  /**
   * Returns the location of this event, if any.  Returns null iff the event
   * wasn't associated with any particular location, for example, a progress
   * message.
   */
  @Nullable public Location getLocation() {
    return location;
  }

  /**
   * Returns <i>some</i> moderately sane representation of the event. Should never be used in
   * user-visible places, only for debugging and testing.
   */
  @Override
  public String toString() {
    return kind + " " + (location != null ? location.print() : "<no location>") + ": "
        + getMessage();
  }

  /**
   * Replay a sequence of events on an {@link EventHandler}.
   */
  public static void replayEventsOn(EventHandler eventHandler, Iterable<Event> events) {
    for (Event event : events) {
      eventHandler.handle(event);
    }
  }

  /**
   * Reports a warning.
   */
  public static Event warn(Location location, String message) {
    return new Event(EventKind.WARNING, location, message);
  }

  /**
   * Reports an error.
   */
  public static Event error(Location location, String message){
    return new Event(EventKind.ERROR, location, message);
  }

  /**
   * Reports atemporal statements about the build, i.e. they're true for the duration of execution.
   */
  public static Event info(Location location, String message) {
    return new Event(EventKind.INFO, location, message);
  }

  /**
   * Reports a temporal statement about the build.
   */
  public static Event progress(Location location, String message) {
    return new Event(EventKind.PROGRESS, location, message);
  }

  /**
   * Reports a warning.
   */
  public static Event warn(String message) {
    return warn(null, message);
  }

  /**
   * Reports an error.
   */
  public static Event error(String message){
    return error(null, message);
  }

  /**
   * Reports atemporal statements about the build, i.e. they're true for the duration of execution.
   */
  public static Event info(String message) {
    return info(null, message);
  }

  /**
   * Reports a temporal statement about the build.
   */
  public static Event progress(String message) {
    return progress(null, message);
  }

}
