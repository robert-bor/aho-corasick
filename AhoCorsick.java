import static java.util.Collections.sort;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.LinkedBlockingDeque;

public class AhoCorsick {
	public interface Intervalable extends Comparable {

		int getStart();

		int getEnd();

		int size();

	}

	public class IntervalableComparatorByPosition implements Comparator<Intervalable> {

		@Override
		public int compare(final Intervalable intervalable, final Intervalable intervalable2) {
			return intervalable.getStart() - intervalable2.getStart();
		}

	}

	public class IntervalableComparatorBySize implements Comparator<Intervalable> {

		@Override
		public int compare(final Intervalable intervalable, final Intervalable intervalable2) {
			int comparison = intervalable2.size() - intervalable.size();

			if (comparison == 0) {
				comparison = intervalable.getStart() - intervalable2.getStart();
			}

			return comparison;
		}

	}

	private enum Direction {
		LEFT, RIGHT
	}

	public class IntervalNode {

		private IntervalNode left;
		private IntervalNode right;
		private int point;
		private List<Intervalable> intervals = new ArrayList<>();

		public IntervalNode(final List<Intervalable> intervals) {
			this.point = determineMedian(intervals);

			final List<Intervalable> toLeft = new ArrayList<>();
			final List<Intervalable> toRight = new ArrayList<>();

			for (Intervalable interval : intervals) {
				if (interval.getEnd() < this.point) {
					toLeft.add(interval);
				} else if (interval.getStart() > this.point) {
					toRight.add(interval);
				} else {
					this.intervals.add(interval);
				}
			}

			if (toLeft.size() > 0) {
				this.left = new IntervalNode(toLeft);
			}
			if (toRight.size() > 0) {
				this.right = new IntervalNode(toRight);
			}
		}

		public int determineMedian(final List<Intervalable> intervals) {
			int start = -1;
			int end = -1;
			for (Intervalable interval : intervals) {
				int currentStart = interval.getStart();
				int currentEnd = interval.getEnd();
				if (start == -1 || currentStart < start) {
					start = currentStart;
				}
				if (end == -1 || currentEnd > end) {
					end = currentEnd;
				}
			}
			return (start + end) / 2;
		}

		public List<Intervalable> findOverlaps(final Intervalable interval) {
			final List<Intervalable> overlaps = new ArrayList<>();

			if (this.point < interval.getStart()) {
				// Tends to the right
				addToOverlaps(interval, overlaps, findOverlappingRanges(this.right, interval));
				addToOverlaps(interval, overlaps, checkForOverlapsToTheRight(interval));
			} else if (this.point > interval.getEnd()) {
				// Tends to the left
				addToOverlaps(interval, overlaps, findOverlappingRanges(this.left, interval));
				addToOverlaps(interval, overlaps, checkForOverlapsToTheLeft(interval));
			} else {
				// Somewhere in the middle
				addToOverlaps(interval, overlaps, this.intervals);
				addToOverlaps(interval, overlaps, findOverlappingRanges(this.left, interval));
				addToOverlaps(interval, overlaps, findOverlappingRanges(this.right, interval));
			}

			return overlaps;
		}

		protected void addToOverlaps(final Intervalable interval, final List<Intervalable> overlaps,
				final List<Intervalable> newOverlaps) {
			for (final Intervalable currentInterval : newOverlaps) {
				if (!currentInterval.equals(interval)) {
					overlaps.add(currentInterval);
				}
			}
		}

		protected List<Intervalable> checkForOverlapsToTheLeft(final Intervalable interval) {
			return checkForOverlaps(interval, Direction.LEFT);
		}

		protected List<Intervalable> checkForOverlapsToTheRight(final Intervalable interval) {
			return checkForOverlaps(interval, Direction.RIGHT);
		}

		protected List<Intervalable> checkForOverlaps(final Intervalable interval, final Direction direction) {
			final List<Intervalable> overlaps = new ArrayList<>();

			for (final Intervalable currentInterval : this.intervals) {
				switch (direction) {
				case LEFT:
					if (currentInterval.getStart() <= interval.getEnd()) {
						overlaps.add(currentInterval);
					}
					break;
				case RIGHT:
					if (currentInterval.getEnd() >= interval.getStart()) {
						overlaps.add(currentInterval);
					}
					break;
				}
			}

			return overlaps;
		}

		protected List<Intervalable> findOverlappingRanges(IntervalNode node, Intervalable interval) {
			return node == null ? Collections.<Intervalable>emptyList() : node.findOverlaps(interval);
		}
	}

	public class IntervalTree {

		private final IntervalNode rootNode;

		public IntervalTree(List<Intervalable> intervals) {
			this.rootNode = new IntervalNode(intervals);
		}

		public List<Intervalable> removeOverlaps(final List<Intervalable> intervals) {

			// Sort the intervals on size, then left-most position
			sort(intervals, new IntervalableComparatorBySize());

			final Set<Intervalable> removeIntervals = new TreeSet<>();

			for (final Intervalable interval : intervals) {
				// If the interval was already removed, ignore it
				if (removeIntervals.contains(interval)) {
					continue;
				}

				// Remove all overallping intervals
				removeIntervals.addAll(findOverlaps(interval));
			}

			// Remove all intervals that were overlapping
			for (final Intervalable removeInterval : removeIntervals) {
				intervals.remove(removeInterval);
			}

			// Sort the intervals, now on left-most position only
			sort(intervals, new IntervalableComparatorByPosition());

			return intervals;
		}

		public List<Intervalable> findOverlaps(final Intervalable interval) {
			return rootNode.findOverlaps(interval);
		}

	}

	public class Interval implements Intervalable {

