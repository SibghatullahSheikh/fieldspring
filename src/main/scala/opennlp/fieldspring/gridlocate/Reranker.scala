package opennlp.fieldspring.gridlocate

import opennlp.fieldspring.util.printutil._
import opennlp.fieldspring.perceptron._
/**
 * A basic ranker.  Given a test item, return a list of ranked answers from
 * best to worst, with a score for each.  The score must not increase from
 * any answer to the next one.
 */
trait Ranker[TestItem, Answer] {
  /**
   * Evaluate a test item, returning a list of ranked answers from best to
   * worst, with a score for each.  The score must not increase from any
   * answer to the next one.  Any answers mentioned in `include` must be
   * included in the returned list.
   */
  def evaluate(item: TestItem, include: Iterable[Answer]):
    Iterable[(Answer, Double)]
}

/**
 * A scoring binary classifier.  Given a test item, return a score, with
 * higher numbers indicating greater likelihood of being "positive".
 */
trait ScoringBinaryClassifier[TestItem] {
  /**
   * The value of `minimum_positive` indicates the dividing line between values
   * that should be considered positive and those that should be considered
   * negative; typically this will be 0 or 0.5. */
  def minimum_positive: Double
  def score_item(item: TestItem): Double
}

/**
 * A reranker.  This is a particular type of ranker that involves two
 * steps: One to compute an initial ranking, and a second to do a more
 * accurate reranking of some subset of the highest-ranked answers.  The
 * idea is that a more accurate value can be computed when there are fewer
 * possible answers to distinguish -- assuming that the initial ranker
 * is able to include the correct answer near the top significantly more
 * often than actually at the top.
 */
trait Reranker[TestItem, Answer] extends Ranker[TestItem, Answer] {
}

/**
 * A pointwise reranker that uses a scoring classifier to assign a score
 * to each possible answer to be reranked.  The idea is that 
 */
trait PointwiseClassifyingReranker[TestItem, RerankInstance, Answer]
    extends Reranker[TestItem, Answer] {
  /** Ranker for generating initial ranking. */
  protected def initial_ranker: Ranker[TestItem, Answer]
  /** Scoring classifier for use in reranking. */
  protected def rerank_classifier: ScoringBinaryClassifier[RerankInstance]

  /**
   * Create a reranking training instance to feed to the classifier, given
   * a test item, a potential answer from the ranker, and whether the answer
   * is correct.  These training instances will be used to train the
   * classifier.
   */
  protected def create_rerank_instance(item: TestItem, possible_answer: Answer,
    score: Double): RerankInstance

  /**
   * Number of top-ranked items to submit to reranking.
   */
  protected val top_n: Int

  /**
   * Generate rerank training instances for a given ranker
   * training instance.
   */
  protected def get_rerank_training_instances(item: TestItem,
      true_answer: Answer) = {
    val answers = initial_ranker.evaluate(item, Iterable(true_answer)).take(top_n)
    for {(possible_answer, score) <- answers
         is_correct = possible_answer == true_answer
        }
      yield (
        create_rerank_instance(item, possible_answer, score), is_correct)
  }

  protected def rerank_answers(item: TestItem,
      answers: Iterable[(Answer, Double)]) = {
    val new_scores =
      for {(answer, score) <- answers
           instance = create_rerank_instance(item, answer, score)
           new_score = rerank_classifier.score_item(instance)
          }
        yield (answer, new_score)
    new_scores.toSeq sortWith (_._2 > _._2)
  }

  def evaluate(item: TestItem, include: Iterable[Answer]) = {
    val initial_answers = initial_ranker.evaluate(item, include)
    val (to_rerank, others) = initial_answers.splitAt(top_n)
    rerank_answers(item, to_rerank) ++ others
  }
}

/**
 * A pointwise reranker that uses a scoring classifier to assign a score
 * to each possible answer to be reranked.  The idea is that 
 */
