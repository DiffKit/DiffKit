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
package org.diffkit.diff.engine;

import java.io.IOException;
import java.net.URI;

/**
 * @author jpanico
 */
public interface DKSource extends DKSourceSink {

   public DKTableModel getModel();

   public URI getURI() throws IOException;

   /**
    * @return null when it's out of rows; at end
    */
   public Object[] getNextRow() throws IOException;

   /**
    * index of last row vended, or -1 if no rows vended yet or empty
    */
   public long getLastIndex();
}
