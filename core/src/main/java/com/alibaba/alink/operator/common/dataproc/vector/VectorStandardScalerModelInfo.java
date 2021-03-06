package com.alibaba.alink.operator.common.dataproc.vector;

import org.apache.flink.api.java.tuple.Tuple4;
import org.apache.flink.types.Row;

import com.alibaba.alink.operator.common.utils.PrettyDisplayUtils;
import org.apache.commons.lang3.ArrayUtils;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import static com.alibaba.alink.operator.common.utils.PrettyDisplayUtils.displayList;

public class VectorStandardScalerModelInfo implements Serializable {
	private static final long serialVersionUID = -7471583420818999739L;
	boolean withMeans;
	boolean withStdDevs;
	double[] means;
	double[] stdDevs;

	public VectorStandardScalerModelInfo(List <Row> rows) {
		Tuple4 <Boolean, Boolean, double[], double[]> tuple4 = new VectorStandardScalerModelDataConverter().load(rows);
		this.withMeans = tuple4.f0;
		this.withStdDevs = tuple4.f1;
		this.means = tuple4.f2;
		this.stdDevs = tuple4.f3;
	}

	public double[] getMeans() {
		return means;
	}

	public double[] getStdDevs() {
		return stdDevs;
	}

	public boolean isWithMeans() {
		return withMeans;
	}

	public boolean isWithStdDevs() {
		return withStdDevs;
	}

	@Override
	public String toString() {
		StringBuilder res = new StringBuilder();
		res.append(PrettyDisplayUtils.displayHeadline("StandardScalerModelSummary", '-') + "\n");
		res.append(PrettyDisplayUtils.displayHeadline("means information", '=') + "\n");
		if (withMeans) {
			res.append(displayList(Arrays.asList(ArrayUtils.toObject(means)), false) + "\n");
		} else {
			res.append("The mean values are not calculated.\n");
		}
		res.append(PrettyDisplayUtils.displayHeadline("standard deviation information", '=') + "\n");
		if (withStdDevs) {
			res.append(displayList(Arrays.asList(ArrayUtils.toObject(stdDevs)), false) + "\n");
		} else {
			res.append("The standard deviation values are not calculated.\n");
		}
		return res.toString();
	}
}
