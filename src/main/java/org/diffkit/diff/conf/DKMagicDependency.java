/**
 * Copyright 2010-2011 Joseph Panico
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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

import org.apache.commons.lang.ClassUtils;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.diffkit.common.DKConstructor;
import org.diffkit.common.DKValidate;
import org.diffkit.util.DKClassUtil;

/**
 * @author jpanico
 */
public class DKMagicDependency<T> {
   private static final ObjectUtils.Null NULL_RESOLUTION = ObjectUtils.NULL;

   private final DKMagicDependency<?> _parentDependency;
   private DKMagicDependency<?>[] _dependencies;
   private final String _parentConstructorParmName;
   private final Class<?> _originalTargetClass;
   private Class<T> _targetClass;
   private DKConstructor<T> _constructor;
   private Object _resolution;
   private final Logger _log = LoggerFactory.getLogger(this.getClass());

   DKMagicDependency(DKMagicDependency<?> parentDependency_,
                     String parentConstructorParmName_, Class<T> targetClass_) {
      _parentDependency = parentDependency_;
      _originalTargetClass = targetClass_;
      _targetClass = targetClass_;
      _parentConstructorParmName = parentConstructorParmName_;
      DKValidate.notNull(_targetClass);
   }

   public DKMagicDependency<?> getParentDependency() {
      return _parentDependency;
   }

   public DKMagicDependency<?>[] getDependencies() {
      if (_dependencies != null)
         return _dependencies;
      _dependencies = this.getDependencies(this.getConstructor());
      return _dependencies;
   }

   /**
    * convenience method
    */
   public DKMagicDependency<?> getDependencyForConstructorParmName(
                                                                   String constructorParmName_) {
      if (constructorParmName_ == null)
         return null;
      DKMagicDependency<?>[] dependencies = this.getDependencies();
      if ((dependencies == null) || (dependencies.length == 0))
         return null;
      for (DKMagicDependency<?> depency : dependencies) {
         if (StringUtils.equals(constructorParmName_,
            depency.getParentConstructorParmName()))
            return depency;
      }
      return null;
   }

   public void setDependency(int index_, DKMagicDependency<?> dependency_) {
      _dependencies[index_] = dependency_;
   }

   public String getParentConstructorParmName() {
      return _parentConstructorParmName;
   }

   public Class<T> getTargetClass() {
      return _targetClass;
   }

   public Class<?> getOriginalTargetClass() {
      return _originalTargetClass;
   }

   /**
    * convenience method
    */
   public Class<?> getDependentTargetClass() {
      DKMagicDependency<?> parentDependency = this.getParentDependency();
      if (parentDependency == null)
         return null;
      return parentDependency.getTargetClass();
   }

   /**
    * convenience method
    */
   public String getParentConstructorParmNamePath() {
      String thisValue = this.getParentConstructorParmName();
      DKMagicDependency<?> parentDependency = this.getParentDependency();
      if (parentDependency == null)
         return thisValue;
      String parentsValue = parentDependency.getParentConstructorParmNamePath();
      if (parentsValue == null)
         return thisValue;
      return parentsValue + "." + thisValue;
   }

   private DKConstructor<T> getConstructor() {
      if (_constructor != null)
         return _constructor;
      _constructor = this.findConstructor(_targetClass);
      return _constructor;
   }

   public void refine(Class<T> targetClass_) {
      DKValidate.notNull(targetClass_);
      _targetClass = targetClass_;
      _constructor = null;
      _dependencies = null;
   }

   /**
    * resolved externally
    */
   public void resolve(Object resolution_) {
      if (resolution_ == null)
         resolution_ = NULL_RESOLUTION;
      _resolution = resolution_;
   }

   /**
    * resolved internally with whatever you've got
    */
   public Object resolve() throws InstantiationException, IllegalAccessException,
      IllegalArgumentException, InvocationTargetException {
      DKMagicDependency<?>[] dependencies = this.getDependencies();
      if ((dependencies == null) || (dependencies.length == 0)) {
         _resolution = _targetClass.newInstance();
         return _resolution;
      }
      Object[] dependencyResolutions = new Object[dependencies.length];
      for (int i = 0; i < dependencies.length; i++) {
         dependencyResolutions[i] = dependencies[i].getResolution();
      }
      _log.debug("resolving dependency->{}", this);
      _log.debug("with constructor->{}", this.getConstructor());
      _log.debug("and dependencyResolutions->{}", Arrays.toString(dependencyResolutions));
      _resolution = this.getConstructor()._constructor.newInstance(dependencyResolutions);
      return _resolution;
   }

   @SuppressWarnings("unchecked")
   public T getResolution() {
      if (_resolution == null)
         throw new RuntimeException(String.format("dependency not resolved %s", this));
      if (_resolution == NULL_RESOLUTION)
         return null;
      return (T) _resolution;
   }

   @SuppressWarnings("unchecked")
   private DKMagicDependency<?>[] getDependencies(DKConstructor<T> constructor_) {
      if ((constructor_ == null) || (constructor_._parameterNames == null)
         || (constructor_._parameterNames.length == 0))
         return null;
      Class<?>[] constructorParameterTypes = constructor_._constructor.getParameterTypes();
      DKMagicDependency<?>[] defaultDependencies = new DKMagicDependency<?>[constructor_._parameterNames.length];
      for (int i = 0; i < defaultDependencies.length; i++) {
         defaultDependencies[i] = new DKMagicDependency(this,
            constructor_._parameterNames[i], constructorParameterTypes[i]);
      }
      return defaultDependencies;
   }

   public DKMagicDependency<?> getSibling(String parentConstructorParmName_) {
      if (parentConstructorParmName_ == null)
         return null;
      DKMagicDependency<?> parent = this.getParentDependency();
      if (parent == null)
         return null;
      return parent.getDependencyForConstructorParmName(parentConstructorParmName_);
   }

   /**
    * @return always finds the largest accessible Constructor of _targetClass
    */
   private DKConstructor<T> findConstructor(Class<T> targetClass_) {
      Constructor<T> constructor = DKClassUtil.findLongestConstructor(targetClass_);
      if (constructor == null)
         return null;
      return new DKConstructor<T>(constructor);
   }

   public String toString() {
      return String.format("Dependency[%s(%s,%s)]",
         ClassUtils.getShortClassName(this.getDependentTargetClass()),
         ClassUtils.getShortClassName(_targetClass), _parentConstructorParmName);
   }
}
