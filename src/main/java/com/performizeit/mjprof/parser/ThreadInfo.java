/*
       This file is part of mjprof.

        mjprof is free software: you can redistribute it and/or modify
        it under the terms of the GNU General Public License as published by
        the Free Software Foundation, either version 3 of the License, or
        (at your option) any later version.

        mjprof is distributed in the hope that it will be useful,
        but WITHOUT ANY WARRANTY; without even the implied warranty of
        MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
        GNU General Public License for more details.

        You should have received a copy of the GNU General Public License
        along with mjprof.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.performizeit.mjprof.parser;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Represents information about a thread, including its stack trace and properties.
 * Uses modern Java features like var, Optional, and enhanced Map handling.
 */
public class ThreadInfo {
  // Use HashMap specifically for backward compatibility
  private final HashMap<String, Object> props;

  public ThreadInfo(Map<String, Object> mtd) {
    this.props = new HashMap<>(mtd);
  }

  public ThreadInfo() {
    this.props = new HashMap<>();
  }

  public HashMap<String, Object> cloneMetaData() {
    return new HashMap<>(props);
  }

  /**
   * Gets a property value wrapped in Optional for null-safety.
   * Note: For direct backward compatibility use getVal(String) without Optional.
   */
  public Optional<Object> getValOptional(String key) {
    return Optional.ofNullable(props.get(key));
  }

  /**
   * Original method for backward compatibility with existing code.
   */
  public Object getVal(String key) {
    return props.get(key);
  }
  
  /**
   * Gets a property value with a default if it's null.
   */
  public Object getVal(String key, Object defaultValue) {
    return Optional.ofNullable(props.get(key)).orElse(defaultValue);
  }

  public Object setVal(String key, Object val) {
    return props.put(key, val);
  }

  public Set<String> getProps() {
    return props.keySet();
  }

  public ThreadInfo(String stackTrace) {
    var parser = new JStackTextualFormatParser();
    this.props = parser.parseLegacyTextualFormat(stackTrace);
  }

  protected String metadataProperty(String metaLine, String propertyName) {
    var pattern = Pattern.compile(".* " + propertyName + "=([0-9a-fx]*) .*");
    var matcher = pattern.matcher(metaLine);

    return matcher.find() ? matcher.group(1) : null;
  }

  @Override
  public String toString() {
    return new JStackTextualFormatParser().toJStackFormat(props);
  }
}
