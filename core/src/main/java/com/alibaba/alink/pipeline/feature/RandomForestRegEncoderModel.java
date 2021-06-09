package com.alibaba.alink.pipeline.feature;

import org.apache.flink.ml.api.misc.param.Params;

import com.alibaba.alink.operator.common.tree.predictors.TreeModelEncoderModelMapper;
import com.alibaba.alink.params.feature.RandomForestEncoderParams;
import com.alibaba.alink.pipeline.MapModel;

public class RandomForestRegEncoderModel extends MapModel <RandomForestRegEncoderModel>
	implements RandomForestEncoderParams <RandomForestRegEncoderModel> {

	private static final long serialVersionUID = -4433726791463849572L;

	public RandomForestRegEncoderModel() {this(null);}

	public RandomForestRegEncoderModel(Params params) {
		super(TreeModelEncoderModelMapper::new, params);
	}

}