/**
 * Copyright 2010 Joseph Panico
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.diffkit.diff.conf;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import org.diffkit.diff.engine.DKDiffEngine;

/**
 * @author jpanico
 */
public class DKTool {
   private static final Logger LOG = LoggerFactory.getLogger(DKTool.class);

   public static void main(String[] args_) {
      LOG.info("args_->", Arrays.toString(args_));
      ApplicationContext context = new ClassPathXmlApplicationContext(args_);
      LOG.info("context->", context);
      DKPlan plan = (DKPlan) context.getBean("plan");
      LOG.info("plan->{}", plan);
      if (plan == null)
         throw new RuntimeException(String.format("no 'plan' bean in config files->",
            Arrays.toString(args_)));
      DKDiffEngine engine = new DKDiffEngine();
      LOG.info("engine->{}", engine);
      try {
         engine.diff(plan.getLhsSource(), plan.getRhsSource(), plan.getSink(),
            plan.getTableComparison());
         System.exit(0);
      }
      catch (Exception e_) {
         LOG.error(null, e_);
         System.exit(-1);
      }
   }
}
