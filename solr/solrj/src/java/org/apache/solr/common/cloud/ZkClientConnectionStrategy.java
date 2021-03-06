package org.apache.solr.common.cloud;

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with this
 * work for additional information regarding copyright ownership. The ASF
 * licenses this file to You under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

import org.apache.solr.common.SolrException;
import org.apache.zookeeper.Watcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public abstract class ZkClientConnectionStrategy {
  private static Logger log = LoggerFactory.getLogger(ZkClientConnectionStrategy.class);
  
  private List<DisconnectedListener> disconnectedListeners = new ArrayList<DisconnectedListener>();
  private List<ConnectedListener> connectedListeners = new ArrayList<ConnectedListener>();
  
  public abstract void connect(String zkServerAddress, int zkClientTimeout, Watcher watcher, ZkUpdate updater) throws IOException, InterruptedException, TimeoutException;
  public abstract void reconnect(String serverAddress, int zkClientTimeout, Watcher watcher, ZkUpdate updater) throws IOException, InterruptedException, TimeoutException;
  
  public synchronized void disconnected() {
    for (DisconnectedListener listener : disconnectedListeners) {
      try {
        listener.disconnected();
      } catch (Throwable t) {
        SolrException.log(log, "", t);
      }
    }
  }
  
  public synchronized void connected() {
    for (ConnectedListener listener : connectedListeners) {
      try {
        listener.connected();
      } catch (Throwable t) {
        SolrException.log(log, "", t);
      }
    }
  }
  
  public interface DisconnectedListener {
    public void disconnected();
  };
  
  public interface ConnectedListener {
    public void connected();
  };
  
  
  public synchronized void addDisconnectedListener(DisconnectedListener listener) {
    disconnectedListeners.add(listener);
  }
  
  public synchronized void addConnectedListener(ConnectedListener listener) {
    connectedListeners.add(listener);
  }
  
  public static abstract class ZkUpdate {
    public abstract void update(SolrZooKeeper zooKeeper) throws InterruptedException, TimeoutException, IOException;
  }
  
}
