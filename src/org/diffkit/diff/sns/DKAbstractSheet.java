/**
 * Copyright 2010-2011 Kiran Ratnapu
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
package org.diffkit.diff.sns;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Iterator;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.reflect.ConstructorUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.diffkit.common.DKUserException;
import org.diffkit.common.DKValidate;
import org.diffkit.diff.engine.DKTableModel;
import org.diffkit.util.DKClassUtil;

/**
 * @author kratnapu
 */
public abstract class DKAbstractSheet implements DKSheet {

   private final File _file;
   private String _requestedName;
   private final boolean _isSorted;
   private final boolean _hasHeader;
   private final boolean _validateLazily;
   private DKTableModel _modelFromSheet;
   private static final Logger LOG = LoggerFactory.getLogger(DKAbstractSheet.class);
   private final Logger _log = LoggerFactory.getLogger(this.getClass());

   public DKAbstractSheet(File file_, String requestedName_, boolean isSorted_,
                          boolean hasHeader_, boolean validateLazily_) {
      _log.debug("file_->{}", file_);
      _log.debug("requestedName_->{}", requestedName_);
      _log.debug("isSorted_->{}", isSorted_);
      _log.debug("hasHeader_->{}", hasHeader_);
      _log.debug("validateLazily_->{}", validateLazily_);
      _file = file_;
      _requestedName = StringUtils.trimToNull(requestedName_);
      _isSorted = isSorted_;
      _hasHeader = hasHeader_;
      _validateLazily = validateLazily_;
      DKValidate.notNull(_file);
      if (!_validateLazily)
         this.validate();
   }

   public static boolean classHandlesExtension(Class<DKSheet> class_, String extension_) {
      if ((class_ == null) || (extension_ == null))
         return false;
      Field handledField = DKClassUtil.findStaticField(
         HANDLED_FILE_EXTENSIONS_FIELD_NAME, class_);
      if (handledField == null)
         throw new IllegalArgumentException(String.format(
            "class_->%s must have static field named->%s", class_,
            HANDLED_FILE_EXTENSIONS_FIELD_NAME));
      if (handledField.getType() != HANDLED_FILE_EXTENSIONS_FIELD_TYPE)
         throw new IllegalArgumentException(String.format(
            "field->%s in class_-> must be type->%s", HANDLED_FILE_EXTENSIONS_FIELD_NAME,
            class_, HANDLED_FILE_EXTENSIONS_FIELD_TYPE));
      String[] handledExtensions = (String[]) DKClassUtil.getValue(handledField, class_);
      LOG.debug("class_->{} handledExtensions->{}", class_, handledExtensions);
      return ArrayUtils.contains(handledExtensions, extension_.toLowerCase());
   }

   private static Class<DKSheet> getHandlerClassForFile(File file_,
                                                        Class<DKSheet>[] availableSheetClasses_) {

      String extension = FilenameUtils.getExtension(file_.getName());
      LOG.debug("file_->{} extension->{}", file_, extension);

      for (Class<DKSheet> sheetClass : availableSheetClasses_) {
         if (classHandlesExtension(sheetClass, extension))
            return sheetClass;
      }
      return null;
   }

   /**
    * throws IllegalArgumentException if it's not able to find a handler in
    * availableSheetClasses_ for file_
    */
   @SuppressWarnings("unchecked")
   public static DKSheet constructSheet(File file_, String requestedName_,
                                        boolean isSorted_, boolean hasHeader_,
                                        boolean validateLazily_,
                                        Class<DKSheet>[] availableSheetClasses_)
      throws NoSuchMethodException, IllegalAccessException, InvocationTargetException,
      InstantiationException {
      DKValidate.notNull(file_);
      DKValidate.notEmpty((Object[]) availableSheetClasses_);
      Class<DKSheet> handlerClass = getHandlerClassForFile(file_, availableSheetClasses_);
      LOG.debug("handlerClass->{}", handlerClass);
      if (handlerClass == null)
         throw new IllegalArgumentException(String.format(
            "Couldn't find handler in availableSheetClasses_->%s for file_->%s",
            Arrays.toString(availableSheetClasses_), file_));
      Constructor<DKSheet> constructor = ConstructorUtils.getAccessibleConstructor(
         handlerClass, IMPLEMENTOR_CONSTRUCTOR_PARAM_TYPES);
      LOG.debug("constructor->{}", constructor);
      if (constructor == null)
         throw new IllegalArgumentException(
            String.format(
               "Couldn't find Constructor in handlerClass->%s for constructor parm types->%s",
               handlerClass, Arrays.toString(IMPLEMENTOR_CONSTRUCTOR_PARAM_TYPES)));
      Object[] constructorArgs = { file_, requestedName_, Boolean.valueOf(isSorted_),
         Boolean.valueOf(hasHeader_), Boolean.valueOf(validateLazily_) };
      return constructor.newInstance(constructorArgs);
   }

   protected void validate() {
      if (!_file.canRead())
         throw new DKUserException(String.format("can't read file->%s", _file));
   }

   /**
    * e.g. A...Z,AA..AZ,BA...BZ...
    */
   public static String getDefaultColumnName(int columnIndex_) {
      if (columnIndex_ < 0)
         return null;
      char major = (columnIndex_ / 26 - 1) >= 0 ? (char) ('A' + (columnIndex_ / 26 - 1))
         : '\0';
      char minor = (char) ('A' + columnIndex_ % 26);
      if (major != '\0')
         return "" + major + minor;
      return "" + minor;
   }

   public File getFile() {
      return _file;
   }

   protected String getRequestedName() {
      return _requestedName;
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.diffkit.diff.sns.DKSheet#getName()
    */
   public String getName() throws IOException {
      return this.getNameFromSheet();
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.diffkit.diff.sns.DKSheet#getModel()
    */
   public DKTableModel getModelFromSheet() throws IOException {
      if (_modelFromSheet != null)
         return _modelFromSheet;
      _modelFromSheet = this.createModelFromSheet();
      return _modelFromSheet;
   }

   protected boolean isSorted() {
      return _isSorted;
   }

   protected boolean hasHeader() {
      return _hasHeader;
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.diffkit.diff.sns.DKSheet#getRowIterator()
    */
   public abstract Iterator<Object[]> getRowIterator(DKTableModel model_)
      throws IOException;

   protected abstract String getNameFromSheet() throws IOException;

   protected abstract DKTableModel createModelFromSheet() throws IOException;
}
