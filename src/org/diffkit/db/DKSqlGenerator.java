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
package org.diffkit.db;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.diffkit.common.DKValidate;
import org.diffkit.util.DKSqlUtil;
import org.diffkit.util.DKStringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jpanico
 */
public class DKSqlGenerator {

	private final DKDatabase _database;
	private final Logger _log = LoggerFactory.getLogger(this.getClass());

	public DKSqlGenerator(DKDatabase database_) {
		_database = database_;
		DKValidate.notNull(database_);
	}

	public String generateCreateDDL(DKDBColumn column_) throws SQLException {
		DKDBType concreteType = this.getConcreteType(column_);
		_log.debug("concreteType->{}", concreteType);
		if (!_database.supportsType(concreteType))
			return null;
		StringBuilder builder = new StringBuilder();
		String notNullSpecifier = column_.isPartOfPrimaryKey() ? " NOT NULL"
				: "";
		builder.append(String.format("%s\t\t%s%s%s",
				this.generateIdentifierString(column_.getName()),
				concreteType.getSqlTypeName(),
				this.generateSizeSpecifier(column_), notNullSpecifier));
		return builder.toString();
	}

	public String generateSizeSpecifier(DKDBColumn column_) throws SQLException {
		if (column_ == null)
			return null;
		DKDBTypeInfo dbTypeInfo = _database.getConcreteTypeInfo(column_
				.getDBTypeName());
		if (dbTypeInfo == null)
			_log.warn("no dbTypeInfo for column_->{}", column_);
		else if (dbTypeInfo.getType().ignoresLengthSpecifier()) {
			return "";
		}
		int size = column_.getSize();
		int scale = column_.getScale();
		if (size <= 0)
			return "";
		if (scale <= 0)
			return String.format("(%s)", size);
		return String.format("(%s,%s)", size, scale);
	}

	public String generateDropDDL(DKDBTable table_) {
		return String.format("DROP TABLE %s",
				this.generateQualifiedTableIdentifierString(table_));
	}

	public String generateCreateDDL(DKDBTable table_) throws SQLException {
		StringBuilder builder = new StringBuilder();
		builder.append(String.format("CREATE TABLE %s\n(\n",
				this.generateQualifiedTableIdentifierString(table_)));
		DKDBColumn[] columns = table_.getColumns();
		DKDBPrimaryKey primaryKey = table_.getPrimaryKey();
		for (int i = 0; i < columns.length; i++) {
			String columnDDL = this.generateCreateDDL(columns[i]);
			if (columnDDL == null)
				continue;
			builder.append(String.format("\t\t%s", columnDDL));
			if ((i < (columns.length - 1)) || (primaryKey != null))
				builder.append(",");
			builder.append("\n");
		}
		if (_log.isDebugEnabled())
			_log.debug("primaryKey->{}", primaryKey);
		if (primaryKey != null)
			builder.append(String.format("\t\t%s",
					this.generateCreateDDL(primaryKey)));

		builder.append(")\n");
		String ddlString = builder.toString();
		if (_log.isDebugEnabled())
			_log.debug("ddlString->{}", ddlString);
		return ddlString;
	}

	public String generateCreateDDL(DKDBPrimaryKey primaryKey_) {
		if (primaryKey_ == null)
			return "";
		StringBuilder builder = new StringBuilder();
		builder.append(String.format("CONSTRAINT %s PRIMARY KEY (",
				this.generateIdentifierString(primaryKey_.getName())));
		String[] columnNames = primaryKey_.getColumnNames();
		for (int i = 0; i < columnNames.length; i++) {
			builder.append(this.generateIdentifierString(columnNames[i]));
			if (i < (columnNames.length - 1))
				builder.append(",");
		}
		builder.append(")");
		return builder.toString();
	}

	public String generateInsertDML(Map<String, ?> row_, DKDBTable table_)
			throws SQLException {
		if (_log.isDebugEnabled()) {
			_log.debug("row_->{}", row_);
			_log.debug("table_->{}", table_);
		}
		DKValidate.notNull(table_);
		if (MapUtils.isEmpty(row_))
			return null;
		List<Object> values = new ArrayList<Object>(row_.size());
		List<DKDBTypeInfo> typeInfos = new ArrayList<DKDBTypeInfo>(row_.size());
		List<String> columnNames = new ArrayList<String>(row_.size());
		DKDBColumn[] columns = table_.getColumns();
		for (DKDBColumn column : columns) {
			if (!row_.containsKey(column.getName()))
				continue;
			values.add(row_.get(column.getName()));
			typeInfos
					.add(_database.getConcreteTypeInfo(column.getDBTypeName()));
			columnNames.add(column.getName());
		}
		return generateInsertDML(values.toArray(),
				typeInfos.toArray(new DKDBTypeInfo[typeInfos.size()]),
				columnNames.toArray(new String[columnNames.size()]),
				table_.getSchema(), table_.getTableName());
	}

