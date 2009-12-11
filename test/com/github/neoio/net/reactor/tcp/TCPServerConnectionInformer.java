package com.github.neoio.net.reactor.tcp;

import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.neoio.net.connection.ConnectionKey;
import com.github.neoio.net.reactor.ClientConnectionInformer;
import com.github.neoio.net.reactor.ServerConnectionInformer;
import com.github.neoio.net.reactor.tcp.TCPIOReactor;
import com.github.neoio.nio.util.NIOUtils;

class TCPServerConnectionInformer implements ServerConnectionInformer
{
   private static Logger logger=LoggerFactory.getLogger(TCPServerConnectionInformer.class);
   private TCPIOReactor reactor;
   private List<String> messages=new ArrayList<String>();
   private ConnectionKey key;
   private CountDownLatch messageCount;
   private CountDownLatch boundBarrier=new CountDownLatch(2); 
   
   public TCPServerConnectionInformer(TCPIOReactor reactor, int messageCount)
   {
      this.reactor=reactor;
      this.messageCount=new CountDownLatch(messageCount);
   }
   @Override
   public void clientConnected(ConnectionKey key, SocketAddress socketAddress)
   {
      this.key=key;
      reactor.registerConnectionInformer(key, new ClientConnectionInformer()
      {
         @Override
         public void unableToConnect(SocketAddress socketAddress)
         {
         
         }
         @Override
         public void received(ConnectionKey connectionKey, ReadableByteChannel message, long messageSize)
         {
            logger.debug("Server message received: {}", messageSize);
            
            ByteBuffer buffer=ByteBuffer.allocate((int) messageSize);
            logger.debug("bytes read from channel: {}", NIOUtils.readToBuffer(message, buffer));
            buffer.flip();
            logger.debug("Buffer limit: {}", buffer.limit());
            String str=TestTCPIOReactor.charset.decode(buffer).toString();
            logger.debug(str);
            messages.add(str);
            messageCount.countDown();
         }
         @Override
         public void closed(ConnectionKey connectionKey)
         {
         
         }
         @Override
         public void ableToConnect(SocketAddress endPointAddress)
         {
            // TODO Auto-generated method stub
            
         }
      });
   }
   @Override
   public void serverBound(SocketAddress bindAddress)
   {
      this.boundBarrier.countDown();
   }
   public void awaitServerBind()
   {
      try
      {
         this.boundBarrier.countDown();
         this.boundBarrier.await();
      }
      catch(InterruptedException e)
      {
         e.printStackTrace();
      }
   }
   @Override
   public void unableToBindServer(SocketAddress socketAddress)
   {
      System.err.println("Unable to bind server to " + socketAddress);
   }
   public List<String> getMessages()
   {
      return messages;
   }
   public ConnectionKey getKey()
   {
      return key;
   }
   public void waitForAllMessages()
   {
      try
      {
         messageCount.await();
      }
      catch(InterruptedException e)
      {
         e.printStackTrace();
      }
   }
}