		private final int start;
		private final int end;

		/**
		 * Constructs an interval with a start and end position.
		 *
		 * @param start The interval's starting text position.
		 * @param end   The interval's ending text position.
		 */
		public Interval(final int start, final int end) {
			this.start = start;
			this.end = end;
		}

		/**
		 * Returns the starting offset into the text for this interval.
		 *
		 * @return A number between 0 (start of text) and the text length.
		 */
		@Override
		public int getStart() {
			return this.start;
		}

		/**
		 * Returns the ending offset into the text for this interval.
		 *
		 * @return A number between getStart() + 1 and the text length.
		 */
		@Override
		public int getEnd() {
			return this.end;
		}

		/**
		 * Returns the length of the interval.
		 *
		 * @return The end position less the start position, plus one.
		 */
		@Override
		public int size() {
			return end - start + 1;
		}

		/**
		 * Answers whether the given interval overlaps this interval instance.
		 *
		 * @param other the other interval to check for overlap
		 * @return true The intervals overlap.
		 */
		public boolean overlapsWith(final Interval other) {
			return this.start <= other.getEnd() && this.end >= other.getStart();
		}

		public boolean overlapsWith(int point) {
			return this.start <= point && point <= this.end;
		}

		@Override
		public boolean equals(Object o) {
			if (!(o instanceof Intervalable)) {
				return false;
			}
			Intervalable other = (Intervalable) o;
			return this.start == other.getStart() && this.end == other.getEnd();
		}

		@Override
		public int hashCode() {
			return this.start % 100 + this.end % 100;
		}

		@Override
		public int compareTo(Object o) {
			if (!(o instanceof Intervalable)) {
				return -1;
			}
			Intervalable other = (Intervalable) o;
			int comparison = this.start - other.getStart();
			return comparison != 0 ? comparison : this.end - other.getEnd();
		}

		/**
		 * Returns the starting offset and ending offset separated by a full colon (:).
		 *
		 * @return A non-null String, never empty.
		 */
		@Override
		public String toString() {
			return this.start + ":" + this.end;
		}
	}

	public class TrieConfig {

		private boolean allowOverlaps = true;

		private boolean onlyWholeWords = false;

		private boolean onlyWholeWordsWhiteSpaceSeparated = false;

		private boolean caseInsensitive = false;

		private boolean stopOnHit = false;

		public boolean isStopOnHit() {
			return stopOnHit;
		}

		public void setStopOnHit(boolean stopOnHit) {
			this.stopOnHit = stopOnHit;
		}

		public boolean isAllowOverlaps() {
			return allowOverlaps;
		}

		public void setAllowOverlaps(boolean allowOverlaps) {
			this.allowOverlaps = allowOverlaps;
		}

		public boolean isOnlyWholeWords() {
			return onlyWholeWords;
		}

		public void setOnlyWholeWords(boolean onlyWholeWords) {
			this.onlyWholeWords = onlyWholeWords;
		}

		public boolean isOnlyWholeWordsWhiteSpaceSeparated() {
			return onlyWholeWordsWhiteSpaceSeparated;
		}

		public void setOnlyWholeWordsWhiteSpaceSeparated(boolean onlyWholeWordsWhiteSpaceSeparated) {
			this.onlyWholeWordsWhiteSpaceSeparated = onlyWholeWordsWhiteSpaceSeparated;
		}

		public boolean isCaseInsensitive() {
			return caseInsensitive;
		}

		public void setCaseInsensitive(boolean caseInsensitive) {
			this.caseInsensitive = caseInsensitive;
		}
	}

	/**
	 * Responsible for tracking the bounds of matched terms.
	 */
	public class Emit extends Interval implements Intervalable {
		private final String keyword;

		public Emit(final int start, final int end, final String keyword) {
			super(start, end);
			this.keyword = keyword;
		}

		public String getKeyword() {
			return this.keyword;
		}

		@Override
		public String toString() {
			return super.toString() + "=" + this.keyword;
		}

	}

	/**
	 * Contains a matched term and its associated payload data.
	 * 
	 * @param <T> Type of the wrapped payload-data.
	 * @author Daniel Beck
	 */
	public class PayloadEmit<T> extends Interval implements Intervalable {

		private final String keyword;

		private final T payload;

		/**
		 * Created a PayloadEmit
		 * 
		 * @param start   Start of the matched search term.
		 * @param end     End of the matched search term.
		 * @param keyword Keyword that matched.
		 * @param payload Emitted payload data.
		 */
		public PayloadEmit(final int start, final int end, String keyword, T payload) {
			super(start, end);
			this.keyword = keyword;
			this.payload = payload;
		}

		public String getKeyword() {
			return this.keyword;
		}

		/**
		 * Returns the payload associated to this emit.
		 * 
		 * @return the associated payload
		 */
		public T getPayload() {
			return this.payload;
		}

		@Override
		public String toString() {
			return super.toString() + "=" + this.keyword + (this.payload != null ? "->" + this.payload : "");
		}
	}

	public interface EmitHandler {
		boolean emit(Emit emit);
	}

	public interface PayloadEmitHandler<T> {
		boolean emit(PayloadEmit<T> emit);
	}

	public interface StatefulEmitHandler extends EmitHandler {
		List<Emit> getEmits();
	}

	public interface StatefulPayloadEmitHandler<T> extends PayloadEmitHandler<T> {
		List<PayloadEmit<T>> getEmits();
	}

	/**
	 * Convenience wrapper class that delegates every method to a
	 * {@link StatefulPayloadEmitHandler}.
	 */
	public class StatefulPayloadEmitDelegateHandler implements StatefulPayloadEmitHandler<String> {

