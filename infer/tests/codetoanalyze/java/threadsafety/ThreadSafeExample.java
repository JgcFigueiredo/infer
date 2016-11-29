/*
 * Copyright (c) 2016 - present Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the BSD style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */

package codetoanalyze.java.checkers;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import java.util.concurrent.locks.Lock;

@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.CLASS)
@interface ThreadSafe {
}

@ThreadSafe
public class ThreadSafeExample{

  /*Included to make sure infer does not report on class initializers*/
  static Class<?> A = ThreadSafeExample.class;

  Integer f;

  public ThreadSafeExample() {
    f = 86;
  }

  public void tsOK() {
    synchronized (this) {
      f = 42;
    }
  }

  public void tsBad() {
    f = 24;
  }

  Lock mLock;

  public void tsBadInOneBranch(boolean b) {
    if (b) {
      mLock.lock();
    }
    f = 24;
    if (b) {
      mLock.unlock();
    }
  }

  // doesn't work because we don't model lock
  public void FP_tsWithLockOk() {
    mLock.lock();
    f = 42;
    mLock.unlock();
  }

  // doesn't work because we don't model lock
  public void FP_tsWithLockBothBranchesOk(boolean b) {
    if (b) {
      mLock.lock();
    } else {
      mLock.lock();
    }
    f = 42;
    mLock.unlock();
  }

}

class ExtendsThreadSafeExample extends ThreadSafeExample{

  Integer field;

  /* Presently,we will warn not just on overwridden methods from
  @ThreadSafe class, but potentially on other methods in subclass */
  public void newmethodBad() {
     field = 22;
  }

  /* Bad now that it's overridden */
  public void tsOK() {
     field = 44;
  }

}
