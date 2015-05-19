/*
 * Copyright 2015 by Thomas Lottermann
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package notaql.datamodel.fixation;

import notaql.datamodel.ComplexValue;
import notaql.model.EvaluationException;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Provides a way to see which path has been selected during evaluation.
 * Such a path can consist of multiple steps which might be bound (by *, ?() or similar expressions) or not.
 */
public class Fixation implements Serializable {
    private static final long serialVersionUID = 1569791160406292056L;
    private List<FixationStep<?>> fixedPath;
    private ComplexValue rootValue;

    /**
     * Create an empty fixation
     * @param rootValue
     */
    public Fixation(ComplexValue rootValue) {
        this(rootValue, new LinkedList<>());
    }

    /**
     * Create a fixation consisting of the provided steps
     * @param rootValue
     * @param fixedPath
     */
    public Fixation(ComplexValue rootValue, FixationStep<?>... fixedPath) {
        this(rootValue, Arrays.asList(fixedPath));
    }

    /**
     * Create a fixation consisting of the provided steps
     * @param rootValue
     * @param fixedPath
     */
    public Fixation(ComplexValue rootValue, List<FixationStep<?>> fixedPath) {
        this.fixedPath = new LinkedList<>(fixedPath);
        this.rootValue = rootValue;
    }

    /**
     * Creates a new fixation from the old one appending the provided step.
     *
     * @param fixation
     * @param step
     */
    public Fixation(Fixation fixation, FixationStep step) {
        this.fixedPath = new LinkedList<>(fixation.fixedPath);
        this.fixedPath.add(step);
        this.rootValue = fixation.rootValue;
    }

    /**
     * Returns the steps (FIFO)
     * @return
     */
    public List<FixationStep<?>> getFixedPath() {
        return fixedPath;
    }

    /**
     * Provides the root value, at which this fixation starts
     * @return
     */
    public ComplexValue getRootValue() {
        return rootValue;
    }

    /**
     * Provides the step which comes after traversing the input fixation to the end.
     *
     * e.g. this is 0.bla.1 and fixation is 0.bla then the output is 1
     *
     * @param fixation
     * @return
     */
    public FixationStep<?> getNextStep(Fixation fixation) {
        if (rootValue != fixation.rootValue)
            return null;

        final Iterator<FixationStep<?>> myIterator = fixedPath.iterator();
        final Iterator<FixationStep<?>> otherIterator = fixation.fixedPath.iterator();

        while (myIterator.hasNext() && otherIterator.hasNext()) {
            final FixationStep mine = myIterator.next();
            final FixationStep others = otherIterator.next();

            if(!mine.equals(others))
                return null;
        }

        if (myIterator.hasNext())
            return myIterator.next();

        return null;
    }

    /**
     * Provides the fixation which is common to both this and the other fixation.
     *
     * e.g. a.b.0.c and a.b.1.c => a.b
     *
     * @param other
     * @return
     */
    public Fixation getCommonPrefix(Fixation other) {
        if (rootValue != other.rootValue)
            return null;

        final Fixation result = new Fixation(rootValue, new LinkedList<>());
        final Iterator<FixationStep<?>> myIterator = fixedPath.iterator();
        final Iterator<FixationStep<?>> otherIterator = other.fixedPath.iterator();

        while (myIterator.hasNext() && otherIterator.hasNext()) {
            final FixationStep myStep = myIterator.next();
            final FixationStep otherStep = otherIterator.next();

            if (!myStep.equals(otherStep))
                break;

            result.fixedPath.add(myStep);
        }

        return result;
    }

    /**
     * Returns the more specific fixation (in terms of bound steps).
     *
     * e.g. 0(bound).a(bound) is more specific than 0(bound).b
     *
     * @param other
     * @return
     */
    public Fixation getMoreSpecific(Fixation other) {
        Fixation commonPrefix = getCommonPrefix(other);

        final FixationStep<?> ownNextStep = this.getNextStep(commonPrefix);
        final FixationStep<?> otherNextStep = other.getNextStep(commonPrefix);

        if (otherNextStep == null)
            return this;

        if (ownNextStep == null)
            return other;

        assert !(ownNextStep.isBound() && otherNextStep.isBound());

        if (ownNextStep.isBound())
            return this;

        if (otherNextStep.isBound())
            return other;

        // Fix the issues that causes fixations to be forgotten.
        // e.g. The query
        // OUT._id <- IN.children[*].name,
        // OUT.parents <- LIST(
        //                  OBJECT(
        //                    name <- IN._id,
        //                    allowance <- IN.children[@].allowance
        //                  )
        //                );
        // FIXME: Issue #22 is caused this way. Actually all fixations may be returned as they are all relevant.
        final long ownBindCount = countBound(this);
        final long otherBindCount = countBound(other);

        return ownBindCount >= otherBindCount ? this : other;
    }

    /**
     * Provides the number of bound steps
     *
     * @param fixation
     * @return
     */
    private static long countBound(Fixation fixation) {
        return fixation.getFixedPath().stream().filter(FixationStep::isBound).count();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Fixation fixation = (Fixation) o;

        if (fixedPath != null ? !fixedPath.equals(fixation.fixedPath) : fixation.fixedPath != null) return false;
        if (rootValue != null ? !rootValue.equals(fixation.rootValue) : fixation.rootValue != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = fixedPath != null ? fixedPath.hashCode() : 0;
        result = 31 * result + (rootValue != null ? rootValue.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return fixedPath.stream().map(FixationStep::toString).collect(Collectors.joining("."));
    }
}
