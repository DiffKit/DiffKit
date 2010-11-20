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
package org.diffkit.util.tst

import org.apache.commons.lang.RandomStringUtils;

/**
 * @author jpanico
 */
public class CSVDataGenerator {
   enum FieldType {
      STRING, INTEGER, DECIMAL
   }
   
   private static CSVDataGenerator _instance = new CSVDataGenerator()
   
   public static void main(String[] args_){
      _instance.generate()
   }
   
   private void generate(){
      def rowSpecifier = [new  FieldSpecifier(FieldType.STRING,10), new FieldSpecifier(FieldType.INTEGER,10), new FieldSpecifier(FieldType.DECIMAL,10,3)]
      def rows = this.generateRows( rowSpecifier, ',', 20)
      rows.each { println it }
   }
   
   private def generateRows(List rowSpecifier_, String delimeter_, int rowCount_){
      return (0..rowCount_).collect { this.generateRow(rowSpecifier_, delimeter_)}
   }
   
   private def generateRow(List rowSpecifier_, String delimeter_){
      def builder = new StringBuilder()
      rowSpecifier_.each { builder.append(this.generateFieldValue(it)+',')}
      // remove the last character
      return builder.toString()[0..-2]
   }
   
   private def generateFieldValue(FieldSpecifier fieldSpecifier_){
      switch (fieldSpecifier_.fieldType) {
         case FieldType.STRING:
            return generateStringFieldValue(fieldSpecifier_.length)
         case FieldType.INTEGER:
            return generateIntegerFieldValue(fieldSpecifier_.length)
         case FieldType.DECIMAL:
            return generateDecimalFieldValue(fieldSpecifier_.length,fieldSpecifier_.precision)
         
         default:
            break;
      }
   }
   
   private def generateStringFieldValue(int length_){
      return RandomStringUtils.random( length_, true, false)
   }
   
   private def generateIntegerFieldValue(int length_){
      return RandomStringUtils.random( length_, false, true)
   }
   
   private def generateDecimalFieldValue(int length_, int precision_){
      def integerString = RandomStringUtils.random( length_, false, true) << ''
      return integerString.insert(integerString.size() - precision_ , ".")
   }
   
   private class FieldSpecifier {
      private final FieldType fieldType;
      private final int length;
      private final int precision;
      
      public FieldSpecifier(FieldType fieldType_, int length_){
         this(fieldType_, length_, 0)
      }
      
      public FieldSpecifier(FieldType fieldType_, int length_, int precision_){
         fieldType = fieldType_
         length = length_
         precision = precision_
      }
   }
}
