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

package org.fusesource.leveldbjni.internal;

import java.util.AbstractMap;
import java.util.Map;
import java.util.NoSuchElementException;
import org.iq80.leveldb.DBIterator;

/**
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
public class JniDBIterator implements DBIterator {

  private final NativeIterator iterator;

  JniDBIterator(NativeIterator iterator) {
    this.iterator = iterator;
  }

  public void close() {
    iterator.delete();
  }

  public void remove() {
    throw new UnsupportedOperationException();
  }

  /**
   * Position at the first key in the source that is at or past target.
   * The iterator is Valid() after this call iff the source contains
   * an entry that comes at or past target.
   *
   * @param key
   */
  public void seek(byte[] key) {
    try {
      iterator.seek(key);
    } catch (NativeDB.DBException e) {
      if (e.isNotFound()) {
        throw new NoSuchElementException();
      } else {
        throw new RuntimeException(e);
      }
    }
  }

  public void seekToFirst() {
    iterator.seekToFirst();
  }

  public void seekToLast() {
    iterator.seekToLast();
  }

  @Override
  public boolean Valid() {
    return iterator.isValid();
  }

  @Override
  public byte[] key() {
    try {
      return iterator.key();
    } catch (NativeDB.DBException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public byte[] value() {
    try {
      return iterator.value();
    } catch (NativeDB.DBException e) {
      throw new RuntimeException(e);
    }
  }


  public Map.Entry<byte[], byte[]> peekNext() {
    return this.entry();
  }

  public boolean hasNext() {
    return this.Valid();
  }

  public Map.Entry<byte[], byte[]> next() {
    Map.Entry<byte[], byte[]> rc = this.peekNext();
    try {
      iterator.next();
    } catch (NativeDB.DBException e) {
      throw new RuntimeException(e);
    }
    return rc;
  }

  public boolean hasPrev() {
    return this.Valid();
  }

  public Map.Entry<byte[], byte[]> peekPrev() {
    return this.entry();
  }

  public Map.Entry<byte[], byte[]> prev() {
    try {
      Map.Entry<byte[], byte[]> rc = this.peekPrev();
      iterator.prev();
      return rc;
    } catch (NativeDB.DBException e) {
      throw new RuntimeException(e);
    }
  }

  private Map.Entry<byte[], byte[]> entry() {
    if (!iterator.isValid()) {
      throw new NoSuchElementException();
    }
    try {
      return new AbstractMap.SimpleImmutableEntry<>(iterator.key(), iterator.value());
    } catch (NativeDB.DBException e) {
      throw new RuntimeException(e);
    }
  }
}
