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
import java.util.Arrays;
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
   * If key > max key, Valid() return false.
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

  /**
   * Seek to the last key that is less than or equal to the target key.
   * @url https://github.com/facebook/rocksdb/wiki/SeekForPrev
   * <pre>
   *  Seek(target);
   *  if (!Valid()) {
   *    SeekToLast();
   *  } else if (key() != target) {
   *    Prev();
   *  }
   * <pre>
   * @param key
   */
  public void seekForPrev(byte[] key) {
    try {
      iterator.seek(key);
      if (!Valid()) {
        iterator.seekToLast();
      } else if (!Arrays.equals(iterator.key(), key)) {
        iterator.prev();
      }
    } catch (NativeDB.DBException e) {
      if (e.isNotFound()) {
        throw new NoSuchElementException();
      } else {
        throw new RuntimeException(e);
      }
    }
  }

  /**
   * Position at the first key in the source.  The iterator is Valid()
   * after this call iff the source is not empty.
   */
  public void seekToFirst() {
    iterator.seekToFirst();
  }

  /**
   *  Position at the last key in the source.  The iterator is
   *  Valid() after this call iff the source is not empty.
   */
  public void seekToLast() {
    iterator.seekToLast();
  }

  /**
   * An iterator is either positioned at a key/value pair, or
  *  not valid.  This method returns true iff the iterator is valid.
   */
  @Override
  public boolean Valid() {
    return iterator.isValid();
  }

  /**
   * the returned slice is valid only until the next modification of
   * the iterator.
   * REQUIRES: Valid()
   * @return the key for the current entry.
   */
  @Override
  public byte[] key() {
    try {
      return iterator.key();
    } catch (NativeDB.DBException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   *  the returned slice is valid only until the next modification of
   *  the iterator.
   *  REQUIRES: Valid()
   * @return the value for the current entry.
   */
  @Override
  public byte[] value() {
    try {
      return iterator.value();
    } catch (NativeDB.DBException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   *
   * @return the current entry.
   */
  @Deprecated
  public Map.Entry<byte[], byte[]> peekNext() {
    if (!iterator.isValid()) {
      throw new NoSuchElementException();
    }
    try {
      return new AbstractMap.SimpleImmutableEntry<>(iterator.key(), iterator.value());
    } catch (NativeDB.DBException e) {
      throw new RuntimeException(e);
    }
  }

  public boolean hasNext() {
    return iterator.isValid();
  }

  /**
   * Moves to the next entry in the source.  After this call, Valid() is
   * true iff the iterator was not positioned at the last entry in the source.
   * REQUIRES: Valid()
   * @return the current entry.
   */
  public Map.Entry<byte[], byte[]> next() {
    Map.Entry<byte[], byte[]> rc = this.peekNext();
    try {
      iterator.next();
    } catch (NativeDB.DBException e) {
      throw new RuntimeException(e);
    }
    return rc;
  }

  /**
   * Keep same as hasNext.
   * Used in combination with pre().
   * @return
   */
  public boolean hasPrev() {
    return iterator.isValid();
  }

  /**
   * Keep same as peekNext
   * @return the current entry.
   */
  @Deprecated
  public Map.Entry<byte[], byte[]> peekPrev() {
    if (!iterator.isValid()) {
      throw new NoSuchElementException();
    }
    try {
      return new AbstractMap.SimpleImmutableEntry<>(iterator.key(), iterator.value());
    } catch (NativeDB.DBException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Moves to the previous entry in the source.  After this call, Valid() is
   * true iff the iterator was not positioned at the first entry in source.
   * REQUIRES: Valid()
   * @return the current entry.
   */
  public Map.Entry<byte[], byte[]> prev() {
    Map.Entry<byte[], byte[]> rc = this.peekPrev();
    try {
      iterator.prev();
    } catch (NativeDB.DBException e) {
      throw new RuntimeException(e);
    }
    return rc;
  }
}
