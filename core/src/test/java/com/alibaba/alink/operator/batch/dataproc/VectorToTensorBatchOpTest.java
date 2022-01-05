package com.alibaba.alink.operator.batch.dataproc;

import org.apache.flink.types.Row;

import com.alibaba.alink.operator.batch.source.MemSourceBatchOp;
import com.alibaba.alink.testutil.AlinkTestBase;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

public class VectorToTensorBatchOpTest extends AlinkTestBase {

	@Test
	public void testVectorToTensorStreamOp() throws Exception {
		List <Row> data = Collections.singletonList(Row.of("0.0 0.1 1.0 1.1 2.0 2.1"));

		MemSourceBatchOp memSourceBatchOp = new MemSourceBatchOp(data, "vec string");

		memSourceBatchOp
			.link(
				new VectorToTensorBatchOp()
					.setSelectedCol("vec")
			)
			.print();
	}
}