trait PointwiseClassifyingRerankerWithTrainingData[
    TestItem, RerankInstance, Answer] extends
  PointwiseClassifyingReranker[TestItem, RerankInstance, Answer] {
  /**
   * Training data used to create the reranker.
   */
  protected val training_data: Iterable[(TestItem, Answer)]

  /**
   * Create the classifier used for reranking, given a set of training data
   * (in the form of pairs of reranking instances and whether they represent
   * correct answers).
   */
  protected def create_rerank_classifier(
    data: Iterable[(RerankInstance, Boolean)]
  ): ScoringBinaryClassifier[RerankInstance]

  protected val rerank_classifier = {
    val rerank_training_data = training_data.flatMap {
      case (item, true_answer) =>
        get_rerank_training_instances(item, true_answer)
    }
    create_rerank_classifier(rerank_training_data)
  }
}

/**
 * @tparam TDoc Type of the training and test documents
 * @tparam TCell Type of a cell in a cell grid
 * @tparam TGrid Type of a cell grid
 *
 * @param strategy Object encapsulating the strategy used for performing
 *   evaluation.
 */
class CellGridRanker[
  TDoc <: DistDocument[_],
  TCell <: GeoCell[_, TDoc],
  TGrid <: CellGrid[_, TDoc, TCell]
](
  strategy: GridLocateDocumentStrategy[TCell, TGrid]
) extends Ranker[TDoc, TCell] {
  def evaluate(item: TDoc, include: Iterable[TCell]) =
    strategy.return_ranked_cells(item.dist, include)
}

class DistDocumentRerankInstance[
  TDoc <: DistDocument[_],
  TCell <: GeoCell[_, TDoc],
  TGrid <: CellGrid[_, TDoc, TCell]
](
  doc: TDoc, cell: TCell, score: Double
) extends SparseFeatureVector(Map("score" -> score)) {
}

abstract class DistDocumentReranker[
  TDoc <: DistDocument[_],
  TCell <: GeoCell[_, TDoc],
  TGrid <: CellGrid[_, TDoc, TCell]
](
  _initial_ranker: Ranker[TDoc, TCell],
  _top_n: Int
) extends PointwiseClassifyingReranker[
  TDoc,
  DistDocumentRerankInstance[TDoc, TCell, TGrid],
  TCell] {
  protected val top_n = _top_n
  protected val initial_ranker = _initial_ranker

  protected def create_rerank_instance(item: TDoc, possible_answer: TCell,
    score: Double) =
      new DistDocumentRerankInstance[TDoc, TCell, TGrid](
        item, possible_answer, score)
}

/**
 * A trivial scoring binary classifier that simply returns the already
 * existing score from a ranker.
 */
class TrivialScoringBinaryClassifier[TestItem <: SparseFeatureVector](
  val minimum_positive: Double
) extends ScoringBinaryClassifier[TestItem] {
  def score_item(item: TestItem) = {
    val retval = item("score")
    errprint("Trivial scoring item %s = %s", item, retval)
    retval
  }
}

class TrivialDistDocumentReranker[
  TDoc <: DistDocument[_],
  TCell <: GeoCell[_, TDoc],
  TGrid <: CellGrid[_, TDoc, TCell]
](
  _initial_ranker: Ranker[TDoc, TCell],
  _top_n: Int
) extends DistDocumentReranker[TDoc, TCell, TGrid](
  _initial_ranker, _top_n
) {
  val rerank_classifier =
    new TrivialScoringBinaryClassifier[
      DistDocumentRerankInstance[TDoc, TCell, TGrid]](
        0 // FIXME: This is incorrect but doesn't matter
      )
}

abstract class PerceptronDistDocumentReranker[
  TDoc <: DistDocument[_],
  TCell <: GeoCell[_, TDoc],
  TGrid <: CellGrid[_, TDoc, TCell]
](
  _initial_ranker: Ranker[TDoc, TCell],
  _top_n: Int
) extends DistDocumentReranker[TDoc, TCell, TGrid](
  _initial_ranker, _top_n
) {
  // FIXME!
  val rerank_classifier = null
}
