package com.example.josekalladanthyil.myapplication;

import java.util.LinkedList;

/**
 * Created by josekalladanthyil on 26/04/15.
 */
public class RK4Integration {
    float[] state = new float[2];
    float[] derivative = new float[2];
    final int X = 0;
    final int V = 1;
    final int DX = 0;
    final int DV = 1;

    public float[] evaluate(float[] state, float t, float acceleration) {
        float[] derivative = new float[2];
        derivative[DX] = state[V];
        derivative[DV] = acceleration;
        return derivative;
    }

    public float[] evaluate(float[] stateI, float t,float dt, float[] derivative, float acceleration) {
        float[] state = new float[2];
        float[] output = new float[2];
        state[X] = stateI[X] + derivative[DX]*dt;
        state[V] = stateI[V] + derivative[DV]*dt;
        output[DX] = state[V];
        output[DV] = acceleration;
        return output;
    }
    public float[] integrate(float[] state, float t, float dt, float accelerate) {
        float[] a = evaluate(state, t, accelerate);
        float[] b = evaluate(state, t, dt*0.5f, a, accelerate);
        float[] c = evaluate(state, t, dt*0.5f, b, accelerate);
        float[] d = evaluate(state, t, dt, c, accelerate);

        float dxdt = 1.0f/6.0f * (a[DX] + 2.0f*(b[DX] + c[DX]) + d[DX]);
        float dvdt = 1.0f/6.0f * (a[DV] + 2.0f*(b[DV] + c[DV]) + d[DV]);

        this.state[X] = state[X] + dxdt*dt;
        this.state[V] = state[V] + dvdt*dt;
        return this.state;
    }

    public Float integrate(LinkedList<Float> accelerate_values_x, LinkedList<Double> timestamps) {
        return null;
    }
}
