package org.altoro.core.capsule;

public interface ProtoCapsule<T> {

  byte[] getData();

  T getInstance();
}
