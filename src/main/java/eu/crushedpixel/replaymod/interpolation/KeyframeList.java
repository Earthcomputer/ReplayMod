package eu.crushedpixel.replaymod.interpolation;

import eu.crushedpixel.replaymod.holders.Keyframe;
import eu.crushedpixel.replaymod.holders.KeyframeComparator;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@NoArgsConstructor
public class KeyframeList<K extends KeyframeValue> extends ArrayList<Keyframe<K>> {

    protected static final KeyframeComparator KEYFRAME_COMPARATOR = new KeyframeComparator();

    protected Boolean previousCallLinear = null;

    protected Interpolation<K> interpolation;

    public KeyframeList(List<Keyframe<K>> initial) {
        for(Keyframe<K> kf : initial) {
            add(kf);
        }
    }

    @Override
    public boolean add(Keyframe<K> t) {
        //remove keyframes that have same timestamp
        for(Keyframe kf : new ArrayList<Keyframe>(this)) {
            if(kf.getRealTimestamp() == t.getRealTimestamp()) {
                super.remove(kf);
            }
        }

        boolean success = super.add(t);
        sort();
        return success;
    }

    @Override
    public void add(int index, Keyframe<K> element) {
        super.add(index, element);
        sort();
    }

    @Override
    public boolean addAll(Collection<? extends Keyframe<K>> c) {
        boolean result = super.addAll(c);
        sort();
        return result;
    }

    @Override
    public boolean addAll(int index, Collection<? extends Keyframe<K>> c) {
        boolean result = super.addAll(index, c);
        sort();
        return result;
    }

    @Override
    public Keyframe<K> remove(int index) {
        Keyframe<K> removed = super.remove(index);
        sort();
        return removed;
    }

    @Override
    public boolean remove(Object o) {
        boolean success = super.remove(o);
        sort();
        return success;
    }

    public void sort() {
        previousCallLinear = null;
        Collections.sort(this, KEYFRAME_COMPARATOR);
    }

    /**
     * Returns the first Keyframe that comes before a given value.
     * @param realTime The value to use
     * @param inclusive Whether the previous Keyframe might have the same timestamp as <b>realTime</b>
     * @return The first Keyframe prior to the given value
     */
    public Keyframe<K> getPreviousKeyframe(int realTime, boolean inclusive) {
        if(this.isEmpty()) return null;

        List<Keyframe<K>> found = new ArrayList<Keyframe<K>>();

        for(Keyframe<K> kf : this) {

            if((inclusive && kf.getRealTimestamp() <= realTime) || (!inclusive && kf.getRealTimestamp() < realTime)) {
                found.add(kf);
            }

        }

        if(found.size() > 0)
            return found.get(found.size() - 1); //last element is nearest

        return null;
    }

    /**
     * Returns the first Keyframe that comes after a given value.
     * @param realTime The value to use
     * @param inclusive Whether the next Keyframe might have the same timestamp as <b>realTime</b>
     * @return The first Keyframe after the given value
     */
    public Keyframe<K> getNextKeyframe(int realTime, boolean inclusive) {
        if(this.isEmpty()) return null;

        for(Keyframe<K> kf : this) {

            if((inclusive && kf.getRealTimestamp() >= realTime) || (!inclusive && kf.getRealTimestamp() > realTime)) {
                return kf; //first found element is next
            }

        }

        return null;
    }

    /**
     * Returns the Keyframe that is closest to the given timestamp.
     * @param realTime The timestamp to start searching at
     * @param tolerance The threshold to allow for close Keyframes
     * @return The closest Keyframe, or null if no Keyframe within treshold
     */
    public Keyframe<K> getClosestKeyframeForTimestamp(int realTime, int tolerance) {
        List<Keyframe<K>> found = new ArrayList<Keyframe<K>>();
        for(Keyframe<K> kf : this) {
            if(Math.abs(kf.getRealTimestamp() - realTime) <= tolerance) {
                found.add(kf);
            }
        }

        Keyframe<K> closest = null;

        for(Keyframe<K> kf : found) {
            if(closest == null || Math.abs(closest.getRealTimestamp() - realTime) > Math.abs(kf.getRealTimestamp() - realTime)) {
                closest = kf;
            }
        }
        return closest;
    }

    public Keyframe<K> first() {
        if(isEmpty()) return null;
        return get(0);
    }

    public Keyframe<K> last() {
        if(isEmpty()) return null;
        return get(size()-1);
    }

    public K getInterpolatedValueForTimestamp(int timestamp, boolean linear) {
        return getInterpolatedValueForPathPosition(getPositionOnPath(timestamp), linear);
    }

    public K getInterpolatedValueForPathPosition(float pathPosition, boolean linear) {
        if(first() == null) return null;
        if(size() == 1) return first().getValue();

        @SuppressWarnings("unchecked")
        K toApply = (K) first().getValue().newInstance();

        if(previousCallLinear != (Boolean)linear) {
            recalculate(linear);
        }

        interpolation.applyPoint(pathPosition, toApply);

        return toApply;
    }

    /**
     * Recalculates the underlying Interpolation instances.
     * @param linear Whether to prepare linear or cubic interpolation
     */
    @SuppressWarnings("unchecked")
    public void recalculate(boolean linear) {
        previousCallLinear = linear;

        if(size() < 2) return;

        interpolation = linear ? first().getValue().getLinearInterpolator() : first().getValue().getCubicInterpolator();

        for(Keyframe<K> keyframe : this) {
            interpolation.addPoint(keyframe.getValue());
        }

        interpolation.prepare();
    }

    /**
     * Returns a value between 0 and 1, representing the number that should be passed
     * to Interpolation#getValue() calls on this list of Keyframes.
     * @param timestamp The value to use
     * @return A value between 0 and 1
     */
    protected float getPositionOnPath(int timestamp) {
        Keyframe previousKeyframe = getPreviousKeyframe(timestamp, true);
        Keyframe nextKeyframe = getNextKeyframe(timestamp, true);

        int previousTimestamp = 0;
        int nextTimestamp = 0;

        if(nextKeyframe != null || previousKeyframe != null) {
            if(nextKeyframe != null) {
                nextTimestamp = nextKeyframe.getRealTimestamp();
            } else {
                nextTimestamp = previousKeyframe.getRealTimestamp();
            }

            if(previousKeyframe != null) {
                previousTimestamp = previousKeyframe.getRealTimestamp();
            } else {
                previousTimestamp = nextKeyframe.getRealTimestamp();
            }
        }

        int currentPosDiff = nextTimestamp - previousTimestamp;
        int currentPos = timestamp - previousTimestamp;

        float currentStepPercentage = (float) currentPos / (float) currentPosDiff;
        if(Float.isInfinite(currentStepPercentage) || Float.isNaN(currentStepPercentage)) currentStepPercentage = 0;

        float value = (indexOf(previousKeyframe) + currentStepPercentage) /
                (float)(size() - 1);

        return Math.max(0, Math.min(1, value));
    }

    protected int getTimestampFromPathPosition(float pathPosition) {
        int keyframeIndex = (int)Math.min(size()-1, pathPosition*(size()-1));
        float remainder = (pathPosition - ((float)keyframeIndex/(size()-1)));
        float partial = remainder / (1f/(size()-1));

        int previousTimestamp = get(keyframeIndex).getRealTimestamp();
        int nextTimestamp = size()-1 > keyframeIndex ? get(keyframeIndex+1).getRealTimestamp() : previousTimestamp;

        int diff = nextTimestamp - previousTimestamp;
        return (int)(previousTimestamp + (partial*diff));
    }

}
