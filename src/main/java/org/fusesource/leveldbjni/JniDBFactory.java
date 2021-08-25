/*
 * Copyright (C) 2011, FuseSource Corp.  All rights reserved.
 *
 *     http://fusesource.com
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 *    * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *    * Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following disclaimer
 * in the documentation and/or other materials provided with the
 * distribution.
 *    * Neither the name of FuseSource Corp. nor the names of its
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.fusesource.leveldbjni;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import org.fusesource.leveldbjni.internal.JniDB;
import org.fusesource.leveldbjni.internal.NativeBuffer;
import org.fusesource.leveldbjni.internal.NativeCache;
import org.fusesource.leveldbjni.internal.NativeComparator;
import org.fusesource.leveldbjni.internal.NativeCompressionType;
import org.fusesource.leveldbjni.internal.NativeDB;
import org.fusesource.leveldbjni.internal.NativeFilter;
import org.fusesource.leveldbjni.internal.NativeLogger;
import org.fusesource.leveldbjni.internal.NativeOptions;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.DBComparator;
import org.iq80.leveldb.DBFactory;
import org.iq80.leveldb.Logger;
import org.iq80.leveldb.Options;

/**
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
public class JniDBFactory implements DBFactory {

  public static final JniDBFactory factory = new JniDBFactory();
  public static final String VERSION;

  static {
    NativeDB.LIBRARY.load();
  }

  static {
    String v = "unknown";
    try (InputStream is = JniDBFactory.class.getResourceAsStream("version.txt")) {
      v = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8)).readLine();
    } catch (Throwable e) {
    }
    VERSION = v;
  }

  public static byte[] bytes(String value) {
    if (value == null) {
      return null;
    }
    return value.getBytes(StandardCharsets.UTF_8);
  }

  public static String asString(byte[] value) {
    if (value == null) {
      return null;
    }
    return new String(value, StandardCharsets.UTF_8);
  }

  public static void pushMemoryPool(int size) {
    NativeBuffer.pushMemoryPool(size);
  }

  public static void popMemoryPool() {
    NativeBuffer.popMemoryPool();
  }

  public DB open(File path, Options options) throws IOException {
    NativeDB db = null;
    OptionsResourceHolder holder = new OptionsResourceHolder();
    try {
      holder.init(options);
      db = NativeDB.open(holder.options, path);
    } finally {
      // if we could not open up the DB, then clean up the
      // other allocated native resouces..
      if (db == null) {
        holder.close();
      }
    }
    return new JniDB(db, holder.cache, holder.filter, holder.comparator, holder.logger);
  }

  public void destroy(File path, Options options) throws IOException {
    OptionsResourceHolder holder = new OptionsResourceHolder();
    try {
      holder.init(options);
      NativeDB.destroy(path, holder.options);
    } finally {
      holder.close();
    }
  }

  public void repair(File path, Options options) throws IOException {
    OptionsResourceHolder holder = new OptionsResourceHolder();
    try {
      holder.init(options);
      NativeDB.repair(path, holder.options);
    } finally {
      holder.close();
    }
  }

  @Override
  public String toString() {
    return String.format("leveldbjni version %s", VERSION);
  }

  private static class OptionsResourceHolder {

    NativeCache cache = null;
    NativeFilter filter = null;
    NativeComparator comparator = null;
    NativeLogger logger = null;
    NativeOptions options;

    public void init(Options value) {

      options = new NativeOptions();
      options.blockRestartInterval(value.blockRestartInterval());
      options.blockSize(value.blockSize());
      options.createIfMissing(value.createIfMissing());
      options.errorIfExists(value.errorIfExists());
      options.maxOpenFiles(value.maxOpenFiles());
      options.paranoidChecks(value.paranoidChecks());
      options.writeBufferSize(value.writeBufferSize());
      options.reuseLogs(value.reuseLogs());
      options.maxFileSize(value.maxFileSize());

      switch (value.compressionType()) {
        case NONE:
          options.compression(NativeCompressionType.kNoCompression);
          break;
        case SNAPPY:
          options.compression(NativeCompressionType.kSnappyCompression);
          break;
      }


      if (value.cacheSize() > 0) {
        cache = new NativeCache(value.cacheSize());
        options.cache(cache);
      }

      if (value.bitsPerKey() > 0) {
        filter = new NativeFilter(value.bitsPerKey());
        options.filter(filter);
      }

      final DBComparator userComparator = value.comparator();
      if (userComparator != null) {
        comparator = new NativeComparator() {
          @Override
          public int compare(byte[] key1, byte[] key2) {
            return userComparator.compare(key1, key2);
          }

          @Override
          public String name() {
            return userComparator.name();
          }
        };
        options.comparator(comparator);
      }

      final Logger userLogger = value.logger();
      if (userLogger != null) {
        logger = new NativeLogger() {
          @Override
          public void log(String message) {
            userLogger.log(message);
          }
        };
        options.infoLog(logger);
      }

    }

    public void close() {
      if (cache != null) {
        cache.delete();
      }
      if (filter != null) {
        filter.delete();
      }
      if (comparator != null) {
        comparator.delete();
      }
      if (logger != null) {
        logger.delete();
      }
    }
  }
}
