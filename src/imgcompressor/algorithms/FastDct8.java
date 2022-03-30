package imgcompressor.algorithms;


public final class FastDct8 {

	public static double[] transform(double[] vector) {
		double[] result = vector.clone();
		final double v0, v1, v2, v3, v4, v5, v6, v7, v8, v9,
			v10, v11, v12, v13, v14, v15, v16, v17, v18, v19,
			v20, v21, v22, v23, v24, v25, v26, v27, v28;
		
		v0 = result[0] + result[7];
		v1 = result[1] + result[6];
		v2 = result[2] + result[5];
		v3 = result[3] + result[4];
		v4 = result[3] - result[4];
		v5 = result[2] - result[5];
		v6 = result[1] - result[6];
		v7 = result[0] - result[7];
		
		v8 = v0 + v3;
		v9 = v1 + v2;
		v10 = v1 - v2;
		v11 = v0 - v3;
		v12 = -v4 - v5;
		v13 = (v5 + v6) * A[3];
		v14 = v6 + v7;
		
		v15 = v8 + v9;
		v16 = v8 - v9;
		v17 = (v10 + v11) * A[1];
		v18 = (v12 + v14) * A[5];
		
		v19 = -v12 * A[2] - v18;
		v20 = v14 * A[4] - v18;
		
		v21 = v17 + v11;
		v22 = v11 - v17;
		v23 = v13 + v7;
		v24 = v7 - v13;
		
		v25 = v19 + v24;
		v26 = v23 + v20;
		v27 = v23 - v20;
		v28 = v24 - v19;
		
		result[0] = S[0] * v15;
		result[1] = S[1] * v26;
		result[2] = S[2] * v21;
		result[3] = S[3] * v28;
		result[4] = S[4] * v16;
		result[5] = S[5] * v25;
		result[6] = S[6] * v22;
		result[7] = S[7] * v27;
		return result;
	}
	
	
	public static double[] inverseTransform(double[] vector) {
		double[] result = vector.clone();
		final double v0, v1, v2, v3, v4, v5, v6, v7, v8, v9,
			v10, v11, v12, v13, v14, v15, v16, v17, v18, v19,
			v20, v21, v22, v23, v24, v25, v26, v27, v28;
		
		v15 = result[0] / S[0];
		v26 = result[1] / S[1];
		v21 = result[2] / S[2];
		v28 = result[3] / S[3];
		v16 = result[4] / S[4];
		v25 = result[5] / S[5];
		v22 = result[6] / S[6];
		v27 = result[7] / S[7];
		
		v19 = (v25 - v28) / 2;
		v20 = (v26 - v27) / 2;
		v23 = (v26 + v27) / 2;
		v24 = (v25 + v28) / 2;
		
		v7  = (v23 + v24) / 2;
		v11 = (v21 + v22) / 2;
		v13 = (v23 - v24) / 2;
		v17 = (v21 - v22) / 2;
		
		v8 = (v15 + v16) / 2;
		v9 = (v15 - v16) / 2;
		
		v18 = (v19 - v20) * A[5];
		v12 = (v19 * A[4] - v18) / (A[2] * A[5] - A[2] * A[4] - A[4] * A[5]);
		v14 = (v18 - v20 * A[2]) / (A[2] * A[5] - A[2] * A[4] - A[4] * A[5]);
		
		v6 = v14 - v7;
		v5 = v13 / A[3] - v6;
		v4 = -v5 - v12;
		v10 = v17 / A[1] - v11;
		
		v0 = (v8 + v11) / 2;
		v1 = (v9 + v10) / 2;
		v2 = (v9 - v10) / 2;
		v3 = (v8 - v11) / 2;
		
		result[0] = (v0 + v7) / 2;
		result[1] = (v1 + v6) / 2;
		result[2] = (v2 + v5) / 2;
		result[3] = (v3 + v4) / 2;
		result[4] = (v3 - v4) / 2;
		result[5] = (v2 - v5) / 2;
		result[6] = (v1 - v6) / 2;
		result[7] = (v0 - v7) / 2;
		return result;
	}
	
	
	
	private static double[] S = new double[8];
	private static double[] A = new double[6];
	
	static {
		double[] C = new double[8];
		for (int i = 0; i < C.length; i++) {
			C[i] = Math.cos(Math.PI / 16 * i);
			S[i] = 1 / (4 * C[i]);
		}
		S[0] = 1 / (2 * Math.sqrt(2));
		
		A[1] = C[4];
		A[2] = C[2] - C[6];
		A[3] = C[4];
		A[4] = C[6] + C[2];
		A[5] = C[6];
	}
	
}