package com.linkedin.pinot.query.aggregation.function;

import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.linkedin.pinot.index.segment.ColumnarReader;
import com.linkedin.pinot.index.segment.IndexSegment;
import com.linkedin.pinot.query.aggregation.AggregationFunction;
import com.linkedin.pinot.query.aggregation.AggregationResult;
import com.linkedin.pinot.query.aggregation.CombineLevel;
import com.linkedin.pinot.query.aggregation.data.LongContainer;
import com.linkedin.pinot.query.request.AggregationInfo;
import com.linkedin.pinot.query.utils.IntArray;


/**
 * This function will take a column and do sum on that.
 *
 */
public class SumAggregationFunction implements AggregationFunction {
  private String _sumByColumn;

  public SumAggregationFunction() {

  }

  @Override
  public void init(AggregationInfo aggregationInfo){
    _sumByColumn = aggregationInfo.getParams().get("column");

  }

  @Override
  public AggregationResult aggregate(IntArray docIds, int docIdCount, IndexSegment indexSegment) {
    long result = 0;
    ColumnarReader columnarReader = indexSegment.getColumnarReader(_sumByColumn);
    for (int i = 0; i < docIdCount; ++i) {
      result += columnarReader.getLongValue(docIds.get(i));
    }
    return new LongContainer(result);
  }

  @Override
  public List<AggregationResult> combine(List<AggregationResult> aggregationResultList, CombineLevel combineLevel) {
    AggregationResult result = reduce(aggregationResultList);
    aggregationResultList.clear();
    aggregationResultList.add(result);
    return aggregationResultList;
  }

  @Override
  public AggregationResult reduce(List<AggregationResult> aggregationResultList) {
    LongContainer result = (LongContainer) (aggregationResultList.get(0));

    for (int i = 1; i < aggregationResultList.size(); ++i) {
      result.increment((LongContainer) aggregationResultList.get(i));
    }
    return result;
  }

  @Override
  public JSONObject render(AggregationResult finalAggregationResult) {
    try {
      if (finalAggregationResult == null) {
        finalAggregationResult = new LongContainer(0);
      }
      return new JSONObject().put("sum", String.format("%1.5f", finalAggregationResult));
    } catch (JSONException e) {
      throw new RuntimeException(e);
    }
  }

}