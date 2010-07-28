/**
 * Copyright © 2010, Joseph Panico
 *	All rights reserved.
 */
package org.diffkit.common.kvc.tst;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;

/**
 * @author jpanico
 * 
 */
public class SubTarget extends SuperTarget {

   private final String _subVarA;
   private final String subVarA;
   private final String _subVarB;
   private final String _subVarC;

   public SubTarget(String superVarA_, String superVarB_, String superVarC_,
                    String subVarA_, String subVarB_, String subVarC_) {
      super(superVarA_, superVarB_, superVarC_);
      _subVarA = null;
      subVarA = subVarA_;
      _subVarB = subVarB_;
      _subVarC = subVarC_;
   }

   private String getSubVarC() {
      return _subVarC;
   }

   private String getPointerToSubVarC() {
      return _subVarC;
   }

   public String toString() {
      return ReflectionToStringBuilder.toString(this);
   }
}
