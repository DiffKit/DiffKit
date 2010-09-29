/**
 * Copyright © 2010, Joseph Panico
 *	All rights reserved.
 */
package org.diffkit.util;

import java.util.Arrays;

import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.AbstractXmlApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

/**
 * @author jpanico
 */
public class DKSpringUtil {

   private static final Logger LOG = LoggerFactory.getLogger(DKSpringUtil.class);

   /**
    * @return will only return non-null value, or throw RuntimeException
    */
   public static Object getBean(String beanName_, String[] xmlConfigFilePaths_,
                                ClassLoader classLoader_) {
      LOG.debug("beanName_->{}", beanName_);
      LOG.debug("xmlConfigFilePaths_->{}", Arrays.toString(xmlConfigFilePaths_));
      if (beanName_ == null)
         return null;
      if (ArrayUtils.isEmpty(xmlConfigFilePaths_))
         return null;
      AbstractXmlApplicationContext context = null;
      if (DKFileUtil.canReadFilePaths(xmlConfigFilePaths_))
         context = new FileSystemXmlApplicationContext(xmlConfigFilePaths_, false);
      else
         context = new ClassPathXmlApplicationContext(xmlConfigFilePaths_, false);
      LOG.debug("context->{}", context);
      if (classLoader_ == null)
         classLoader_ = DKSpringUtil.class.getClassLoader();
      context.setClassLoader(classLoader_);
      context.refresh();

      Object bean = context.getBean(beanName_);
      LOG.debug("bean->{}", bean);
      if (bean == null)
         throw new RuntimeException(String.format(
            "no bean named->%s in xmlConfigFilePaths_->%s", beanName_,
            Arrays.toString(xmlConfigFilePaths_)));
      return bean;
   }
}
