package org.apache.lucene.benchmark.byTask.feeds;

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.lucene.benchmark.byTask.utils.Config;
import org.apache.lucene.benchmark.byTask.utils.Format;

/**
 * Base class for source of data for benchmarking
 * <p>
 * Keeps track of various statistics, such as how many data items were generated, 
 * size in bytes etc.
 * <p>
 * Supports the following configuration parameters:
 * <ul>
 * <li><b>content.source.forever</b> - specifies whether to generate items
 * forever (<b>default=true</b>).
 * <li><b>content.source.verbose</b> - specifies whether messages should be
 * output by the content source (<b>default=false</b>).
 * <li><b>content.source.encoding</b> - specifies which encoding to use when
 * reading the files of that content source. Certain implementations may define
 * a default value if this parameter is not specified. (<b>default=null</b>).
 * <li><b>content.source.log.step</b> - specifies for how many items a
 * message should be logged. If set to 0 it means no logging should occur.
 * <b>NOTE:</b> if verbose is set to false, logging should not occur even if
 * logStep is not 0 (<b>default=0</b>).
 * </ul>
 */
public abstract class ContentItemsSource implements Closeable {
  
  private long bytesCount;
  private long totalBytesCount;
  private int itemCount;
  private int totalItemCount;
  private Config config;

  private int lastPrintedNumUniqueTexts = 0;
  private long lastPrintedNumUniqueBytes = 0;
  private int printNum = 0;

  protected boolean forever;
  protected int logStep;
  protected boolean verbose;
  protected String encoding;
  
  /** update count of bytes generated by this source */  
  protected final synchronized void addBytes(long numBytes) {
    bytesCount += numBytes;
    totalBytesCount += numBytes;
  }
  
  /** update count of items generated by this source */  
  protected final synchronized void addItem() {
    ++itemCount;
    ++totalItemCount;
  }

  /**
   * A convenience method for collecting all the files of a content source from
   * a given directory. The collected {@link File} instances are stored in the
   * given <code>files</code>.
   */
  protected final void collectFiles(File dir, ArrayList<File> files) {
    if (!dir.canRead()) {
      return;
    }
    
    File[] dirFiles = dir.listFiles();
    Arrays.sort(dirFiles);
    for (int i = 0; i < dirFiles.length; i++) {
      File file = dirFiles[i];
      if (file.isDirectory()) {
        collectFiles(file, files);
      } else if (file.canRead()) {
        files.add(file);
      }
    }
  }

  /**
   * Returns true whether it's time to log a message (depending on verbose and
   * the number of items generated).
   */
  protected final boolean shouldLog() {
    return verbose && logStep > 0 && itemCount % logStep == 0;
  }

  /** Called when reading from this content source is no longer required. */
  @Override
  public abstract void close() throws IOException;
  
  /** Returns the number of bytes generated since last reset. */
  public final long getBytesCount() { return bytesCount; }

  /** Returns the number of generated items since last reset. */
  public final int getItemsCount() { return itemCount; }
  
  public final Config getConfig() { return config; }

  /** Returns the total number of bytes that were generated by this source. */ 
  public final long getTotalBytesCount() { return totalBytesCount; }

  /** Returns the total number of generated items. */
  public final int getTotalItemsCount() { return totalItemCount; }

  /**
   * Resets the input for this content source, so that the test would behave as
   * if it was just started, input-wise.
   * <p>
   * <b>NOTE:</b> the default implementation resets the number of bytes and
   * items generated since the last reset, so it's important to call
   * super.resetInputs in case you override this method.
   */
  public void resetInputs() throws IOException {
    bytesCount = 0;
    itemCount = 0;
  }

  /**
   * Sets the {@link Config} for this content source. If you override this
   * method, you must call super.setConfig.
   */
  public void setConfig(Config config) {
    this.config = config;
    forever = config.get("content.source.forever", true);
    logStep = config.get("content.source.log.step", 0);
    verbose = config.get("content.source.verbose", false);
    encoding = config.get("content.source.encoding", null);
  }

  public void printStatistics(String itemsName) {
    if (!verbose) {
      return;
    }
    boolean print = false;
    String col = "                  ";
    StringBuilder sb = new StringBuilder();
    String newline = System.getProperty("line.separator");
    sb.append("------------> ").append(getClass().getSimpleName()).append(" statistics (").append(printNum).append("): ").append(newline);
    int nut = getTotalItemsCount();
    if (nut > lastPrintedNumUniqueTexts) {
      print = true;
      sb.append("total count of "+itemsName+": ").append(Format.format(0,nut,col)).append(newline);
      lastPrintedNumUniqueTexts = nut;
    }
    long nub = getTotalBytesCount();
    if (nub > lastPrintedNumUniqueBytes) {
      print = true;
      sb.append("total bytes of "+itemsName+": ").append(Format.format(0,nub,col)).append(newline);
      lastPrintedNumUniqueBytes = nub;
    }
    if (getItemsCount() > 0) {
      print = true;
      sb.append("num "+itemsName+" added since last inputs reset:   ").append(Format.format(0,getItemsCount(),col)).append(newline);
      sb.append("total bytes added for "+itemsName+" since last inputs reset: ").append(Format.format(0,getBytesCount(),col)).append(newline);
    }
    if (print) {
      System.out.println(sb.append(newline).toString());
      printNum++;
    }
  }
  
}
