package com.alibaba.alink.operator.stream.feature.aggfunc;

import org.apache.flink.table.functions.AggregateFunction;

import com.alibaba.alink.common.sql.builtin.agg.ListAggUdaf;
import com.alibaba.alink.common.sql.builtin.agg.ListAggUdaf.ListAggData;
import org.junit.Before;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ListAggTest extends AggFunctionTestBase <Object[], String, ListAggData> {

	@Before
	public void init() {
		multiInput = true;
	}

	@Override
	protected List <List <Object[]>> getInputValueSets() {
		List <List <Object[]>> res = new ArrayList <>();

		res.add(Arrays.asList(new Object[] {"q"}, new Object[] {"w"}, new Object[] {"e"}));
		res.add(Arrays.asList(new Object[] {"a"}, new Object[] {"s"}));

		return res;
	}

	@Override
	protected List <String> getExpectedResults() {
		return Arrays.asList("q,w,e", "a,s");
	}

	@Override
	protected AggregateFunction <String, ListAggData> getAggregator() {
		return new ListAggUdaf();
	}

	@Override
	protected Class <?> getAccClass() {
		return ListAggData.class;
	}
}
