/*
 * Copyright 2009 Red Hat, Inc.
 * Red Hat licenses this file to you under the Apache License, version
 * 2.0 (the "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *    http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.  See the License for the specific language governing
 * permissions and limitations under the License.
 */

package org.hornetq.utils;

import java.lang.ref.WeakReference;

import org.hornetq.core.logging.Logger;

/**
 * A MemorySize
 *
 * @author Clebert Suconic
 * @author Tim Fox
 *
 *
 */
public class MemorySize
{
   private static final Logger log = Logger.getLogger(MemorySize.class);

   private static final int numberOfObjects = 10000;

   private static Object newObject(final ObjectFactory factory) throws Exception
   {
      return factory.createObject();
   }
   
   public static boolean is64bitArch()
   {
      boolean is64bit = true; //Default to 64 e.g. if can't retrieve property
      
      try
      {
         String arch = System.getProperty("os.arch");
         
         if (arch != null)
         {
            is64bit = arch.contains("64");
         }
      }
      catch (Exception e)
      {
         //Ignore
      }
      
      return is64bit;
   }
   
   public interface ObjectFactory
   {
      Object createObject();
   }
   
   public static int calculateSize(final ObjectFactory factory) throws Exception
   {
      final Runtime runtime = Runtime.getRuntime();
      
      getMemorySize(runtime);
      
      newObject(factory);
      
      int i = 0;
      long heap1 = 0;
      long heap2 = 0;
      long totalMemory1 = 0;
      long totalMemory2 = 0;

      //First we do a dry run with twice as many then throw away the results
      
      Object[] obj = new Object[numberOfObjects * 2];
      
      for (i = 0; i < numberOfObjects * 2; i++)
      {
         obj[i] = newObject(factory);
      }
      
      obj = new Object[numberOfObjects * 2];
      
      heap1 = getMemorySize(runtime);

      totalMemory1 = runtime.totalMemory();

      for (i = 0; i < numberOfObjects; i++)
      {
         obj[i] = newObject(factory);
      }

      heap2 = getMemorySize(runtime);

      totalMemory2 = runtime.totalMemory();

      final int size = Math.round(((float)(heap2 - heap1)) / numberOfObjects);

      if (totalMemory1 != totalMemory2)
      {
         //throw new IllegalStateException("Warning: JVM allocated more data what would make results invalid " +
         //                              totalMemory1 + ":" + totalMemory2);
         
         log.warn("Warning: JVM allocated more data what would make results invalid " +
                                                totalMemory1 + ":" + totalMemory2);
      }

      return size;
   }

   private static long getMemorySize(final Runtime runtime)
   {
      for (int i = 0; i < 5; i++)
      {
         forceGC();
      }
      return runtime.totalMemory() - runtime.freeMemory();
   }
   
   private static void forceGC()
   {
      WeakReference<Object> dumbReference = new WeakReference<Object>(new Object());
      // A loop that will wait GC, using the minimal time as possible
      while (dumbReference.get() != null)
      {
         System.gc();
         try
         {
            Thread.sleep(500);
         }
         catch (InterruptedException e)
         {
         }
      }
   }

}