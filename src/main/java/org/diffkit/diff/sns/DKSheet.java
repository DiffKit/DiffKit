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
package org.diffkit.diff.sns;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import org.diffkit.diff.engine.DKTableModel;

/**
 * @author jpanico
 */
public interface DKSheet {

   /**
    * all implementing classes must have a static field of type String[] with
    * this name
    */
   public static final String HANDLED_FILE_EXTENSIONS_FIELD_NAME = "HANDLED_FILE_EXTENSIONS";
   public static final Class<String[]> HANDLED_FILE_EXTENSIONS_FIELD_TYPE = String[].class;
   /**
    * all implementing classes must have a Constructor that looks like this:
    * DKAbstractSheet(File file_, String requestedName_, boolean isSorted_,
    * boolean hasHeader_, boolean validateLazily_)
    */
   public static final Class<?>[] IMPLEMENTOR_CONSTRUCTOR_PARAM_TYPES = { File.class,
      String.class, Boolean.TYPE, Boolean.TYPE, Boolean.TYPE };

   public File getFile();

   /**
    * always the name extracted from the sheet itself
    */
   public String getName() throws IOException;

   /**
    * this is always the model extracted from the sheet itself, never a
    * client-supplied model
    */
   public DKTableModel getModelFromSheet() throws IOException;

   public Iterator<Object[]> getRowIterator(DKTableModel model_) throws IOException;

   public void close() throws IOException;

}
