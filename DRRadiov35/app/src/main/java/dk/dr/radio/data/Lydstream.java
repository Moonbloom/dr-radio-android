package dk.dr.radio.data;

/**
 * Created by j on 08-02-14.
 */
public class Lydstream implements Comparable<Lydstream> {
  public String url;
  public DRJson.StreamType type;
  //public DRJson.StreamKind kind;
  public DRJson.StreamQuality kvalitet;
  public String format;
  public int score;
  public boolean foretrukken;
  public int kbps;

  @Override
  public String toString() {
    return score + "/" + type + "/" + kvalitet + kbps + "/" + format + "/" + url;
  }

  @Override
  public int compareTo(Lydstream another) {
    return another.score - score;
  }
}