		private StatefulEmitHandler handler;

		public StatefulPayloadEmitDelegateHandler(StatefulEmitHandler handler) {
			this.handler = handler;

		}

		private List<PayloadEmit<String>> asEmits(Collection<Emit> emits) {
			List<PayloadEmit<String>> result = new ArrayList<>();
			for (Emit emit : emits) {
				result.add(new PayloadEmit<String>(emit.getStart(), emit.getEnd(), emit.getKeyword(), null));
			}
			return result;
		}

		@Override
		public boolean emit(PayloadEmit<String> emit) {
			Emit newEmit = new Emit(emit.getStart(), emit.getEnd(), emit.getKeyword());
			return handler.emit(newEmit);
		}

		@Override
		public List<PayloadEmit<String>> getEmits() {
			List<Emit> emits = this.handler.getEmits();
			return asEmits(emits);
		}
	}

	public class PayloadEmitDelegateHandler implements PayloadEmitHandler<String> {

		private EmitHandler handler;

		public PayloadEmitDelegateHandler(EmitHandler handler) {
			this.handler = handler;

		}

		@Override
		public boolean emit(PayloadEmit<String> emit) {
			Emit newEmit = new Emit(emit.getStart(), emit.getEnd(), emit.getKeyword());
			return handler.emit(newEmit);
		}

	}

	public class DefaultPayloadEmitHandler<T> implements StatefulPayloadEmitHandler<T> {

		private final List<PayloadEmit<T>> emits = new ArrayList<>();

		@Override
		public boolean emit(final PayloadEmit<T> emit) {
			this.emits.add(emit);
			return true;
		}

		@Override
		public List<PayloadEmit<T>> getEmits() {
			return this.emits;
		}
	}

	public class DefaultEmitHandler implements StatefulEmitHandler {

		private final List<Emit> emits = new ArrayList<>();

		@Override
		public boolean emit(final Emit emit) {
			this.emits.add(emit);
			return true;
		}

		@Override
		public List<Emit> getEmits() {
			return this.emits;
		}
	}

	public abstract class AbstractStatefulPayloadEmitHandler<T> implements StatefulPayloadEmitHandler<T> {

		private final List<PayloadEmit<T>> emits = new ArrayList<>();

		public void addEmit(final PayloadEmit<T> emit) {
			this.emits.add(emit);
		}

		@Override
		public List<PayloadEmit<T>> getEmits() {
			return this.emits;
		}

	}

	public abstract class AbstractStatefulEmitHandler implements StatefulEmitHandler {

		private final List<Emit> emits = new ArrayList<>();

		public void addEmit(final Emit emit) {
			this.emits.add(emit);
		}

		@Override
		public List<Emit> getEmits() {
			return this.emits;
		}

	}

	public abstract class Token {
		private String fragment;

		public Token(String fragment) {
			this.fragment = fragment;
		}

		public String getFragment() {
			return this.fragment;
		}

		public abstract boolean isMatch();

		public abstract Emit getEmit();
	}

	public class FragmentToken extends Token {

		public FragmentToken(String fragment) {
			super(fragment);
		}

		@Override
		public boolean isMatch() {
			return false;
		}

		@Override
		public Emit getEmit() {
			return null;
		}

	}

	public class MatchToken extends Token {

		private final Emit emit;

		public MatchToken(final String fragment, final Emit emit) {
			super(fragment);
			this.emit = emit;

		}

		@Override
		public boolean isMatch() {
			return true;
		}

		@Override
		public Emit getEmit() {
			return this.emit;
		}
	}

	/**
	 * Contains the matched keyword and some payload data.
	 * 
	 * @author Daniel Beck
	 * @param <T> The type of the wrapped payload data.
	 */
	public class Payload<T> implements Comparable<Payload<T>> {

		private final String keyword;
		private final T data;

		public Payload(final String keyword, final T data) {
			super();
			this.keyword = keyword;
			this.data = data;
		}

		public String getKeyword() {
			return keyword;
		}

		public T getData() {
			return data;
		}

		@Override
		public int compareTo(Payload<T> other) {
			return keyword.compareTo(other.getKeyword());
		}
	}

	/***
	 * PayloadToken holds a text ("the fragment") an emits some output. If
	 * {@link #isMatch()} returns {@code true}, the token matched a search term.
	 *
	 * @author Daniel Beck
	 *
	 * @param <T> The Type of the emitted payloads.
	 */
	public abstract class PayloadToken<T> {
		private String fragment;

		public PayloadToken(String fragment) {
			this.fragment = fragment;
		}

		public String getFragment() {
			return this.fragment;
		}

		/**
		 * Return {@code true} if a search term matched.
		 * 
		 * @return {@code true} if this is a match
		 */
		public abstract boolean isMatch();

		/**
		 * @return the payload
		 */
		public abstract PayloadEmit<T> getEmit();
	}

	public class PayloadFragmentToken<T> extends PayloadToken<T> {

		public PayloadFragmentToken(String fragment) {
			super(fragment);
		}

		@Override
		public boolean isMatch() {
			return false;
		}

		/**
		 * Returns null.
		 */
		@Override
		public PayloadEmit<T> getEmit() {
			return null;
		}
	}

	/*
	 **
	 * Container for a token ("the fragment") that can emit a type of payload. <p>
	 * This token indicates a matching search term was found, so {@link #isMatch()}
	 * always returns {@code true}. </p>
	 * 
	 * @author Daniel Beck
	 *
	 * @param <T> The Type of the emitted payloads.
	 */
	public class PayloadMatchToken<T> extends PayloadToken<T> {

		private final PayloadEmit<T> emit;

