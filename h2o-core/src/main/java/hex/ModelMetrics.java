package hex;

import water.DKV;
import water.Key;
import water.Keyed;
import water.Value;
import water.api.ModelMetricsBase;
import water.api.ModelMetricsV3;
import water.fvec.Frame;
import water.util.Log;


/**
 * Container to hold the metric for a model as scored on a specific frame.
 */

public final class ModelMetrics extends Keyed {
  public Key model = null;
  public long model_checksum = -1;
  public Model.ModelCategory model_category = null;
  public Key frame = null;
  public long frame_checksum = -1;

  public long duration_in_ms =-1L;
  public long scoring_time = -1L;

  public AUCData auc = null;
  public ConfusionMatrix cm = null;

  public ModelMetrics(Model model, Frame frame) {
    super(buildKey(model, frame));
    this.model = model._key;
    this.model_checksum = model.checksum();
    this.model_category = model._output.getModelCategory();
    this.frame = frame._key;
    this.frame_checksum = frame.checksum();
  }

  public ModelMetrics(Model model, Frame frame, long duration_in_ms, long scoring_time, AUCData auc, ConfusionMatrix cm) {
    this(model, frame);
    this.duration_in_ms = duration_in_ms;
    this.scoring_time = scoring_time;
    this.auc = auc;
    this.cm = cm;
  }

  /**
   * Factory method for creating a ModelMetrics and storing it in the DKV for later.
   */
  public static ModelMetrics createModelMetrics(Model model, Frame frame, long duration_in_ms, long scoring_time, AUCData auc, ConfusionMatrix cm) {
    ModelMetrics mm = new ModelMetrics(model, frame, duration_in_ms, scoring_time, auc, cm);
    DKV.put(mm.buildKey(), mm);
    return mm;
  }

  /**
   * Externally visible default schema
   * TODO: this is in the wrong layer: the internals should not know anything about the schemas!!!
   * This puts a reverse edge into the dependency graph.
   */
  public ModelMetricsBase schema() {
    return new ModelMetricsV3();
  }


  private static Key buildKey(Key model_key, long model_checksum, Key frame_key, long frame_checksum) {
    return Key.make("modelmetrics_" + model_key + "@" + model_checksum + "_on_" + frame_key + "@" + frame_checksum);
  }

  public static Key buildKey(Model model, Frame frame) {
    return buildKey(model._key, model.checksum(), frame._key, frame.checksum());
  }

  public Key buildKey() {
    return buildKey(this.model, this.model_checksum, this.frame, this.frame_checksum);
  }

  public boolean isForModel(Model m) {
    return (null != model && this.model_checksum == m.checksum());
  }

  public boolean isForFrame(Frame f) {
    return (null != frame && frame_checksum == f.checksum());
  }

  public void putInDKV() {
    Log.debug("Putting ModelMetrics: " + _key.toString());
    DKV.put(_key, this);
  }

  public static ModelMetrics getFromDKV(Model model, Frame frame) {
    Key metricsKey = buildKey(model, frame);

    Log.debug("Getting ModelMetrics: " + metricsKey.toString());
    Value v = DKV.get(metricsKey);

    if (null == v)
      return null;

    return (ModelMetrics)v.get();
  }

  public long checksum() {
    return frame_checksum * 13 + model_checksum * 17;
  }
}
