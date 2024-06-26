package com.alibaba.alink.operator.common.dataproc;

import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.ml.api.misc.param.Params;

import com.alibaba.alink.common.exceptions.AkIllegalArgumentException;
import com.alibaba.alink.common.exceptions.AkPreconditions;
import com.alibaba.alink.common.utils.TableUtil;
import com.alibaba.alink.params.shared.colname.HasSelectedCols;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * The model data of {@link com.alibaba.alink.pipeline.dataproc.MultiStringIndexerModel}.
 */
public class MultiStringIndexerModelData {
	/**
	 * The meta data.
	 */
	public Params meta;

	/**
	 * The mapping from token to index. A list of (column index, token, token index)
	 */
	public List <Tuple3 <Integer, String, Long>> tokenAndIndex;

	/**
	 * Number of tokens of each columns.
	 */
	public Map <Integer, Long> tokenNumber;

	/**
	 * Get the number of tokens of a column.
	 *
	 * @param columnName Name of the column.
	 * @return The number of tokens of that column.
	 */
	public long getNumberOfTokensOfColumn(String columnName) {
		int colIndex = TableUtil.findColIndexWithAssertAndHint(meta.get(HasSelectedCols.SELECTED_COLS), columnName);
		AkPreconditions.checkArgument(tokenNumber != null);
		return tokenNumber.get(colIndex);
	}

	public String getToken(String columnName, Long value) {
		Integer colIndex = TableUtil.findColIndex(meta.get(HasSelectedCols.SELECTED_COLS), columnName);

		for (Tuple3 <Integer, String, Long> index : tokenAndIndex) {
			if (index.f0.equals(colIndex) && index.f2.equals(value)) {
				return index.f1;
			}
		}

		throw new AkIllegalArgumentException("Could not find the token.");
	}

	public List <String> getTokens(String column) {
		Integer colIndex = TableUtil.findColIndex(meta.get(HasSelectedCols.SELECTED_COLS), column);

		List <String> ret = new ArrayList <>();

		for (Tuple3 <Integer, String, Long> index : tokenAndIndex) {
			if (index.f0.equals(colIndex)) {
				ret.add(index.f1);
			}
		}

		return ret;
	}
}