		public PayloadMatchToken(final String fragment, final PayloadEmit<T> emit) {
			super(fragment);
			this.emit = emit;
		}

		@Override
		public boolean isMatch() {
			return true;
		}

		@Override
		public PayloadEmit<T> getEmit() {
			return this.emit;
		}
	}

	/**
	 * <p>
	 * A state has various important tasks it must attend to:
	 * </p>
	 * <ul>
	 * <li>success; when a character points to another state, it must return that
	 * state</li>
	 * <li>failure; when a character has no matching state, the algorithm must be
	 * able to fall back on a state with less depth</li>
	 * <li>emits; when this state is passed and keywords have been matched, the
	 * matches and their payloads must be 'emitted' so that they can be used later
	 * on.</li>
	 * </ul>
	 * <p>
	 * The root state is special in the sense that it has no failure state; it
	 * cannot fail. If it 'fails' it will still parse the next character and start
	 * from the root node. This ensures that the algorithm always runs. All other
	 * states always have a fail state.
	 * </p>
	 *
	 * @author Daniel Beck
	 */
	public class PayloadState<T> {

		/**
		 * effective the size of the keyword
		 */
		private final int depth;

		/**
		 * only used for the root state to refer to itself in case no matches have been
		 * found
		 */
		private final PayloadState<T> rootState;

		/**
		 * referred to in the white paper as the 'goto' structure. From a state it is
		 * possible to go to other states, depending on the character passed.
		 */
		private final Map<Character, PayloadState<T>> success = new HashMap<>();

		/**
		 * if no matching states are found, the failure state will be returned
		 */
		private PayloadState<T> failure;

		/**
		 * whenever this state is reached, it will emit the matches keywords for future
		 * reference
		 */
		private Set<Payload<T>> emits;

		public PayloadState() {
			this(0);
		}

		public PayloadState(final int depth) {
			this.depth = depth;
			this.rootState = depth == 0 ? this : null;
		}

		private PayloadState<T> nextState(final Character character, final boolean ignoreRootState) {
			PayloadState<T> nextState = this.success.get(character);

			if (!ignoreRootState && nextState == null && this.rootState != null) {
				nextState = this.rootState;
			}

			return nextState;
		}

		public PayloadState<T> nextState(final Character character) {
			return nextState(character, false);
		}

		public PayloadState<T> nextStateIgnoreRootState(Character character) {
			return nextState(character, true);
		}

		public PayloadState<T> addState(Character character) {
			PayloadState<T> nextState = nextStateIgnoreRootState(character);
			if (nextState == null) {
				nextState = new PayloadState<>(this.depth + 1);
				this.success.put(character, nextState);
			}
			return nextState;
		}

		public int getDepth() {
			return this.depth;
		}

		/**
		 * Adds a payload to be emitted for this state.
		 * 
		 * @param payload to be emitted.
		 */
		public void addEmit(Payload<T> payload) {
			if (this.emits == null) {
				this.emits = new TreeSet<>();
			}
			this.emits.add(payload);
		}

		/**
		 * Adds a collection of payloads to be emitted for this state.
		 * 
		 * @param emits Collection of payloads to be emitted.
		 */
		public void addEmit(Collection<Payload<T>> emits) {
			for (Payload<T> emit : emits) {
				addEmit(emit);
			}
		}

		/**
		 * Returns a collection of emitted payloads for this state.
		 * 
		 * @return Collection of emitted payloads.
		 */
		public Collection<Payload<T>> emit() {
			return this.emits == null ? Collections.<Payload<T>>emptyList() : this.emits;
		}

		public PayloadState<T> failure() {
			return this.failure;
		}

		public void setFailure(PayloadState<T> failState) {
			this.failure = failState;
		}

		public Collection<PayloadState<T>> getStates() {
			return this.success.values();
		}

		public Collection<Character> getTransitions() {
			return this.success.keySet();
		}
	}

	/**
	 * A trie implementation that carries a payload. See {@link Trie} for details on
	 * usage.
	 *
	 * <p>
	 * The payload trie adds the possibility to specify emitted payloads for each
	 * added keyword.
	 * </p>
	 *
	 * @author Daniel Beck
	 * @param <T> The type of the supplied of the payload.
	 */
	public class PayloadTrie<T> {

		private final TrieConfig trieConfig;

		private final PayloadState<T> rootState;

		protected PayloadTrie(final TrieConfig trieConfig) {
			this.trieConfig = trieConfig;
			this.rootState = new PayloadState<>();
		}

		public PayloadTrie() {
			this.trieConfig = null;
			this.rootState = null;
			// TODO Auto-generated constructor stub
		}

		/**
		 * Used by the builder to add a text search keyword with a emit payload.
		 *
		 * @param keyword The search term to add to the list of search terms.
		 * @param emit    the payload to emit for this search term.
		 * @throws NullPointerException if the keyword is null.
		 */
		private void addKeyword(String keyword, T emit) {
			if (keyword.isEmpty()) {
				return;
			}

			addState(keyword).addEmit(new Payload<>(keyword, emit));
		}

		/**
		 * Used by the builder to add a text search keyword.
		 *
		 * @param keyword The search term to add to the list of search terms.
		 * @throws NullPointerException if the keyword is null.
		 */
		private void addKeyword(String keyword) {
			if (keyword.isEmpty()) {
				return;
			}

			addState(keyword).addEmit(new Payload<>(keyword, null));
		}

		private PayloadState<T> addState(final String keyword) {
			PayloadState<T> state = getRootState();
			for (final Character character : keyword.toCharArray()) {
				Character adjustedChar = isCaseInsensitive() ? Character.toLowerCase(character) : character;
				state = state.addState(adjustedChar);
			}
			return state;
		}

