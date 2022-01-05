package com.alibaba.alink.operator.stream.tensorflow;

import com.alibaba.alink.common.AlinkGlobalConfiguration;
import com.alibaba.alink.common.MLEnvironmentFactory;
import com.alibaba.alink.common.dl.DLEnvConfig;
import com.alibaba.alink.common.dl.DLEnvConfig.Version;
import com.alibaba.alink.common.dl.DLLauncherStreamOp;
import com.alibaba.alink.common.io.plugin.PluginDownloader;
import com.alibaba.alink.common.io.plugin.RegisterKey;
import com.alibaba.alink.common.utils.JsonConverter;
import com.alibaba.alink.operator.stream.StreamOperator;
import com.alibaba.alink.operator.stream.dataproc.TypeConvertStreamOp;
import com.alibaba.alink.operator.stream.source.RandomTableSourceStreamOp;
import com.alibaba.alink.params.dataproc.HasTargetType.TargetType;
import com.alibaba.alink.testutil.categories.DLTest;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.HashMap;
import java.util.Map;

public class TensorFlow2StreamOpTest {

	@Category(DLTest.class)
	@Test
	public void testPs() throws Exception {
		int savedStreamParallelism = MLEnvironmentFactory.getDefault().getStreamExecutionEnvironment().getParallelism();
		StreamOperator.setParallelism(3);
		DLLauncherStreamOp.DL_CLUSTER_START_TIME = 30 * 1000;

		StreamOperator <?> source = new RandomTableSourceStreamOp()
			.setMaxRows(1000L)
			.setNumCols(10);

		String[] colNames = source.getColNames();
		source = source.select("*, case when RAND() > 0.5 then 1. else 0. end as label");
		source = source.link(new TypeConvertStreamOp().setSelectedCols("num").setTargetType(TargetType.DOUBLE));
		String label = "label";

		Map <String, Object> userParams = new HashMap<>();
		userParams.put("featureCols", JsonConverter.toJson(colNames));
		userParams.put("labelCol", label);
		userParams.put("batch_size", 16);
		userParams.put("num_epochs", 1);

		TensorFlow2StreamOp tensorFlow2StreamOp = new TensorFlow2StreamOp()
			.setUserFiles(new String[] {"res:///tf_dnn_stream.py"})
			.setMainScriptFile("res:///tf_dnn_stream.py")
			.setUserParams(JsonConverter.toJson(userParams))
			.setNumWorkers(2)
			.setNumPSs(1)
			.setOutputSchemaStr("model_id long, model_info string")
			.linkFrom(source);
		tensorFlow2StreamOp.print();
		StreamOperator.execute();
		StreamOperator.setParallelism(savedStreamParallelism);
	}

	@Test
	public void testAllReduce() throws Exception {
		int savedStreamParallelism = MLEnvironmentFactory.getDefault().getStreamExecutionEnvironment().getParallelism();
		AlinkGlobalConfiguration.setPrintProcessInfo(true);
		PluginDownloader pluginDownloader = AlinkGlobalConfiguration.getPluginDownloader();

		RegisterKey registerKey = DLEnvConfig.getRegisterKey(Version.TF231);
		pluginDownloader.downloadPlugin(registerKey.getName(), registerKey.getVersion());

		StreamOperator.setParallelism(3);
		DLLauncherStreamOp.DL_CLUSTER_START_TIME = 30 * 1000;

		StreamOperator <?> source = new RandomTableSourceStreamOp()
			.setMaxRows(1000L)
			.setNumCols(10);

		String[] colNames = source.getColNames();
		source = source.select("*, case when RAND() > 0.5 then 1. else 0. end as label");
		source = source.link(new TypeConvertStreamOp().setSelectedCols("num").setTargetType(TargetType.DOUBLE));
		String label = "label";

		Map <String, Object> userParams = new HashMap<>();
		userParams.put("featureCols", JsonConverter.toJson(colNames));
		userParams.put("labelCol", label);
		userParams.put("batch_size", 16);
		userParams.put("num_epochs", 1);

		TensorFlow2StreamOp tensorFlow2StreamOp = new TensorFlow2StreamOp()
			.setUserFiles(new String[] {"res:///tf_dnn_stream.py"})
			.setMainScriptFile("res:///tf_dnn_stream.py")
			.setUserParams(JsonConverter.toJson(userParams))
			.setNumWorkers(3)
			.setNumPSs(0)
			.setOutputSchemaStr("model_id long, model_info string")
			.linkFrom(source);
		tensorFlow2StreamOp.print();
		StreamOperator.execute();
		StreamOperator.setParallelism(savedStreamParallelism);
	}

	@Test
	public void testWithAutoWorkersPSs() throws Exception {
		AlinkGlobalConfiguration.setPrintProcessInfo(true);
		PluginDownloader pluginDownloader = AlinkGlobalConfiguration.getPluginDownloader();

		RegisterKey registerKey = DLEnvConfig.getRegisterKey(Version.TF231);
		pluginDownloader.downloadPlugin(registerKey.getName(), registerKey.getVersion());

		int savedStreamParallelism = MLEnvironmentFactory.getDefault().getStreamExecutionEnvironment().getParallelism();
		StreamOperator.setParallelism(3);
		DLLauncherStreamOp.DL_CLUSTER_START_TIME = 30 * 1000;

		StreamOperator <?> source = new RandomTableSourceStreamOp()
			.setMaxRows(1000L)
			.setNumCols(10);

		String[] colNames = source.getColNames();
		source = source.select("*, case when RAND() > 0.5 then 1. else 0. end as label");
		source = source.link(new TypeConvertStreamOp().setSelectedCols("num").setTargetType(TargetType.DOUBLE));
		String label = "label";

		Map <String, Object> userParams = new HashMap<>();
		userParams.put("featureCols", JsonConverter.toJson(colNames));
		userParams.put("labelCol", label);
		userParams.put("batch_size", 16);
		userParams.put("num_epochs", 1);

		TensorFlow2StreamOp tensorFlow2StreamOp = new TensorFlow2StreamOp()
			.setUserFiles(new String[] {"res:///tf_dnn_stream.py"})
			.setMainScriptFile("res:///tf_dnn_stream.py")
			.setUserParams(JsonConverter.toJson(userParams))
			.setOutputSchemaStr("model_id long, model_info string")
			.linkFrom(source);
		tensorFlow2StreamOp.print();
		StreamOperator.execute();
		StreamOperator.setParallelism(savedStreamParallelism);
	}
}
