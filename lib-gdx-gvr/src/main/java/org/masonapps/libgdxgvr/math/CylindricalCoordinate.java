package org.masonapps.libgdxgvr.math;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Pool;

/**
 * Created by Bob Mason on 10/5/2017.
 */

public class CylindricalCoordinate implements Pool.Poolable {

    public float radius;
    public float theta;
    public float vertical;
    private AngleMode angleMode = AngleMode.radians;

    public CylindricalCoordinate() {
    }

    public CylindricalCoordinate(float radius, float theta, float vertical) {
        this(radius, theta, vertical, AngleMode.radians);
    }

    public CylindricalCoordinate(float radius, float theta, float vertical, AngleMode angleMode) {
        this.radius = radius;
        this.theta = theta;
        this.vertical = vertical;
        this.angleMode = angleMode;
    }

    @Override
    public void reset() {
        radius = 0f;
        theta = 0f;
        vertical = 0f;
    }

    public Vector3 toCartesian() {
        return toCartesian(new Vector3());
    }

    public Vector3 toCartesian(Vector3 out) {
        out.x = radius * (angleMode == AngleMode.radians ? MathUtils.cos(theta) : MathUtils.cosDeg(theta));
        out.y = vertical;
        out.z = -radius * (angleMode == AngleMode.radians ? MathUtils.sin(theta) : MathUtils.sinDeg(theta));
        return out;
    }

    public CylindricalCoordinate setFromCartesian(Vector3 cartesian) {
        radius = cartesian.len();
        theta = MathUtils.atan2(-cartesian.z, cartesian.x);
        vertical = cartesian.y;
        return this;
    }

    public AngleMode getAngleMode() {
        return angleMode;
    }

    public void setAngleMode(AngleMode angleMode) {
        this.angleMode = angleMode;
    }

    public void set(CylindricalCoordinate other) {
        this.radius = other.radius;
        this.theta = other.theta;
        this.vertical = other.vertical;
    }

    public void set(float radius, float theta, float vertical) {
        this.set(radius, theta, vertical, this.angleMode);
    }

    public void set(float radius, float theta, float vertical, AngleMode angleMode) {
        this.radius = radius;
        this.theta = theta;
        this.vertical = vertical;
        this.angleMode = angleMode;
    }

    public enum AngleMode {
        degrees, radians
    }
}