		/**
		 * Tokenizes the specified text and returns the emitted outputs.
		 *
		 * @param text The text to tokenize.
		 * @return the emitted outputs
		 */
		public Collection<PayloadToken<T>> tokenize(final String text) {
			final Collection<PayloadToken<T>> tokens = new LinkedList<>();
			final Collection<PayloadEmit<T>> collectedEmits = parseText(text);
			int lastCollectedPosition = -1;

			for (final PayloadEmit<T> emit : collectedEmits) {
				if (emit.getStart() - lastCollectedPosition > 1) {
					tokens.add(createFragment(emit, text, lastCollectedPosition));
				}

				tokens.add(createMatch(emit, text));
				lastCollectedPosition = emit.getEnd();
			}

			if (text.length() - lastCollectedPosition > 1) {
				tokens.add(createFragment(null, text, lastCollectedPosition));
			}

			return tokens;
		}

		private PayloadToken<T> createFragment(final PayloadEmit<T> emit, final String text,
				final int lastCollectedPosition) {
			return new PayloadFragmentToken<>(
					text.substring(lastCollectedPosition + 1, emit == null ? text.length() : emit.getStart()));
		}

		private PayloadToken<T> createMatch(PayloadEmit<T> emit, String text) {
			return new PayloadMatchToken<>(text.substring(emit.getStart(), emit.getEnd() + 1), emit);
		}

		/**
		 * Tokenizes a specified text and returns the emitted outputs.
		 *
		 * @param text The character sequence to tokenize.
		 * @return A collection of emits.
		 */
		public Collection<PayloadEmit<T>> parseText(final CharSequence text) {
			return parseText(text, new DefaultPayloadEmitHandler<>());
		}

		/**
		 * Tokenizes the specified text by using a custom EmitHandler and returns the
		 * emitted outputs.
		 *
		 * @param text        The character sequence to tokenize.
		 * @param emitHandler The emit handler that will be used to parse the text.
		 * @return A collection of emits.
		 */
		@SuppressWarnings("unchecked")
		public Collection<PayloadEmit<T>> parseText(final CharSequence text,
				final StatefulPayloadEmitHandler<T> emitHandler) {
			parseText(text, (PayloadEmitHandler<T>) emitHandler);

			final List<PayloadEmit<T>> collectedEmits = emitHandler.getEmits();

			if (!trieConfig.isAllowOverlaps()) {
				IntervalTree intervalTree = new IntervalTree((List<Intervalable>) (List<?>) collectedEmits);
				intervalTree.removeOverlaps((List<Intervalable>) (List<?>) collectedEmits);
			}

			return collectedEmits;
		}

		/**
		 * Returns true if the text contains contains one of the search terms. Else,
		 * returns false.
		 *
		 * @param text Specified text.
		 * @return true if the text contains one of the search terms. Else, returns
		 *         false.
		 */
		public boolean containsMatch(final CharSequence text) {
			return firstMatch(text) != null;
		}

		/**
		 * Tokenizes the specified text by using a custom EmitHandler and returns the
		 * emitted outputs.
		 *
		 * @param text        The character sequence to tokenize.
		 * @param emitHandler The emit handler that will be used to parse the text.
		 */
		public void parseText(final CharSequence text, final PayloadEmitHandler<T> emitHandler) {
			PayloadState<T> currentState = getRootState();

			for (int position = 0; position < text.length(); position++) {
				char character = text.charAt(position);

				if (trieConfig.isCaseInsensitive()) {
					character = Character.toLowerCase(character);
				}

				currentState = getState(currentState, character);
				final Collection<Payload<T>> payloads = currentState.emit();
				if (processEmits(text, position, payloads, emitHandler) && trieConfig.isStopOnHit()) {
					return;
				}
			}
		}

		/**
		 * The first matching text sequence.
		 *
		 * @param text The text to search for keywords.
		 * @return null if no matches found.
		 */
		public PayloadEmit<T> firstMatch(final CharSequence text) {
			if (!trieConfig.isAllowOverlaps()) {
				// Slow path. Needs to find all the matches to detect overlaps.
				final Collection<PayloadEmit<T>> parseText = parseText(text);

				if (parseText != null && !parseText.isEmpty()) {
					return parseText.iterator().next();
				}
			} else {
				// Fast path. Returns first match found.
				PayloadState<T> currentState = getRootState();

				for (int position = 0; position < text.length(); position++) {
					char character = text.charAt(position);

					if (trieConfig.isCaseInsensitive()) {
						character = Character.toLowerCase(character);
					}

					currentState = getState(currentState, character);
					Collection<Payload<T>> payloads = currentState.emit();

					if (payloads != null && !payloads.isEmpty()) {
						for (final Payload<T> payload : payloads) {
							final PayloadEmit<T> emit = new PayloadEmit<>(position - payload.getKeyword().length() + 1,
									position, payload.getKeyword(), payload.getData());
							if (trieConfig.isOnlyWholeWords()) {
								if (!isPartialMatch(text, emit)) {
									return emit;
								}
							} else {
								return emit;
							}
						}
					}
				}
			}

			return null;
		}

		private boolean isPartialMatch(final CharSequence searchText, final PayloadEmit<T> emit) {
			return (emit.getStart() != 0 && Character.isAlphabetic(searchText.charAt(emit.getStart() - 1)))
					|| (emit.getEnd() + 1 != searchText.length()
							&& Character.isAlphabetic(searchText.charAt(emit.getEnd() + 1)));
		}

