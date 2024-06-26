package com.alibaba.alink.params.similarity;

import org.apache.flink.ml.api.misc.param.ParamInfo;
import org.apache.flink.ml.api.misc.param.ParamInfoFactory;
import org.apache.flink.ml.api.misc.param.WithParams;

import com.alibaba.alink.common.annotation.DescCn;
import com.alibaba.alink.common.annotation.NameCn;

/**
 * Param: number of hash tables.
 */
public interface HasNumHashTablesDefaultAs10<T> extends
	WithParams <T> {

	@NameCn("哈希表个数")
	@DescCn("哈希表的数目")
	ParamInfo <Integer> NUM_HASH_TABLES = ParamInfoFactory
		.createParamInfo("numHashTables", Integer.class)
		.setDescription("The number of hash tables")
		.setHasDefaultValue(10)
		.setAlias(new String[] {"minHashK"})
		.build();

	default Integer getNumHashTables() {
		return get(NUM_HASH_TABLES);
	}

	default T setNumHashTables(Integer value) {
		return set(NUM_HASH_TABLES, value);
	}
}
