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
@SuppressWarnings("unused")
public class SuperTarget {

   private final String _superVarA;
   private final String superVarA;
   public String _superVarB;
   private String _superVarC;

   public SuperTarget(String superVarA_, String superVarB_, String superVarC_) {
      _superVarA = null;
      superVarA = superVarA_;
      _superVarB = superVarB_;
      _superVarC = superVarC_;
   }

   public String getPointerToSuperVarC() {
      return _superVarC;
   }

   public void setPointerToSuperVarC(String superVarC_) {
      _superVarC = superVarC_;
   }

   public void setSuperVarB(String superVarB_) {
      _superVarB = superVarB_;
   }

   public String getSuperVarA() {
      return superVarA;
   }

   public String toString() {
      return ReflectionToStringBuilder.toString(this);
   }
}
