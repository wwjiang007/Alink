package com.alibaba.alink;

import com.alibaba.alink.common.AlinkTypes;
import com.alibaba.alink.common.MLEnvironment;
import com.alibaba.alink.common.MLEnvironmentFactory;
import com.alibaba.alink.common.utils.DataStreamConversionUtil;
import com.alibaba.alink.common.utils.Stopwatch;
import com.alibaba.alink.operator.batch.BatchOperator;
import com.alibaba.alink.operator.batch.sql.*;
import com.alibaba.alink.operator.stream.StreamOperator;
import com.alibaba.alink.operator.stream.source.TableSourceStreamOp;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import org.apache.flink.table.api.Table;
import org.apache.flink.table.api.java.BatchTableEnvironment;
import org.apache.flink.table.api.java.StreamTableEnvironment;
import org.apache.flink.table.functions.ScalarFunction;
import org.apache.flink.types.Row;

import java.util.HashMap;
import java.util.Map;

public class Chap05 {

    public static void main(String[] args) throws Exception {
        Stopwatch sw = new Stopwatch();
        sw.start();

        BatchOperator.setParallelism(1);

        c_1();

        c_2_1();

        c_2_2();

        c_2_3();

        c_3();

        c_3_4();

        sw.stop();
        System.out.println(sw.getElapsedTimeSpan());
    }

    static void c_1() throws Exception {
        BatchOperator<?> ratings = Chap24.getSourceRatings();
        BatchOperator<?> users = Chap24.getSourceUsers();
        BatchOperator<?> items = Chap24.getSourceItems();

        ratings.registerTableName("ratings");
        items.registerTableName("items");
        users.registerTableName("users");

        BatchOperator.sqlQuery(
            "SELECT title, cnt, avg_rating"
                + " FROM ( SELECT item_id, COUNT(*) AS cnt, AVG(rating) AS avg_rating"
                + "        FROM ratings "
                + "        GROUP BY item_id "
                + "        ORDER BY cnt DESC LIMIT 10 "
                + "      ) AS t"
                + " JOIN items"
                + " ON t.item_id=items.item_id"
                + " ORDER BY cnt DESC"
        ).print();

        BatchOperator.registerFunction("from_unix_timestamp", new FromUnixTimestamp());

        BatchOperator.sqlQuery(
            "SELECT MIN(dt) AS min_dt, MAX(dt) AS max_dt "
                + " FROM ( SELECT from_unix_timestamp(ts) AS dt, 1 AS grp FROM ratings) "
                + " GROUP BY grp "
        ).print();

        ratings
            .select("from_unix_timestamp(ts) AS dt, 1 AS grp")
            .groupBy("grp", "MIN(dt) AS min_dt, MAX(dt) AS max_dt")
            .print();

        BatchOperator.sqlQuery(
            "SELECT title, cnt, m_rating, f_rating, ABS(m_rating - f_rating) AS diff_rating"
                + " FROM ( SELECT item_id, COUNT(rating) AS cnt, "
                + "               AVG(CASE WHEN gender='M' THEN rating ELSE NULL END) AS m_rating, "
                + "               AVG(CASE WHEN gender='F' THEN rating ELSE NULL END) AS f_rating "
                + "        FROM (SELECT item_id, rating, gender FROM ratings "
                + "                     JOIN users ON ratings.user_id=users.user_id)"
                + "        GROUP BY item_id "
                + "      ) AS t"
                + " JOIN items"
                + " ON t.item_id=items.item_id"
                + " ORDER BY diff_rating DESC LIMIT 10"
        ).print();

        BatchOperator.sqlQuery(
            "SELECT title, cnt, m_rating, f_rating, ABS(m_rating - f_rating) AS diff_rating"
                + " FROM ( SELECT item_id, COUNT(rating) AS cnt, "
                + "               AVG(CASE WHEN gender='M' THEN rating ELSE NULL END) AS m_rating, "
                + "               AVG(CASE WHEN gender='F' THEN rating ELSE NULL END) AS f_rating "
                + "        FROM (SELECT item_id, rating, gender FROM ratings "
                + "                     JOIN users ON ratings.user_id=users.user_id)"
                + "        GROUP BY item_id "
                + "        HAVING COUNT(rating)>=50 "
                + "      ) AS t"
                + " JOIN items"
                + " ON t.item_id=items.item_id"
                + " ORDER BY diff_rating DESC LIMIT 10"
        ).print();
    }

