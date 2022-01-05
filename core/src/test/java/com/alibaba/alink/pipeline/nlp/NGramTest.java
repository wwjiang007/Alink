package com.alibaba.alink.pipeline.nlp;

import org.apache.flink.ml.api.misc.param.Params;
import org.apache.flink.types.Row;

import com.alibaba.alink.operator.batch.BatchOperator;
import com.alibaba.alink.operator.batch.nlp.NGramBatchOp;
import com.alibaba.alink.operator.batch.source.MemSourceBatchOp;
import com.alibaba.alink.operator.stream.StreamOperator;
import com.alibaba.alink.operator.stream.nlp.NGramStreamOp;
import com.alibaba.alink.operator.stream.sink.CollectSinkStreamOp;
import com.alibaba.alink.operator.stream.source.MemSourceStreamOp;
import com.alibaba.alink.testutil.AlinkTestBase;
import com.google.common.collect.HashMultiset;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

/**
 * Test for NGram.
 */

public class NGramTest extends AlinkTestBase {
	@Test
	public void testNGram() throws Exception {
		Row[] rows = new Row[] {
			Row.of(0, "a a b b c c a")
		};
		List <Row> expected = Arrays.asList(
			Row.of(0, "a_a a_b b_b b_c c_c c_a")
		);

		BatchOperator <?> data = new MemSourceBatchOp(rows, new String[] {"id", "sentence"});
		StreamOperator <?> dataStream = new MemSourceStreamOp(rows, new String[] {"id", "sentence"});

		NGram op = new NGram().setSelectedCol("sentence");
		assertListRowEqualWithoutOrder(expected, op.transform(data).collect());

		CollectSinkStreamOp sink = new CollectSinkStreamOp()
			.linkFrom(op.transform(dataStream));
		StreamOperator.execute();
		assertListRowEqualWithoutOrder(expected, sink.getAndRemoveValues());
	}

	@Test
	public void testInitializer() {
		NGram op = new NGram(new Params());
		Assert.assertEquals(op.getParams().size(), 0);

		BatchOperator <?> b = new NGramBatchOp();
		Assert.assertEquals(b.getParams().size(), 0);
		b = new NGramBatchOp(new Params());
		Assert.assertEquals(b.getParams().size(), 0);

		StreamOperator <?> s = new NGramStreamOp();
		Assert.assertEquals(s.getParams().size(), 0);
		s = new NGramStreamOp(new Params());
		Assert.assertEquals(s.getParams().size(), 0);
	}
}
