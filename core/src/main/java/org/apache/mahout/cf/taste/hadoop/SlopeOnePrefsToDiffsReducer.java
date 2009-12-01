/**
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

package org.apache.mahout.cf.taste.hadoop;

import org.apache.hadoop.io.FloatWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reporter;
import org.apache.hadoop.mapred.Reducer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public final class SlopeOnePrefsToDiffsReducer
    extends MapReduceBase
    implements Reducer<LongWritable, ItemPrefWritable, ItemItemWritable, FloatWritable> {

  @Override
  public void reduce(LongWritable key,
                     Iterator<ItemPrefWritable> values,
                     OutputCollector<ItemItemWritable, FloatWritable> output,
                     Reporter reporter) throws IOException {
    List<ItemPrefWritable> prefs = new ArrayList<ItemPrefWritable>();
    while (values.hasNext()) {
      prefs.add(new ItemPrefWritable(values.next()));
    }
    Collections.sort(prefs, ByItemIDComparator.getInstance());
    int size = prefs.size();
    for (int i = 0; i < size; i++) {
      ItemPrefWritable first = prefs.get(i);
      long itemAID = first.getItemID();
      float itemAValue = first.getPrefValue();
      for (int j = i + 1; j < size; j++) {
        ItemPrefWritable second = prefs.get(j);
        long itemBID = second.getItemID();
        float itemBValue = second.getPrefValue();
        output.collect(new ItemItemWritable(itemAID, itemBID), new FloatWritable(itemBValue - itemAValue));
      }
    }
  }

}