    static void c_2_1() throws Exception {
        BatchOperator<?> ratings = Chap24.getSourceRatings();
        BatchOperator<?> users = Chap24.getSourceUsers();

        BatchOperator ratings_select = ratings.select("user_id, item_id AS movie_id");
        ratings_select.firstN(5).print();

        ratings.select("user_id, item_id AS movie_id").firstN(5).print();

        ratings_select = ratings.select("*");
        ratings_select.firstN(5).print();

        ratings.as("f1,f2,f3,f4").firstN(5).print();

        ratings.filter("rating > 3").firstN(5).print();
        ratings.where("rating > 3").firstN(5).print();

        users.select("gender").distinct().print();

        users.groupBy("gender", "gender, COUNT(*) AS cnt").print();

        users.orderBy("age", 5).print();
        users.orderBy("age", 1, 3).print();

        users.orderBy("age", 5, false).print();
        users.orderBy("age", 1, 3, false).print();
    }

    static void c_2_2() throws Exception {
        BatchOperator<?> ratings = Chap24.getSourceRatings();
        BatchOperator<?> items = Chap24.getSourceItems();

        BatchOperator left_ratings
            = ratings
            .filter("user_id<3 AND item_id<4")
            .select("user_id, item_id, rating");

        BatchOperator right_movies
            = items
            .select("item_id AS movie_id, title")
            .filter("movie_id < 6 AND MOD(movie_id, 2) = 1");

        System.out.println("# left_ratings #");
        left_ratings.print();
        System.out.println("\n# right_movies #");
        right_movies.print();

        System.out.println("# JOIN #");
        new JoinBatchOp()
            .setJoinPredicate("item_id = movie_id")
            .setSelectClause("user_id, item_id, title, rating")
            .linkFrom(left_ratings, right_movies)
            .print();

        System.out.println("\n# LEFT OUTER JOIN #");
        new LeftOuterJoinBatchOp()
            .setJoinPredicate("item_id = movie_id")
            .setSelectClause("user_id, item_id, title, rating")
            .linkFrom(left_ratings, right_movies)
            .print();

        System.out.println("\n# RIGHT OUTER JOIN #");
        new RightOuterJoinBatchOp()
            .setJoinPredicate("item_id = movie_id")
            .setSelectClause("user_id, item_id, title, rating")
            .linkFrom(left_ratings, right_movies)
            .print();

        System.out.println("\n# FULL OUTER JOIN #");
        new FullOuterJoinBatchOp()
            .setJoinPredicate("item_id = movie_id")
            .setSelectClause("user_id, item_id, title, rating")
            .linkFrom(left_ratings, right_movies)
            .print();
    }

    static void c_2_3() throws Exception {
        BatchOperator<?> users = Chap24.getSourceUsers();

        BatchOperator users_1_4 = users.filter("user_id<5");
        System.out.println("# users_1_4 #");
        users_1_4.print();

        BatchOperator users_3_6 = users.filter("user_id>2 AND user_id<7");
        System.out.println("\n# users_3_6 #");
        users_3_6.print();

        new UnionAllBatchOp().linkFrom(users_1_4, users_3_6).print();

        new UnionBatchOp().linkFrom(users_1_4, users_3_6).print();

        new IntersectBatchOp().linkFrom(users_1_4, users_3_6).print();

        new IntersectAllBatchOp()
            .linkFrom(
                new UnionAllBatchOp().linkFrom(users_1_4, users_1_4),
                new UnionAllBatchOp().linkFrom(users_1_4, users_3_6)
            )
            .print();

        new MinusBatchOp().linkFrom(users_1_4, users_3_6).print();

        new MinusAllBatchOp()
            .linkFrom(
                new UnionAllBatchOp().linkFrom(users_1_4, users_1_4),
                new UnionAllBatchOp().linkFrom(users_1_4, users_3_6)
            )
            .print();
    }

