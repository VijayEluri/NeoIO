package com.github.neoio.net.message.staging.file;

import java.io.File;


import org.apache.commons.lang.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.neoio.net.exception.NetIOException;
import com.github.neoio.net.message.staging.MessageStaging;
import com.github.neoio.net.message.staging.MessageStagingFactory;

public class FileMessageStagingFactory implements MessageStagingFactory
{
   private Logger logger=LoggerFactory.getLogger(this.getClass());
   private File stagingDir;
   
   public FileMessageStagingFactory()throws NetIOException
   {
      this(SystemUtils.getJavaIoTmpDir());
   }
   public FileMessageStagingFactory(File stagingDir)throws NetIOException
   {
      logger.debug("Factory staging directory: {}", stagingDir.getAbsolutePath());
      
      if(stagingDir.exists() == true && stagingDir.isDirectory() == false)
         throw new NetIOException("stagingDir [" + stagingDir.getName() + "] exists but is not a directory");
      else if(stagingDir.exists() == false)
         stagingDir.mkdirs();
      
      this.stagingDir=stagingDir;
   }
   @Override
   public MessageStaging newInstance()throws NetIOException
   {
      return new FileMessageStaging(stagingDir);
   }
}