		private boolean isPartialMatchWhiteSpaceSeparated(final CharSequence searchText, final PayloadEmit<T> emit) {
			final long size = searchText.length();
			return (emit.getStart() != 0 && !Character.isWhitespace(searchText.charAt(emit.getStart() - 1)))
					|| (emit.getEnd() + 1 != size && !Character.isWhitespace(searchText.charAt(emit.getEnd() + 1)));
		}

		private PayloadState<T> getState(PayloadState<T> currentState, final Character character) {
			PayloadState<T> newCurrentState = currentState.nextState(character);

			while (newCurrentState == null) {
				currentState = currentState.failure();
				newCurrentState = currentState.nextState(character);
			}

			return newCurrentState;
		}

		private void constructFailureStates() {
			final Queue<PayloadState<T>> queue = new LinkedBlockingDeque<>();
			final PayloadState<T> startState = getRootState();

			// First, set the fail state of all depth 1 states to the root state
			for (PayloadState<T> depthOneState : startState.getStates()) {
				depthOneState.setFailure(startState);
				queue.add(depthOneState);
			}

			// Second, determine the fail state for all depth > 1 state
			while (!queue.isEmpty()) {
				final PayloadState<T> currentState = queue.remove();

				for (final Character transition : currentState.getTransitions()) {
					PayloadState<T> targetState = currentState.nextState(transition);
					queue.add(targetState);

					PayloadState<T> traceFailureState = currentState.failure();
					while (traceFailureState.nextState(transition) == null) {
						traceFailureState = traceFailureState.failure();
					}

					final PayloadState<T> newFailureState = traceFailureState.nextState(transition);
					targetState.setFailure(newFailureState);
					targetState.addEmit(newFailureState.emit());
				}
			}
		}

		private boolean processEmits(final CharSequence text, final int position, final Collection<Payload<T>> payloads,
				final PayloadEmitHandler<T> emitHandler) {
			boolean emitted = false;
			for (final Payload<T> payload : payloads) {
				final PayloadEmit<T> payloadEmit = new PayloadEmit<>(position - payload.getKeyword().length() + 1,
						position, payload.getKeyword(), payload.getData());
				if (!(trieConfig.isOnlyWholeWords() && isPartialMatch(text, payloadEmit))
						&& !(trieConfig.isOnlyWholeWordsWhiteSpaceSeparated()
								&& isPartialMatchWhiteSpaceSeparated(text, payloadEmit))) {
					emitted = emitHandler.emit(payloadEmit) || emitted;
					if (emitted && trieConfig.isStopOnHit()) {
						break;
					}
				}
			}

			return emitted;
		}

		private boolean isCaseInsensitive() {
			return trieConfig.isCaseInsensitive();
		}

		private PayloadState<T> getRootState() {
			return this.rootState;
		}

		/**
		 * Provides a fluent interface for constructing Trie instances with payloads.
		 * 
		 * @param <T> The type of the emitted payload.
		 *
		 * @return The builder used to configure its Trie.
		 */
		public <T> PayloadTrieBuilder<T> builder() {
			return new PayloadTrieBuilder<>();
		}

		/**
		 * Builder class to create a PayloadTrie instance.
		 *
		 * @param <T> The type of the emitted payload.
		 */
		public class PayloadTrieBuilder<T> {

			private final TrieConfig trieConfig = new TrieConfig();

			private final PayloadTrie<T> trie = new PayloadTrie<>(trieConfig);

			/**
			 * Default (empty) constructor.
			 */
			private PayloadTrieBuilder() {
			}

			/**
			 * Configure the Trie to ignore case when searching for keywords in the text.
			 * This must be called before calling addKeyword because the algorithm converts
			 * keywords to lowercase as they are added, depending on this case sensitivity
			 * setting.
			 *
			 * @return This builder.
			 */
			public PayloadTrieBuilder<T> ignoreCase() {
				this.trieConfig.setCaseInsensitive(true);
				return this;
			}

			/**
			 * Configure the Trie to ignore overlapping keywords.
			 *
			 * @return This builder.
			 */
			public PayloadTrieBuilder<T> ignoreOverlaps() {
				this.trieConfig.setAllowOverlaps(false);
				return this;
			}

			/**
			 * Adds a keyword to the Trie's list of text search keywords. No Payload is
			 * supplied.
			 *
			 * @param keyword The keyword to add to the list.
			 * @return This builder.
			 * @throws NullPointerException if the keyword is null.
			 */
			public PayloadTrieBuilder<T> addKeyword(final String keyword) {
				this.trie.addKeyword(keyword);
				return this;
			}

			/**
			 * Adds a keyword and a payload to the Trie's list of text search keywords.
			 *
			 * @param keyword The keyword to add to the list.
			 * @param payload the payload to add
			 * @return This builder.
			 * @throws NullPointerException if the keyword is null.
			 */
			public PayloadTrieBuilder<T> addKeyword(final String keyword, final T payload) {
				this.trie.addKeyword(keyword, payload);
				return this;
			}

			/**
			 * Adds a list of keywords and payloads to the Trie's list of text search
			 * keywords.
			 *
			 * @param keywords The keywords to add to the list.
			 * @return This builder.
			 */
			public PayloadTrieBuilder<T> addKeywords(final Collection<Payload<T>> keywords) {
				for (Payload<T> payload : keywords) {
					this.trie.addKeyword(payload.getKeyword(), payload.getData());
				}
				return this;
			}

			/**
			 * Configure the Trie to match whole keywords in the text.
			 *
			 * @return This builder.
			 */
			public PayloadTrieBuilder<T> onlyWholeWords() {
				this.trieConfig.setOnlyWholeWords(true);
				return this;
			}