    static void c_3() throws Exception {
        BatchTableEnvironment benv = MLEnvironmentFactory.getDefault().getBatchTableEnvironment();
        for (String name : benv.listTables()) {
            benv.sqlUpdate("DROP TABLE IF EXISTS " + name);
        }

        BatchOperator<?> ratings = Chap24.getSourceRatings();
        BatchOperator<?> users = Chap24.getSourceUsers();
        BatchOperator<?> items = Chap24.getSourceItems();

        ratings.registerTableName("ratings");
        items.registerTableName("items");
        users.registerTableName("users");

        String[] tableNames
            = MLEnvironmentFactory.getDefault().getBatchTableEnvironment().listTables();
        System.out.println("Table Names : ");
        for (String name : tableNames) {
            System.out.println(name);
        }

        BatchTableEnvironment batchTableEnvironment
            = MLEnvironmentFactory.getDefault().getBatchTableEnvironment();

        System.out.println("Table Names : ");
        for (String name : batchTableEnvironment.listTables()) {
            System.out.println(name);
        }

        batchTableEnvironment.sqlUpdate("DROP TABLE IF EXISTS users");

        System.out.println("\nTable Names After DROP : ");
        for (String name : batchTableEnvironment.listTables()) {
            System.out.println(name);
        }

        BatchOperator ratings_scan
            = BatchOperator.fromTable(batchTableEnvironment.scan("ratings"));
        ratings_scan.firstN(5).print();

        for (String name : benv.listTables()) {
            benv.sqlUpdate("DROP TABLE IF EXISTS " + name);
        }
    }

    public static class FromUnixTimestamp extends ScalarFunction {

        public java.sql.Timestamp eval(Long ts) {
            return new java.sql.Timestamp(ts * 1000);
        }

    }

    static void c_3_4() throws Exception {
        MLEnvironment mlEnv = MLEnvironmentFactory.getDefault();
        StreamExecutionEnvironment env = mlEnv.getStreamExecutionEnvironment();
        StreamTableEnvironment tenv = mlEnv.getStreamTableEnvironment();

        DataStreamSource<Map<String, Object>> inputDataStreamMap = env.addSource(
            new SourceFunction<Map<String, Object>>() {
                @Override
                public void run(SourceContext<Map<String, Object>> out) throws Exception {
                    Map<String, Object> item = new HashMap<>();
                    item.put("name", "a");
                    item.put("val", 110);
                    out.collect(item);

                    Map<String, Object> item1 = new HashMap<>();
                    item1.put("name", "b");
                    item1.put("val", 111);
                    out.collect(item1);

                    Map<String, Object> item2 = new HashMap<>();
                    item2.put("name", "c");
                    item2.put("val", 113);
                    out.collect(item2);
                }

                @Override
                public void cancel() {
                }
            });
        inputDataStreamMap.print();

        DataStream<Row> inputDataStreamRow = inputDataStreamMap.map(new MapFunction<Map<String, Object>, Row>() {
            @Override
            public Row map(Map<String, Object> value) throws Exception {
                return Row.of(value.get("name"), value.get("val"));
            }
        });
        inputDataStreamRow.print();

        Table inputTable = DataStreamConversionUtil.toTable(mlEnv, inputDataStreamRow, new String[]{"name", "val"},
            new TypeInformation<?>[]{AlinkTypes.STRING, AlinkTypes.INT});
        inputTable.printSchema();

        TableSourceStreamOp inputStreamOp = new TableSourceStreamOp(inputTable);

        StreamOperator<?> outputStreamOp = inputStreamOp
            .select("name, val + 1 AS val, 'output' AS type");

        outputStreamOp.print();

        Table outputTable = outputStreamOp.getOutputTable();
        outputTable.printSchema();

        StreamOperator.execute();
    }
}