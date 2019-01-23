
package com.badlogicgames.gdx.bench;

import com.badlogic.gdx.graphics.g3d.model.NodeKeyframe;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;

public class AnimationControllerBench {

	public static void main (String[] args) {
		Array<NodeKeyframe<String>> keyFrames = new Array<NodeKeyframe<String>>();

		int nentities = 100;
		int nnodePerEntity = 8;
		int ntracks = 3;

		float entropyFactor = 0; // 0 for minimum entropy

		System.out.println("| frames | samples | With Heuristic | Without heuristic | Linear |");
		System.out.println("|--------|---------|----------------|-------------------|--------|");

		int nframes = 2;
		for (int k = 0; k < 15; k++) {
			nframes *= 2;
			int nfactor = nentities * nnodePerEntity * ntracks;
			int ntests = MathUtils.floor(nfactor * 100000f / (float)nframes);

			float duration = 0;
			float time = 0;
			for (int i = 0; i < nframes; i++) {
				duration = time;
				time += MathUtils.random() * entropyFactor + 1;
				keyFrames.add(new NodeKeyframe<String>(duration, "frame"));
			}

			float bstime;
			{
				long ptime = System.currentTimeMillis();
				for (int i = 0; i < ntests; i++) {
					getFirstKeyframeIndexAtTimeBinSearchHeuristic(keyFrames, duration * (float)i / (float)(ntests - 1));
				}
				long ctime = System.currentTimeMillis();
				bstime = (ctime - ptime);
			}
			float nhtime;
			{
				long ptime = System.currentTimeMillis();
				for (int i = 0; i < ntests; i++) {
					getFirstKeyframeIndexAtTimeBinSearch(keyFrames, duration * (float)i / (float)(ntests - 1));
				}
				long ctime = System.currentTimeMillis();
				nhtime = (ctime - ptime);
			}
			float lrtime;
			{
				long ptime = System.currentTimeMillis();
				for (int i = 0; i < ntests; i++) {
					getFirstKeyframeIndexAtTimeLinear(keyFrames, duration * (float)i / (float)(ntests - 1));
				}
				long ctime = System.currentTimeMillis();
				lrtime = (ctime - ptime);
			}
			System.out.println("| " + nframes + " | " + ntests + " | " + bstime + " | " + nhtime + " | " + lrtime + " |");
		}

	}

	static <T> int getFirstKeyframeIndexAtTimeLinear (final Array<NodeKeyframe<T>> arr, final float time) {
		final int n = arr.size - 1;
		for (int i = 0; i < n; i++) {
			if (time >= arr.get(i).keytime && time <= arr.get(i + 1).keytime) {
				return i;
			}
		}
		return 0;
	}

	static <T> int getFirstKeyframeIndexAtTimeBinSearch (final Array<NodeKeyframe<T>> arr, final float time) {
		final int lastIndex = arr.size - 1;

		// edges cases : time out of range always return first index
		if (lastIndex <= 0 || time < arr.get(0).keytime || time > arr.get(lastIndex).keytime) {
			return 0;
		}

		// binary search
		int minIndex = 0;
		int maxIndex = lastIndex;

		while (minIndex < maxIndex) {
			int i = (minIndex + maxIndex) / 2;
			if (time > arr.get(i + 1).keytime) {
				minIndex = i + 1;
			} else if (time < arr.get(i).keytime) {
				maxIndex = i - 1;
			} else {
				return i;
			}
		}
		return minIndex;
	}

	static <T> int getFirstKeyframeIndexAtTimeBinSearchHeuristic (final Array<NodeKeyframe<T>> arr, final float time) {
		final int lastIndex = arr.size - 1;

		// edges cases : time out of range always return first index
		if (lastIndex <= 0 || time < arr.get(0).keytime || time > arr.get(lastIndex).keytime) {
			return 0;
		}

		// binary search
		int minIndex = 0;
		int maxIndex = lastIndex;

		for (;;) {
			final float minTime = arr.get(minIndex).keytime;
			final float maxTime = arr.get(maxIndex).keytime;

			if (time < minTime) {
				return Math.max(0, minIndex - 1);
			}
			if (time > maxTime) {
				return Math.min(lastIndex - 1, maxIndex);
			}
			if (minTime == maxTime) {
				return minIndex;
			}

			// best guess index based on time range
			float t = (time - minTime) / (maxTime - minTime);
			int index = (int)(minIndex + t * (maxIndex - minIndex));

			if (time < arr.get(index).keytime) {
				maxIndex = index - 1;
			} else if (time > arr.get(index).keytime) {
				minIndex = index + 1;
			} else {
				return Math.min(lastIndex - 1, index);
			}
		}
	}
}