			/**
			 * Configure the Trie to match whole keywords that are separated by whitespace
			 * in the text. For example, "this keyword thatkeyword" would only match the
			 * first occurrence of "keyword".
			 *
			 * @return This builder.
			 */
			public PayloadTrieBuilder<T> onlyWholeWordsWhiteSpaceSeparated() {
				this.trieConfig.setOnlyWholeWordsWhiteSpaceSeparated(true);
				return this;
			}

			/**
			 * Configure the Trie to stop after the first keyword is found in the text.
			 *
			 * @return This builder.
			 */
			public PayloadTrieBuilder<T> stopOnHit() {
				trie.trieConfig.setStopOnHit(true);
				return this;
			}

			/**
			 * Configure the PayloadTrie based on the builder settings.
			 *
			 * @return The configured PayloadTrie.
			 */
			public PayloadTrie<T> build() {
				this.trie.constructFailureStates();
				return this.trie;
			}

			/**
			 * @return This builder.
			 * @deprecated Use ignoreCase()
			 */
			@Deprecated
			public PayloadTrieBuilder<T> caseInsensitive() {
				return ignoreCase();
			}

			/**
			 * @return This builder.
			 * @deprecated Use ignoreOverlaps()
			 */
			@Deprecated
			public PayloadTrieBuilder<T> removeOverlaps() {
				return ignoreOverlaps();
			}
		}

	}

	/**
	 * <p>
	 * A state has various important tasks it must attend to:
	 * </p>
	 * <ul>
	 * <li>success; when a character points to another state, it must return that
	 * state</li>
	 * <li>failure; when a character has no matching state, the algorithm must be
	 * able to fall back on a state with less depth</li>
	 * <li>emits; when this state is passed and keywords have been matched, the
	 * matches must be 'emitted' so that they can be used later on.</li>
	 * </ul>
	 * <p>
	 * The root state is special in the sense that it has no failure state; it
	 * cannot fail. If it 'fails' it will still parse the next character and start
	 * from the root node. This ensures that the algorithm always runs. All other
	 * states always have a fail state.
	 * </p>
	 *
	 * @author Robert Bor
	 */
	public class State {

		/**
		 * effective the size of the keyword
		 */
		private final int depth;

		/**
		 * only used for the root state to refer to itself in case no matches have been
		 * found
		 */
		private final State rootState;

		/**
		 * referred to in the white paper as the 'goto' structure. From a state it is
		 * possible to go to other states, depending on the character passed.
		 */
		private final Map<Character, State> success = new HashMap<>();

		/**
		 * if no matching states are found, the failure state will be returned
		 */
		private State failure;

		/**
		 * whenever this state is reached, it will emit the matches keywords for future
		 * reference
		 */
		private Set<String> emits;

		public State() {
			this(0);
		}

		public State(final int depth) {
			this.depth = depth;
			this.rootState = depth == 0 ? this : null;
		}

		private State nextState(final Character character, final boolean ignoreRootState) {
			State nextState = this.success.get(character);

			if (!ignoreRootState && nextState == null && this.rootState != null) {
				nextState = this.rootState;
			}

			return nextState;
		}

		public State nextState(final Character character) {
			return nextState(character, false);
		}

		public State nextStateIgnoreRootState(Character character) {
			return nextState(character, true);
		}

		public State addState(String keyword) {
			State state = this;

			for (final Character character : keyword.toCharArray()) {
				state = state.addState(character);
			}

			return state;
		}

		public State addState(Character character) {
			State nextState = nextStateIgnoreRootState(character);
			if (nextState == null) {
				nextState = new State(this.depth + 1);
				this.success.put(character, nextState);
			}
			return nextState;
		}

		public int getDepth() {
			return this.depth;
		}

		public void addEmit(String keyword) {
			if (this.emits == null) {
				this.emits = new TreeSet<>();
			}
			this.emits.add(keyword);
		}

		public void addEmit(Collection<String> emits) {
			for (String emit : emits) {
				addEmit(emit);
			}
		}

		public Collection<String> emit() {
			return this.emits == null ? Collections.<String>emptyList() : this.emits;
		}

		public State failure() {
			return this.failure;
		}

		public void setFailure(State failState) {
			this.failure = failState;
		}

		public Collection<State> getStates() {
			return this.success.values();
		}

		public Collection<Character> getTransitions() {
			return this.success.keySet();
		}
	}

	public class DefaultToken extends Token {

		private PayloadToken<String> payloadToken;

		public DefaultToken(PayloadToken<String> payloadToken) {
			super(payloadToken.getFragment());
			this.payloadToken = payloadToken;
		}

		public boolean isMatch() {
			return payloadToken.isMatch();
		}

		public Emit getEmit() {
			PayloadEmit<String> emit = payloadToken.getEmit();
			return new Emit(emit.getStart(), emit.getEnd(), emit.getKeyword());
		}

	}

	public class Trie {

		private final PayloadTrie<String> payloadTrie;

		private Trie(final PayloadTrie<String> payloadTrie) {
			this.payloadTrie = payloadTrie;
		}

		public Trie() {
			payloadTrie = null;
			System.out.println("Default Trie constructor");
			// TODO Auto-generated constructor stub
		}

		public Collection<Token> tokenize(final String text) {
			Collection<PayloadToken<String>> tokens = this.payloadTrie.tokenize(text);
			return asTokens(tokens);
		}

		private Collection<Token> asTokens(Collection<PayloadToken<String>> tokens) {
			Collection<Token> result = new ArrayList<>();
			for (PayloadToken<String> payloadToken : tokens) {
				result.add(new DefaultToken(payloadToken));
			}
			return result;
		}

