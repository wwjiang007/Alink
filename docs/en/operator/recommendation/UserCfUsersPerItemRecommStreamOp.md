## Description
Recommend users for item with userCF model.

## Parameters
| Name | Description | Type | Required？ | Default Value |
| --- | --- | --- | --- | --- |
| itemCol | Item column name | String | ✓ |  |
| k | Number of the recommended top objects. | Integer |  | 10 |
| excludeKnown | Flag of excluding the known objects in recommended top objects. | Boolean |  | false |
| recommCol | Column name of recommend result. | String | ✓ |  |
| reservedCols | Names of the columns to be retained in the output table | String[] |  | null |
| numThreads | Thread number of operator. | Integer |  | 1 |

## Script Example
### Code

```python
from pyalink.alink import *
import pandas as pd
import numpy as np

data = np.array([
    [1, 1, 0.6],
    [2, 2, 0.8],
    [2, 3, 0.6],
    [4, 1, 0.6],
    [4, 2, 0.3],
    [4, 3, 0.4],
])

df_data = pd.DataFrame({
    "user": data[:, 0],
    "item": data[:, 1],
    "rating": data[:, 2],
})
df_data["user"] = df_data["user"].astype('int')
df_data["item"] = df_data["item"].astype('int')

schema = 'user bigint, item bigint, rating double'
data = dataframeToOperator(df_data, schemaStr=schema, op_type='batch')
sdata = dataframeToOperator(df_data, schemaStr=schema, op_type='stream')

model = UserCfTrainBatchOp()\
    .setUserCol("user")\
    .setItemCol("item")\
    .setRateCol("rating").linkFrom(data);

predictor = UserCfUsersPerItemRecommStreamOp(model)\
    .setItemCol("item")\
    .setReservedCols(["item"])\
    .setK(1)\
    .setRecommCol("prediction_result");

predictor.linkFrom(sdata).print()
StreamOperator.execute()
```

### Results
```
        item	prediction_result
0	1	{"object":"[1]","score":"[0.23046638387921276]"}
1	2	{"object":"[4]","score":"[0.2458308094711603]"}
2	3	{"object":"[4]","score":"[0.1843731071033702]"}
3	1	{"object":"[1]","score":"[0.23046638387921276]"}
4	2	{"object":"[4]","score":"[0.2458308094711603]"}
5	3	{"object":"[4]","score":"[0.1843731071033702]"}
```
