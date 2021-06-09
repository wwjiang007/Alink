package com.alibaba.alink.pipeline.feature;

import org.apache.flink.ml.api.misc.param.Params;

import com.alibaba.alink.operator.batch.BatchOperator;
import com.alibaba.alink.operator.batch.classification.Id3TrainBatchOp;
import com.alibaba.alink.params.classification.Id3TrainParams;
import com.alibaba.alink.params.feature.Id3EncoderParams;
import com.alibaba.alink.pipeline.Trainer;

public class Id3Encoder extends Trainer <Id3Encoder, Id3EncoderModel> implements
	Id3TrainParams <Id3Encoder>,
	Id3EncoderParams <Id3Encoder> {

	private static final long serialVersionUID = -8593371277511217184L;

	public Id3Encoder() {
	}

	public Id3Encoder(Params params) {
		super(params);
	}

	@Override
	protected BatchOperator <?> train(BatchOperator <?> in) {
		return new Id3TrainBatchOp(this.getParams()).linkFrom(in);
	}
}
