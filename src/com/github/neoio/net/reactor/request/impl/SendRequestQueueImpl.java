/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.neoio.net.reactor.request.impl;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.Semaphore;

import com.github.neoio.net.connection.ConnectionKey;
import com.github.neoio.net.message.Message;
import com.github.neoio.net.reactor.request.SendRequestQueue;


public class SendRequestQueueImpl implements SendRequestQueue
{
   private Semaphore mapaphore=new Semaphore(1, true);
   private Map<ConnectionKey, LinkedList<Message>> keyListMap=new TreeMap<ConnectionKey, LinkedList<Message>>(new Comparator<ConnectionKey>()
   {
      @Override
      public int compare(ConnectionKey o1, ConnectionKey o2)
      {
         return o1.uniqueKey()-o2.uniqueKey();
      }
   });
   
   @Override
   public Message getCurrent(ConnectionKey connectionKey)
   {
      mapaphore.acquireUninterruptibly();
      
      try
      {
         if(keyListMap.containsKey(connectionKey) == true)
            return keyListMap.get(connectionKey).peekLast();
         else
            return null;
      }
      finally
      {
         mapaphore.release();
      }
   }
   @Override
   public Message removeCurrent(ConnectionKey connectionKey)
   {
      mapaphore.acquireUninterruptibly();
      
      try
      {
         if(keyListMap.containsKey(connectionKey) == true)
         {
            LinkedList<Message> list=keyListMap.get(connectionKey);
            
            Message msg=list.removeLast();
            
            if(list.isEmpty() == true)
               keyListMap.remove(connectionKey);
            
            return msg;
         }
         else
            return null;
      }
      finally
      {
         mapaphore.release();
      }
   }
   @Override
   public void add(ConnectionKey connectionKey, Message sendRequest)
   {
      mapaphore.acquireUninterruptibly();
      
      try
      {
         if(keyListMap.containsKey(connectionKey) == true)
            keyListMap.get(connectionKey).addFirst(sendRequest);
         else
         {
            LinkedList<Message> list=new LinkedList<Message>();
            
            list.add(sendRequest);
            keyListMap.put(connectionKey, list);
         }
      }
      finally
      {
         mapaphore.release();
      }
   }
   @Override
   public boolean contains(ConnectionKey connectionKey)
   {
      try
      {
         mapaphore.acquireUninterruptibly();
         
         return keyListMap.containsKey(connectionKey);
      }
      finally
      {
         mapaphore.release();
      }
   }
   @Override
   public void clear(ConnectionKey connectionKey)
   {
      mapaphore.acquireUninterruptibly();
      
      if(keyListMap.containsKey(connectionKey) == true)
      {
         keyListMap.get(connectionKey).clear();
         keyListMap.remove(connectionKey);
      }
      
      mapaphore.release();
   }
}
