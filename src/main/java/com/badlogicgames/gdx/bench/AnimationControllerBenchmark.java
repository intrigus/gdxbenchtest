/*
 * Copyright (c) 2014, Oracle America, Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  * Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 *  * Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 *  * Neither the name of Oracle nor the names of its contributors may be used
 *    to endorse or promote products derived from this software without
 *    specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.badlogicgames.gdx.bench;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;

import com.badlogic.gdx.graphics.g3d.model.NodeKeyframe;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;

@State(Scope.Benchmark)
public class AnimationControllerBenchmark {

	Array<NodeKeyframe<String>> keyFrames = new Array<NodeKeyframe<String>>();

	@Param({"2", "4", "6", "8", "10", "12", "14", "16", "18", "20"}) public long potSize;

	public float entropyFactor = 0; // 0 for minimum entropy

	public float duration;
	public long ntests;

	@Setup
	public void setup () {
		duration = 0;
		float time = 0;
		for (long i = 1; i < (1 << potSize); i++) {
			duration = time;
			time += MathUtils.random() * entropyFactor + 1;
			keyFrames.add(new NodeKeyframe<String>(duration, "frame"));
		}
		ntests = 1 << potSize;
		System.out.println("setup: " + (1 << potSize));
	}

	@Benchmark
	public void testGetFirstKeyframeIndexAtTimeBinSearchHeuristic (Blackhole bh) {
		for (int i = 0; i < ntests; i++) {
			bh.consume(AnimationControllerBench.getFirstKeyframeIndexAtTimeBinSearchHeuristic(keyFrames,
				duration * (float)i / (float)(ntests - 1)));
		}
	}

	@Benchmark
	public void testGetFirstKeyframeIndexAtTimeBinSearch (Blackhole bh) {
		for (int i = 0; i < ntests; i++) {
			bh.consume(
				AnimationControllerBench.getFirstKeyframeIndexAtTimeBinSearch(keyFrames, duration * (float)i / (float)(ntests - 1)));
		}
	}

	//@Benchmark
	public void testGetFirstKeyframeIndexAtTimeLinear (Blackhole bh) {
		for (int i = 0; i < ntests; i++) {
			bh.consume(
				AnimationControllerBench.getFirstKeyframeIndexAtTimeLinear(keyFrames, duration * (float)i / (float)(ntests - 1)));
		}
	}

	public static void main (String[] args) throws RunnerException {
		Options opt = new OptionsBuilder().include(AnimationControllerBenchmark.class.getSimpleName()).forks(1)
			.measurementTime(TimeValue.seconds(5)).warmupTime(TimeValue.seconds(5)).build();

		new Runner(opt).run();
	}
}
