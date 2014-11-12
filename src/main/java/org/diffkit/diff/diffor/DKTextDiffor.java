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
package org.diffkit.diff.diffor;

import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.diffkit.diff.engine.DKContext;
import org.diffkit.diff.engine.DKDiffor;

/**
 * @author jpanico
 */
public class DKTextDiffor implements DKDiffor {

	private static final Pattern NEWLINE_TAB_PATTERN = Pattern
			.compile("[\n\r\t]{1}");
	private static final Pattern SPACE_RUN_PATTERN = Pattern.compile("[ ]+");

	private final String _ignoreChars;

	public DKTextDiffor(String ignoreChars_) {
		_ignoreChars = ignoreChars_;
	}

	/**
	 * @see org.diffkit.diff.engine.DKDiffor#isDiff(java.lang.Object,
	 *      java.lang.Object, org.diffkit.diff.engine.DKContext)
	 */
	public boolean isDiff(Object lhs_, Object rhs_, DKContext context_) {
		boolean lhsNull = (lhs_ == null);
		boolean rhsNull = (rhs_ == null);
		if (lhsNull && rhsNull)
			return false;
		if (lhsNull || rhsNull)
			return true;
		boolean equals = lhs_.equals(rhs_);
		if (equals)
			return false;
		String normalizedLhs = this.normalize((String) lhs_);
		String normalizedRhs = this.normalize((String) rhs_);
		lhsNull = (normalizedLhs == null);
		rhsNull = (normalizedRhs == null);
		if (lhsNull && rhsNull)
			return false;
		if (lhsNull || rhsNull)
			return true;
		return !normalizedLhs.equals(normalizedRhs);
	}

	private String normalize(String target_) {
		String normalizedString = NEWLINE_TAB_PATTERN.matcher(target_)
				.replaceAll(" ");
		normalizedString = SPACE_RUN_PATTERN.matcher(normalizedString)
				.replaceAll(" ");
		return StringUtils.trimToNull(normalizedString);
	}
}
