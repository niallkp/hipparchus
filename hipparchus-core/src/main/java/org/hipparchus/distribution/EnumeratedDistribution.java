/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.hipparchus.distribution;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.hipparchus.exception.LocalizedCoreFormats;
import org.hipparchus.exception.MathIllegalArgumentException;
import org.hipparchus.util.MathArrays;
import org.hipparchus.util.Pair;

/**
 * A generic implementation of a
 * <a href="http://en.wikipedia.org/wiki/Probability_distribution#Discrete_probability_distribution">
 * discrete probability distribution (Wikipedia)</a> over a finite sample space,
 * based on an enumerated list of &lt;value, probability&gt; pairs.
 * <p>
 * Input probabilities must all be non-negative, but zero values are allowed and
 * their sum does not have to equal one. Constructors will normalize input
 * probabilities to make them sum to one.
 * <p>
 * The list of &lt;value, probability&gt; pairs does not, strictly speaking, have
 * to be a function and it can contain null values.  The pmf created by the constructor
 * will combine probabilities of equal values and will treat null values as equal.
 * <p>
 * For example, if the list of pairs &lt;"dog", 0.2&gt;, &lt;null, 0.1&gt;,
 * &lt;"pig", 0.2&gt;, &lt;"dog", 0.1&gt;, &lt;null, 0.4&gt; is provided to the
 * constructor, the resulting pmf will assign mass of 0.5 to null, 0.3 to "dog"
 * and 0.2 to null.
 *
 * @param <T> type of the elements in the sample space.
 */
public class EnumeratedDistribution<T> implements Serializable {

    /** Serializable UID. */
    private static final long serialVersionUID = 20123308L;

    /**
     * List of random variable values.
     */
    private final List<T> singletons;

    /**
     * Probabilities of respective random variable values. For i = 0, ..., singletons.size() - 1,
     * probability[i] is the probability that a random variable following this distribution takes
     * the value singletons[i].
     */
    private final double[] probabilities;

    /**
     * Cumulative probabilities, cached to speed up sampling.
     */
    private final double[] cumulativeProbabilities;

    /**
     * Create an enumerated distribution using the given random number generator
     * and probability mass function enumeration.
     *
     * @param pmf probability mass function enumerated as a list of &lt;T, probability&gt;
     * pairs.
     * @throws MathIllegalArgumentException if any of the probabilities are negative.
     * @throws MathIllegalArgumentException if any of the probabilities are NaN.
     * @throws MathIllegalArgumentException if any of the probabilities are infinite.
     */
    public EnumeratedDistribution(final List<Pair<T, Double>> pmf)
        throws MathIllegalArgumentException {

        singletons = new ArrayList<>(pmf.size());
        final double[] probs = new double[pmf.size()];

        for (int i = 0; i < pmf.size(); i++) {
            final Pair<T, Double> sample = pmf.get(i);
            singletons.add(sample.getKey());
            final double p = sample.getValue();
            if (p < 0) {
                throw new MathIllegalArgumentException(LocalizedCoreFormats.NUMBER_TOO_SMALL, p, 0);
            }
            if (Double.isInfinite(p)) {
                throw new MathIllegalArgumentException(LocalizedCoreFormats.NOT_FINITE_NUMBER, p);
            }
            if (Double.isNaN(p)) {
                throw new MathIllegalArgumentException(LocalizedCoreFormats.NAN_NOT_ALLOWED);
            }
            probs[i] = p;
        }

        probabilities = MathArrays.normalizeArray(probs, 1.0);

        cumulativeProbabilities = new double[probabilities.length];
        double sum = 0;
        for (int i = 0; i < probabilities.length; i++) {
            sum += probabilities[i];
            cumulativeProbabilities[i] = sum;
        }
    }

    /**
     * For a random variable {@code X} whose values are distributed according to
     * this distribution, this method returns {@code P(X = x)}. In other words,
     * this method represents the probability mass function (PMF) for the
     * distribution.
     * <p>
     * Note that if {@code x1} and {@code x2} satisfy {@code x1.equals(x2)},
     * or both are null, then {@code probability(x1) = probability(x2)}.
     *
     * @param x the point at which the PMF is evaluated
     * @return the value of the probability mass function at {@code x}
     */
    public double probability(final T x) {
        double probability = 0;

        for (int i = 0; i < probabilities.length; i++) {
            if ((x == null && singletons.get(i) == null) ||
                (x != null && x.equals(singletons.get(i)))) {
                probability += probabilities[i];
            }
        }

        return probability;
    }

    /**
     * Return the probability mass function as a list of <value, probability> pairs.
     * <p>
     * Note that if duplicate and / or null values were provided to the constructor
     * when creating this EnumeratedDistribution, the returned list will contain these
     * values.  If duplicates values exist, what is returned will not represent
     * a pmf (i.e., it is up to the caller to consolidate duplicate mass points).
     *
     * @return the probability mass function.
     */
    public List<Pair<T, Double>> getPmf() {
        final List<Pair<T, Double>> samples = new ArrayList<>(probabilities.length);

        for (int i = 0; i < probabilities.length; i++) {
            samples.add(new Pair<>(singletons.get(i), probabilities[i]));
        }

        return samples;
    }

}