		private Collection<Emit> asEmits(Collection<PayloadEmit<String>> emits) {
			Collection<Emit> result = new ArrayList<>();
			for (PayloadEmit<String> emit : emits) {
				result.add(asEmit(emit));
			}
			return result;
		}

		private Emit asEmit(PayloadEmit<String> payloadEmit) {
			return new Emit(payloadEmit.getStart(), payloadEmit.getEnd(), payloadEmit.getKeyword());
		}

		public Collection<Emit> parseText(final CharSequence text) {
			Collection<PayloadEmit<String>> parsedText = this.payloadTrie.parseText(text);
			return asEmits(parsedText);
		}

		@SuppressWarnings("UnusedReturnValue")
		public Collection<Emit> parseText(final CharSequence text, final StatefulEmitHandler emitHandler) {
			Collection<PayloadEmit<String>> parsedText = this.payloadTrie.parseText(text,
					new StatefulPayloadEmitDelegateHandler(emitHandler));
			return asEmits(parsedText);
		}

		public boolean containsMatch(final CharSequence text) {
			return firstMatch(text) != null;
		}

		public void parseText(final CharSequence text, final EmitHandler emitHandler) {
			this.payloadTrie.parseText(text, new PayloadEmitDelegateHandler(emitHandler));
		}

		/**
		 * The first matching text sequence.
		 *
		 * @param text The text to search for keywords.
		 * @return null if no matches found.
		 */
		public Emit firstMatch(final CharSequence text) {
			final PayloadEmit<String> payload = this.payloadTrie.firstMatch(text);
			return payload == null ? null : new Emit(payload.getStart(), payload.getEnd(), payload.getKeyword());
		}

		/**
		 * Provides a fluent interface for constructing Trie instances.
		 *
		 * @return The builder used to configure its Trie.
		 */
		public TrieBuilder builder() {
			return new TrieBuilder();
		}

		public class TrieBuilder {

			private final PayloadTrie<String>.PayloadTrieBuilder<String> delegate = new PayloadTrie<String>().builder();

			/**
			 * Default (empty) constructor.
			 */
			public TrieBuilder() {
			}

			/**
			 * Configure the Trie to ignore case when searching for keywords in the text.
			 * This must be called before calling addKeyword because the algorithm converts
			 * keywords to lowercase as they are added, depending on this case sensitivity
			 * setting.
			 *
			 * @return This builder.
			 */
			public TrieBuilder ignoreCase() {
				delegate.ignoreCase();
//	            this.trieConfig.setCaseInsensitive(true);
				return this;
			}

			/**
			 * Configure the Trie to ignore overlapping keywords.
			 *
			 * @return This builder.
			 */
			public TrieBuilder ignoreOverlaps() {
				delegate.ignoreOverlaps();
				return this;
			}

			/**
			 * Adds a keyword to the Trie's list of text search keywords.
			 *
			 * @param keyword The keyword to add to the list.
			 * @return This builder.
			 * @throws NullPointerException if the keyword is null.
			 */
			public TrieBuilder addKeyword(final String keyword) {
				delegate.addKeyword(keyword, null);
				return this;
			}

			/**
			 * Adds a list of keywords to the Trie's list of text search keywords.
			 *
			 * @param keywords The keywords to add to the list.
			 * @return This builder.
			 */
			public TrieBuilder addKeywords(final String... keywords) {
				for (String keyword : keywords) {
					delegate.addKeyword(keyword, null);
				}
				return this;
			}

			/**
			 * Adds a list of keywords to the Trie's list of text search keywords.
			 *
			 * @param keywords The keywords to add to the list.
			 * @return This builder.
			 */
			@SuppressWarnings("unused")
			public TrieBuilder addKeywords(final Collection<String> keywords) {
				for (String keyword : keywords) {
					this.delegate.addKeyword(keyword, null);
				}
				return this;
			}

			/**
			 * Configure the Trie to match whole keywords in the text.
			 *
			 * @return This builder.
			 */
			public TrieBuilder onlyWholeWords() {
				this.delegate.onlyWholeWords();
				return this;
			}

			/**
			 * Configure the Trie to match whole keywords that are separated by whitespace
			 * in the text. For example, "this keyword thatkeyword" would only match the
			 * first occurrence of "keyword".
			 *
			 * @return This builder.
			 */
			public TrieBuilder onlyWholeWordsWhiteSpaceSeparated() {
				this.delegate.onlyWholeWordsWhiteSpaceSeparated();
				return this;
			}

			/**
			 * Configure the Trie to stop after the first keyword is found in the text.
			 *
			 * @return This builder.
			 */
			public TrieBuilder stopOnHit() {
				this.delegate.stopOnHit();
				return this;
			}

			/**
			 * Configure the Trie based on the builder settings.
			 *
			 * @return The configured Trie.
			 */
			public Trie build() {
				PayloadTrie<String> payloadTrie = this.delegate.build();
				return new Trie(payloadTrie);
			}

			/**
			 * @return This builder.
			 * @deprecated Use ignoreCase()
			 */
			public TrieBuilder caseInsensitive() {
				return ignoreCase();
			}

			/**
			 * @return This builder.
			 * @deprecated Use ignoreOverlaps()
			 */
			public TrieBuilder removeOverlaps() {
				return ignoreOverlaps();
			}
		}

	}

	public static void main(String[] args) {
		AhoCorsick a = new AhoCorsick();
		Trie trie = a.new Trie().builder().addKeyword("hers").addKeyword("his").addKeyword("she").addKeyword("he")
				.build();
		Collection<Emit> emits = trie.parseText("ushers");
		for (Emit emit : emits) {
			System.out.println(emit);
			System.out.println(emit.keyword);
		}
	}
}