	public String generateDeleteDML(Object[] keyValues_,
			DKDBTypeInfo[] keyTypeInfos_, String[] keyColumnNames_,
			String schemaName_, String tableName_) {

		if (_log.isDebugEnabled()) {
			_log.debug("keyValues_->{}", Arrays.toString(keyValues_));
			_log.debug("keyTypeInfos_->{}", Arrays.toString(keyTypeInfos_));
			_log.debug("keyColumnNames_->{}", Arrays.toString(keyColumnNames_));
			_log.debug("schemaName_->{}", schemaName_);
			_log.debug("tableName_->{}", tableName_);
		}
		if (ArrayUtils.isEmpty(keyValues_) || ArrayUtils.isEmpty(keyTypeInfos_)
				|| ArrayUtils.isEmpty(keyColumnNames_) || (tableName_ == null))
			throw new IllegalArgumentException(
					"null or empty value not allowed here");
		if (!((keyValues_.length == keyTypeInfos_.length) && (keyTypeInfos_.length == keyColumnNames_.length)))
			throw new IllegalArgumentException(
					String.format(
							"keyValues_ must be same size as keyTypeInfos_ must be the same size as keyColumnNames_; keyValues_->%s, keyTypeInfos_->%s, keyColumnNames_->%s",
							keyValues_, keyTypeInfos_, keyColumnNames_));
	}

	private String generateWhereClause(Object[] values_,
			DKDBTypeInfo[] typeInfos_, String[] columnNames_) {
		if (ArrayUtils.isEmpty(values_))
			return null;
		StringBuilder builder = new StringBuilder();
		builder.append("WHERE ");
		for (int i = 0; i < values_.length; i++) {
			builder.append(String.format(
					"(%s=%s )",
					columnNames_[i],
					DKSqlUtil.formatForSql(values_[i],
							typeInfos_[i].getWriteType())));
			if (i < values_.length - 1)
				builder.append(" AND ");
		}
		return builder.toString();
	}

	public String generateInsertDML(Object[] values_,
			DKDBTypeInfo[] typeInfos_, String[] columnNames_,
			String schemaName_, String tableName_) {
		if (_log.isDebugEnabled()) {
			_log.debug("values_->{}", Arrays.toString(values_));
			_log.debug("typeInfos_->{}", Arrays.toString(typeInfos_));
			_log.debug("columnNames_->{}", Arrays.toString(columnNames_));
			_log.debug("schemaName_->{}", schemaName_);
			_log.debug("tableName_->{}", tableName_);
		}
		if (ArrayUtils.isEmpty(values_) || ArrayUtils.isEmpty(typeInfos_)
				|| ArrayUtils.isEmpty(columnNames_) || (tableName_ == null))
			throw new IllegalArgumentException(
					"null or empty value not allowed here");
		if (!((values_.length == typeInfos_.length) && (typeInfos_.length == columnNames_.length)))
			throw new IllegalArgumentException(
					String.format(
							"values_ must be same size as typeInfos_ must be the same size as columnNames_; values_->%s, typeInfos_->%s, columnNames_->%s",
							values_, typeInfos_, columnNames_));

		// deal with case sensitivity
		String tableIdentifier = this.generateQualifiedTableIdentifierString(
				schemaName_, tableName_);
		String[] columnNames = new String[columnNames_.length];
		for (int i = 0; i < columnNames_.length; i++)
			columnNames[i] = this.generateIdentifierString(columnNames_[i]);

		String[] valueStrings = new String[values_.length];
		for (int i = 0; i < values_.length; i++)
			valueStrings[i] = DKSqlUtil.formatForSql(values_[i],
					typeInfos_[i].getWriteType());

		String insertDML = String.format("INSERT INTO %s %s\nVALUES %s",
				tableIdentifier, DKStringUtil.toSetString(columnNames),
				DKStringUtil.toSetString(valueStrings));
		_log.debug("insertDML->{}", insertDML);
		return insertDML;
	}

	public String generateSelectDML(DKDBTable table_) {
		return String.format("SELECT * FROM %s", this
				.generateIdentifierString(this
						.generateQualifiedTableIdentifierString(table_)));
	}

	private DKDBType getConcreteType(DKDBColumn column_) {
		if (column_ == null)
			return null;
		return DKDBType.getConcreteType(_database.getFlavor(),
				column_.getDBTypeName());
	}

	public String generateQualifiedTableIdentifierString(DKDBTable table_) {
		if (table_ == null)
			return null;
		return this.generateQualifiedTableIdentifierString(table_.getSchema(),
				table_.getTableName());
	}

	public String generateQualifiedTableIdentifierString(String schemaName_,
			String tableName_) {
		if (tableName_ == null)
			return null;
		if (StringUtils.isEmpty(schemaName_))
			return this.generateIdentifierString(tableName_);
		return String.format("%s.%s",
				this.generateIdentifierString(schemaName_),
				this.generateIdentifierString(tableName_));
	}

	public String generateIdentifierString(String rawIdentifier_) {
		if (!_database.getCaseSensitive())
			return rawIdentifier_;
		return DKStringUtil.quote(rawIdentifier_, DKStringUtil.Quote.DOUBLE);
	}
}
